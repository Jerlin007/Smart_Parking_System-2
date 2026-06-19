import { useState, useEffect } from 'react';
import { vehicleAPI } from '../../services/api';
import DataTable from '../../components/ui/DataTable';
import StatusBadge from '../../components/ui/StatusBadge';
import Breadcrumb from '../../components/ui/Breadcrumb';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import { FiTruck, FiDownload } from 'react-icons/fi';

export default function AdminVehicles() {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadVehicles();
  }, []);

  const loadVehicles = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await vehicleAPI.getAll();
      setVehicles(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError('Failed to load vehicles');
      setVehicles([]);
    } finally {
      setLoading(false);
    }
  };

  const exportCSV = () => {
    const headers = ['Vehicle Number', 'Type', 'Owner Name', 'Mobile'];
    const rows = vehicles.map(v => [v.vehicleNumber, v.vehicleType, v.ownerName, v.mobileNumber]);
    const csv = [headers, ...rows].map(r => r.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'vehicles.csv';
    a.click();
    URL.revokeObjectURL(url);
  };

  const columns = [
    { header: '#', accessor: 'vehicleId', sortable: true },
    { header: 'Vehicle Number', accessor: 'vehicleNumber', sortable: true },
    {
      header: 'Type',
      accessor: 'vehicleType',
      cell: (row) => <StatusBadge status={row.vehicleType} />,
    },
    { header: 'Owner Name', accessor: 'ownerName', sortable: true },
    { header: 'Mobile Number', accessor: 'mobileNumber' },
  ];

  if (error) return <ErrorState message={error} onRetry={loadVehicles} />;

  return (
    <div>
      <Breadcrumb items={[{ label: 'Vehicles' }]} />
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white">All Vehicles</h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">{vehicles.length} vehicles registered</p>
        </div>
        <button onClick={exportCSV} className="btn-secondary">
          <FiDownload className="h-4 w-4" />
          Export CSV
        </button>
      </div>

      {vehicles.length === 0 && !loading ? (
        <EmptyState icon={FiTruck} title="No vehicles found" message="No vehicles have been registered yet." />
      ) : (
        <DataTable
          columns={columns}
          data={vehicles}
          loading={loading}
          searchable
          searchPlaceholder="Search vehicles by number or owner..."
        />
      )}
    </div>
  );
}
