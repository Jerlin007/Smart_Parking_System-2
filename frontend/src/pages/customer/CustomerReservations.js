import { useState, useEffect, useCallback } from 'react';
import { reservationAPI, vehicleAPI, parkingSlotAPI, parkingLotAPI } from '../../services/api';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import StatusBadge from '../../components/ui/StatusBadge';
import Modal from '../../components/ui/Modal';
import LoadingSkeleton from '../../components/ui/LoadingSkeleton';
import { FiCalendar, FiPlus, FiXCircle, FiClock, FiLoader } from 'react-icons/fi';
import toast from 'react-hot-toast';

export default function CustomerReservations() {
  const [reservations, setReservations] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [lots, setLots] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [createModal, setCreateModal] = useState(false);
  const [form, setForm] = useState({ vehicleId: '', lotId: '', slotId: '', startTime: '', endTime: '' });
  const [selectedVehicleType, setSelectedVehicleType] = useState('');
  const [saving, setSaving] = useState(false);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [resRes, vehRes, lotsRes] = await Promise.allSettled([
        reservationAPI.getMy(),
        vehicleAPI.getMy(),
        parkingLotAPI.getAll(),
      ]);
      setReservations(Array.isArray(resRes.value?.data) ? resRes.value.data : []);
      setVehicles(Array.isArray(vehRes.value?.data) ? vehRes.value.data : []);
      setLots(Array.isArray(lotsRes.value?.data) ? lotsRes.value.data : []);
    } catch (err) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const fetchAvailableSlots = useCallback(async () => {
    setSlotsLoading(true);
    setAvailableSlots([]);
    try {
      const res = await parkingSlotAPI.getAvailable(form.lotId, selectedVehicleType);
      setAvailableSlots(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setAvailableSlots([]);
    } finally {
      setSlotsLoading(false);
    }
  }, [form.lotId, selectedVehicleType]);

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (form.vehicleId && form.lotId && selectedVehicleType) {
      fetchAvailableSlots();
    } else {
      setAvailableSlots([]);
    }
  }, [form.vehicleId, form.lotId, selectedVehicleType, fetchAvailableSlots]);

  const handleVehicleChange = (e) => {
    const vehicleId = e.target.value;
    setForm({ ...form, vehicleId, slotId: '' });
    const selected = vehicles.find(v => (v.vehicleId || v.id) === Number(vehicleId));
    setSelectedVehicleType(selected ? selected.vehicleType : '');
  };

  const handleLotChange = (e) => {
    const lotId = e.target.value;
    setForm({ ...form, lotId, slotId: '' });
  };

  const handleCreate = async () => {
    if (!form.vehicleId || !form.lotId || !form.slotId) {
      toast.error('Please select a vehicle, parking lot, and slot');
      return;
    }
    setSaving(true);
    try {
      await reservationAPI.create(form.vehicleId, form.slotId, form.startTime || null, form.endTime || null);
      toast.success('Reservation created');
      setCreateModal(false);
      setForm({ vehicleId: '', lotId: '', slotId: '', startTime: '', endTime: '' });
      setSelectedVehicleType('');
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create reservation');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = async (id) => {
    try {
      await reservationAPI.cancel(id);
      toast.success('Reservation cancelled');
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel reservation');
    }
  };

  if (error) return <ErrorState message={error} onRetry={loadData} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Reservations' }]} />
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white">My Reservations</h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{reservations.length} reservations</p>
        </div>
        <button onClick={() => setCreateModal(true)} className="btn-primary">
          <FiPlus className="h-4 w-4" />
          New Reservation
        </button>
      </div>

      {loading ? (
        <LoadingSkeleton type="card" count={3} />
      ) : reservations.length === 0 ? (
        <EmptyState icon={FiCalendar} title="No reservations" message="Book a parking slot to create your first reservation." action={() => setCreateModal(true)} actionLabel="Book Now" />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {reservations.map(res => (
            <div key={res.reservationId || res.id} className="card p-5 animate-fade-in">
              <div className="flex items-center gap-3 mb-4">
                <div className={`h-10 w-10 rounded-xl flex items-center justify-center ${
                  res.status === 'CONFIRMED' ? 'bg-emerald-50 dark:bg-emerald-900/20 text-emerald-600' :
                  res.status === 'PENDING' ? 'bg-amber-50 dark:bg-amber-900/20 text-amber-600' :
                  'bg-red-50 dark:bg-red-900/20 text-red-600'
                }`}>
                  <FiCalendar className="h-5 w-5" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-semibold text-surface-900 dark:text-white">
                    {res.vehicleNumber || 'Vehicle'}
                  </p>
                  <p className="text-xs text-surface-400">
                    Slot {res.slotNumber || '—'}
                  </p>
                </div>
                <StatusBadge status={res.status} />
              </div>

              <div className="space-y-2 mb-4">
                <div className="flex items-center gap-2 text-sm text-surface-600 dark:text-surface-400">
                  <FiClock className="h-3.5 w-3.5" />
                  <span>From: {res.startTime ? new Date(res.startTime).toLocaleString() : 'Now'}</span>
                </div>
                <div className="flex items-center gap-2 text-sm text-surface-600 dark:text-surface-400">
                  <FiClock className="h-3.5 w-3.5" />
                  <span>To: {res.endTime ? new Date(res.endTime).toLocaleString() : 'Flexible'}</span>
                </div>
              </div>

              {res.status !== 'CANCELLED' && (
                <button onClick={() => handleCancel(res.reservationId || res.id)} className="btn-outline w-full text-red-500 border-red-200 hover:bg-red-50 dark:border-red-800 dark:hover:bg-red-900/20">
                  <FiXCircle className="h-4 w-4" />
                  Cancel Reservation
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      <Modal open={createModal} onClose={() => setCreateModal(false)} title="New Reservation" size="md">
        <div className="space-y-4">
          <div className="p-3 rounded-lg bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800">
            <p className="text-xs text-blue-600 dark:text-blue-400">Select vehicle and parking lot to see available slots.</p>
          </div>

          <div>
            <label className="label">Step 1: Select Vehicle</label>
            <select value={form.vehicleId} onChange={handleVehicleChange} className="select-field" required>
              <option value="">Choose your vehicle</option>
              {vehicles.map(v => (
                <option key={v.vehicleId || v.id} value={v.vehicleId || v.id}>
                  {v.vehicleNumber} ({v.vehicleType})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="label">Step 2: Select Parking Lot</label>
            <select value={form.lotId} onChange={handleLotChange} className="select-field" required disabled={!form.vehicleId}>
              <option value="">Choose parking lot</option>
              {lots.map(l => (
                <option key={l.lotId || l.id} value={l.lotId || l.id}>
                  {l.lotName} — {l.location}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="label">Step 3: Select Available Slot</label>
            <select
              value={form.slotId}
              onChange={e => setForm({ ...form, slotId: e.target.value })}
              className="select-field"
              required
              disabled={!form.vehicleId || !form.lotId || slotsLoading}
            >
              <option value="">
                {!form.vehicleId || !form.lotId
                  ? 'Select vehicle and lot first'
                  : slotsLoading
                    ? 'Loading available slots...'
                    : availableSlots.length === 0
                      ? 'No available slots'
                      : 'Choose a slot'}
              </option>
              {availableSlots.map(s => (
                <option key={s.slotId || s.id} value={s.slotId || s.id}>
                  {s.slotNumber} — Floor {s.floorNumber} ({s.slotType})
                </option>
              ))}
            </select>
            {slotsLoading && (
              <div className="flex items-center gap-2 mt-2 text-sm text-surface-500">
                <FiLoader className="h-4 w-4 animate-spin" />
                Loading available slots...
              </div>
            )}
            {!slotsLoading && form.vehicleId && form.lotId && availableSlots.length === 0 && selectedVehicleType && (
              <p className="text-sm text-amber-600 dark:text-amber-400 mt-2">
                No available {selectedVehicleType} slots in this parking lot.
              </p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">Start Time (optional)</label>
              <input type="datetime-local" value={form.startTime} onChange={e => setForm({ ...form, startTime: e.target.value })} className="input-field" />
            </div>
            <div>
              <label className="label">End Time (optional)</label>
              <input type="datetime-local" value={form.endTime} onChange={e => setForm({ ...form, endTime: e.target.value })} className="input-field" />
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setCreateModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={handleCreate} className="btn-primary" disabled={saving || !form.slotId}>
              {saving ? 'Booking...' : 'Book Slot'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
