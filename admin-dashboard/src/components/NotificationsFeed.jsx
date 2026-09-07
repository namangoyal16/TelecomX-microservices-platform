import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function NotificationsFeed() {
  const [items, setItems] = useState([]);

  useEffect(() => {
    let cancelled = false;
    async function poll() {
      const data = await api.getRecentNotifications().catch(() => []);
      if (!cancelled) setItems(data.slice(0, 8));
    }
    poll();
    const interval = setInterval(poll, 6000);
    return () => { cancelled = true; clearInterval(interval); };
  }, []);

  return (
    <div className="panel">
      <p className="panel-title">Recent Notifications</p>
      {items.map(n => (
        <div className="notif-item" key={n.id}>
          <div className="notif-subject">{n.subject}</div>
          <div className="notif-message">{n.message}</div>
          <div className="notif-meta">{n.channel} · customer #{n.customerId} · {new Date(n.sentAt).toLocaleTimeString()}</div>
        </div>
      ))}
      {items.length === 0 && <p className="muted">No notifications yet — trigger an action to see events flow through Kafka.</p>}
    </div>
  );
}
