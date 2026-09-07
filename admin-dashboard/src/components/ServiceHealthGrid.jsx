import { useEffect, useState } from 'react';
import { api } from '../api.js';

const SERVICES = [
  { name: 'API Gateway', url: 'http://localhost:8080/actuator/health' },
  { name: 'Customer Service', url: 'http://localhost:8081/actuator/health' },
  { name: 'Provisioning Service', url: 'http://localhost:8082/actuator/health' },
  { name: 'Usage/CDR Service', url: 'http://localhost:8083/actuator/health' },
  { name: 'Billing Service', url: 'http://localhost:8084/actuator/health' },
  { name: 'Notification Service', url: 'http://localhost:8085/actuator/health' },
];

export default function ServiceHealthGrid() {
  const [health, setHealth] = useState(SERVICES.map(s => ({ name: s.name, status: 'unknown' })));

  useEffect(() => {
    let cancelled = false;

    async function poll() {
      const results = await Promise.all(SERVICES.map(s => api.getServiceHealth(s.name, s.url)));
      if (!cancelled) setHealth(results);
    }

    poll();
    const interval = setInterval(poll, 15000); // 15s: cheap enough to poll, fresh enough for an ops view
    return () => { cancelled = true; clearInterval(interval); };
  }, []);

  return (
    <div className="panel">
      <p className="panel-title">Service Health</p>
      {health.map(h => (
        <div className="health-row" key={h.name}>
          <span className="health-name">
            <span className={`health-dot dot-${h.status}`}></span>
            {h.name}
          </span>
          <span className="muted" style={{ fontFamily: 'var(--mono)', fontSize: 11 }}>
            {h.status.toUpperCase()}
          </span>
        </div>
      ))}
    </div>
  );
}
