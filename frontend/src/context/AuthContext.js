import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authAPI, userAPI } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    if (storedToken && storedUser) {
      setToken(storedToken);
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (username, password, rememberMe = false) => {
    const response = await authAPI.login({ username, password });
    const { token: newToken, username: userName, role } = response.data;
    const storage = rememberMe ? localStorage : sessionStorage;
    storage.setItem('token', newToken);
    const userData = { username: userName, role };
    storage.setItem('user', JSON.stringify(userData));
    if (!rememberMe) {
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(userData));
    }
    setToken(newToken);
    setUser(userData);

    try {
      const profileRes = await userAPI.getMe();
      const fullUser = { ...userData, ...profileRes.data };
      storage.setItem('user', JSON.stringify(fullUser));
      localStorage.setItem('user', JSON.stringify(fullUser));
      setUser(fullUser);
    } catch (err) {
      console.error('Failed to fetch user profile:', err);
    }
    return response.data;
  }, []);

  const register = useCallback(async (userData) => {
    const response = await authAPI.register(userData);
    return response.data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  const isAuthenticated = !!token;
  const isAdmin = user?.role === 'ROLE_ADMIN' || user?.role === 'ADMIN';

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, isAuthenticated, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
