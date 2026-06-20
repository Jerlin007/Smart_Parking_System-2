import { useState, useEffect } from 'react';
import { billingAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import StatusBadge from '../../components/ui/StatusBadge';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import { FiFileText } from 'react-icons/fi';

export default function Bills() {
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadBills();
  }, []);

  const loadBills = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await billingAPI.getMy();
      setBills(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError('Failed to load bills');
      setBills([]);
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { header: 'Bill ID', accessor: 'billingId', sortable: true },
    { header: 'Transaction', accessor: 'transactionId', cell: (row) => row.transactionId || '—' },
    { header: 'Entry Time', accessor: 'entryTime', cell: (row) => row.entryTime ? new Date(row.entryTime).toLocaleString() : '—' },
    { header: 'Exit Time', accessor: 'exitTime', cell: (row) => row.exitTime ? new Date(row.exitTime).toLocaleString() : '—' },
    { header: 'Duration', accessor: 'duration', cell: (row) => row.duration ? `${Math.round(row.duration * 60)} Minutes` : '—' },
    { header: 'Rate/Hr', accessor: 'ratePerHour', cell: (row) => row.ratePerHour ? `₹${row.ratePerHour}` : '—' },
    { header: 'Total', accessor: 'totalAmount', cell: (row) => <span className="font-semibold">₹{row.totalAmount || 0}</span> },
    { header: 'Payment', accessor: 'paymentStatus', cell: (row) => <StatusBadge status={row.paymentStatus === 'PAID' ? 'PAID' : 'UNPAID'} /> },
  ];

  if (error) return <ErrorState message={error} onRetry={loadBills} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Bills' }]} />
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-surface-900 dark:text-white">My Bills</h1>
        <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{bills.length} bills</p>
      </div>
      {bills.length === 0 && !loading ? (
        <EmptyState icon={FiFileText} title="No bills" message="Bills will appear here after you complete a parking session." />
      ) : (
        <DataTable columns={columns} data={bills} loading={loading} searchable searchPlaceholder="Search bills..." />
      )}
    </div>
  );
}
