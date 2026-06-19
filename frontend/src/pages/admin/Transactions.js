import { useState, useEffect } from 'react';
import { parkingAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import StatusBadge from '../../components/ui/StatusBadge';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import { FiArrowRightCircle } from 'react-icons/fi';

export default function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadTransactions();
  }, []);

  const loadTransactions = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await parkingAPI.getAll();
      setTransactions(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError('Failed to load transactions');
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { header: 'Transaction ID', accessor: 'transactionId', sortable: true },
    { header: 'Vehicle', accessor: 'vehicleNumber', cell: (row) => row.vehicleNumber || '—' },
    { header: 'Slot', accessor: 'slotNumber', cell: (row) => row.slotNumber || '—' },
    { header: 'Entry Time', accessor: 'entryTime', cell: (row) => row.entryTime ? new Date(row.entryTime).toLocaleString() : '—' },
    { header: 'Exit Time', accessor: 'exitTime', cell: (row) => row.exitTime ? new Date(row.exitTime).toLocaleString() : '—' },
    { header: 'Duration', accessor: 'duration', cell: (row) => row.duration ? `${row.duration}h` : '—' },
    { header: 'Status', accessor: 'status', cell: (row) => <StatusBadge status={row.status} /> },
  ];

  if (error) return <ErrorState message={error} onRetry={loadTransactions} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Transactions' }]} />
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Transactions</h1>
        <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{transactions.length} transactions</p>
      </div>
      {transactions.length === 0 && !loading ? (
        <EmptyState icon={FiArrowRightCircle} title="No transactions" message="No parking transactions recorded yet." />
      ) : (
        <DataTable columns={columns} data={transactions} loading={loading} searchable searchPlaceholder="Search transactions..." />
      )}
    </div>
  );
}
