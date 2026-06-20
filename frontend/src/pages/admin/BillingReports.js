import { useState, useEffect } from 'react';
import { billingAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import StatusBadge from '../../components/ui/StatusBadge';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import StatCard from '../../components/ui/StatCard';
import { FiFileText, FiDollarSign, FiTrendingUp, FiClock } from 'react-icons/fi';

export default function BillingReports() {
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
      const res = await billingAPI.getAll();
      setBills(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError('Failed to load billing data');
      setBills([]);
    } finally {
      setLoading(false);
    }
  };

  const totalRevenue = bills.reduce((sum, b) => sum + (b.totalAmount || 0), 0);
  const paidBills = bills.filter(b => b.paymentStatus === 'PAID').length;
  const pendingBills = bills.filter(b => b.paymentStatus !== 'PAID').length;

  const columns = [
    { header: 'Bill ID', accessor: 'billingId', sortable: true },
    { header: 'Transaction ID', accessor: 'transactionId', cell: (row) => row.transactionId || '—' },
    { header: 'Entry Time', accessor: 'entryTime', cell: (row) => row.entryTime ? new Date(row.entryTime).toLocaleString() : '—' },
    { header: 'Exit Time', accessor: 'exitTime', cell: (row) => row.exitTime ? new Date(row.exitTime).toLocaleString() : '—' },
    { header: 'Duration', accessor: 'duration', cell: (row) => row.duration ? `${Math.round(row.duration * 60)} Minutes` : '—' },
    { header: 'Rate/Hour', accessor: 'ratePerHour', cell: (row) => row.ratePerHour ? `₹${row.ratePerHour}` : '—' },
    { header: 'Total', accessor: 'totalAmount', cell: (row) => row.totalAmount ? `₹${row.totalAmount}` : '—' },
    { header: 'Payment', accessor: 'paymentStatus', cell: (row) => <StatusBadge status={row.paymentStatus === 'PAID' ? 'PAID' : 'UNPAID'} /> },
  ];

  if (error) return <ErrorState message={error} onRetry={loadBills} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Billing Reports' }]} />
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Billing Reports</h1>
        <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">Revenue analytics</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard icon={FiFileText} label="Total Bills" value={bills.length} color="primary" />
        <StatCard icon={FiDollarSign} label="Total Revenue" value={`₹${totalRevenue.toLocaleString()}`} color="emerald" />
        <StatCard icon={FiTrendingUp} label="Paid Bills" value={paidBills} color="blue" />
        <StatCard icon={FiClock} label="Pending Bills" value={pendingBills} color="amber" />
      </div>

      {bills.length === 0 && !loading ? (
        <EmptyState icon={FiFileText} title="No bills" message="Bills will be generated automatically when vehicles exit." />
      ) : (
        <DataTable columns={columns} data={bills} loading={loading} searchable searchPlaceholder="Search bills..." />
      )}
    </div>
  );
}
