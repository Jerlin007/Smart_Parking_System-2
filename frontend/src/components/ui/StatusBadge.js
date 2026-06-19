export default function StatusBadge({ status, size = 'sm' }) {
  const statusMap = {
    AVAILABLE: { class: 'badge-green', label: 'Available' },
    RESERVED: { class: 'badge-yellow', label: 'Reserved' },
    OCCUPIED: { class: 'badge-red', label: 'Occupied' },
    CONFIRMED: { class: 'badge-green', label: 'Confirmed' },
    PENDING: { class: 'badge-yellow', label: 'Pending' },
    CANCELLED: { class: 'badge-red', label: 'Cancelled' },
    ACTIVE: { class: 'badge-green', label: 'Active' },
    INACTIVE: { class: 'badge-gray', label: 'Inactive' },
    PAID: { class: 'badge-green', label: 'Paid' },
    UNPAID: { class: 'badge-yellow', label: 'Unpaid' },
    FAILED: { class: 'badge-red', label: 'Failed' },
    SUCCESS: { class: 'badge-green', label: 'Success' },
    CAR: { class: 'badge-blue', label: 'Car' },
    BIKE: { class: 'badge-green', label: 'Bike' },
    EV: { class: 'badge-cyan', label: 'EV' },
    ROLE_ADMIN: { class: 'badge-purple', label: 'Admin' },
    ADMIN: { class: 'badge-purple', label: 'Admin' },
    ROLE_CUSTOMER: { class: 'badge-blue', label: 'Customer' },
    CUSTOMER: { class: 'badge-blue', label: 'Customer' },
    CASH: { class: 'badge-green', label: 'Cash' },
    UPI: { class: 'badge-blue', label: 'UPI' },
    CARD: { class: 'badge-purple', label: 'Card' },
  };

  const info = statusMap[status] || { class: 'badge-gray', label: status };

  return (
    <span className={`${info.class} ${size === 'sm' ? 'text-xs' : 'text-sm'}`}>
      {info.label}
    </span>
  );
}
