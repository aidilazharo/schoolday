// Cache only this application's static shell. Never cache network messages or third-party requests.
const CACHE = 'schoolday-shell-v26';
self.addEventListener('install', event => {
  event.waitUntil((async () => {
    const cache = await caches.open(CACHE);
    const shell = new URL('./', self.location.href);
    const response = await fetch(shell, {cache:'reload'});
    if (!response.ok) throw new Error('Shell unavailable');
    const html = await response.clone().text();
    const assets = [...html.matchAll(/(?:src|href)="([^"]+\/assets\/[^"]+)"/g)].map(m => new URL(m[1], shell).href);
    await cache.put(shell, response);
    await cache.addAll([...new Set(assets)]);
    await self.skipWaiting();
  })());
});
self.addEventListener('activate', event => event.waitUntil(self.clients.claim()));
self.addEventListener('fetch', event => {
  const url = new URL(event.request.url), scope = new URL(self.registration.scope);
  if (event.request.method !== 'GET' || url.origin !== scope.origin || !url.pathname.startsWith(scope.pathname)) return;
  if (event.request.mode === 'navigate') {
    event.respondWith(fetch(event.request).catch(async () => (await caches.open(CACHE)).match(scope.href).then(r => r || Response.error())));
  } else if (url.pathname.startsWith(scope.pathname+'assets/')) {
    event.respondWith((async () => {
      const cache = await caches.open(CACHE);
      const cached = await cache.match(event.request);
      if (cached) return cached;
      const res = await fetch(event.request);
      if (res.ok) await cache.put(event.request, res.clone());
      return res;
    })());
  }
});
