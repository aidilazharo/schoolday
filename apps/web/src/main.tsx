import { useState, type FormEvent } from 'react';
import { createRoot } from 'react-dom/client';
import { loadDrafts, saveDrafts } from './storage';
import './style.css';

function App() {
  const [drafts, setDrafts] = useState(loadDrafts);
  const [text, setText] = useState('');
  const [error, setError] = useState('');
  const [opened, setOpened] = useState(false);
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
  return <main className={opened ? 'app chat-open' : 'app'}>
    <aside className="sidebar" aria-label="Chats">
      <header><div className="brand">schoolday</div><span className="status">Local only</span></header>
      <h1>Chats</h1>
      <button className="chat-item" onClick={()=>setOpened(true)}>
        <span className="avatar" aria-hidden="true">S</span>
        <span className="chat-label"><strong>Local outbox</strong><small>{drafts.at(-1)?.text ?? 'No saved messages'}</small></span>
        <span className="chevron" aria-hidden="true">›</span>
      </button>
    </aside>
    <section className="chat-panel" aria-label="Local outbox">
    <header><div><h2>Local outbox</h2><small>Local only</small></div><div className="actions"><button className="quiet" onClick={clear} disabled={!drafts.length}>Clear</button><button className="quiet back" onClick={()=>setOpened(false)}>Back</button></div></header>
    <section className="conversation" aria-label="Local draft conversation">
      {drafts.length === 0 ? <div className="empty"><h2>No saved messages</h2><p>Save a local test message below.</p></div> :
      drafts.map(d => <article key={d.id}><div>{d.text}</div><small>Saved locally · Not sent</small></article>)}
    </section>
    {error && <p role="alert">{error}</p>}
    <form onSubmit={save}><input aria-label="Test message" placeholder="Message" maxLength={500} value={text} onChange={e=>setText(e.target.value)}/><button type="submit" disabled={!text.trim()}>Save</button></form>
    </section>
  </main>;
}
createRoot(document.getElementById('root')!).render(<App/>);
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => { navigator.serviceWorker.register(import.meta.env.BASE_URL+'sw.js').catch(console.warn); });
}
