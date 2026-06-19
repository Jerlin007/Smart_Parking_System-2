import { useState, useEffect } from 'react';
import { paymentAPI, billingAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import Modal from '../../components/ui/Modal';
import StatusBadge from '../../components/ui/StatusBadge';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import { FiCreditCard, FiDownload, FiCheckCircle } from 'react-icons/fi';
import toast from 'react-hot-toast';

export default function CustomerPayments() {
  const [payments, setPayments] = useState([]);
  const [unpaidBills, setUnpaidBills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [payModal, setPayModal] = useState(false);
  const [selectedBill, setSelectedBill] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('UPI');
  const [processing, setProcessing] = useState(false);
  const [successScreen, setSuccessScreen] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [payRes, billRes] = await Promise.allSettled([
        paymentAPI.getMy(),
        billingAPI.getMy(),
      ]);
      setPayments(Array.isArray(payRes.value?.data) ? payRes.value.data : []);
      const bills = Array.isArray(billRes.value?.data) ? billRes.value.data : [];
      setUnpaidBills(bills.filter(b => b.paymentStatus !== 'PAID'));
    } catch (err) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handlePay = async () => {
    if (!selectedBill) return;
    setProcessing(true);
    try {
      const res = await paymentAPI.pay(selectedBill.billingId || selectedBill.id, paymentMethod);
      setSuccessScreen({
        amount: selectedBill.totalAmount,
        method: paymentMethod,
        paymentId: res.data?.paymentId || res.data?.id,
        time: new Date().toLocaleString(),
      });
      setPayModal(false);
      toast.success('Payment successful!');
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Payment failed');
    } finally {
      setProcessing(false);
    }
  };

  const downloadReceipt = (payment) => {
    const receipt = `
SMART PARKING SYSTEM - PAYMENT RECEIPT
========================================
Receipt #: ${payment.paymentId || payment.id}
Amount: ₹${payment.amount || 0}
Method: ${payment.paymentMethod}
Status: ${payment.status}
Date: ${payment.paymentTime ? new Date(payment.paymentTime).toLocaleString() : ''}
========================================
Thank you for using Smart Parking!
    `.trim();
    const blob = new Blob([receipt], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `receipt-${payment.paymentId || payment.id}.txt`;
    a.click();
    URL.revokeObjectURL(url);
    toast.success('Receipt downloaded');
  };

  const columns = [
    { header: 'Payment ID', accessor: 'paymentId', sortable: true },
    { header: 'Amount', accessor: 'amount', cell: (row) => `₹${row.amount || 0}` },
    { header: 'Method', accessor: 'paymentMethod', cell: (row) => <StatusBadge status={row.paymentMethod} /> },
    { header: 'Status', accessor: 'status', cell: (row) => <StatusBadge status={row.status === 'SUCCESS' ? 'SUCCESS' : row.status || '—'} /> },
    { header: 'Date', accessor: 'paymentTime', cell: (row) => row.paymentTime ? new Date(row.paymentTime).toLocaleString() : '—' },
    {
      header: 'Actions',
      accessor: 'actions',
      cell: (row) => (
        <button onClick={(e) => { e.stopPropagation(); downloadReceipt(row); }} className="btn-ghost p-1.5 text-primary-500">
          <FiDownload className="h-3.5 w-3.5" />
        </button>
      ),
    },
  ];

  if (error) return <ErrorState message={error} onRetry={loadData} />;

  if (successScreen) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="card p-8 max-w-md w-full text-center animate-scale-in">
          <div className="mx-auto h-16 w-16 rounded-full bg-emerald-50 dark:bg-emerald-900/20 flex items-center justify-center mb-4">
            <FiCheckCircle className="h-8 w-8 text-emerald-500" />
          </div>
          <h2 className="text-xl font-bold text-surface-900 dark:text-white mb-2">Payment Successful!</h2>
          <p className="text-sm text-surface-500 mb-6">Your payment has been processed successfully.</p>
          <div className="bg-surface-50 dark:bg-surface-800 rounded-lg p-4 mb-6 text-left space-y-2">
            <div className="flex justify-between"><span className="text-sm text-surface-500">Amount</span><span className="text-sm font-semibold">₹{successScreen.amount}</span></div>
            <div className="flex justify-between"><span className="text-sm text-surface-500">Method</span><span className="text-sm">{successScreen.method}</span></div>
            <div className="flex justify-between"><span className="text-sm text-surface-500">Payment ID</span><span className="text-sm">#{successScreen.paymentId}</span></div>
            <div className="flex justify-between"><span className="text-sm text-surface-500">Time</span><span className="text-sm">{successScreen.time}</span></div>
          </div>
          <div className="flex gap-3 justify-center">
            <button onClick={() => { setSuccessScreen(null); downloadReceipt(successScreen); }} className="btn-secondary">
              <FiDownload className="h-4 w-4" />
              Download Receipt
            </button>
            <button onClick={() => setSuccessScreen(null)} className="btn-primary">
              Continue
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <Breadcrumb items={[{ label: 'Payments' }]} />
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Payments</h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{payments.length} transactions</p>
        </div>
      </div>

      {unpaidBills.length > 0 && (
        <div className="card p-5 mb-6 border-2 border-amber-200 dark:border-amber-800">
          <h3 className="text-sm font-semibold text-surface-900 dark:text-white mb-4">Pending Payments</h3>
          <div className="space-y-3">
            {unpaidBills.map(bill => (
              <div key={bill.billingId || bill.id} className="flex items-center justify-between p-3 rounded-lg bg-amber-50 dark:bg-amber-900/10">
                <div>
                  <p className="text-sm font-medium text-surface-900 dark:text-white">Bill #{bill.billingId || bill.id}</p>
                  <p className="text-xs text-surface-400">Amount: ₹{bill.totalAmount || 0}</p>
                </div>
                <button
                  onClick={() => { setSelectedBill(bill); setPayModal(true); }}
                  className="btn-primary text-sm"
                >
                  <FiCreditCard className="h-4 w-4" />
                  Pay Now
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {payments.length === 0 && !loading ? (
        <EmptyState icon={FiCreditCard} title="No payments" message="Your payment history will appear here." />
      ) : (
        <DataTable columns={columns} data={payments} loading={loading} searchable searchPlaceholder="Search payments..." />
      )}

      <Modal open={payModal} onClose={() => setPayModal(false)} title="Pay Bill" size="sm">
        <div className="space-y-4">
          <div className="p-4 rounded-lg bg-surface-50 dark:bg-surface-800 text-center">
            <p className="text-sm text-surface-500">Amount Due</p>
            <p className="text-3xl font-bold text-surface-900 dark:text-white">₹{selectedBill?.totalAmount || 0}</p>
          </div>
          <div>
            <label className="label">Payment Method</label>
            <div className="grid grid-cols-3 gap-2">
              {['UPI', 'CARD', 'CASH'].map(method => (
                <button
                  key={method}
                  onClick={() => setPaymentMethod(method)}
                  className={`p-3 rounded-lg border text-sm font-medium transition-all ${
                    paymentMethod === method
                      ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-300'
                      : 'border-surface-200 dark:border-surface-700 text-surface-600 dark:text-surface-400 hover:border-surface-300'
                  }`}
                >
                  {method}
                </button>
              ))}
            </div>
          </div>
          <button onClick={handlePay} className="btn-primary w-full" disabled={processing}>
            {processing ? 'Processing...' : `Pay ₹${selectedBill?.totalAmount || 0}`}
          </button>
        </div>
      </Modal>
    </div>
  );
}
