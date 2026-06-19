import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import AppLayout from './components/layout/AppLayout';
import { Toaster } from 'react-hot-toast';

import Login from './pages/auth/Login';
import Register from './pages/auth/Register';

import AdminDashboard from './pages/admin/AdminDashboard';
import AdminUsers from './pages/admin/Users';
import AdminVehicles from './pages/admin/AdminVehicles';
import AdminParkingLots from './pages/admin/ParkingLots';
import AdminParkingSlots from './pages/admin/ParkingSlots';
import AdminReservations from './pages/admin/AdminReservations';
import Transactions from './pages/admin/Transactions';
import BillingReports from './pages/admin/BillingReports';
import AdminPayments from './pages/admin/AdminPayments';
import Settings from './pages/admin/Settings';

import CustomerDashboard from './pages/customer/CustomerDashboard';
import MyVehicles from './pages/customer/MyVehicles';
import CustomerReservations from './pages/customer/CustomerReservations';
import EntryExit from './pages/customer/EntryExit';
import Bills from './pages/customer/Bills';
import CustomerPayments from './pages/customer/CustomerPayments';
import Profile from './pages/customer/Profile';

function AdminRoute({ children }) {
  const { isAdmin, isAuthenticated, loading } = useAuth();
  if (loading) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!isAdmin) return <Navigate to="/dashboard" replace />;
  return children;
}

function RoleRedirect() {
  const { isAdmin, isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={isAdmin ? '/admin' : '/dashboard'} replace />;
}

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Router>
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 3000,
              style: {
                borderRadius: '12px',
                background: '#fff',
                color: '#0f172a',
                fontSize: '14px',
                boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
              },
            }}
          />
          <Routes>
            <Route path="/login" element={<AppLayout><Login /></AppLayout>} />
            <Route path="/register" element={<AppLayout><Register /></AppLayout>} />

            <Route path="/dashboard" element={<AppLayout><CustomerDashboard /></AppLayout>} />
            <Route path="/vehicles" element={<AppLayout><MyVehicles /></AppLayout>} />
            <Route path="/reservations" element={<AppLayout><CustomerReservations /></AppLayout>} />
            <Route path="/parking" element={<AppLayout><EntryExit /></AppLayout>} />
            <Route path="/bills" element={<AppLayout><Bills /></AppLayout>} />
            <Route path="/payments" element={<AppLayout><CustomerPayments /></AppLayout>} />
            <Route path="/profile" element={<AppLayout><Profile /></AppLayout>} />

            <Route path="/admin" element={<AppLayout><AdminRoute><AdminDashboard /></AdminRoute></AppLayout>} />
            <Route path="/admin/users" element={<AppLayout><AdminRoute><AdminUsers /></AdminRoute></AppLayout>} />
            <Route path="/admin/vehicles" element={<AppLayout><AdminRoute><AdminVehicles /></AdminRoute></AppLayout>} />
            <Route path="/admin/lots" element={<AppLayout><AdminRoute><AdminParkingLots /></AdminRoute></AppLayout>} />
            <Route path="/admin/slots" element={<AppLayout><AdminRoute><AdminParkingSlots /></AdminRoute></AppLayout>} />
            <Route path="/admin/reservations" element={<AppLayout><AdminRoute><AdminReservations /></AdminRoute></AppLayout>} />
            <Route path="/admin/transactions" element={<AppLayout><AdminRoute><Transactions /></AdminRoute></AppLayout>} />
            <Route path="/admin/billing" element={<AppLayout><AdminRoute><BillingReports /></AdminRoute></AppLayout>} />
            <Route path="/admin/payments" element={<AppLayout><AdminRoute><AdminPayments /></AdminRoute></AppLayout>} />
            <Route path="/admin/settings" element={<AppLayout><AdminRoute><Settings /></AdminRoute></AppLayout>} />

            <Route path="/" element={<AppLayout><RoleRedirect /></AppLayout>} />
            <Route path="*" element={<AppLayout><RoleRedirect /></AppLayout>} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
