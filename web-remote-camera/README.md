# Steply Remote Camera Demo

모바일을 카메라 리모컨처럼 쓰고, PC 웹 화면에서 영상을 보는 데모입니다.

## 구조

- PC: `viewer.html` 화면을 띄움
- 모바일: `sender.html` 화면에서 `컴퓨터로 영상 보내기` 버튼을 누름
- 서버: WebSocket으로 모바일 JPEG 프레임을 PC 화면에 전달

## 실행

```bash
cd web-remote-camera
npm install
npm start
```

서버가 실행되면 터미널에 이런 주소가 나옵니다.

```txt
PC viewer : http://192.168.x.x:3000/viewer.html
Mobile cam: http://192.168.x.x:3000/sender.html
```

1. PC에서 `PC viewer` 주소를 엽니다.
2. 휴대폰에서 `Mobile cam` 주소를 엽니다.
3. 휴대폰에서 `컴퓨터로 영상 보내기` 버튼을 누릅니다.
4. PC 화면에 휴대폰 카메라 영상이 표시됩니다.

## 중요: 모바일 카메라 권한

모바일 브라우저의 `getUserMedia()`는 보통 HTTPS에서만 동작합니다. HTTP 주소로 접속하면 카메라 권한이 막힐 수 있습니다.

빠른 발표/데모라면 아래 중 하나를 쓰면 됩니다.

### 방법 A: ngrok 사용

```bash
npm start
ngrok http 3000
```

ngrok가 만들어준 `https://...` 주소로 PC와 모바일 모두 접속합니다.

- PC: `https://.../viewer.html`
- 모바일: `https://.../sender.html`

### 방법 B: 로컬 HTTPS 인증서 사용

`certs/key.pem`, `certs/cert.pem`을 만든 뒤 아래처럼 실행합니다.

```bash
HTTPS=1 npm start
```

Windows PowerShell에서는 아래처럼 실행합니다.

```powershell
$env:HTTPS="1"; npm start
```

## MVP 용도

이 데모는 실제 AI 분석용 스트리밍이 아니라 발표/프로토타입용입니다. 프레임을 JPEG로 압축해서 10fps 정도로 보내므로 카운트/안내 UI 시연에는 충분하지만, 정밀한 자세 분석에는 WebRTC 또는 네이티브 카메라 파이프라인을 권장합니다.

## Android 앱에서 직접 송출하기

이번 버전은 모바일 브라우저 `sender.html`뿐 아니라 Steply Android 앱의 검사 화면에서도 같은 WebSocket 서버로 카메라 프레임을 보낼 수 있습니다.

### PC 준비

```bash
cd web-remote-camera
npm install
npm start
```

터미널에 출력되는 주소 중 PC 뷰어 주소를 브라우저에서 엽니다.

```txt
PC viewer : http://192.168.x.x:3000/viewer.html
```

### 휴대폰 앱 준비

1. 휴대폰과 PC를 같은 Wi-Fi에 연결합니다.
2. Steply 앱에서 30초 Chair Stand 검사 화면까지 들어갑니다.
3. 카메라 카드 아래의 `PC로 카메라 송출` 영역에 PC IPv4 주소를 입력합니다.
   - 예: `192.168.0.12`
   - 직접 주소를 쓰려면 `ws://192.168.0.12:3000/ws` 형식도 가능합니다.
4. `PC 연결` 버튼을 누릅니다.
5. PC 브라우저의 `viewer.html` 화면에 휴대폰 앱 카메라 영상이 표시됩니다.

### 네트워크 주의사항

- Android 앱은 데모 편의를 위해 `ws://` cleartext 통신을 허용합니다.
- 학교/공공 Wi-Fi는 기기 간 통신을 막는 경우가 있습니다. 이때는 휴대폰 핫스팟에 PC를 연결해서 테스트하세요.
- Windows 방화벽에서 Node.js 또는 포트 `3000`을 허용해야 할 수 있습니다.

### 구현 메모

- Android 앱은 `RemoteCameraStreamer`에서 WebSocket 연결을 열고, 카메라 프레임을 JPEG binary frame으로 전송합니다.
- PC Node 서버는 Android binary JPEG frame과 기존 브라우저 sender의 base64 JSON frame을 모두 지원합니다.
- 전송은 약 10fps, 최대 폭 640px, JPEG 품질 62로 제한했습니다.
