import { useState, useEffect } from 'react';
import { parkingAPI, vehicleAPI, parkingSlotAPI } from '../../services/api';
import Breadcrumb from '../../components/ui/Breadcrumb';
import StatusBadge from '../../components/ui/StatusBadge';
import { FiArrowRightCircle, FiLogOut, FiClock, FiTruck } from 'react-icons/fi';
import toast from 'react-hot-toast';

export default function EntryExit() {
  const [tab, setTab] = useState('entry');
  const [vehicles, setVehicles] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [reservedSlots, setReservedSlots] = useState([]);
  const [activeTransactions, setActiveTransactions] = useState([]);
  const [entryForm, setEntryForm] = useState({ vehicleNumber: '', slotId: '' });
  const [exitForm, setExitForm] = useState({ transactionId: '' });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [vehRes, slotRes, reservedRes, transRes] = await Promise.allSettled([
        vehicleAPI.getMy(),
        parkingSlotAPI.getAvailable(),
        parkingAPI.getReservedSlots(),
        parkingAPI.getMy(),
      ]);

      const veh = vehRes.status === 'fulfilled' ? vehRes.value.data : [];
      const slots = slotRes.status === 'fulfilled' ? slotRes.value.data : [];
      const reserved = reservedRes.status === 'fulfilled' ? reservedRes.value.data : [];
      const trans = transRes.status === 'fulfilled' ? transRes.value.data : [];

      setVehicles(Array.isArray(veh) ? veh : []);
      setAvailableSlots(Array.isArray(slots) ? slots : []);
      setReservedSlots(Array.isArray(reserved) ? reserved : []);
      setActiveTransactions(Array.isArray(trans) ? trans.filter(t => t.status === 'ACTIVE') : []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleEntry = async () => {
    if (!entryForm.vehicleNumber || !entryForm.slotId) {
      toast.error('Please select vehicle and slot');
      return;
    }
    setLoading(true);
    try {
      const res = await parkingAPI.entry(entryForm.vehicleNumber, entryForm.slotId);
      setResult(res.data);
      toast.success('Vehicle entry recorded');
      loadData();
      setEntryForm({ vehicleNumber: '', slotId: '' });
    } catch (err) {
      toast.error(err.response?.data?.message || 'Entry failed');
    } finally {
      setLoading(false);
    }
  };

  const handleExit = async () => {
    if (!exitForm.transactionId) {
      toast.error('Please select a transaction');
      return;
    }
    setLoading(true);
    try {
      const res = await parkingAPI.exit(exitForm.transactionId);
      setResult({ ...res.data, exitCompleted: true });
      toast.success('Vehicle exit recorded');
      loadData();
      setExitForm({ transactionId: '' });
    } catch (err) {
      toast.error(err.response?.data?.message || 'Exit failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Breadcrumb items={[{ label: 'Entry / Exit' }]} />
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Vehicle Entry & Exit</h1>
        <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">Manage parking sessions</p>
      </div>

      {/* Tab Switcher */}
      <div className="flex gap-1 mb-6 card p-1 w-fit">
        <button onClick={() => setTab('entry')} className={`px-5 py-2 rounded-lg text-sm font-medium transition-colors ${tab === 'entry' ? 'bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-300' : 'text-surface-500 hover:text-surface-700 dark:hover:text-surface-300'}`}>
          <FiArrowRightCircle className="h-4 w-4 inline mr-1.5" />
          Vehicle Entry
        </button>
        <button onClick={() => setTab('exit')} className={`px-5 py-2 rounded-lg text-sm font-medium transition-colors ${tab === 'exit' ? 'bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-300' : 'text-surface-500 hover:text-surface-700 dark:hover:text-surface-300'}`}>
          <FiLogOut className="h-4 w-4 inline mr-1.5" />
          Vehicle Exit
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Entry/Exit Form */}
        <div className="card p-5">
          <h3 className="text-sm font-semibold text-surface-900 dark:text-white mb-4">
            {tab === 'entry' ? 'Record Vehicle Entry' : 'Record Vehicle Exit'}
          </h3>

          {tab === 'entry' ? (
            <div className="space-y-4">
              {reservedSlots.length > 0 && (
                <div>
                  <p className="text-xs font-medium text-surface-500 mb-2">Your Reserved Slots:</p>
                  <div className="flex flex-wrap gap-2 mb-3">
                    {reservedSlots.map(s => (
                      <button key={s.slotId || s.id} onClick={() => {
                        setEntryForm({ vehicleNumber: s.vehicleNumber || entryForm.vehicleNumber, slotId: s.slotId || s.id });
                      }} className="badge-yellow text-xs cursor-pointer hover:bg-amber-100 dark:hover:bg-amber-900/40">
                        {s.slotNumber} — {s.vehicleNumber}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              <div>
                <label className="label">Vehicle Number</label>
                <div className="relative">
                  <FiTruck className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-surface-400" />
                  <input
                    type="text"
                    value={entryForm.vehicleNumber}
                    onChange={e => setEntryForm({ ...entryForm, vehicleNumber: e.target.value })}
                    className="input-field pl-9"
                    placeholder="e.g. KA01AB1234"
                    list="vehicleList"
                  />
                  <datalist id="vehicleList">
                    {vehicles.map(v => (
                      <option key={v.vehicleId || v.id} value={v.vehicleNumber} />
                    ))}
                  </datalist>
                </div>
              </div>

              <div>
                <label className="label">Available Slot</label>
                <select value={entryForm.slotId} onChange={e => setEntryForm({ ...entryForm, slotId: e.target.value })} className="select-field">
                  <option value="">Select Slot</option>
                  {reservedSlots.length > 0 && (
                    <optgroup label="Your Reserved Slots">
                      {reservedSlots.map(s => (
                        <option key={s.slotId || s.id} value={s.slotId || s.id}>{s.slotNumber} ({s.slotType})</option>
                      ))}
                    </optgroup>
                  )}
                  <optgroup label="Available Slots">
                    {availableSlots.map(s => (
                      <option key={s.slotId || s.id} value={s.slotId || s.id}>{s.slotNumber} ({s.slotType})</option>
                    ))}
                  </optgroup>
                </select>
              </div>

              <button onClick={handleEntry} className="btn-primary w-full" disabled={loading}>
                {loading ? 'Processing...' : 'Record Entry'}
              </button>
            </div>
          ) : (
            <div className="space-y-4">
              <div>
                <label className="label">Active Transaction</label>
                {activeTransactions.length > 0 ? (
                  <select value={exitForm.transactionId} onChange={e => setExitForm({ transactionId: e.target.value })} className="select-field">
                    <option value="">Select Transaction</option>
                    {activeTransactions.map(t => (
                      <option key={t.transactionId || t.id} value={t.transactionId || t.id}>
                        #{t.transactionId} — {t.vehicle?.vehicleNumber || t.vehicleNumber}
                      </option>
                    ))}
                  </select>
                ) : (
                  <input type="text" value={exitForm.transactionId} onChange={e => setExitForm({ transactionId: e.target.value })} className="input-field" placeholder="Enter Transaction ID" />
                )}
              </div>

              <button onClick={handleExit} className="btn-danger w-full" disabled={loading}>
                {loading ? 'Processing...' : 'Record Exit'}
              </button>
            </div>
          )}
        </div>

        {/* Result Panel */}
        <div className="card p-5">
          <h3 className="text-sm font-semibold text-surface-900 dark:text-white mb-4">Transaction Details</h3>
          {result ? (
            <div className="space-y-3 animate-fade-in">
              {result.exitCompleted && (
                <div className="p-3 rounded-lg bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-200 dark:border-emerald-800 text-center">
                  <p className="text-sm font-medium text-emerald-700 dark:text-emerald-400">Exit completed successfully!</p>
                  <p className="text-xs text-emerald-600 dark:text-emerald-500 mt-1">Bill has been generated automatically.</p>
                </div>
              )}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <p className="text-xs text-surface-400">Transaction ID</p>
                  <p className="text-sm font-medium text-surface-900 dark:text-white">#{result.transactionId || result.id}</p>
                </div>
                <div>
                  <p className="text-xs text-surface-400">Vehicle</p>
                  <p className="text-sm font-medium text-surface-900 dark:text-white">{result.vehicle?.vehicleNumber || result.vehicleNumber}</p>
                </div>
                <div>
                  <p className="text-xs text-surface-400">Slot</p>
                  <p className="text-sm font-medium text-surface-900 dark:text-white">{result.slot?.slotNumber || result.slotNumber}</p>
                </div>
                <div>
                  <p className="text-xs text-surface-400">Status</p>
                  <StatusBadge status={result.status} />
                </div>
                <div>
                  <p className="text-xs text-surface-400">Entry Time</p>
                  <p className="text-sm text-surface-700 dark:text-surface-300">{result.entryTime ? new Date(result.entryTime).toLocaleString() : '—'}</p>
                </div>
                <div>
                  <p className="text-xs text-surface-400">Exit Time</p>
                  <p className="text-sm text-surface-700 dark:text-surface-300">{result.exitTime ? new Date(result.exitTime).toLocaleString() : '—'}</p>
                </div>
              </div>
              {result.duration && (
                <div className="flex items-center gap-2 pt-2 border-t border-surface-200 dark:border-surface-700">
                  <FiClock className="h-4 w-4 text-surface-400" />
                  <span className="text-sm text-surface-700 dark:text-surface-300">Duration: <strong>{result.duration ? `${Math.round(result.duration * 60)} Minutes` : '—'}</strong></span>
                </div>
              )}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-12 text-surface-400">
              <FiArrowRightCircle className="h-10 w-10 mb-3" />
              <p className="text-sm">No transaction details yet</p>
              <p className="text-xs">Complete an entry or exit to see details</p>
            </div>
          )}
        </div>
      </div>

      {/* Active Transactions */}
      {activeTransactions.length > 0 && (
        <div className="card p-5 mt-6">
          <h3 className="text-sm font-semibold text-surface-900 dark:text-white mb-4">Active Transactions</h3>
          <div className="space-y-3">
            {activeTransactions.map(t => (
              <div key={t.transactionId || t.id} className="flex items-center justify-between p-3 rounded-lg bg-surface-50 dark:bg-surface-800">
                <div className="flex items-center gap-3">
                  <FiTruck className="h-4 w-4 text-primary-500" />
                  <div>
                    <p className="text-sm font-medium text-surface-900 dark:text-white">{t.vehicle?.vehicleNumber || t.vehicleNumber}</p>
                    <p className="text-xs text-surface-400">Slot {t.slot?.slotNumber || t.slotNumber}</p>
                  </div>
                </div>
                <div className="text-right">
                  <StatusBadge status="ACTIVE" />
                  <p className="text-xs text-surface-400 mt-0.5">{t.entryTime ? new Date(t.entryTime).toLocaleTimeString() : ''}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
