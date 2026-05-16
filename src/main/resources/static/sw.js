// Empty service worker — satisfies Swagger UI's registration request
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', () => self.clients.claim());