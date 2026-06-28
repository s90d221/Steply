const statusEl = document.getElementById('status');
const videoEl = document.getElementById('localVideo');
const canvas = document.getElementById('canvas');
const startButton = document.getElementById('startButton');
const stopButton = document.getElementById('stopButton');
const placeholderEl = document.getElementById('placeholder');
const ctx = canvas.getContext('2d');

let ws;
let stream;
let timerId;
let sending = false;

function setStatus(text, live = false) {
  statusEl.textContent = text;
  statusEl.className = live ? 'badge live' : 'badge';
}

function openSocket() {
  return new Promise((resolve, reject) => {
    const wsProtocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
    ws = new WebSocket(`${wsProtocol}//${location.host}/ws`);
    ws.addEventListener('open', () => {
      ws.send(JSON.stringify({ type: 'hello', role: 'sender' }));
      resolve();
    });
    ws.addEventListener('error', reject);
    ws.addEventListener('close', () => {
      if (sending) stopSending('서버 연결 끊김');
    });
  });
}

async function startSending() {
  startButton.disabled = true;
  setStatus('연결 중');

  try {
    await openSocket();

    stream = await navigator.mediaDevices.getUserMedia({
      video: {
        facingMode: 'environment',
        width: { ideal: 1280 },
        height: { ideal: 720 }
      },
      audio: false
    });

    videoEl.srcObject = stream;
    placeholderEl.style.display = 'none';
    stopButton.disabled = false;
    sending = true;
    setStatus('송출 중', true);

    timerId = setInterval(sendFrame, 100); // 10fps 정도. 데모용으로 충분함.
  } catch (error) {
    console.error(error);
    setStatus('실패');
    startButton.disabled = false;
    alert('카메라 또는 서버 연결에 실패했습니다. 모바일 브라우저는 HTTPS가 필요할 수 있습니다.');
  }
}

function sendFrame() {
  if (!sending || !ws || ws.readyState !== WebSocket.OPEN) return;
  if (!videoEl.videoWidth || !videoEl.videoHeight) return;

  const targetWidth = 640;
  const targetHeight = Math.round(targetWidth * videoEl.videoHeight / videoEl.videoWidth);
  canvas.width = targetWidth;
  canvas.height = targetHeight;
  ctx.drawImage(videoEl, 0, 0, targetWidth, targetHeight);

  const frame = canvas.toDataURL('image/jpeg', 0.62);
  ws.send(JSON.stringify({ type: 'frame', frame }));
}

function stopSending(reason = '중지됨') {
  sending = false;
  clearInterval(timerId);

  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ type: 'stopped' }));
    ws.close();
  }

  if (stream) {
    stream.getTracks().forEach(track => track.stop());
    stream = null;
  }

  videoEl.srcObject = null;
  placeholderEl.style.display = 'grid';
  startButton.disabled = false;
  stopButton.disabled = true;
  setStatus(reason);
}

startButton.addEventListener('click', startSending);
stopButton.addEventListener('click', () => stopSending());
