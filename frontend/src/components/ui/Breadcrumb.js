import { Link } from 'react-router-dom';
import { FiChevronRight, FiHome } from 'react-icons/fi';

export default function Breadcrumb({ items = [] }) {
  return (
    <nav className="flex items-center gap-1.5 text-sm text-surface-500 dark:text-surface-400 mb-4">
      <Link to="/" className="hover:text-surface-700 dark:hover:text-surface-200 transition-colors">
        <FiHome className="h-4 w-4" />
      </Link>
      {items.map((item, i) => (
        <div key={i} className="flex items-center gap-1.5">
          <FiChevronRight className="h-3.5 w-3.5" />
          {item.path ? (
            <Link to={item.path} className="hover:text-surface-700 dark:hover:text-surface-200 transition-colors">
              {item.label}
            </Link>
          ) : (
            <span className="text-surface-900 dark:text-white font-medium">{item.label}</span>
          )}
        </div>
      ))}
    </nav>
  );
}
