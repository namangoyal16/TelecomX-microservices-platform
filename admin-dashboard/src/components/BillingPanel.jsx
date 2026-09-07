import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function BillingPanel({ customerId, msisdn }) {
  const [invoices, setInvoices] = useState([]);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');

  async function refresh() {
    const inv = await api.getInvoices(customerId).catch(() => []);
    setInvoices(inv);
  }

  useEffect(() => { refresh(); const i = setInterval(refresh, 5000); return () => clearInterval(i); }, [customerId]);

  async function simulateUsage() {
    setBusy(true); setMessage('');
    try {
      await api.simulateUsage(customerId, msisdn || '9000000000', 15);
      setMessage('Simulated 15 usage events (calls/SMS/data). Billing accrues these asynchronously via Kafka.');
    } catch (err) { setMessage(err.message); } finally { setBusy(false); }
  }

  async function generateInvoice() {
    setBusy(true); setMessage('');
    try {
      await api.generateInvoice(customerId);
      setMessage('Invoice generated from plan charge + accrued usage.');
      await refresh();
    } catch (err) { setMessage(err.message); } finally { setBusy(false); }
  }

  async function pay(invoiceId, simulateFailure) {
    setBusy(true); setMessage('');
    try {
      await api.payInvoice(customerId, invoiceId, simulateFailure);
      setMessage(simulateFailure ? 'Payment simulated as FAILED (watch for a suspend signal after 3 failures).' : 'Payment successful.');
      await refresh();
    } catch (err) { setMessage(err.message); } finally { setBusy(false); }
  }

  return (
    <div className="panel">
      <p className="panel-title">Billing</p>

      <div style={{ display: 'flex', gap: 8, marginBottom: 14 }}>
        <button disabled={busy} onClick={simulateUsage} style={{ width: 'auto' }}>Simulate usage</button>
        <button disabled={busy} onClick={generateInvoice} style={{ width: 'auto' }}>Generate invoice</button>
      </div>

      {message && <p className="muted">{message}</p>}

      <table>
        <thead>
          <tr><th>Invoice</th><th>Plan</th><th>Usage</th><th>Total</th><th>Status</th><th></th></tr>
        </thead>
        <tbody>
          {invoices.map(inv => (
            <tr key={inv.id}>
              <td>#{inv.id}</td>
              <td>₹{inv.planCharge}</td>
              <td>₹{inv.usageCharge}</td>
              <td>₹{inv.totalAmount}</td>
              <td><span className={`badge badge-${inv.status.toLowerCase()}`}>{inv.status}</span></td>
              <td>
                {inv.status === 'PENDING' && (
                  <div style={{ display: 'flex', gap: 4 }}>
                    <button disabled={busy} onClick={() => pay(inv.id, false)} style={{ width: 'auto', padding: '4px 8px', fontSize: 11 }}>Pay</button>
                    <button disabled={busy} onClick={() => pay(inv.id, true)} style={{ width: 'auto', padding: '4px 8px', fontSize: 11, background: 'var(--signal-red)' }}>Fail</button>
                  </div>
                )}
              </td>
            </tr>
          ))}
          {invoices.length === 0 && (
            <tr><td colSpan="6" className="muted">No invoices yet.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
