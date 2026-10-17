# Arcade Common — 실제 독립 Android 라이브러리 (v1.0.0)

이 디렉터리는 **Tower Builder에 종속되지 않는 Gradle Android Library**다. `:app`이 없어도 다른 Android 프로젝트에 `:arcade-common` 모듈을 복사해 빌드할 수 있다. Android 4.4/API 19 이상, Java 8 소스, 오프라인 작동을 기준으로 한다.

> 구현 상태: **추출 완료** — 입력/터치/프레임/캔버스 뷰포트/오디오·진동/적응형 효과 품질/반복 보상/1회 예약 입력.  
> **미추출** — 게임 세션·저장 포맷, 모달 전체 UI, 인벤토리, XP, FEVER 판정/타이머, 타워 레이아웃 및 실제 게임 룰. 타워 코드를 공통 모듈이라고 부르지 않는다.

## 1. 사용 방법

이 `arcade-common/` 디렉터리를 다른 Android Gradle 프로젝트의 루트에 복사한다.

`settings.gradle`:

```groovy
include ':app', ':arcade-common'
```

루트 `build.gradle` 의 `plugins`에 다음 한 줄을 선언한다 (버전은 사용하는 Android Gradle Plugin과 맞춰야 함):

```groovy
id 'com.android.library' version '8.3.2' apply false
```

새 게임의 `app/build.gradle`:

```groovy
dependencies { implementation project(':arcade-common') }
```

`gradle :arcade-common:assembleRelease` → `arcade-common/build/outputs/aar/arcade-common-release.aar` 생성. 통합 앱은 `gradle :app:assembleDebug`. 프로젝트 전체 테스트는 `bash tests/test-engine.sh`.

**주의:** 이 모듈은 게임 리소스(`R.raw`)와 앱의 `applicationId`를 요구하지 않는다. 새로운 게임의 소리 목록과 설정은 호출 앱이 제공한다.

## 2. 공개 API / 책임

| 패키지 · 클래스 | 공통 역할 | 새 게임 설정 포인트 |
|---|---|---|
| `input.DeviceKeyPolicy` | 통화/볼륨/미디어 시스템 키를 앱이 가로채지 않도록 정책 | 게임별 키 매핑은 앱에서 |
| `input.PhysicalKeyGate` | 물리키 길게 누르기 중복 이벤트 차단/KeyUp 재입력 | `onWindowFocusChanged`와 `onPause`에서 `reset()` |
| `input.TouchInputGate` | 다중 터치, 화면 밖 터치, 드래그 오발동 차단 | `new TouchInputGate(가로,세로,이동허용px)` |
| `input.OneShotActionBuffer` | 쿨다운 중 한 번의 입력만 예약 | 게임별 예약 시간, 허용 상태 및 모달 조건 |
| `render.FramePacer` | 정적 메뉴/팝업/게임 갱신 간격, 큰 `dt` 상한 | FPS 정책은 새 게임에서 변경 가능 |
| `render.FrameDiagnostics` | 활성 플레이 FPS와 프레임 스파이크 | `record(now, 실제실시간상태)` |
| `render.AspectViewport` | 종횡비 유지 레터박스 화면 좌표·터치 좌표 변환 | 논리 해상도 (`320×480` 외 가능) |
| `render.AdaptiveFxQuality` | 낮은 FPS가 지속될 때 *연출 품질만* 낮춤 | 최저·최고 단계/프레임 샘플/임계 FPS |
| `audio.ArcadeAudioEngine` | SoundPool·ToneGenerator·Vibrator API19, 포커스시 정지 | 리소스 배열, `Preferences`, 효과음 인덱스 |
| `rewards.RecurringRewardCycle` | 콤보 N부터 반복 지급하는 기본 일정 | 시작 콤보·보상 타입 배열 (정수 의미는 게임 소유) |

### 입력과 레이아웃

```java
PhysicalKeyGate keys = new PhysicalKeyGate();
TouchInputGate touch = new TouchInputGate(360, 640, 20);
AspectViewport viewport = new AspectViewport(360, 640);
viewport.fit(viewWidth, viewHeight);
float gameX = viewport.x(touchX), gameY = viewport.y(touchY);
if (touch.press(gameX, gameY, 1)) { /* MOVE, UP 이벤트에서 검증 후 게임 액션 */ }
```

> 타워 프로젝트의 `TowerTouchMap`와 `TowerUiLayout`는 계속 게임 전용이다. 새 게임은 자신의 버튼 좌표, 화면 전환과 hit-box를 정의해야 한다.

### 보상 정책과 피버

```java
// 예: 2 ITEM, 3 MYSTERY, 4 FEVER, 5 ITEM... (정수 타입은 게임에서 정의)
RecurringRewardCycle cycle = new RecurringRewardCycle(2, 1, 2, 3);
int kind = cycle.reward(naturalCombo, 0);
```

`RecurringRewardCycle`은 **언제 어떤 종류의 이벤트를 발생시킬지**만 결정한다. 아이템 효과, 피버 12초/자석/3배 점수, 자연 콤보와 보조 콤보의 구분은 타워 게임의 `TowerItemSystem`·`TowerFever`·`TowerEngine`에서 관리한다. 새로운 게임은 그 규칙을 새로 구현한다.

### 오디오/진동

```java
int[] effects = { R.raw.hit, R.raw.reward };
ArcadeAudioEngine engine = new ArcadeAudioEngine(context, effects,
    new ArcadeAudioEngine.Preferences() {
        public boolean soundEnabled(){ return settings.sound; }
        public boolean vibrationEnabled(){ return settings.vibration; }
    });
engine.play(0, 1.0f);
engine.vibrate(20);
// 앱의 onPause/onResume/onDestroy 각각 enterBackground/enterForeground/close 호출
```

`ArcadeAudioEngine`는 Android `Context`가 필요하므로 독립적인 순수 JVM 엔진 테스트 대상은 아니다. UI 없는 입력·렌더 시간·보상 정책은 JVM에서 직접 검사한다.

## 3. 새 게임 개발 시 파일 경계

```text
새 게임 프로젝트/
├── settings.gradle
├── build.gradle
├── arcade-common/                  # 이 폴더를 그대로 복사
│   ├── build.gradle                 # com.android.library
│   └── src/main/java/com/winearcadex/arcadecommon/
│       ├── input/                  # 키/터치/1회 예약
│       ├── render/                 # 뷰포트/프레임/FX 품질
│       ├── audio/                  # 효과음/진동 기반
│       └── rewards/                # 반복 보상 정책
└── app/                            # 새 게임 전용 모듈
    └── src/main/java/.../game/     # 게임 규칙/아이템/피버/저장
```

게임 앱은 `arcade-common`을 의존하지만, **반대 의존은 금지**한다. 현재 `arcade-common/src/main/java` 내에는 `com.winearcadex.tower` 참조가 없다. 사용자는 각 게임에 별도 패키지·저장 namespace·리소스를 부여한다.

## 4. 설계 상 남아 있는 공통화 후보

- **저장·스냅샷:** 타워 체크포인트는 v1/v2/v3 호환 및 CRC32가 있지만 타워 모델을 직접 직렬화한다. 별도 `SnapshotCodec<T>` 계약 추출 전까지 게임 전용 유지.
- **모달:** 아이템 획득 `5/OK`와 피버 결과 확인은 타워 상태를 참조한다. 공통 UI·모달 프리미티브 추출은 차기 작업.
- **인벤토리 / 게임 경제 / 성장:** 슬롯·아이템 종류·랭크·업적 값이 타워에 묶임. 공통 이벤트 계약을 우선 확립한 후 단계적으로 분리.
- **생명주기:** `MainActivity`는 타워 상태·팝업·리시버를 직접 사용한다. 지금 단계에서 추상 Activity를 강제로 상속시키지 않았음.

## 5. 호환성·검증

- `minSdk 19`, `compileSdk 34`, Java 8.
- 인터넷 권한/네트워크 통신 없음.
- 앱의 `applicationId`·저장 키는 변경하지 않는다.
- 순수 Java 모듈 계약: `tests/ArcadeCommonContractTest.java` (사용 프로젝트 루트).
- GitHub Actions에서 공통 모듈 빌드 + 타워 APK 빌드. Android SDK가 없는 환경에서는 APK/AAR 실제 컴파일을 **검증했다고 주장하지 않는다**.

**관련:** [공통 모듈 상세 설계](../docs/ARCADE_COMMON_README.md) · [타워 게임 README](../README.md).
