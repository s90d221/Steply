const statusEl = document.getElementById('status');
const imgEl = document.getElementById('remoteFrame');
const placeholderEl = document.getElementById('placeholder');

const wsProtocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
const ws = new WebSocket(`${wsProtocol}//${location.host}/ws`);

function setWaiting(text = '대기 중') {
  statusEl.textContent = text;
  statusEl.className = 'badge';
  placeholderEl.style.display = 'grid';
}

function setLive() {
  statusEl.textContent = '수신 중';
  statusEl.className = 'badge live';
  placeholderEl.style.display = 'none';
}

ws.addEventListener('open', () => {
  ws.send(JSON.stringify({ type: 'hello', role: 'viewer' }));
});

ws.addEventListener('message', (event) => {
  const msg = JSON.parse(event.data);

  if (msg.type === 'status') {
    if (msg.senderConnected) setLive();
    else setWaiting('연결 끊김');
    return;
  }

  if (msg.type === 'frame') {
    imgEl.src = msg.frame;
    setLive();
  }
});

ws.addEventListener('close', () => setWaiting('서버 끊김'));
ws.addEventListener('error', () => setWaiting('오류'));
