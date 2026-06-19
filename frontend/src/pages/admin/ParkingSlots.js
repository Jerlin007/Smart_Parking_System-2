import { useState, useEffect } from 'react';
import { parkingSlotAPI, parkingLotAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import Modal from '../../components/ui/Modal';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import Breadcrumb from '../../components/ui/Breadcrumb';
import ErrorState from '../../components/ui/ErrorState';
import EmptyState from '../../components/ui/EmptyState';
import StatusBadge from '../../components/ui/StatusBadge';
import { FiLayers, FiEdit2, FiTrash2, FiGrid, FiList, FiPlus } from 'react-icons/fi';
import toast from 'react-hot-toast';

export default function ParkingSlots() {
  const [slots, setSlots] = useState([]);
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [viewMode, setViewMode] = useState('table');
  const [editModal, setEditModal] = useState(false);
  const [createModal, setCreateModal] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [form, setForm] = useState({ slotNumber: '', slotType: 'CAR', floorNumber: '', parkingLot: '', status: 'AVAILABLE' });
  const [filters, setFilters] = useState({ status: '', type: '', floor: '', lot: '' });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [slotsRes, lotsRes] = await Promise.all([parkingSlotAPI.getAll(), parkingLotAPI.getAll()]);
      setSlots(Array.isArray(slotsRes.data) ? slotsRes.data : []);
      setLots(Array.isArray(lotsRes.data) ? lotsRes.data : []);
    } catch (err) {
      setError('Failed to load parking slots');
    } finally {
      setLoading(false);
    }
  };

  const filteredSlots = slots.filter(s => {
    if (filters.status && s.status !== filters.status) return false;
    if (filters.type && s.slotType !== filters.type) return false;
    if (filters.floor && s.floorNumber !== parseInt(filters.floor)) return false;
    if (filters.lot && (s.lotId !== parseInt(filters.lot) && s.parkingLot?.lotId !== parseInt(filters.lot) && s.parkingLot?.id !== parseInt(filters.lot))) return false;
    return true;
  });

  const handleEdit = async () => {
    if (!selectedSlot) return;
    setSaving(true);
    try {
      const payload = {
        slotNumber: form.slotNumber,
        slotType: form.slotType,
        floorNumber: parseInt(form.floorNumber),
        lotId: parseInt(form.parkingLot),
      };
      await parkingSlotAPI.update(selectedSlot.id || selectedSlot.slotId, payload);
      toast.success('Parking slot updated');
      setEditModal(false);
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update slot');
    } finally {
      setSaving(false);
    }
  };

  const handleCreate = async () => {
    setSaving(true);
    try {
      const payload = {
        slotNumber: form.slotNumber,
        slotType: form.slotType,
        floorNumber: parseInt(form.floorNumber),
        lotId: parseInt(form.parkingLot),
      };
      await parkingSlotAPI.create(payload);
      toast.success('Parking slot created');
      setCreateModal(false);
      setForm({ slotNumber: '', slotType: 'CAR', floorNumber: '', parkingLot: '', status: 'AVAILABLE' });
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create slot');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedSlot) return;
    setSaving(true);
    try {
      await parkingSlotAPI.delete(selectedSlot.id || selectedSlot.slotId);
      toast.success('Parking slot deleted');
      setDeleteConfirm(false);
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete slot');
    } finally {
      setSaving(false);
    }
  };

  const openEdit = (slot) => {
    setSelectedSlot(slot);
    setForm({
      slotNumber: slot.slotNumber || '',
      slotType: slot.slotType || 'CAR',
      floorNumber: slot.floorNumber?.toString() || '',
      parkingLot: slot.lotId?.toString() || '',
      status: slot.status || 'AVAILABLE',
    });
    setEditModal(true);
  };

  const statusColor = (status) => {
    switch (status) {
      case 'AVAILABLE': return 'border-emerald-500 bg-emerald-50 dark:bg-emerald-900/20';
      case 'RESERVED': return 'border-amber-500 bg-amber-50 dark:bg-amber-900/20';
      case 'OCCUPIED': return 'border-red-500 bg-red-50 dark:bg-red-900/20';
      default: return 'border-surface-300';
    }
  };

  const columns = [
    { header: 'Slot #', accessor: 'slotNumber', sortable: true },
    { header: 'Type', accessor: 'slotType', cell: (row) => <StatusBadge status={row.slotType} /> },
    { header: 'Floor', accessor: 'floorNumber', sortable: true },
    { header: 'Status', accessor: 'status', cell: (row) => <StatusBadge status={row.status} /> },
    { header: 'Lot', accessor: 'parkingLot', cell: (row) => row.parkingLot?.lotName || row.lotName || '—' },
    {
      header: 'Actions',
      accessor: 'actions',
      cell: (row) => (
        <div className="flex items-center gap-1">
          <button onClick={(e) => { e.stopPropagation(); openEdit(row); }} className="btn-ghost p-1.5 text-blue-500" title="Edit"><FiEdit2 className="h-3.5 w-3.5" /></button>
          <button onClick={(e) => { e.stopPropagation(); setSelectedSlot(row); setDeleteConfirm(true); }} className="btn-ghost p-1.5 text-red-500" title="Delete"><FiTrash2 className="h-3.5 w-3.5" /></button>
        </div>
      ),
    },
  ];

  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Parking Slots' }]} />
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Parking Slots</h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{slots.length} slots</p>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex rounded-lg border border-surface-200 dark:border-surface-700 overflow-hidden">
            <button onClick={() => setViewMode('table')} className={`p-2 ${viewMode === 'table' ? 'bg-primary-50 dark:bg-primary-900/20 text-primary-600' : 'text-surface-400 hover:bg-surface-50 dark:hover:bg-surface-800'}`}><FiList className="h-4 w-4" /></button>
            <button onClick={() => setViewMode('grid')} className={`p-2 ${viewMode === 'grid' ? 'bg-primary-50 dark:bg-primary-900/20 text-primary-600' : 'text-surface-400 hover:bg-surface-50 dark:hover:bg-surface-800'}`}><FiGrid className="h-4 w-4" /></button>
          </div>
          <button onClick={() => { setForm({ slotNumber: '', slotType: 'CAR', floorNumber: '', parkingLot: '', status: 'AVAILABLE' }); setCreateModal(true); }} className="btn-primary">
            <FiPlus className="h-4 w-4" />
            Add Slot
          </button>
        </div>
      </div>

      <div className="card p-4 mb-6">
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <select value={filters.status} onChange={e => setFilters({ ...filters, status: e.target.value })} className="select-field text-sm">
            <option value="">All Status</option>
            <option value="AVAILABLE">Available</option>
            <option value="RESERVED">Reserved</option>
            <option value="OCCUPIED">Occupied</option>
          </select>
          <select value={filters.type} onChange={e => setFilters({ ...filters, type: e.target.value })} className="select-field text-sm">
            <option value="">All Types</option>
            <option value="CAR">Car</option>
            <option value="BIKE">Bike</option>
            <option value="EV">EV</option>
          </select>
          <input type="number" value={filters.floor} onChange={e => setFilters({ ...filters, floor: e.target.value })} className="input-field text-sm" placeholder="Floor #" />
          <select value={filters.lot} onChange={e => setFilters({ ...filters, lot: e.target.value })} className="select-field text-sm">
            <option value="">All Lots</option>
            {lots.map(lot => (
              <option key={lot.lotId || lot.id} value={lot.lotId || lot.id}>{lot.lotName}</option>
            ))}
          </select>
        </div>
      </div>

      {viewMode === 'grid' ? (
        filteredSlots.length === 0 && !loading ? (
          <EmptyState icon={FiLayers} title="No slots match filters" message="Try adjusting your filters." />
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-3">
            {filteredSlots.map(slot => (
              <div key={slot.id || slot.slotId} className={`card p-3 border-l-4 ${statusColor(slot.status)}`}>
                <p className="text-sm font-bold text-surface-900 dark:text-white">{slot.slotNumber}</p>
                <p className="text-xs text-surface-500">{slot.slotType} · Floor {slot.floorNumber}</p>
                <StatusBadge status={slot.status} />
                <p className="text-xs text-surface-400 mt-1">{slot.parkingLot?.lotName || slot.lotName || ''}</p>
              </div>
            ))}
          </div>
        )
      ) : (
        filteredSlots.length === 0 && !loading ? (
          <EmptyState icon={FiLayers} title="No slots match filters" message="Try adjusting your filters." />
        ) : (
          <DataTable columns={columns} data={filteredSlots} loading={loading} />
        )
      )}

      <Modal open={createModal} onClose={() => setCreateModal(false)} title="Create Parking Slot">
        <div className="space-y-4">
          <div>
            <label className="label">Parking Lot</label>
            <select value={form.parkingLot} onChange={e => setForm({ ...form, parkingLot: e.target.value })} className="select-field" required>
              <option value="">Select Lot</option>
              {lots.map(lot => (
                <option key={lot.lotId || lot.id} value={lot.lotId || lot.id}>{lot.lotName}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Slot Number</label>
            <input type="text" value={form.slotNumber} onChange={e => setForm({ ...form, slotNumber: e.target.value })} className="input-field" placeholder="e.g. A01" required />
          </div>
          <div>
            <label className="label">Slot Type</label>
            <select value={form.slotType} onChange={e => setForm({ ...form, slotType: e.target.value })} className="select-field">
              <option value="CAR">Car</option>
              <option value="BIKE">Bike</option>
              <option value="EV">EV</option>
            </select>
          </div>
          <div>
            <label className="label">Floor Number</label>
            <input type="number" value={form.floorNumber} onChange={e => setForm({ ...form, floorNumber: e.target.value })} className="input-field" placeholder="e.g. 1" min="0" required />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setCreateModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={handleCreate} className="btn-primary" disabled={saving}>{saving ? 'Creating...' : 'Create Slot'}</button>
          </div>
        </div>
      </Modal>

      <Modal open={editModal} onClose={() => setEditModal(false)} title="Edit Parking Slot">
        <div className="space-y-4">
          <div>
            <label className="label">Parking Lot</label>
            <select value={form.parkingLot} onChange={e => setForm({ ...form, parkingLot: e.target.value })} className="select-field" required>
              <option value="">Select Lot</option>
              {lots.map(lot => (
                <option key={lot.lotId || lot.id} value={lot.lotId || lot.id}>{lot.lotName}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Slot Number</label>
            <input type="text" value={form.slotNumber} onChange={e => setForm({ ...form, slotNumber: e.target.value })} className="input-field" placeholder="e.g. A01" required />
          </div>
          <div>
            <label className="label">Slot Type</label>
            <select value={form.slotType} onChange={e => setForm({ ...form, slotType: e.target.value })} className="select-field">
              <option value="CAR">Car</option>
              <option value="BIKE">Bike</option>
              <option value="EV">EV</option>
            </select>
          </div>
          <div>
            <label className="label">Floor Number</label>
            <input type="number" value={form.floorNumber} onChange={e => setForm({ ...form, floorNumber: e.target.value })} className="input-field" placeholder="e.g. 1" min="0" required />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setEditModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={handleEdit} className="btn-primary" disabled={saving}>{saving ? 'Saving...' : 'Save Changes'}</button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog open={deleteConfirm} onClose={() => setDeleteConfirm(false)} onConfirm={handleDelete} title="Delete Slot" message={`Delete slot ${selectedSlot?.slotNumber}?`} loading={saving} />
    </div>
  );
}
