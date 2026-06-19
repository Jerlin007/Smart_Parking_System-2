import { useState, useEffect, useCallback } from 'react';
import { userAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import StatusBadge from '../../components/ui/StatusBadge';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import { FiTrash2, FiUsers } from 'react-icons/fi';
import toast from 'react-hot-toast';

export default function Users() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [saving, setSaving] = useState(false);

  const loadUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await userAPI.getAll();
      const data = Array.isArray(res.data) ? res.data : [];
      setUsers(data);
    } catch (err) {
      setError('Failed to load users');
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadUsers(); }, [loadUsers]);

  const handleDelete = async () => {
    if (!selectedUser) return;
    setSaving(true);
    try {
      await userAPI.delete(selectedUser.id || selectedUser.userId);
      toast.success('User deleted successfully');
      setDeleteConfirm(false);
      loadUsers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete user');
    } finally {
      setSaving(false);
    }
  };

  const columns = [
    { header: 'ID', accessor: 'userId', sortable: true },
    { header: 'Username', accessor: 'username', sortable: true },
    { header: 'Email', accessor: 'email', sortable: true },
    {
      header: 'Role',
      accessor: 'role',
      cell: (row) => <StatusBadge status={row.role} />,
    },
    {
      header: 'Status',
      accessor: 'status',
      cell: (row) => <StatusBadge status={row.status === 'INACTIVE' ? 'INACTIVE' : 'ACTIVE'} />,
    },
    {
      header: 'Created',
      accessor: 'createdDate',
      cell: (row) => row.createdDate ? new Date(row.createdDate).toLocaleDateString() : '—',
    },
    {
      header: 'Actions',
      accessor: 'actions',
      cell: (row) => (
        <div className="flex items-center gap-1">
          <button onClick={(e) => { e.stopPropagation(); setSelectedUser(row); setDeleteConfirm(true); }} className="btn-ghost p-1.5 text-red-500" title="Delete">
            <FiTrash2 className="h-3.5 w-3.5" />
          </button>
        </div>
      ),
    },
  ];

  if (error) return <ErrorState message={error} onRetry={loadUsers} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Users' }]} />
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white">User Management</h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{users.length} users registered</p>
        </div>
      </div>

      {users.length === 0 && !loading ? (
        <EmptyState icon={FiUsers} title="No users found" message="Users will appear here once they register." />
      ) : (
        <DataTable
          columns={columns}
          data={users}
          loading={loading}
          searchable
          searchPlaceholder="Search users..."
        />
      )}

      <ConfirmDialog
        open={deleteConfirm}
        onClose={() => setDeleteConfirm(false)}
        onConfirm={handleDelete}
        title="Delete User"
        message={`Are you sure you want to delete "${selectedUser?.username}"? This action cannot be undone.`}
        loading={saving}
      />
    </div>
  );
}
