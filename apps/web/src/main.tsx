import { useState, type FormEvent } from 'react';
import { createRoot } from 'react-dom/client';
import { loadDrafts, saveDrafts } from './storage';
import './style.css';

function App() {
  const [drafts, setDrafts] = useState(loadDrafts);
  const [text, setText] = useState('');
  const [error, setError] = useState('');
  function save(event: FormEvent) {
    event.preventDefault();
    if (!text.trim()) return;
    const next = [...drafts, {id: crypto.randomUUID(), text: text.trim(), createdAt: Date.now()}];
    try { saveDrafts(next); setDrafts(next); setText(''); setError(''); }
    catch { setError('Unable to save locally. Your draft was retained.'); }
  }
  function clear() {
    if (!window.confirm('Clear local drafts?')) return;
    try { saveDrafts([]); setDrafts([]); setError(''); } catch { setError('Unable to clear local drafts.'); }
  }
  return <main>
    <header><div className="brand">schoolday</div><span className="status">Local only</span></header>
    <section className="heading"><h1>Chats</h1><button onClick={clear} disabled={!drafts.length}>Clear</button></section>
    <section className="conversation" aria-label="Local draft conversation">
      {drafts.length === 0 ? <div className="empty"><h2>No conversations yet</h2><p>Save a local test message below.</p></div> :
      drafts.map(d => <article key={d.id}><div>{d.text}</div><small>Saved locally · Not sent</small></article>)}
    </section>
    {error && <p role="alert">{error}</p>}
    <form onSubmit={save}><input aria-label="Test message" placeholder="Message" maxLength={500} value={text} onChange={e=>setText(e.target.value)}/><button type="submit" disabled={!text.trim()}>Save</button></form>
  </main>;
}
createRoot(document.getElementById('root')!).render(<App/>);
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => { navigator.serviceWorker.register(import.meta.env.BASE_URL+'sw.js').catch(console.warn); });
}
