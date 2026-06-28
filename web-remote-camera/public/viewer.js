const statusEl = document.getElementById('status');
const imgEl = document.getElementById('remoteFrame');
const placeholderEl = document.getElementById('placeholder');
const senderLinkEl = document.getElementById('senderLink');
const senderUrlTextEl = document.getElementById('senderUrlText');
const senderQrEl = document.getElementById('senderQr');
const copySenderUrlButton = document.getElementById('copySenderUrlButton');

const wsProtocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
const ws = new WebSocket(`${wsProtocol}//${location.host}/ws`);
let remoteCameraAppLink = `steply://remote-camera?host=${encodeURIComponent(location.hostname)}`;

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

async function loadConnectionQr() {
  let senderUrl = `${location.origin}/sender.html`;
  let host = location.hostname;

  try {
    const response = await fetch('/api/links', { cache: 'no-store' });
    const data = await response.json();
    const firstLink = data.links && data.links[0];

    if (firstLink && isLocalhost(location.hostname)) {
      senderUrl = firstLink.senderUrl;
      host = firstLink.ip;
    }
  } catch (error) {
    console.warn('Unable to load LAN URL list:', error);
  }

  remoteCameraAppLink = `steply://remote-camera?host=${encodeURIComponent(host)}`;
  senderLinkEl.href = senderUrl;
  senderUrlTextEl.textContent = host;
  senderQrEl.innerHTML = createQrSvg(remoteCameraAppLink);
}

function isLocalhost(hostname) {
  return hostname === 'localhost' || hostname === '127.0.0.1' || hostname === '::1';
}

copySenderUrlButton.addEventListener('click', async () => {
  try {
    await navigator.clipboard.writeText(remoteCameraAppLink);
    copySenderUrlButton.textContent = '복사됨';
    setTimeout(() => {
      copySenderUrlButton.textContent = '앱 연결 주소 복사';
    }, 1200);
  } catch (_) {
    window.prompt('앱 연결 주소를 복사하세요.', remoteCameraAppLink);
  }
});

loadConnectionQr();

function createQrSvg(text) {
  const modules = createQrModules(text);
  const size = modules.length;
  const quietZone = 4;
  const viewBoxSize = size + quietZone * 2;
  const cells = [];

  for (let y = 0; y < size; y += 1) {
    for (let x = 0; x < size; x += 1) {
      if (modules[y][x]) {
        cells.push(`M${x + quietZone},${y + quietZone}h1v1h-1z`);
      }
    }
  }

  return [
    `<svg viewBox="0 0 ${viewBoxSize} ${viewBoxSize}" role="img" aria-label="QR code" xmlns="http://www.w3.org/2000/svg">`,
    '<rect width="100%" height="100%" fill="#fff"/>',
    `<path fill="#111827" d="${cells.join('')}"/>`,
    '</svg>'
  ].join('');
}

function createQrModules(text) {
  const version = 4;
  const size = version * 4 + 17;
  const dataCodewords = 80;
  const errorCodewords = 20;
  const bytes = Array.from(new TextEncoder().encode(text));

  if (bytes.length > dataCodewords - 3) {
    throw new Error('QR payload is too long for version 4-L.');
  }

  const bits = [];
  appendBits(bits, 0b0100, 4);
  appendBits(bits, bytes.length, 8);
  bytes.forEach((byte) => appendBits(bits, byte, 8));

  const maxBits = dataCodewords * 8;
  appendBits(bits, 0, Math.min(4, maxBits - bits.length));
  while (bits.length % 8 !== 0) appendBits(bits, 0, 1);

  const data = [];
  for (let i = 0; i < bits.length; i += 8) {
    data.push(bitsToNumber(bits.slice(i, i + 8)));
  }

  const pads = [0xec, 0x11];
  for (let i = 0; data.length < dataCodewords; i += 1) {
    data.push(pads[i % 2]);
  }

  const codewords = data.concat(createErrorCorrection(data, errorCodewords));
  const matrix = Array.from({ length: size }, () => Array(size).fill(false));
  const reserved = Array.from({ length: size }, () => Array(size).fill(false));

  addFinder(matrix, reserved, 0, 0);
  addFinder(matrix, reserved, size - 7, 0);
  addFinder(matrix, reserved, 0, size - 7);
  addTiming(matrix, reserved);
  addAlignment(matrix, reserved, 26, 26);
  reserveFormatAreas(reserved, size);
  matrix[size - 8][8] = true;
  reserved[size - 8][8] = true;

  placeData(matrix, reserved, codewords, 0);
  addFormatInfo(matrix, reserved, 0);

  return matrix;
}

function appendBits(bits, value, length) {
  for (let i = length - 1; i >= 0; i -= 1) {
    bits.push((value >>> i) & 1);
  }
}

function bitsToNumber(bits) {
  return bits.reduce((value, bit) => (value << 1) | bit, 0);
}

function createErrorCorrection(data, degree) {
  const generator = rsGenerator(degree);
  const message = data.concat(Array(degree).fill(0));

  data.forEach((_, i) => {
    const coef = message[i];
    if (coef === 0) return;

    for (let j = 0; j < generator.length; j += 1) {
      message[i + j] ^= gfMul(generator[j], coef);
    }
  });

  return message.slice(data.length);
}

function rsGenerator(degree) {
  let poly = [1];
  for (let i = 0; i < degree; i += 1) {
    poly = polyMultiply(poly, [1, gfPow(2, i)]);
  }
  return poly;
}

function polyMultiply(a, b) {
  const result = Array(a.length + b.length - 1).fill(0);
  for (let i = 0; i < a.length; i += 1) {
    for (let j = 0; j < b.length; j += 1) {
      result[i + j] ^= gfMul(a[i], b[j]);
    }
  }
  return result;
}

const gfExp = Array(512).fill(0);
const gfLog = Array(256).fill(0);
let gfValue = 1;
for (let i = 0; i < 255; i += 1) {
  gfExp[i] = gfValue;
  gfLog[gfValue] = i;
  gfValue <<= 1;
  if (gfValue & 0x100) gfValue ^= 0x11d;
}
for (let i = 255; i < 512; i += 1) {
  gfExp[i] = gfExp[i - 255];
}

function gfMul(a, b) {
  if (a === 0 || b === 0) return 0;
  return gfExp[gfLog[a] + gfLog[b]];
}

function gfPow(a, power) {
  if (power === 0) return 1;
  return gfExp[(gfLog[a] * power) % 255];
}

function setModule(matrix, reserved, x, y, dark) {
  if (x < 0 || y < 0 || y >= matrix.length || x >= matrix.length) return;
  matrix[y][x] = dark;
  reserved[y][x] = true;
}

function addFinder(matrix, reserved, x, y) {
  for (let dy = -1; dy <= 7; dy += 1) {
    for (let dx = -1; dx <= 7; dx += 1) {
      const xx = x + dx;
      const yy = y + dy;
      if (xx < 0 || yy < 0 || yy >= matrix.length || xx >= matrix.length) continue;
      const inPattern = dx >= 0 && dx <= 6 && dy >= 0 && dy <= 6;
      const dark = inPattern && (dx === 0 || dx === 6 || dy === 0 || dy === 6 || (dx >= 2 && dx <= 4 && dy >= 2 && dy <= 4));
      setModule(matrix, reserved, xx, yy, dark);
    }
  }
}

function addTiming(matrix, reserved) {
  for (let i = 8; i < matrix.length - 8; i += 1) {
    setModule(matrix, reserved, i, 6, i % 2 === 0);
    setModule(matrix, reserved, 6, i, i % 2 === 0);
  }
}

function addAlignment(matrix, reserved, centerX, centerY) {
  for (let dy = -2; dy <= 2; dy += 1) {
    for (let dx = -2; dx <= 2; dx += 1) {
      const distance = Math.max(Math.abs(dx), Math.abs(dy));
      setModule(matrix, reserved, centerX + dx, centerY + dy, distance !== 1);
    }
  }
}

function reserveFormatAreas(reserved, size) {
  for (let i = 0; i < 9; i += 1) {
    reserved[8][i] = true;
    reserved[i][8] = true;
  }
  for (let i = 0; i < 8; i += 1) {
    reserved[8][size - 1 - i] = true;
    reserved[size - 1 - i][8] = true;
  }
}

function placeData(matrix, reserved, codewords, mask) {
  const bits = [];
  codewords.forEach((codeword) => appendBits(bits, codeword, 8));

  let bitIndex = 0;
  let upward = true;

  for (let right = matrix.length - 1; right >= 1; right -= 2) {
    if (right === 6) right -= 1;

    for (let rowOffset = 0; rowOffset < matrix.length; rowOffset += 1) {
      const y = upward ? matrix.length - 1 - rowOffset : rowOffset;

      for (let colOffset = 0; colOffset < 2; colOffset += 1) {
        const x = right - colOffset;
        if (reserved[y][x]) continue;

        const maskBit = getMask(mask, x, y);
        matrix[y][x] = Boolean((bits[bitIndex] || 0) ^ maskBit);
        bitIndex += 1;
      }
    }

    upward = !upward;
  }
}

function getMask(mask, x, y) {
  if (mask === 0) return (x + y) % 2 === 0 ? 1 : 0;
  return 0;
}

function addFormatInfo(matrix, reserved, mask) {
  const size = matrix.length;
  const errorLevelL = 0b01;
  const data = (errorLevelL << 3) | mask;
  let bits = data << 10;

  for (let i = 14; i >= 10; i -= 1) {
    if ((bits >>> i) & 1) bits ^= 0x537 << (i - 10);
  }

  const format = ((data << 10) | bits) ^ 0x5412;
  const bit = (i) => Boolean((format >>> i) & 1);

  const first = [
    [8, 0], [8, 1], [8, 2], [8, 3], [8, 4], [8, 5], [8, 7], [8, 8],
    [7, 8], [5, 8], [4, 8], [3, 8], [2, 8], [1, 8], [0, 8]
  ];
  const second = [
    [size - 1, 8], [size - 2, 8], [size - 3, 8], [size - 4, 8], [size - 5, 8], [size - 6, 8], [size - 7, 8],
    [8, size - 8], [8, size - 7], [8, size - 6], [8, size - 5], [8, size - 4], [8, size - 3], [8, size - 2], [8, size - 1]
  ];

  for (let i = 0; i < 15; i += 1) {
    const [x1, y1] = first[i];
    const [x2, y2] = second[i];
    matrix[y1][x1] = bit(i);
    matrix[y2][x2] = bit(i);
    reserved[y1][x1] = true;
    reserved[y2][x2] = true;
  }
}
