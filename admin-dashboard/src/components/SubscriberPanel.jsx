import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function SubscriberPanel({ customerId }) {
  const [plans, setPlans] = useState([]);
  const [subscriptions, setSubscriptions] = useState([]);
  const [provisioning, setProvisioning] = useState(null);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');

  async function refresh() {
    const subs = await api.getSubscriptions(customerId).catch(() => []);
    setSubscriptions(subs);
    const prov = await api.getProvisioningStatus(customerId).catch(() => null);
    setProvisioning(prov);
  }

  useEffect(() => {
    api.getPlans().then(setPlans).catch(() => {});
    refresh();
    const interval = setInterval(refresh, 5000); // short poll so provisioning's async result shows up quickly
    return () => clearInterval(interval);
  }, [customerId]);

  async function subscribe(planCode) {
    setBusy(true);
    setMessage('');
    try {
      await api.subscribe(customerId, planCode);
      setMessage(`Subscription requested for ${planCode}. Provisioning happens asynchronously — status will update below shortly.`);
      await refresh();
    } catch (err) {
      setMessage(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="panel">
      <p className="panel-title">Subscriber Plan & Provisioning</p>

      <div style={{ display: 'flex', gap: 8, marginBottom: 14, flexWrap: 'wrap' }}>
        {plans.map(p => (
          <button key={p.code} disabled={busy} onClick={() => subscribe(p.code)}
                  style={{ width: 'auto', flex: '1 1 140px' }}>
            {p.name} · ₹{p.monthlyPrice}
          </button>
        ))}
      </div>

      {message && <p className="muted">{message}</p>}

      <table>
        <thead>
          <tr><th>Subscription</th><th>Plan</th><th>Status</th><th>MSISDN</th></tr>
        </thead>
        <tbody>
          {subscriptions.map(s => (
            <tr key={s.id}>
              <td>#{s.id}</td>
              <td>{s.planCode}</td>
              <td><span className={`badge badge-${s.status.toLowerCase()}`}>{s.status}</span></td>
              <td>{s.msisdn || '—'}</td>
            </tr>
          ))}
          {subscriptions.length === 0 && (
            <tr><td colSpan="4" className="muted">No subscriptions yet — pick a plan above.</td></tr>
          )}
        </tbody>
      </table>

      {provisioning && (
        <p className="muted" style={{ marginTop: 10 }}>
          Provisioning record: <span style={{ fontFamily: 'var(--mono)' }}>{provisioning.msisdn}</span> is{' '}
          <span className={`badge badge-${provisioning.status.toLowerCase()}`}>{provisioning.status}</span>
        </p>
      )}
    </div>
  );
}
