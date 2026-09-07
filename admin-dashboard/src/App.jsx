import { useState } from 'react';
import Login from './components/Login.jsx';
import ServiceHealthGrid from './components/ServiceHealthGrid.jsx';
import SubscriberPanel from './components/SubscriberPanel.jsx';
import BillingPanel from './components/BillingPanel.jsx';
import NotificationsFeed from './components/NotificationsFeed.jsx';

export default function App() {
  const [customerId, setCustomerId] = useState(() => localStorage.getItem('telecomx_customer_id'));

  function handleLogout() {
    localStorage.removeItem('telecomx_token');
    localStorage.removeItem('telecomx_customer_id');
    setCustomerId(null);
  }

  if (!customerId) {
    return (
      <div className="app-shell">
        <Login onAuthenticated={(r) => setCustomerId(String(r.customerId))} />
      </div>
    );
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <span className="brand-bars"><span></span><span></span><span></span><span></span></span>
          <div>
            <div className="brand-name">TelecomX</div>
            <div className="brand-sub">ops console · customer #{customerId}</div>
          </div>
        </div>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <span className="env-pill">● live</span>
          <button className="logout-link" onClick={handleLogout}>Sign out</button>
        </div>
      </header>

      <section className="block">
        <p className="block-heading">Platform status</p>
        <ServiceHealthGrid />
      </section>

      <section className="block two-col">
        <SubscriberPanel customerId={customerId} />
        <NotificationsFeed />
      </section>

      <section className="block">
        <p className="block-heading">Billing</p>
        <BillingPanel customerId={customerId} />
      </section>
    </div>
  );
}
