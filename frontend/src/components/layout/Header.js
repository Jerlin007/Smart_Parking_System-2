import { useState, useRef, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import ThemeToggle from './ThemeToggle';
import { FiMenu, FiChevronDown, FiUser, FiLogOut, FiSettings } from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';

export default function Header({ onMenuClick }) {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [profileOpen, setProfileOpen] = useState(false);
  const profileRef = useRef(null);

  useEffect(() => {
    const handleClick = (e) => {
      if (profileRef.current && !profileRef.current.contains(e.target)) setProfileOpen(false);
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-20 bg-white/80 dark:bg-surface-900/80 backdrop-blur-xl border-b border-surface-200 dark:border-surface-700">
      <div className="flex items-center justify-between h-16 px-4 lg:px-6">
        <div className="flex items-center gap-3">
          <button onClick={onMenuClick} className="btn-ghost p-2 lg:hidden">
            <FiMenu className="h-5 w-5" />
          </button>
        </div>

        <div className="flex items-center gap-2">
          <ThemeToggle />

          <div className="relative" ref={profileRef}>
            <button
              onClick={() => setProfileOpen(!profileOpen)}
              className="flex items-center gap-2 pl-2 pr-1 py-1 rounded-lg hover:bg-surface-100 dark:hover:bg-surface-800 transition-colors"
            >
              <div className="h-7 w-7 rounded-lg bg-primary-600 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
                {user?.username?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div className="hidden md:block text-left">
                <p className="text-sm font-medium text-surface-900 dark:text-white leading-tight">{user?.username || 'User'}</p>
                <p className="text-xs text-surface-500 dark:text-surface-400 leading-tight">{isAdmin ? 'Admin' : 'Customer'}</p>
              </div>
              <FiChevronDown className="h-3.5 w-3.5 text-surface-400" />
            </button>
            {profileOpen && (
              <div className="absolute right-0 top-full mt-2 w-48 card py-1 animate-scale-in shadow-xl">
                <div className="px-4 py-2 border-b border-surface-200 dark:border-surface-700">
                  <p className="text-sm font-medium text-surface-900 dark:text-white">{user?.username}</p>
                  <p className="text-xs text-surface-500">{user?.email || ''}</p>
                </div>
                <button onClick={() => { setProfileOpen(false); navigate('/profile'); }} className="flex items-center gap-2 w-full px-4 py-2 text-sm text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-800 transition-colors">
                  <FiUser className="h-4 w-4" />
                  Profile
                </button>
                <button className="flex items-center gap-2 w-full px-4 py-2 text-sm text-surface-700 dark:text-surface-300 hover:bg-surface-50 dark:hover:bg-surface-800 transition-colors">
                  <FiSettings className="h-4 w-4" />
                  Settings
                </button>
                <div className="border-t border-surface-200 dark:border-surface-700 mt-1 pt-1">
                  <button onClick={handleLogout} className="flex items-center gap-2 w-full px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors">
                    <FiLogOut className="h-4 w-4" />
                    Logout
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
