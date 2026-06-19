import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import StatCard from '../../components/ui/StatCard';
import Breadcrumb from '../../components/ui/Breadcrumb';
import { FiTruck, FiCalendar, FiArrowRightCircle, FiFileText, FiCreditCard, FiClock, FiMapPin } from 'react-icons/fi';
import { vehicleAPI, reservationAPI, parkingAPI, parkingLotAPI, billingAPI } from '../../services/api';
import LoadingSkeleton from '../../components/ui/LoadingSkeleton';

export default function CustomerDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [vehiclesRes, reservationsRes, transactionsRes, lotsRes, billsRes] = await Promise.allSettled([
        vehicleAPI.getMy(),
        reservationAPI.getMy(),
        parkingAPI.getMy(),
        parkingLotAPI.getAll(),
        billingAPI.getMy(),
      ]);

      const vehicles = vehiclesRes.status === 'fulfilled' ? vehiclesRes.value.data : [];
      const reservations = reservationsRes.status === 'fulfilled' ? reservationsRes.value.data : [];
      const transactions = transactionsRes.status === 'fulfilled' ? transactionsRes.value.data : [];
      const lots = lotsRes.status === 'fulfilled' ? lotsRes.value.data : [];
      const bills = billsRes.status === 'fulfilled' ? billsRes.value.data : [];

      const activeTransactions = Array.isArray(transactions) ? transactions.filter(t => t.status === 'ACTIVE').length : 0;
      const pendingBills = Array.isArray(bills) ? bills.filter(b => b.paymentStatus !== 'PAID').length : 0;
      setStats({
        myVehicles: Array.isArray(vehicles) ? vehicles.length : 0,
        myReservations: Array.isArray(reservations) ? reservations.length : 0,
        activeTransactions,
        pendingBills,
        totalPayments: transactions.length,
        totalLots: Array.isArray(lots) ? lots.length : 0,
        availableSlots: 0,
        totalSlots: 0,
      });
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Breadcrumb items={[{ label: 'Dashboard' }]} />

      <div className="mb-6">
        <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Welcome, {user?.username}</h1>
        <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">Here's your parking overview</p>
      </div>

      {loading ? (
        <LoadingSkeleton type="card" count={6} />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
          <StatCard icon={FiTruck} label="My Vehicles" value={stats?.myVehicles} color="primary" />
          <StatCard icon={FiCalendar} label="My Reservations" value={stats?.myReservations} color="purple" />
          <StatCard icon={FiClock} label="Active Transactions" value={stats?.activeTransactions} color="amber" />
          <StatCard icon={FiFileText} label="Pending Bills" value={stats?.pendingBills} color="red" />
          <StatCard icon={FiCreditCard} label="Total Payments" value={stats?.totalPayments} color="emerald" />
          <StatCard icon={FiMapPin} label="Parking Lots" value={stats?.totalLots} color="blue" />
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <Link to="/vehicles" className="card-hover p-5 flex items-center gap-4">
          <div className="h-12 w-12 rounded-xl bg-primary-50 dark:bg-primary-900/20 flex items-center justify-center text-primary-600 dark:text-primary-400">
            <FiTruck className="h-6 w-6" />
          </div>
          <div>
            <h3 className="font-semibold text-surface-900 dark:text-white">My Vehicles</h3>
            <p className="text-sm text-surface-500">Register & manage vehicles</p>
          </div>
        </Link>

        <Link to="/reservations" className="card-hover p-5 flex items-center gap-4">
          <div className="h-12 w-12 rounded-xl bg-purple-50 dark:bg-purple-900/20 flex items-center justify-center text-purple-600 dark:text-purple-400">
            <FiCalendar className="h-6 w-6" />
          </div>
          <div>
            <h3 className="font-semibold text-surface-900 dark:text-white">Reservations</h3>
            <p className="text-sm text-surface-500">Book & manage slots</p>
          </div>
        </Link>

        <Link to="/parking" className="card-hover p-5 flex items-center gap-4">
          <div className="h-12 w-12 rounded-xl bg-emerald-50 dark:bg-emerald-900/20 flex items-center justify-center text-emerald-600 dark:text-emerald-400">
            <FiArrowRightCircle className="h-6 w-6" />
          </div>
          <div>
            <h3 className="font-semibold text-surface-900 dark:text-white">Entry / Exit</h3>
            <p className="text-sm text-surface-500">Park & retrieve vehicles</p>
          </div>
        </Link>

        <Link to="/bills" className="card-hover p-5 flex items-center gap-4">
          <div className="h-12 w-12 rounded-xl bg-amber-50 dark:bg-amber-900/20 flex items-center justify-center text-amber-600 dark:text-amber-400">
            <FiFileText className="h-6 w-6" />
          </div>
          <div>
            <h3 className="font-semibold text-surface-900 dark:text-white">Bills</h3>
            <p className="text-sm text-surface-500">View billing history</p>
          </div>
        </Link>

        <Link to="/payments" className="card-hover p-5 flex items-center gap-4">
          <div className="h-12 w-12 rounded-xl bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center text-blue-600 dark:text-blue-400">
            <FiCreditCard className="h-6 w-6" />
          </div>
          <div>
            <h3 className="font-semibold text-surface-900 dark:text-white">Payments</h3>
            <p className="text-sm text-surface-500">Pay bills & view history</p>
          </div>
        </Link>
      </div>
    </div>
  );
}
