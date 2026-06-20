import { useState, useEffect } from 'react';
import { paymentAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import StatusBadge from '../../components/ui/StatusBadge';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import StatCard from '../../components/ui/StatCard';
import { FiDollarSign, FiCreditCard, FiCheckCircle } from 'react-icons/fi';

export default function AdminPayments() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadPayments();
  }, []);

  const loadPayments = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await paymentAPI.getAll();
      setPayments(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError('Failed to load payments');
      setPayments([]);
    } finally {
      setLoading(false);
    }
  };

  const successfulPayments = payments.filter(p => p.status === 'SUCCESS').length;
  const totalCollected = payments.reduce((sum, p) => sum + (p.amount || 0), 0);

  const columns = [
    { header: 'Payment ID', accessor: 'paymentId', sortable: true },
    { header: 'Bill ID', accessor: 'billingId', cell: (row) => row.billingId || '—' },
    { header: 'Amount', accessor: 'amount', cell: (row) => `₹${row.amount || 0}` },
    { header: 'Method', accessor: 'paymentMethod', cell: (row) => <StatusBadge status={row.paymentMethod || '—'} /> },
    { header: 'Status', accessor: 'status', cell: (row) => <StatusBadge status={row.status === 'SUCCESS' ? 'SUCCESS' : row.status || '—'} /> },
    { header: 'Date', accessor: 'paymentTime', cell: (row) => row.paymentTime ? new Date(row.paymentTime).toLocaleString() : '—' },
  ];

  if (error) return <ErrorState message={error} onRetry={loadPayments} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Payments' }]} />
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Payments</h1>
        <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{payments.length} transactions</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <StatCard icon={FiCreditCard} label="Total Payments" value={payments.length} color="primary" />
        <StatCard icon={FiCheckCircle} label="Successful" value={successfulPayments} color="emerald" />
        <StatCard icon={FiDollarSign} label="Total Collected" value={`₹${totalCollected.toLocaleString()}`} color="blue" />
      </div>

      {payments.length === 0 && !loading ? (
        <EmptyState icon={FiDollarSign} title="No payments" message="No payments have been processed yet." />
      ) : (
        <DataTable columns={columns} data={payments} loading={loading} searchable searchPlaceholder="Search payments..." />
      )}
    </div>
  );
}
