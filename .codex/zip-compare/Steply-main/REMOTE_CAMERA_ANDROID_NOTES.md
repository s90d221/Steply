# Android 앱 WebSocket 원격 카메라 구현 메모

## 이번 구현 내용

같은 네트워크 안에서 Steply Android 앱의 카메라 프레임을 PC 브라우저 화면으로 보내는 데모 기능을 추가했습니다.

```txt
Android Steply 앱 CameraX
→ MediaPipePoseFrameDetector에서 Bitmap 획득
→ JPEG 640px / 품질 62 / 약 10fps로 압축
→ OkHttp WebSocket binary frame 전송
→ web-remote-camera Node 서버
→ viewer.html에 표시
```

## 수정된 주요 파일

- `app/build.gradle.kts`
  - `com.squareup.okhttp3:okhttp:4.12.0` 추가
- `app/src/main/AndroidManifest.xml`
  - `INTERNET` 권한 추가
  - 데모용 `android:usesCleartextTraffic="true"` 추가
- `app/src/main/java/com/steply/app/remote/RemoteCameraStreamer.kt`
  - Android 앱에서 PC WebSocket 서버로 JPEG binary frame을 보내는 클래스 추가
- `app/src/main/java/com/steply/app/analysis/MediaPipePoseFrameDetector.kt`
  - 카메라 분석 중 생성되는 `rotatedBitmap`을 외부 콜백으로 전달하도록 확장
- `app/src/main/java/com/steply/app/ui/screens/chaircheck/PoseCameraPreview.kt`
  - Bitmap을 JPEG로 압축해 약 10fps로 `RemoteCameraStreamer`에 전달
- `app/src/main/java/com/steply/app/ui/screens/chaircheck/ChairCheckScreen.kt`
  - Chair Stand 검사 화면 카메라 카드 아래에 PC IP 입력/연결/송출 중지 UI 추가
- `web-remote-camera/server.js`
  - 기존 브라우저 sender의 JSON base64 frame과 Android 앱의 binary JPEG frame을 모두 받도록 수정
- `web-remote-camera/README.md`
  - Android 앱 송출 사용법 추가

## 실행 방법

### 1. PC에서 서버 실행

```bash
cd web-remote-camera
npm install
npm start
```

PC 브라우저에서 터미널에 나온 viewer 주소를 엽니다.

```txt
http://192.168.x.x:3000/viewer.html
```

### 2. Android 앱에서 송출

1. PC와 휴대폰을 같은 Wi-Fi에 연결합니다.
2. Steply 앱에서 Chair Stand 검사 화면까지 들어갑니다.
3. `PC로 카메라 송출` 영역에 PC IPv4 주소를 입력합니다.
   - 예: `192.168.0.12`
   - 또는 직접 `ws://192.168.0.12:3000/ws` 입력 가능
4. `PC 연결`을 누릅니다.
5. PC의 `viewer.html`에서 휴대폰 앱 카메라 영상이 보이면 성공입니다.

## 안 될 때 확인

- Windows 방화벽에서 Node.js 또는 포트 3000 허용
- 휴대폰과 PC가 같은 Wi-Fi인지 확인
- 공공/학교 Wi-Fi가 기기 간 통신을 막으면 휴대폰 핫스팟 사용
- PC IP는 `ipconfig`의 Wi-Fi IPv4 주소 사용
