import { useAuth } from '../../context/AuthContext';
import Breadcrumb from '../../components/ui/Breadcrumb';
import { FiUser, FiMail, FiShield, FiCalendar } from 'react-icons/fi';

export default function Profile() {
  const { user } = useAuth();

  return (
    <div>
      <Breadcrumb items={[{ label: 'Profile' }]} />
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-surface-900 dark:text-white">My Profile</h1>
        <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">Manage your account information</p>
      </div>

      <div className="max-w-2xl">
        <div className="card p-6">
          <div className="flex items-center gap-4 mb-6">
            <div className="h-16 w-16 rounded-2xl bg-primary-600 flex items-center justify-center text-white text-2xl font-bold">
              {user?.username?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <div>
              <h2 className="text-xl font-bold text-surface-900 dark:text-white">{user?.username}</h2>
              <p className="text-sm text-surface-500">{user?.email || 'No email'}</p>
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex items-center gap-3 p-3 rounded-lg bg-surface-50 dark:bg-surface-800">
              <FiUser className="h-5 w-5 text-primary-500" />
              <div>
                <p className="text-xs text-surface-400">Username</p>
                <p className="text-sm font-medium text-surface-900 dark:text-white">{user?.username}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 p-3 rounded-lg bg-surface-50 dark:bg-surface-800">
              <FiMail className="h-5 w-5 text-primary-500" />
              <div>
                <p className="text-xs text-surface-400">Email</p>
                <p className="text-sm font-medium text-surface-900 dark:text-white">{user?.email || 'Not provided'}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 p-3 rounded-lg bg-surface-50 dark:bg-surface-800">
              <FiShield className="h-5 w-5 text-primary-500" />
              <div>
                <p className="text-xs text-surface-400">Role</p>
                <p className="text-sm font-medium text-surface-900 dark:text-white">{user?.role?.replace('ROLE_', '') || 'User'}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 p-3 rounded-lg bg-surface-50 dark:bg-surface-800">
              <FiCalendar className="h-5 w-5 text-primary-500" />
              <div>
                <p className="text-xs text-surface-400">User ID</p>
                <p className="text-sm font-medium text-surface-900 dark:text-white">#{user?.userId || user?.id || '—'}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
