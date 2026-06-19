import { useState, useEffect } from 'react';
import { vehicleAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import Modal from '../../components/ui/Modal';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import StatusBadge from '../../components/ui/StatusBadge';
import { FiTruck, FiPlus, FiEdit2 } from 'react-icons/fi';
import toast from 'react-hot-toast';

export default function MyVehicles() {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [createModal, setCreateModal] = useState(false);
  const [editModal, setEditModal] = useState(false);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [form, setForm] = useState({ vehicleNumber: '', vehicleType: 'CAR', ownerName: '', mobileNumber: '' });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadVehicles();
  }, []);

  const loadVehicles = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await vehicleAPI.getMy();
      setVehicles(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError('Failed to load vehicles');
      setVehicles([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    setSaving(true);
    try {
      await vehicleAPI.create(form);
      toast.success('Vehicle registered');
      setCreateModal(false);
      setForm({ vehicleNumber: '', vehicleType: 'CAR', ownerName: '', mobileNumber: '' });
      loadVehicles();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to register vehicle');
    } finally {
      setSaving(false);
    }
  };

  const handleUpdate = async () => {
    if (!selectedVehicle) return;
    setSaving(true);
    try {
      await vehicleAPI.update(selectedVehicle.id || selectedVehicle.vehicleId, form);
      toast.success('Vehicle updated');
      setEditModal(false);
      loadVehicles();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update vehicle');
    } finally {
      setSaving(false);
    }
  };

  const columns = [
    { header: '#', accessor: 'vehicleId', sortable: true },
    { header: 'Vehicle Number', accessor: 'vehicleNumber', sortable: true },
    { header: 'Type', accessor: 'vehicleType', cell: (row) => <StatusBadge status={row.vehicleType} /> },
    { header: 'Owner Name', accessor: 'ownerName' },
    { header: 'Mobile', accessor: 'mobileNumber' },
    {
      header: 'Actions',
      accessor: 'actions',
      cell: (row) => (
        <button onClick={(e) => { e.stopPropagation(); setSelectedVehicle(row); setForm({ vehicleNumber: row.vehicleNumber, vehicleType: row.vehicleType, ownerName: row.ownerName || '', mobileNumber: row.mobileNumber || '' }); setEditModal(true); }} className="btn-ghost p-1.5 text-blue-500">
          <FiEdit2 className="h-3.5 w-3.5" />
        </button>
      ),
    },
  ];

  if (error) return <ErrorState message={error} onRetry={loadVehicles} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'My Vehicles' }]} />
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white">My Vehicles</h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{vehicles.length} vehicles registered</p>
        </div>
        <button onClick={() => { setForm({ vehicleNumber: '', vehicleType: 'CAR', ownerName: '', mobileNumber: '' }); setCreateModal(true); }} className="btn-primary">
          <FiPlus className="h-4 w-4" />
          Add Vehicle
        </button>
      </div>

      {vehicles.length === 0 && !loading ? (
        <EmptyState icon={FiTruck} title="No vehicles" message="Register your first vehicle to start using the parking system." action={() => setCreateModal(true)} actionLabel="Add Vehicle" />
      ) : (
        <DataTable columns={columns} data={vehicles} loading={loading} searchable searchPlaceholder="Search vehicles..." />
      )}

      <Modal open={createModal} onClose={() => setCreateModal(false)} title="Register Vehicle">
        <div className="space-y-4">
          <div>
            <label className="label">Vehicle Number</label>
            <input type="text" value={form.vehicleNumber} onChange={e => setForm({ ...form, vehicleNumber: e.target.value })} className="input-field" placeholder="e.g. KA01AB1234" required />
          </div>
          <div>
            <label className="label">Vehicle Type</label>
            <select value={form.vehicleType} onChange={e => setForm({ ...form, vehicleType: e.target.value })} className="select-field">
              <option value="CAR">Car</option>
              <option value="BIKE">Bike</option>
              <option value="EV">EV</option>
            </select>
          </div>
          <div>
            <label className="label">Owner Name</label>
            <input type="text" value={form.ownerName} onChange={e => setForm({ ...form, ownerName: e.target.value })} className="input-field" placeholder="Your name" />
          </div>
          <div>
            <label className="label">Mobile Number</label>
            <input type="text" value={form.mobileNumber} onChange={e => setForm({ ...form, mobileNumber: e.target.value })} className="input-field" placeholder="Phone number" />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setCreateModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={handleCreate} className="btn-primary" disabled={saving}>{saving ? 'Registering...' : 'Register Vehicle'}</button>
          </div>
        </div>
      </Modal>

      <Modal open={editModal} onClose={() => setEditModal(false)} title="Edit Vehicle">
        <div className="space-y-4">
          <div>
            <label className="label">Vehicle Number</label>
            <input type="text" value={form.vehicleNumber} onChange={e => setForm({ ...form, vehicleNumber: e.target.value })} className="input-field" />
          </div>
          <div>
            <label className="label">Vehicle Type</label>
            <select value={form.vehicleType} onChange={e => setForm({ ...form, vehicleType: e.target.value })} className="select-field">
              <option value="CAR">Car</option>
              <option value="BIKE">Bike</option>
              <option value="EV">EV</option>
            </select>
          </div>
          <div>
            <label className="label">Owner Name</label>
            <input type="text" value={form.ownerName} onChange={e => setForm({ ...form, ownerName: e.target.value })} className="input-field" />
          </div>
          <div>
            <label className="label">Mobile Number</label>
            <input type="text" value={form.mobileNumber} onChange={e => setForm({ ...form, mobileNumber: e.target.value })} className="input-field" />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setEditModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={handleUpdate} className="btn-primary" disabled={saving}>{saving ? 'Saving...' : 'Save Changes'}</button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
