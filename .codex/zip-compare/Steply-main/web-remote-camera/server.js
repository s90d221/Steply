const http = require('http');
const https = require('https');
const fs = require('fs');
const os = require('os');
const path = require('path');
const { WebSocketServer } = require('ws');

const PORT = Number(process.env.PORT || 3000);
const PUBLIC_DIR = path.join(__dirname, 'public');

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.svg': 'image/svg+xml'
};

function getLocalIps() {
  const nets = os.networkInterfaces();
  const ips = [];
  for (const net of Object.values(nets)) {
    for (const item of net || []) {
      if (item.family === 'IPv4' && !item.internal) ips.push(item.address);
    }
  }
  return ips;
}

function sendFile(res, filePath) {
  const ext = path.extname(filePath).toLowerCase();
  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('Not found');
      return;
    }
    res.writeHead(200, {
      'Content-Type': MIME[ext] || 'application/octet-stream',
      'Cache-Control': 'no-store'
    });
    res.end(data);
  });
}

function requestHandler(req, res) {
  const url = new URL(req.url, `http://${req.headers.host}`);
  let pathname = decodeURIComponent(url.pathname);
  if (pathname === '/') pathname = '/viewer.html';

  const filePath = path.normalize(path.join(PUBLIC_DIR, pathname));
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end('Forbidden');
    return;
  }

  sendFile(res, filePath);
}

const useHttps = process.env.HTTPS === '1';
let server;

if (useHttps) {
  const keyPath = process.env.SSL_KEY || path.join(__dirname, 'certs', 'key.pem');
  const certPath = process.env.SSL_CERT || path.join(__dirname, 'certs', 'cert.pem');

  server = https.createServer(
    {
      key: fs.readFileSync(keyPath),
      cert: fs.readFileSync(certPath)
    },
    requestHandler
  );
} else {
  server = http.createServer(requestHandler);
}

const wss = new WebSocketServer({ server, path: '/ws' });

const viewers = new Set();
let sender = null;
let lastFrame = null;

function broadcastJson(data) {
  const payload = JSON.stringify(data);

  for (const viewer of viewers) {
    if (viewer.readyState === viewer.OPEN) {
      viewer.send(payload);
    }
  }
}

wss.on('connection', (socket, req) => {
  console.log('[WS] connected:', req.url);

  let role = 'unknown';

  socket.on('message', (raw, isBinary) => {
    console.log('[WS] message:', {
      isBinary,
      length: raw.length,
      role
    });

    // Android app may send binary JPEG frames directly.
    // If binary arrives, treat this socket as sender automatically.
    if (isBinary) {
      if (role !== 'sender') {
        role = 'sender';
        sender = socket;
        console.log('[WS] sender auto registered');
        broadcastJson({ type: 'status', senderConnected: true });
      }

      console.log('[WS] jpeg frame:', raw.length);

      const frame = `data:image/jpeg;base64,${Buffer.from(raw).toString('base64')}`;
      lastFrame = frame;
      broadcastJson({ type: 'frame', frame });
      return;
    }

    let msg;
    try {
      msg = JSON.parse(raw.toString());
    } catch (_) {
      console.log('[WS] ignored non-json text message');
      return;
    }

    if (msg.type === 'hello') {
      role = msg.role;
      console.log('[WS] hello:', msg);

      if (role === 'viewer') {
        viewers.add(socket);
        socket.send(JSON.stringify({ type: 'status', senderConnected: Boolean(sender) }));
        if (lastFrame) {
          socket.send(JSON.stringify({ type: 'frame', frame: lastFrame }));
        }
      }

      if (role === 'sender') {
        sender = socket;
        broadcastJson({ type: 'status', senderConnected: true });
      }

      return;
    }

    if (role === 'sender' && msg.type === 'frame' && typeof msg.frame === 'string') {
      console.log('[WS] text frame received');

      lastFrame = msg.frame;
      broadcastJson({ type: 'frame', frame: msg.frame });
      return;
    }

    if (role === 'sender' && msg.type === 'stopped') {
      console.log('[WS] sender stopped');
      broadcastJson({ type: 'status', senderConnected: false });
      return;
    }
  });

  socket.on('close', () => {
    console.log('[WS] closed:', role);

    if (role === 'viewer') {
      viewers.delete(socket);
    }

    if (socket === sender) {
      sender = null;
      broadcastJson({ type: 'status', senderConnected: false });
    }
  });

  socket.on('error', (err) => {
    console.log('[WS] error:', err.message);
  });
});

server.listen(PORT, '0.0.0.0', () => {
  const protocol = useHttps ? 'https' : 'http';

  console.log(`\nSteply Remote Camera Demo running on ${protocol}://localhost:${PORT}`);

  for (const ip of getLocalIps()) {
    console.log(`PC viewer : ${protocol}://${ip}:${PORT}/viewer.html`);
    console.log(`Mobile cam: ${protocol}://${ip}:${PORT}/sender.html`);
  }

  if (!useHttps) {
    console.log('\n주의: 모바일 브라우저 카메라는 보통 HTTPS에서만 허용됩니다.');
    console.log('브라우저 sender.html 카메라는 HTTPS가 필요할 수 있습니다. Android 앱 송출은 같은 Wi-Fi의 ws:// 주소로 바로 테스트할 수 있습니다.\n');
  }
});