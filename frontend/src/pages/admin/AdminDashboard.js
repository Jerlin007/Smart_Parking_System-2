import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import StatCard from '../../components/ui/StatCard';
import LoadingSkeleton from '../../components/ui/LoadingSkeleton';
import Breadcrumb from '../../components/ui/Breadcrumb';
import {
  FiUsers, FiTruck, FiMapPin, FiLayers, FiCalendar,
  FiArrowRightCircle, FiDollarSign, FiTrendingUp, FiActivity,
  FiUserCheck, FiClock
} from 'react-icons/fi';
import { userAPI, vehicleAPI, parkingLotAPI, parkingSlotAPI, reservationAPI, parkingAPI, billingAPI } from '../../services/api';
import { Tooltip, ResponsiveContainer, PieChart, Pie, Cell, AreaChart, Area, LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';

const COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

export default function AdminDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [recentActivity, setRecentActivity] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [usersRes, vehiclesRes, lotsRes, slotsRes, reservationsRes, transactionsRes, billsRes] = await Promise.allSettled([
        userAPI.getAll(),
        vehicleAPI.getAll(),
        parkingLotAPI.getAll(),
        parkingSlotAPI.getAll(),
        reservationAPI.getAll(),
        parkingAPI.getAll(),
        billingAPI.getAll(),
      ]);

      const users = usersRes.status === 'fulfilled' ? usersRes.value.data : [];
      const vehicles = vehiclesRes.status === 'fulfilled' ? vehiclesRes.value.data : [];
      const lots = lotsRes.status === 'fulfilled' ? lotsRes.value.data : [];
      const slots = slotsRes.status === 'fulfilled' ? slotsRes.value.data : [];
      const reservations = reservationsRes.status === 'fulfilled' ? reservationsRes.value.data : [];
      const transactions = transactionsRes.status === 'fulfilled' ? transactionsRes.value.data : [];
      const bills = billsRes.status === 'fulfilled' ? billsRes.value.data : [];

      const activeUsers = Array.isArray(users) ? users.filter(u => u.status !== 'INACTIVE').length : 0;
      const totalSlots = Array.isArray(slots) ? slots.length : 0;
      const availableSlots = Array.isArray(slots) ? slots.filter(s => s.status === 'AVAILABLE').length : 0;
      const occupiedSlots = Array.isArray(slots) ? slots.filter(s => s.status === 'OCCUPIED').length : 0;
      const reservedSlots = Array.isArray(slots) ? slots.filter(s => s.status === 'RESERVED').length : 0;
      const totalReservations = Array.isArray(reservations) ? reservations.length : 0;
      const activeTransactions = Array.isArray(transactions) ? transactions.filter(t => t.status === 'ACTIVE').length : 0;
      const totalRevenue = Array.isArray(bills) ? bills.reduce((sum, b) => sum + (b.totalAmount || 0), 0) : 0;

      const slotUtilization = [
        { name: 'Available', value: availableSlots, color: '#10b981' },
        { name: 'Occupied', value: occupiedSlots, color: '#ef4444' },
        { name: 'Reserved', value: reservedSlots, color: '#f59e0b' },
      ].filter(s => s.value > 0);

      const vehicleTypeCounts = {};
      if (Array.isArray(vehicles)) {
        vehicles.forEach(v => { vehicleTypeCounts[v.vehicleType] = (vehicleTypeCounts[v.vehicleType] || 0) + 1; });
      }
      const vehicleTypeData = Object.entries(vehicleTypeCounts).map(([name, value]) => ({ name, value }));

      const monthlyMap = {};
      if (Array.isArray(bills)) {
        bills.forEach(b => {
          if (b.entryTime || (b.transaction?.entryTime)) {
            const date = new Date(b.entryTime || b.transaction.entryTime);
            const key = date.toLocaleString('default', { month: 'short' });
            monthlyMap[key] = (monthlyMap[key] || 0) + (b.totalAmount || 0);
          }
        });
      }
      const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      const revenueData = monthNames.filter(m => monthlyMap[m]).map(m => ({ month: m, revenue: monthlyMap[m], occupancy: 0 }));

      setStats({
        totalUsers: Array.isArray(users) ? users.length : 0,
        activeUsers,
        totalVehicles: Array.isArray(vehicles) ? vehicles.length : 0,
        totalLots: Array.isArray(lots) ? lots.length : 0,
        totalSlots,
        availableSlots,
        occupiedSlots,
        reservedSlots,
        totalReservations,
        activeTransactions,
        totalRevenue,
        monthlyRevenue: revenueData.length > 0 ? revenueData[revenueData.length - 1].revenue : 0,
        slotUtilization,
        vehicleTypeData,
        revenueData,
      });

      const activity = [];
      if (Array.isArray(transactions)) {
        transactions.slice(-6).reverse().forEach(t => {
          const isEntry = t.status === 'ACTIVE';
          activity.push({
            type: isEntry ? 'entry' : 'exit',
            text: isEntry ? `Vehicle entered slot ${t.slotNumber || ''}` : `Vehicle exited slot ${t.slotNumber || ''}`,
            time: t.entryTime ? new Date(t.entryTime).toLocaleString() : '',
            icon: FiArrowRightCircle,
            color: isEntry ? 'text-emerald-500' : 'text-blue-500',
          });
        });
      }
      if (Array.isArray(reservations)) {
        reservations.slice(-3).reverse().forEach(r => {
          activity.push({
            type: 'reservation',
            text: `Reservation created for slot ${r.slotNumber || ''}`,
            time: r.startTime ? new Date(r.startTime).toLocaleString() : '',
            icon: FiCalendar,
            color: 'text-primary-500',
          });
        });
      }
      activity.sort((a, b) => new Date(b.time) - new Date(a.time));
      setRecentActivity(activity.slice(0, 8));
    } catch (err) {
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const statCards = [
    { icon: FiUsers, label: 'Total Users', value: stats?.totalUsers, color: 'primary' },
    { icon: FiUserCheck, label: 'Active Users', value: stats?.activeUsers, color: 'emerald' },
    { icon: FiTruck, label: 'Total Vehicles', value: stats?.totalVehicles, color: 'blue' },
    { icon: FiMapPin, label: 'Parking Lots', value: stats?.totalLots, color: 'purple' },
    { icon: FiLayers, label: 'Total Slots', value: stats?.totalSlots, color: 'cyan' },
    { icon: FiTruck, label: 'Available Slots', value: stats?.availableSlots, color: 'emerald' },
    { icon: FiClock, label: 'Occupied Slots', value: stats?.occupiedSlots, color: 'red' },
    { icon: FiCalendar, label: 'Reserved Slots', value: stats?.reservedSlots, color: 'amber' },
    { icon: FiCalendar, label: 'Total Reservations', value: stats?.totalReservations, color: 'primary' },
    { icon: FiActivity, label: 'Active Transactions', value: stats?.activeTransactions, color: 'blue' },
    { icon: FiDollarSign, label: 'Total Revenue', value: stats ? `₹${stats.totalRevenue.toLocaleString()}` : null, color: 'emerald' },
    { icon: FiTrendingUp, label: 'Monthly Revenue', value: stats ? `₹${(stats.monthlyRevenue).toLocaleString()}` : null, color: 'purple' },
  ];

  return (
    <div>
      <Breadcrumb items={[{ label: 'Dashboard' }]} />

      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Admin Dashboard</h1>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">Welcome back, {user?.username}</p>
        </div>
      </div>

      {error && (
        <div className="mb-6 p-4 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
          <p className="text-sm text-red-600 dark:text-red-400">{error}</p>
        </div>
      )}

      {loading ? (
        <LoadingSkeleton type="card" count={12} />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 mb-8">
          {statCards.map((card, i) => (
            <StatCard key={i} {...card} loading={false} />
          ))}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        {stats?.revenueData && stats.revenueData.length > 0 && (
          <div className="card p-5">
            <h3 className="text-sm font-semibold text-surface-900 dark:text-white mb-4">Revenue Trend</h3>
            {loading ? (
              <LoadingSkeleton type="chart" />
            ) : (
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={stats.revenueData}>
                    <defs>
                      <linearGradient id="revenueGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3} />
                        <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="month" stroke="#94a3b8" fontSize={12} />
                    <YAxis stroke="#94a3b8" fontSize={12} />
                    <Tooltip contentStyle={{ backgroundColor: '#fff', border: '1px solid #e2e8f0', borderRadius: '8px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }} />
                    <Area type="monotone" dataKey="revenue" stroke="#6366f1" fill="url(#revenueGradient)" strokeWidth={2} />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        )}

        {stats?.revenueData && stats.revenueData.length > 0 && (
          <div className="card p-5">
            <h3 className="text-sm font-semibold text-surface-900 dark:text-white mb-4">Occupancy Trend</h3>
            {loading ? (
              <LoadingSkeleton type="chart" />
            ) : (
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={stats.revenueData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="month" stroke="#94a3b8" fontSize={12} />
                    <YAxis stroke="#94a3b8" fontSize={12} />
                    <Tooltip contentStyle={{ backgroundColor: '#fff', border: '1px solid #e2e8f0', borderRadius: '8px' }} />
                    <Line type="monotone" dataKey="revenue" stroke="#10b981" strokeWidth={2} dot={{ fill: '#10b981', strokeWidth: 2 }} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        )}

        <div className="card p-5">
          <h3 className="text-sm font-semibold text-surface-900 dark:text-white mb-4">Slot Utilization</h3>
          {loading ? (
            <LoadingSkeleton type="chart" />
          ) : (
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={stats?.slotUtilization} cx="50%" cy="50%" innerRadius={60} outerRadius={90} paddingAngle={4} dataKey="value">
                    {stats?.slotUtilization?.map((entry, index) => (
                      <Cell key={index} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
              <div className="flex justify-center gap-6 mt-2">
                {stats?.slotUtilization?.map((item, i) => (
                  <div key={i} className="flex items-center gap-2">
                    <div className="h-3 w-3 rounded-full" style={{ backgroundColor: item.color }} />
                    <span className="text-xs text-surface-600 dark:text-surface-400">{item.name}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {stats?.vehicleTypeData && stats.vehicleTypeData.length > 0 && (
          <div className="card p-5">
            <h3 className="text-sm font-semibold text-surface-900 dark:text-white mb-4">Vehicle Type Distribution</h3>
            {loading ? (
              <LoadingSkeleton type="chart" />
            ) : (
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={stats.vehicleTypeData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                    <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} />
                    <YAxis stroke="#94a3b8" fontSize={12} />
                    <Tooltip />
                    <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                      {stats.vehicleTypeData.map((entry, index) => (
                        <Cell key={index} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 card">
          <div className="px-5 py-4 border-b border-surface-200 dark:border-surface-700">
            <h3 className="text-sm font-semibold text-surface-900 dark:text-white">Recent Activity</h3>
          </div>
          <div className="p-5">
            {loading ? (
              <LoadingSkeleton type="list" count={6} />
            ) : recentActivity.length === 0 ? (
              <p className="text-sm text-surface-400 text-center py-8">No recent activity</p>
            ) : (
              <div className="space-y-4">
                {recentActivity.map((item, i) => (
                  <div key={i} className="flex items-start gap-3">
                    <div className={`h-8 w-8 rounded-lg bg-surface-100 dark:bg-surface-800 flex items-center justify-center flex-shrink-0 ${item.color}`}>
                      <item.icon className="h-4 w-4" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm text-surface-700 dark:text-surface-300">{item.text}</p>
                      <p className="text-xs text-surface-400 mt-0.5">{item.time}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="px-5 py-4 border-b border-surface-200 dark:border-surface-700">
            <h3 className="text-sm font-semibold text-surface-900 dark:text-white">Quick Actions</h3>
          </div>
          <div className="p-5 space-y-2">
            <Link to="/admin/users" className="flex items-center gap-3 p-3 rounded-lg hover:bg-surface-50 dark:hover:bg-surface-800 transition-colors">
              <FiUsers className="h-4 w-4 text-primary-500" />
              <span className="text-sm font-medium text-surface-700 dark:text-surface-300">Manage Users</span>
            </Link>
            <Link to="/admin/lots" className="flex items-center gap-3 p-3 rounded-lg hover:bg-surface-50 dark:hover:bg-surface-800 transition-colors">
              <FiMapPin className="h-4 w-4 text-emerald-500" />
              <span className="text-sm font-medium text-surface-700 dark:text-surface-300">Manage Parking Lots</span>
            </Link>
            <Link to="/admin/slots" className="flex items-center gap-3 p-3 rounded-lg hover:bg-surface-50 dark:hover:bg-surface-800 transition-colors">
              <FiLayers className="h-4 w-4 text-amber-500" />
              <span className="text-sm font-medium text-surface-700 dark:text-surface-300">Manage Slots</span>
            </Link>
            <Link to="/admin/billing" className="flex items-center gap-3 p-3 rounded-lg hover:bg-surface-50 dark:hover:bg-surface-800 transition-colors">
              <FiDollarSign className="h-4 w-4 text-purple-500" />
              <span className="text-sm font-medium text-surface-700 dark:text-surface-300">Billing Reports</span>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
