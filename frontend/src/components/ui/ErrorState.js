import { FiAlertCircle, FiRefreshCw } from 'react-icons/fi';

export default function ErrorState({ title = 'Something went wrong', message = 'An error occurred while loading data.', onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-4">
      <div className="h-14 w-14 rounded-full bg-red-50 dark:bg-red-900/20 flex items-center justify-center mb-4">
        <FiAlertCircle className="h-7 w-7 text-red-500" />
      </div>
      <h3 className="text-lg font-semibold text-surface-900 dark:text-white mb-1">{title}</h3>
      <p className="text-sm text-surface-500 dark:text-surface-400 text-center max-w-sm mb-6">{message}</p>
      {onRetry && (
        <button onClick={onRetry} className="btn-primary">
          <FiRefreshCw className="h-4 w-4" />
          Try Again
        </button>
      )}
    </div>
  );
}
