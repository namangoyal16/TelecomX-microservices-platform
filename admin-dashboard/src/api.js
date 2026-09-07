const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function authHeaders() {
  const token = localStorage.getItem('telecomx_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
      ...(options.headers || {}),
    },
  });
  if (!res.ok) {
    let detail = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      detail = body.detail || detail;
    } catch (_) { /* non-JSON error body */ }
    throw new Error(detail);
  }
  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  login: (email, password) =>
    request('/api/v1/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),

  register: (fullName, email, phoneNumber, password) =>
    request('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify({ fullName, email, phoneNumber, password }),
    }),

  getPlans: () => request('/api/v1/plans'),

  getSubscriptions: (customerId) => request(`/api/v1/customers/${customerId}/subscriptions`),
  subscribe: (customerId, planCode) =>
    request(`/api/v1/customers/${customerId}/subscriptions`, {
      method: 'POST',
      body: JSON.stringify({ planCode }),
    }),

  getProvisioningStatus: (customerId) => request(`/api/v1/provisioning/customers/${customerId}`),

  getInvoices: (customerId) => request(`/api/v1/customers/${customerId}/invoices`),
  generateInvoice: (customerId) =>
    request(`/api/v1/customers/${customerId}/invoices/generate`, { method: 'POST' }),
  payInvoice: (customerId, invoiceId, simulateFailure = false) =>
    request(`/api/v1/customers/${customerId}/invoices/${invoiceId}/pay`, {
      method: 'POST',
      headers: { 'Idempotency-Key': crypto.randomUUID() },
      body: JSON.stringify({ simulateFailure }),
    }),

  simulateUsage: (customerId, msisdn, count = 10) =>
    request(`/api/v1/usage/simulate/customers/${customerId}?msisdn=${msisdn}&count=${count}`, {
      method: 'POST',
    }),

  getRecentNotifications: () => request('/api/v1/notifications/recent'),

  // Actuator health is exposed per-service; the dashboard polls each directly
  // through the gateway is not wired per-service, so this hits public actuator
  // ports directly for the local dev topology (see README on why this differs
  // from normal traffic routing).
  getServiceHealth: async (name, url) => {
    try {
      const res = await fetch(url, { method: 'GET' });
      const body = await res.json();
      return { name, status: body.status === 'UP' ? 'up' : 'down' };
    } catch (_) {
      return { name, status: 'down' };
    }
  },
};

export { BASE_URL };
