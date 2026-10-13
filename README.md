# Wine Arcade X — Flip Edition

LG Wine Smart LG-F480K 전용 미니게임 플랫폼 1차 구현.

## 목표
일반 스마트폰용 게임을 단순 이식하는 대신 F480K의 폴더 개폐, 물리 숫자키, 방향키, 통화키, 진동, 소리를 입력장치로 사용합니다.

## 포함 기능

### Device Lab
- Key Test: 실제 Android KeyCode 표시
- Flip Test: ACTION_SCREEN_ON/OFF 및 lifecycle 로그
- Vibration Test
- Sound Test
- Screen Event Test
- Device Info

### Flip Games
1. Flip Fishing
2. Flip Reaction
3. Flip Memory
4. Vault Phone

### Keypad Games
1. Secret Dial
2. Retro Operator

### Signal Games
1. Pocket Signal

### Classic
- 1차 구조에 Snake 구현
- 기존 V3의 나머지 Classic 게임은 후속 통합용 메뉴 슬롯을 유지

### Progression
- Wine Coin
- XP / Level
- 일일 랜덤 미션 3개
- 낚은 물고기 / 희귀도 저장
- 최고 Flip Reaction 기록 저장

## 중요: F480K 폴더 감지 검증

현재 FlipDetector는 표준 Android 브로드캐스트를 사용합니다.

- ACTION_SCREEN_OFF → Flip Close 후보
- ACTION_SCREEN_ON → Flip Open 후보
- ACTION_USER_PRESENT → 로그

LG F480K에서 실제 폴더 개폐가 위 이벤트를 발생시키는지는 반드시 Device Lab에서 먼저 확인해야 합니다.

### 테스트 순서
1. 앱 실행
2. `6 DEVICE LAB`
3. `2 FLIP TEST`
4. 폴더를 닫았다가 열기
5. 로그에서 `ACTION_SCREEN_OFF`, `ACTION_SCREEN_ON` 확인
6. 확인되면 Flip Reaction → Flip Fishing 순서로 테스트

폴더 이벤트가 다른 방식으로 들어오는 기기라면 Device Lab 로그를 기준으로 FlipDetector에 LG 전용 fallback을 추가하십시오.

## GitHub Actions 빌드

저장소 루트에 프로젝트 내용을 업로드합니다.

Actions → Build APK → Artifacts → `WineArcadeX-debug-apk`

결과:
`app-debug.apk`

## 설치
```bat
adb install -r app-debug.apk
```

## 환경
- Java
- minSdk 19
- targetSdk 28
- compileSdk 35
- AndroidX 없음
- Compose 없음
- 외부 게임엔진 없음
- 네트워크 권한 없음
- 광고/Analytics 없음

## 다음 검증 포인트
Device Lab 실행 후 아래 로그를 공유하면 F480K에 맞게 FlipDetector를 정밀 보정할 수 있습니다.

- 폴더 닫기 직전/직후 로그
- 폴더 열기 직전/직후 로그
- 통화키 KEYCODE
- 종료키 KEYCODE
- 방향키/중앙키 KEYCODE


## Flip lifecycle 보정
- FlipDetector는 Activity onStop에서 해제하지 않습니다.
- 폴더를 닫아 화면이 꺼진 뒤에도 ACTION_SCREEN_ON을 받을 수 있게 onDestroy에서만 해제합니다.
- PARTIAL_WAKE_LOCK은 화면 OFF 중 Flip 게임의 랜덤 대기/진동이 멈추지 않도록 사용합니다.
- Home 등 화면이 켜진 일반 백그라운드 이동에서는 wake lock을 해제해 불필요한 배터리 소모를 줄입니다.


# F480K Launcher 복귀 대응 수정

F480K 실기에서 폴더 CLOSE 후 Activity가 뒤로 가고, OPEN 시 Launcher가 앞에 오는 현상을 보정합니다.

- MainActivity: launchMode=singleTask
- FlipKeepAliveService 추가
- Flip 게임/Device Lab Flip Test가 활성화된 경우에만 restore ARM
- SCREEN_ON 수신 시 기존 MainActivity를 REORDER_TO_FRONT
- 기존 GameEngine 인스턴스와 Flip 게임 상태를 유지
- 일반 Home 이동은 자동 복귀하지 않음

테스트 순서:
1. 1 FLIP GAMES → 2 FLIP REACTION
2. 폴더 닫기
3. 진동 기다리기
4. 폴더 열기
5. Wine Arcade X가 자동 복귀하고 반응시간이 표시되는지 확인
6. 성공하면 Flip Fishing도 동일하게 테스트


## 복귀 ARM 타이밍 보강
F480K에서 onPause와 실제 SCREEN_OFF 순서가 일정하지 않을 가능성을 고려해,
Flip 게임을 선택하는 순간부터 FlipKeepAliveService를 ARM합니다.
메뉴로 복귀하거나 화면이 켜진 상태에서 Home/다른 앱으로 이동하면 DISARM합니다.
따라서 onPause 타이밍 레이스에 덜 민감합니다.
