import { useState } from 'react';
import Breadcrumb from '../../components/ui/Breadcrumb';
import { FiBell, FiShield, FiGlobe, FiSave } from 'react-icons/fi';
import toast from 'react-hot-toast';

export default function Settings() {
  const [settings, setSettings] = useState({
    siteName: 'Smart Parking System',
    timezone: 'Asia/Kolkata',
    currency: 'INR',
    ratePerHour: 50,
    enableNotifications: true,
    enableAutoBilling: true,
    maintenanceMode: false,
  });

  const handleSave = () => {
    toast.success('Settings saved successfully');
  };

  return (
    <div>
      <Breadcrumb items={[{ label: 'Settings' }]} />
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-surface-900 dark:text-white">Settings</h1>
        <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">System configuration</p>
      </div>

      <div className="space-y-6 max-w-2xl">
        {/* General */}
        <div className="card p-5">
          <div className="flex items-center gap-3 mb-4">
            <FiGlobe className="h-5 w-5 text-primary-500" />
            <h3 className="text-sm font-semibold text-surface-900 dark:text-white">General Settings</h3>
          </div>
          <div className="space-y-4">
            <div>
              <label className="label">Site Name</label>
              <input type="text" value={settings.siteName} onChange={e => setSettings({ ...settings, siteName: e.target.value })} className="input-field" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Timezone</label>
                <select value={settings.timezone} onChange={e => setSettings({ ...settings, timezone: e.target.value })} className="select-field">
                  <option value="Asia/Kolkata">Asia/Kolkata (UTC+5:30)</option>
                  <option value="UTC">UTC</option>
                  <option value="America/New_York">America/New_York</option>
                </select>
              </div>
              <div>
                <label className="label">Currency</label>
                <select value={settings.currency} onChange={e => setSettings({ ...settings, currency: e.target.value })} className="select-field">
                  <option value="INR">INR (₹)</option>
                  <option value="USD">USD ($)</option>
                  <option value="EUR">EUR (€)</option>
                </select>
              </div>
            </div>
            <div>
              <label className="label">Rate Per Hour (₹)</label>
              <input type="number" value={settings.ratePerHour} onChange={e => setSettings({ ...settings, ratePerHour: e.target.value })} className="input-field" min="0" />
            </div>
          </div>
        </div>

        {/* Notifications */}
        <div className="card p-5">
          <div className="flex items-center gap-3 mb-4">
            <FiBell className="h-5 w-5 text-amber-500" />
            <h3 className="text-sm font-semibold text-surface-900 dark:text-white">Notifications</h3>
          </div>
          <div className="space-y-4">
            <label className="flex items-center justify-between">
              <span className="text-sm text-surface-700 dark:text-surface-300">Enable Notifications</span>
              <input type="checkbox" checked={settings.enableNotifications} onChange={e => setSettings({ ...settings, enableNotifications: e.target.checked })} className="rounded border-surface-300 text-primary-600 focus:ring-primary-500" />
            </label>
            <label className="flex items-center justify-between">
              <span className="text-sm text-surface-700 dark:text-surface-300">Auto Billing</span>
              <input type="checkbox" checked={settings.enableAutoBilling} onChange={e => setSettings({ ...settings, enableAutoBilling: e.target.checked })} className="rounded border-surface-300 text-primary-600 focus:ring-primary-500" />
            </label>
          </div>
        </div>

        {/* Security */}
        <div className="card p-5">
          <div className="flex items-center gap-3 mb-4">
            <FiShield className="h-5 w-5 text-red-500" />
            <h3 className="text-sm font-semibold text-surface-900 dark:text-white">Security</h3>
          </div>
          <div className="space-y-4">
            <label className="flex items-center justify-between">
              <div>
                <span className="text-sm text-surface-700 dark:text-surface-300">Maintenance Mode</span>
                <p className="text-xs text-surface-400">Block new entries and show maintenance page</p>
              </div>
              <input type="checkbox" checked={settings.maintenanceMode} onChange={e => setSettings({ ...settings, maintenanceMode: e.target.checked })} className="rounded border-surface-300 text-primary-600 focus:ring-primary-500" />
            </label>
          </div>
        </div>

        <div className="flex justify-end">
          <button onClick={handleSave} className="btn-primary">
            <FiSave className="h-4 w-4" />
            Save Settings
          </button>
        </div>
      </div>
    </div>
  );
}
