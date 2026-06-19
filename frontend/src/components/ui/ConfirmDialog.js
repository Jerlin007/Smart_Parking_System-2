import { FiAlertTriangle } from 'react-icons/fi';

export default function ConfirmDialog({ open, onClose, onConfirm, title = 'Confirm', message = 'Are you sure you want to proceed?', confirmLabel = 'Confirm', variant = 'danger', loading = false }) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />
      <div className="relative w-full max-w-sm card p-6 animate-scale-in text-center">
        <div className={`mx-auto h-12 w-12 rounded-full flex items-center justify-center mb-4 ${
          variant === 'danger' ? 'bg-red-50 dark:bg-red-900/20' : 'bg-amber-50 dark:bg-amber-900/20'
        }`}>
          <FiAlertTriangle className={`h-6 w-6 ${
            variant === 'danger' ? 'text-red-500' : 'text-amber-500'
          }`} />
        </div>
        <h3 className="text-lg font-semibold text-surface-900 dark:text-white mb-2">{title}</h3>
        <p className="text-sm text-surface-500 dark:text-surface-400 mb-6">{message}</p>
        <div className="flex gap-3 justify-center">
          <button onClick={onClose} className="btn-secondary" disabled={loading}>Cancel</button>
          <button onClick={onConfirm} className={variant === 'danger' ? 'btn-danger' : 'btn-primary'} disabled={loading}>
            {loading ? 'Processing...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
