import { useState, useEffect } from 'react';
import { parkingLotAPI, parkingSlotAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import Modal from '../../components/ui/Modal';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import Breadcrumb from '../../components/ui/Breadcrumb';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import StatCard from '../../components/ui/StatCard';
import { FiMapPin, FiPlus, FiEdit2, FiTrash2 } from 'react-icons/fi';
import toast from 'react-hot-toast';

export default function ParkingLots() {
  const [lots, setLots] = useState([]);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [createModal, setCreateModal] = useState(false);
  const [editModal, setEditModal] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [selectedLot, setSelectedLot] = useState(null);
  const [form, setForm] = useState({ lotName: '', location: '', totalSlots: '', carSlots: '', bikeSlots: '', evSlots: '' });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [lotsRes, slotsRes] = await Promise.all([
        parkingLotAPI.getAll(),
        parkingSlotAPI.getAll(),
      ]);
      setLots(Array.isArray(lotsRes.data) ? lotsRes.data : []);
      setSlots(Array.isArray(slotsRes.data) ? slotsRes.data : []);
    } catch (err) {
      setError('Failed to load parking lots');
    } finally {
      setLoading(false);
    }
  };

  const totalCapacity = lots.reduce((sum, l) => sum + (l.totalSlots || 0), 0);
  const totalSlotsCount = slots.length;
  const availableSlots = slots.filter(s => s.status === 'AVAILABLE').length;
  const occupancyRate = totalSlotsCount > 0 ? Math.round((totalSlotsCount - availableSlots) / totalSlotsCount * 100) : 0;

  const getLotStats = (lot) => {
    const lotSlots = slots.filter(s => (s.lotId === lot.id || s.lotId === lot.lotId));
    return {
      total: lotSlots.length,
      available: lotSlots.filter(s => s.status === 'AVAILABLE').length,
      occupied: lotSlots.filter(s => s.status === 'OCCUPIED').length,
    };
  };

  const handleCreate = async () => {
    setSaving(true);
    try {
      const payload = {
        ...form,
        totalSlots: parseInt(form.totalSlots),
        carSlots: parseInt(form.carSlots),
        bikeSlots: parseInt(form.bikeSlots),
        evSlots: parseInt(form.evSlots),
      };
      if (payload.carSlots + payload.bikeSlots + payload.evSlots !== payload.totalSlots) {
        toast.error('Breakdown must sum to total slots');
        setSaving(false);
        return;
      }
      await parkingLotAPI.create(payload);
      toast.success('Parking lot created');
      setCreateModal(false);
      setForm({ lotName: '', location: '', totalSlots: '', carSlots: '', bikeSlots: '', evSlots: '' });
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create lot');
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = async () => {
    if (!selectedLot) return;
    setSaving(true);
    try {
      const payload = {
        ...form,
        totalSlots: parseInt(form.totalSlots),
        carSlots: parseInt(form.carSlots),
        bikeSlots: parseInt(form.bikeSlots),
        evSlots: parseInt(form.evSlots),
      };
      if (payload.carSlots + payload.bikeSlots + payload.evSlots !== payload.totalSlots) {
        toast.error('Breakdown must sum to total slots');
        setSaving(false);
        return;
      }
      await parkingLotAPI.update(selectedLot.id || selectedLot.lotId, payload);
      toast.success('Parking lot updated');
      setEditModal(false);
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update lot');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedLot) return;
    setSaving(true);
    try {
      const lotSlots = slots.filter(s => s.lotId === selectedLot.id || s.lotId === selectedLot.lotId);
      if (lotSlots.some(s => s.status === 'OCCUPIED' || s.status === 'RESERVED')) {
        toast.error('Cannot delete lot with active slots');
        setDeleteConfirm(false);
        return;
      }
      await parkingLotAPI.delete(selectedLot.id || selectedLot.lotId);
      toast.success('Parking lot deleted');
      setDeleteConfirm(false);
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete lot');
    } finally {
      setSaving(false);
    }
  };

  const openEdit = (lot) => {
    setSelectedLot(lot);
    setForm({
      lotName: lot.lotName || '',
      location: lot.location || '',
      totalSlots: lot.totalSlots || '',
      carSlots: lot.carSlots || '',
      bikeSlots: lot.bikeSlots || '',
      evSlots: lot.evSlots || '',
    });
    setEditModal(true);
  };

  const columns = [
    { header: 'Lot Name', accessor: 'lotName', sortable: true },
    { header: 'Location', accessor: 'location', sortable: true },
    {
      header: 'Total Slots',
      accessor: 'totalSlots',
      sortable: true,
      cell: (row) => row.totalSlots || getLotStats(row).total,
    },
    {
      header: 'Available',
      cell: (row) => getLotStats(row).available,
    },
    {
      header: 'Occupied',
      cell: (row) => getLotStats(row).occupied,
    },
    {
      header: 'Actions',
      accessor: 'actions',
      cell: (row) => (
        <div className="flex items-center gap-1">
          <button onClick={(e) => { e.stopPropagation(); openEdit(row); }} className="btn-ghost p-1.5 text-blue-500"><FiEdit2 className="h-3.5 w-3.5" /></button>
          <button onClick={(e) => { e.stopPropagation(); setSelectedLot(row); setDeleteConfirm(true); }} className="btn-ghost p-1.5 text-red-500"><FiTrash2 className="h-3.5 w-3.5" /></button>
        </div>
      ),
    },
  ];

  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Parking Lots' }]} />
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Parking Lots</h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{lots.length} lots</p>
        </div>
        <button onClick={() => { setForm({ lotName: '', location: '', totalSlots: '', carSlots: '', bikeSlots: '', evSlots: '' }); setCreateModal(true); }} className="btn-primary">
          <FiPlus className="h-4 w-4" />
          Add Lot
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <StatCard icon={FiMapPin} label="Total Capacity" value={totalCapacity} color="primary" />
        <StatCard icon={FiMapPin} label="Available Capacity" value={availableSlots} color="emerald" />
        <StatCard icon={FiMapPin} label="Occupancy Rate" value={`${occupancyRate}%`} color="amber" />
      </div>

      {lots.length === 0 && !loading ? (
        <EmptyState icon={FiMapPin} title="No parking lots" message="Create your first parking lot to get started." action={() => { setForm({ lotName: '', location: '', totalSlots: '', carSlots: '', bikeSlots: '', evSlots: '' }); setCreateModal(true); }} actionLabel="Create Lot" />
      ) : (
        <DataTable columns={columns} data={lots} loading={loading} searchable searchPlaceholder="Search lots..." />
      )}

      <Modal open={createModal} onClose={() => setCreateModal(false)} title="Create Parking Lot">
        <div className="space-y-4">
          <div>
            <label className="label">Lot Name</label>
            <input type="text" value={form.lotName} onChange={e => setForm({ ...form, lotName: e.target.value })} className="input-field" placeholder="e.g. North Parking" required />
          </div>
          <div>
            <label className="label">Location</label>
            <input type="text" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} className="input-field" placeholder="e.g. Ground Floor, Block A" required />
          </div>
          <div>
            <label className="label">Total Slots</label>
            <input type="number" value={form.totalSlots} onChange={e => setForm({ ...form, totalSlots: e.target.value })} className="input-field" placeholder="e.g. 50" min="1" required />
          </div>
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="label">Car Slots</label>
              <input type="number" value={form.carSlots} onChange={e => setForm({ ...form, carSlots: e.target.value })} className="input-field" placeholder="0" min="0" required />
            </div>
            <div>
              <label className="label">Bike Slots</label>
              <input type="number" value={form.bikeSlots} onChange={e => setForm({ ...form, bikeSlots: e.target.value })} className="input-field" placeholder="0" min="0" required />
            </div>
            <div>
              <label className="label">EV Slots</label>
              <input type="number" value={form.evSlots} onChange={e => setForm({ ...form, evSlots: e.target.value })} className="input-field" placeholder="0" min="0" required />
            </div>
          </div>
          <div className={`text-sm px-3 py-2 rounded-md ${(() => {
            const c = parseInt(form.carSlots) || 0;
            const b = parseInt(form.bikeSlots) || 0;
            const e = parseInt(form.evSlots) || 0;
            const t = parseInt(form.totalSlots) || 0;
            const sum = c + b + e;
            if (t === 0) return 'bg-surface-100 dark:bg-surface-800 text-surface-500';
            return sum === t ? 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400' : 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400';
          })()}`}>
            {(() => {
              const c = parseInt(form.carSlots) || 0;
              const b = parseInt(form.bikeSlots) || 0;
              const e = parseInt(form.evSlots) || 0;
              const t = parseInt(form.totalSlots) || 0;
              const sum = c + b + e;
              if (t === 0) return 'Enter total slots and breakdown';
              return `Total: ${sum} (Car: ${c}, Bike: ${b}, EV: ${e})${sum !== t ? ` — Expected ${t}` : ''}`;
            })()}
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setCreateModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={handleCreate} className="btn-primary" disabled={saving}>{saving ? 'Creating...' : 'Create Lot'}</button>
          </div>
        </div>
      </Modal>

      <Modal open={editModal} onClose={() => setEditModal(false)} title="Edit Parking Lot">
        <div className="space-y-4">
          <div>
            <label className="label">Lot Name</label>
            <input type="text" value={form.lotName} onChange={e => setForm({ ...form, lotName: e.target.value })} className="input-field" />
          </div>
          <div>
            <label className="label">Location</label>
            <input type="text" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} className="input-field" />
          </div>
          <div>
            <label className="label">Total Slots</label>
            <input type="number" value={form.totalSlots} onChange={e => setForm({ ...form, totalSlots: e.target.value })} className="input-field" min="1" />
          </div>
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="label">Car Slots</label>
              <input type="number" value={form.carSlots} onChange={e => setForm({ ...form, carSlots: e.target.value })} className="input-field" placeholder="0" min="0" />
            </div>
            <div>
              <label className="label">Bike Slots</label>
              <input type="number" value={form.bikeSlots} onChange={e => setForm({ ...form, bikeSlots: e.target.value })} className="input-field" placeholder="0" min="0" />
            </div>
            <div>
              <label className="label">EV Slots</label>
              <input type="number" value={form.evSlots} onChange={e => setForm({ ...form, evSlots: e.target.value })} className="input-field" placeholder="0" min="0" />
            </div>
          </div>
          <div className={`text-sm px-3 py-2 rounded-md ${(() => {
            const c = parseInt(form.carSlots) || 0;
            const b = parseInt(form.bikeSlots) || 0;
            const e = parseInt(form.evSlots) || 0;
            const t = parseInt(form.totalSlots) || 0;
            const sum = c + b + e;
            if (t === 0) return 'bg-surface-100 dark:bg-surface-800 text-surface-500';
            return sum === t ? 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-400' : 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400';
          })()}`}>
            {(() => {
              const c = parseInt(form.carSlots) || 0;
              const b = parseInt(form.bikeSlots) || 0;
              const e = parseInt(form.evSlots) || 0;
              const t = parseInt(form.totalSlots) || 0;
              const sum = c + b + e;
              if (t === 0) return 'Enter total slots and breakdown';
              return `Total: ${sum} (Car: ${c}, Bike: ${b}, EV: ${e})${sum !== t ? ` — Expected ${t}` : ''}`;
            })()}
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setEditModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={handleEdit} className="btn-primary" disabled={saving}>{saving ? 'Saving...' : 'Save Changes'}</button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog open={deleteConfirm} onClose={() => setDeleteConfirm(false)} onConfirm={handleDelete} title="Delete Parking Lot" message={`Are you sure you want to delete "${selectedLot?.lotName}"? Active slots must be cleared first.`} loading={saving} />
    </div>
  );
}
