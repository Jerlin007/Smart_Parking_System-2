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
      const paymentData = res.data;
      setSuccessScreen({
        paymentId: paymentData?.paymentId || paymentData?.id,
        billingId: selectedBill.billingId || selectedBill.id,
        transactionId: selectedBill.transactionId,
        amount: paymentData?.amount || selectedBill.totalAmount,
        paymentMethod: paymentData?.paymentMethod || paymentMethod,
        status: paymentData?.status || 'SUCCESS',
        paymentTime: paymentData?.paymentTime || new Date().toISOString(),
        vehicleNumber: selectedBill.vehicleNumber,
        vehicleType: selectedBill.vehicleType,
        slotNumber: selectedBill.slotNumber,
        entryTime: selectedBill.entryTime,
        exitTime: selectedBill.exitTime,
        duration: selectedBill.duration,
        lotName: selectedBill.lotName,
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

  const printReceipt = (payment) => {
    const receiptContent = generateReceiptHtml(payment);
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>Payment Receipt</title>
        <style>
          body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 40px; color: #1e293b; }
          .receipt-card { max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; padding: 40px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
          .header { text-align: center; border-bottom: 2px solid #1e293b; padding-bottom: 20px; margin-bottom: 24px; }
          .header h1 { font-size: 22px; margin: 0; color: #1e293b; letter-spacing: 1px; }
          .header h2 { font-size: 16px; margin: 4px 0 0; color: #64748b; font-weight: 400; }
          .section { margin-bottom: 20px; }
          .section-title { font-size: 13px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
          .row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; border-bottom: 1px solid #f1f5f9; }
          .row:last-child { border-bottom: none; }
          .label { color: #64748b; }
          .value { font-weight: 600; color: #1e293b; }
          .total-row { font-size: 16px; padding: 10px 0; border-top: 2px solid #1e293b; margin-top: 8px; }
          .total-row .value { font-size: 18px; }
          .footer { text-align: center; margin-top: 24px; padding-top: 16px; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8; }
          .badge { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; background: #dcfce7; color: #166534; }
          .badge.failed { background: #fef2f2; color: #991b1b; }
          .badge.pending { background: #fef9c3; color: #854d0e; }
          @media print { body { padding: 20px; } .receipt-card { box-shadow: none; border: 1px solid #ccc; } }
        </style>
      </head>
      <body>
        ${receiptContent}
        <script>window.print();<\/script>
      </body>
      </html>
    `);
    printWindow.document.close();
  };

  const downloadReceipt = (payment) => {
    const receiptContent = generateReceiptHtml(payment);
    const blob = new Blob([`
      <!DOCTYPE html>
      <html>
      <head>
        <title>Payment Receipt</title>
        <style>
          body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 40px; color: #1e293b; }
          .receipt-card { max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; padding: 40px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
          .header { text-align: center; border-bottom: 2px solid #1e293b; padding-bottom: 20px; margin-bottom: 24px; }
          .header h1 { font-size: 22px; margin: 0; color: #1e293b; letter-spacing: 1px; }
          .header h2 { font-size: 16px; margin: 4px 0 0; color: #64748b; font-weight: 400; }
          .section { margin-bottom: 20px; }
          .section-title { font-size: 13px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
          .row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; border-bottom: 1px solid #f1f5f9; }
          .row:last-child { border-bottom: none; }
          .label { color: #64748b; }
          .value { font-weight: 600; color: #1e293b; }
          .total-row { font-size: 16px; padding: 10px 0; border-top: 2px solid #1e293b; margin-top: 8px; }
          .total-row .value { font-size: 18px; }
          .footer { text-align: center; margin-top: 24px; padding-top: 16px; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8; }
        </style>
      </head>
      <body>${receiptContent}</body>
      </html>
    `], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `receipt-${payment.paymentId || payment.id}.html`;
    a.click();
    URL.revokeObjectURL(url);
    toast.success('Receipt downloaded');
  };

  const generateReceiptHtml = (p) => {
    const statusClass = p.status === 'SUCCESS' || p.status === 'PAID' ? '' : p.status === 'FAILED' ? 'failed' : 'pending';
    return `
      <div class="receipt-card">
        <div class="header">
          <h1>SMART PARKING MANAGEMENT SYSTEM</h1>
          <h2>Payment Receipt</h2>
        </div>
        <div class="section">
          <div class="section-title">Receipt Information</div>
          <div class="row"><span class="label">Receipt Number</span><span class="value">#${p.paymentId || '—'}</span></div>
          <div class="row"><span class="label">Bill Number</span><span class="value">#${p.billingId || '—'}</span></div>
          <div class="row"><span class="label">Transaction ID</span><span class="value">#${p.transactionId || '—'}</span></div>
        </div>
        <div class="section">
          <div class="section-title">Vehicle Information</div>
          <div class="row"><span class="label">Vehicle Number</span><span class="value">${p.vehicleNumber || '—'}</span></div>
          <div class="row"><span class="label">Vehicle Type</span><span class="value">${p.vehicleType || '—'}</span></div>
        </div>
        <div class="section">
          <div class="section-title">Parking Information</div>
          <div class="row"><span class="label">Parking Lot</span><span class="value">${p.lotName || '—'}</span></div>
          <div class="row"><span class="label">Slot Number</span><span class="value">${p.slotNumber || '—'}</span></div>
          <div class="row"><span class="label">Entry Time</span><span class="value">${p.entryTime ? new Date(p.entryTime).toLocaleString() : '—'}</span></div>
          <div class="row"><span class="label">Exit Time</span><span class="value">${p.exitTime ? new Date(p.exitTime).toLocaleString() : '—'}</span></div>
          <div class="row"><span class="label">Duration</span><span class="value">${p.duration ? Math.round(p.duration * 60) + ' Minutes' : '—'}</span></div>
        </div>
        <div class="section">
          <div class="section-title">Payment Information</div>
          <div class="row"><span class="label">Amount</span><span class="value">₹${(p.amount || 0).toFixed(2)}</span></div>
          <div class="row"><span class="label">Payment Method</span><span class="value">${p.paymentMethod || '—'}</span></div>
          <div class="row"><span class="label">Payment Status</span><span class="value"><span class="badge ${statusClass}">${p.status || '—'}</span></span></div>
          <div class="row total-row"><span class="label">Payment Date</span><span class="value">${p.paymentTime ? new Date(p.paymentTime).toLocaleString() : '—'}</span></div>
        </div>
        <div class="footer">Thank you for using Smart Parking Management System</div>
      </div>
    `;
  };

  const columns = [
    { header: 'Payment ID', accessor: 'paymentId', sortable: true },
    { header: 'Amount', accessor: 'amount', cell: (row) => `₹${row.amount || 0}` },
    { header: 'Method', accessor: 'paymentMethod', cell: (row) => <StatusBadge status={row.paymentMethod || '—'} /> },
    { header: 'Status', accessor: 'status', cell: (row) => <StatusBadge status={row.status === 'SUCCESS' ? 'SUCCESS' : row.status || '—'} /> },
    { header: 'Date', accessor: 'paymentTime', cell: (row) => row.paymentTime ? new Date(row.paymentTime).toLocaleString() : '—' },
    {
      header: 'Receipt',
      accessor: 'actions',
      cell: (row) => (
        <button onClick={(e) => { e.stopPropagation(); printReceipt(row); }} className="btn-ghost p-1.5 text-primary-500" title="Print Receipt">
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
            <div className="flex justify-between"><span className="text-sm text-surface-500">Method</span><span className="text-sm">{successScreen.paymentMethod}</span></div>
            <div className="flex justify-between"><span className="text-sm text-surface-500">Status</span><span className="text-sm"><StatusBadge status={successScreen.status} /></span></div>
            <div className="flex justify-between"><span className="text-sm text-surface-500">Payment ID</span><span className="text-sm">#{successScreen.paymentId}</span></div>
            <div className="flex justify-between"><span className="text-sm text-surface-500">Time</span><span className="text-sm">{successScreen.paymentTime ? new Date(successScreen.paymentTime).toLocaleString() : ''}</span></div>
          </div>
          <div className="flex gap-3 justify-center">
            <button onClick={() => { printReceipt(successScreen); }} className="btn-secondary">
              <FiDownload className="h-4 w-4" />
              Print / PDF
            </button>
            <button onClick={() => { downloadReceipt(successScreen); }} className="btn-secondary">
              <FiDownload className="h-4 w-4" />
              Download
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
