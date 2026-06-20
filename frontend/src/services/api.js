import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || '';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
};

export const userAPI = {
  getAll: (params) => api.get('/api/users', { params }),
  getMe: () => api.get('/api/users/me'),
  getById: (id) => api.get(`/api/users/${id}`),
  create: (userData) => api.post('/api/users', userData),
  update: (id, userData) => api.put(`/api/users/${id}`, userData),
  delete: (id) => api.delete(`/api/users/${id}`),
  activate: (id) => api.put(`/api/users/${id}/activate`),
  deactivate: (id) => api.put(`/api/users/${id}/deactivate`),
};

export const vehicleAPI = {
  getAll: (params) => api.get('/api/vehicles', { params }),
  getMy: () => api.get('/api/vehicles/my'),
  getById: (id) => api.get(`/api/vehicles/${id}`),
  create: (vehicleData) => api.post('/api/vehicles', vehicleData),
  update: (id, vehicleData) => api.put(`/api/vehicles/${id}`, vehicleData),
  delete: (id) => api.delete(`/api/vehicles/${id}`),
};

export const parkingLotAPI = {
  getAll: () => api.get('/api/lots'),
  getById: (id) => api.get(`/api/lots/${id}`),
  create: (lotData) => api.post('/api/lots', lotData),
  update: (id, lotData) => api.put(`/api/lots/${id}`, lotData),
  delete: (id) => api.delete(`/api/lots/${id}`),
};

export const parkingSlotAPI = {
  getAll: () => api.get('/api/slots'),
  getById: (id) => api.get(`/api/slots/${id}`),
  getAvailable: (lotId, slotType) => {
    const params = {};
    if (lotId) params.lotId = lotId;
    if (slotType) params.slotType = slotType;
    return api.get('/api/slots/available', { params });
  },
  create: (slotData) => api.post('/api/slots', slotData),
  update: (id, slotData) => api.put(`/api/slots/${id}`, slotData),
  delete: (id) => api.delete(`/api/slots/${id}`),
};

export const reservationAPI = {
  getAll: () => api.get('/api/reservations'),
  getMy: () => api.get('/api/reservations/my'),
  getById: (id) => api.get(`/api/reservations/${id}`),
  create: (vehicleId, slotId, startTime, endTime) => {
    let url = `/api/reservations?vehicleId=${vehicleId}&slotId=${slotId}`;
    if (startTime) url += `&startTime=${startTime}`;
    if (endTime) url += `&endTime=${endTime}`;
    return api.post(url);
  },
  cancel: (id) => api.put(`/api/reservations/cancel/${id}`),
};

export const parkingAPI = {
  entry: (vehicleNumber, slotId) =>
    api.post(`/api/parking/entry?vehicleNumber=${vehicleNumber}&slotId=${slotId}`),
  exit: (transactionId) =>
    api.post(`/api/parking/exit?transactionId=${transactionId}`),
  getById: (id) => api.get(`/api/parking/${id}`),
  getMy: () => api.get('/api/parking/my'),
  getAll: () => api.get('/api/parking/all'),
  getReservedSlots: () => api.get('/api/parking/reserved-slots'),
};

export const billingAPI = {
  generate: (transactionId) => api.post(`/api/billing/generate/${transactionId}`),
  getById: (id) => api.get(`/api/billing/${id}`),
  getAll: (params) => api.get('/api/billing', { params }),
  getMy: () => api.get('/api/billing/my'),
};

export const paymentAPI = {
  pay: (billingId, method) =>
    api.post(`/api/payments/pay/${billingId}?method=${method}`),
  getById: (id) => api.get(`/api/payments/${id}`),
  getAll: (params) => api.get('/api/payments', { params }),
  getMy: () => api.get('/api/payments/my'),
};

export const dashboardAPI = {
  getAdminStats: () => api.get('/api/dashboard/admin/stats'),
  getCustomerStats: () => api.get('/api/dashboard/customer/stats'),
};

export const revenueAPI = {
  getReports: (params) => api.get('/api/revenue', { params }),
};

export const occupancyAPI = {
  getReports: (params) => api.get('/api/occupancy', { params }),
};

export default api;
