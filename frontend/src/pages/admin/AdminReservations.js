import { useState, useEffect } from 'react';
import { reservationAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import StatusBadge from '../../components/ui/StatusBadge';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import { FiCalendar } from 'react-icons/fi';

export default function AdminReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    loadReservations();
  }, []);

  const loadReservations = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await reservationAPI.getAll();
      setReservations(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError('Failed to load reservations');
      setReservations([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredReservations = statusFilter
    ? reservations.filter(r => r.status === statusFilter)
    : reservations;

  const columns = [
    { header: 'ID', accessor: 'reservationId', sortable: true },
    { header: 'Vehicle', accessor: 'vehicleNumber', cell: (row) => row.vehicleNumber || '—' },
    { header: 'Slot', accessor: 'slotNumber', cell: (row) => row.slotNumber || '—' },
    { header: 'Reservation Time', accessor: 'reservationTime', cell: (row) => row.reservationTime ? new Date(row.reservationTime).toLocaleString() : '—' },
    { header: 'Start Time', accessor: 'startTime', cell: (row) => row.startTime ? new Date(row.startTime).toLocaleString() : '—' },
    { header: 'End Time', accessor: 'endTime', cell: (row) => row.endTime ? new Date(row.endTime).toLocaleString() : '—' },
    { header: 'Status', accessor: 'status', cell: (row) => <StatusBadge status={row.status} /> },
  ];

  if (error) return <ErrorState message={error} onRetry={loadReservations} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Reservations' }]} />
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white">All Reservations</h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{reservations.length} reservations</p>
        </div>
      </div>

      <div className="card p-4 mb-6">
        <div className="flex gap-3">
          <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)} className="select-field text-sm">
            <option value="">All Status</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="PENDING">Pending</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      {filteredReservations.length === 0 && !loading ? (
        <EmptyState icon={FiCalendar} title="No reservations" message="No reservations match your criteria." />
      ) : (
        <DataTable columns={columns} data={filteredReservations} loading={loading} searchable searchPlaceholder="Search reservations..." />
      )}
    </div>
  );
}
