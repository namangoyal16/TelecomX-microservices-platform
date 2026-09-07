import { useState } from 'react';
import { api } from '../api.js';

export default function Login({ onAuthenticated }) {
  const [mode, setMode] = useState('login');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('admin@telecomx.dev');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const result = mode === 'login'
        ? await api.login(email, password)
        : await api.register(fullName, email, phone, password);
      localStorage.setItem('telecomx_token', result.token);
      localStorage.setItem('telecomx_customer_id', String(result.customerId));
      onAuthenticated(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-shell panel">
      <p className="login-title">TelecomX Ops Console</p>
      <p className="login-sub">
        {mode === 'login' ? 'Sign in to view platform status' : 'Create a demo subscriber account'}
      </p>
      <form onSubmit={submit}>
        {mode === 'register' && (
          <>
            <input placeholder="Full name" value={fullName} onChange={e => setFullName(e.target.value)} required />
            <input placeholder="Phone number" value={phone} onChange={e => setPhone(e.target.value)} required />
          </>
        )}
        <input type="email" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} required />
        <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} required />
        {error && <p className="error-text">{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
        </button>
      </form>
      <p className="muted" style={{ marginTop: 14, cursor: 'pointer' }}
         onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>
        {mode === 'login' ? "Don't have an account? Register" : 'Already have an account? Sign in'}
      </p>
    </div>
  );
}
