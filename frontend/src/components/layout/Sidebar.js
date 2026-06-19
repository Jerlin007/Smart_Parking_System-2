import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  FiGrid, FiUsers, FiTruck, FiMapPin, FiLayers, FiCalendar,
  FiArrowRightCircle, FiFileText, FiCreditCard, FiSettings,
  FiChevronLeft, FiChevronRight, FiLogOut, FiX,
  FiDollarSign
} from 'react-icons/fi';

const adminLinks = [
  { to: '/admin', icon: FiGrid, label: 'Dashboard' },
  { to: '/admin/users', icon: FiUsers, label: 'Users' },
  { to: '/admin/vehicles', icon: FiTruck, label: 'Vehicles' },
  { to: '/admin/lots', icon: FiMapPin, label: 'Parking Lots' },
  { to: '/admin/slots', icon: FiLayers, label: 'Parking Slots' },
  { to: '/admin/reservations', icon: FiCalendar, label: 'Reservations' },
  { to: '/admin/transactions', icon: FiArrowRightCircle, label: 'Transactions' },
  { to: '/admin/billing', icon: FiFileText, label: 'Billing Reports' },
  { to: '/admin/payments', icon: FiDollarSign, label: 'Payments' },
  { to: '/admin/settings', icon: FiSettings, label: 'Settings' },
];

const customerLinks = [
  { to: '/dashboard', icon: FiGrid, label: 'Dashboard' },
  { to: '/vehicles', icon: FiTruck, label: 'My Vehicles' },
  { to: '/reservations', icon: FiCalendar, label: 'Reservations' },
  { to: '/parking', icon: FiArrowRightCircle, label: 'Entry / Exit' },
  { to: '/bills', icon: FiFileText, label: 'Bills' },
  { to: '/payments', icon: FiCreditCard, label: 'Payments' },
  { to: '/profile', icon: FiSettings, label: 'Profile' },
];

export default function Sidebar({ mobileOpen, setMobileOpen, collapsed, setCollapsed }) {
  const { isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const links = isAdmin ? adminLinks : customerLinks;

  const handleLogout = () => {
    logout();
    navigate('/login');
    setMobileOpen?.(false);
  };

  const linkContent = (
    <div className="flex flex-col h-full">
      <div className="px-4 py-5 border-b border-surface-200 dark:border-surface-700">
        <div className="flex items-center gap-3">
          <div className="h-8 w-8 rounded-lg bg-primary-600 flex items-center justify-center flex-shrink-0">
            <FiLayers className="h-4 w-4 text-white" />
          </div>
          {!collapsed && (
            <div>
              <h1 className="text-sm font-bold text-surface-900 dark:text-white">Smart Parking</h1>
              <p className="text-xs text-surface-500 dark:text-surface-400">{isAdmin ? 'Admin Panel' : 'Customer Portal'}</p>
            </div>
          )}
        </div>
      </div>

      <nav className="flex-1 px-2 py-4 space-y-1 overflow-y-auto scrollbar-thin">
        {links.map(link => (
          <NavLink
            key={link.to}
            to={link.to}
            onClick={() => setMobileOpen?.(false)}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 ${
                isActive
                  ? 'bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-300'
                  : 'text-surface-600 dark:text-surface-400 hover:bg-surface-100 dark:hover:bg-surface-800 hover:text-surface-900 dark:hover:text-white'
              }`
            }
          >
            <link.icon className="h-4.5 w-4.5 flex-shrink-0" />
            {!collapsed && <span>{link.label}</span>}
          </NavLink>
        ))}
      </nav>

      <div className="px-2 py-4 border-t border-surface-200 dark:border-surface-700 space-y-1">
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
        >
          <FiLogOut className="h-4.5 w-4.5 flex-shrink-0" />
          {!collapsed && <span>Logout</span>}
        </button>
        {!collapsed && (
          <button
            onClick={() => setCollapsed?.(true)}
            className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-surface-400 hover:text-surface-600 dark:hover:text-surface-300 hover:bg-surface-100 dark:hover:bg-surface-800 transition-colors lg:hidden"
          >
            <FiChevronLeft className="h-4 w-4" />
            <span>Collapse</span>
          </button>
        )}
      </div>
    </div>
  );

  return (
    <>
      <aside
        className={`hidden lg:flex flex-col fixed left-0 top-0 h-full bg-white dark:bg-surface-900 border-r border-surface-200 dark:border-surface-700 z-30 transition-all duration-300 ${
          collapsed ? 'w-[68px]' : 'w-60'
        }`}
      >
        {linkContent}
        <button
          onClick={() => setCollapsed?.(c => !c)}
          className="absolute -right-3 top-20 h-6 w-6 rounded-full bg-white dark:bg-surface-800 border border-surface-200 dark:border-surface-700 flex items-center justify-center text-surface-400 hover:text-surface-600 dark:hover:text-surface-300 transition-colors shadow-sm"
        >
          {collapsed ? <FiChevronRight className="h-3 w-3" /> : <FiChevronLeft className="h-3 w-3" />}
        </button>
      </aside>

      {mobileOpen && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div className="absolute inset-0 bg-black/50" onClick={() => setMobileOpen(false)} />
          <aside className="relative w-72 h-full bg-white dark:bg-surface-900 shadow-xl animate-slide-in">
            <div className="flex justify-end p-2">
              <button onClick={() => setMobileOpen(false)} className="btn-ghost p-1.5">
                <FiX className="h-5 w-5" />
              </button>
            </div>
            {linkContent}
          </aside>
        </div>
      )}
    </>
  );
}
