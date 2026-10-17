# Wine Arcade X — Arcade Common Framework

> **공통 모듈 README — 여러 캐주얼 게임에서 재사용할 기반 규칙·코드·확장 계약**  
> 설계 기준 소스: **Tower Builder v1.1.2** · Android 4.4(API 19)+ · Java/Canvas · LG Wine Smart F480K 물리키 + 일반 스마트폰 터치

> [!IMPORTANT]
> **구현 현황:** `arcade-common`은 아직 별도 Gradle/Java 라이브러리로 분리되지 않았다. 이 문서는 타워 프로젝트 안에서 검증된 **직접 이식 가능한 구현**, **분리·리팩터링이 필요한 구현**, **향후 제안 인터페이스**를 엄격히 구별하는 공통 개발 기준서다. 이 문서만 받아서는 독립적인 `arcade-common` 바이너리나 완성된 공통 API가 존재하는 것은 아니다.

**함께 읽을 문서:** [Tower Builder 게임 README](../README.md) — 실제 타워 규칙, 밸런스, 19단계 개발 이력, 아이템 5종·자석 피버·출시 상태는 이 문서에서 관리한다. 새 게임을 만드는 경우 **공통 README → 새 게임 고유 README** 순서로 읽는다.

## 목차

1. [공통 모듈의 목적과 경계](#1-공통-모듈의-목적과-경계)
2. [공통화 과정의 변경 히스토리](#2-공통화-과정의-변경-히스토리)
3. [실제 소스의 재사용 가능 수준](#3-실제-소스의-재사용-가능-수준)
4. [신규 게임을 위한 권장 아키텍처](#4-신규-게임을-위한-권장-아키텍처)
5. [새 게임 이식 및 튜닝 절차](#5-새-게임-이식-및-튜닝-절차)
6. [공통 모듈 개발·추출 우선순위](#6-공통-모듈-개발추출-우선순위)
7. [검증·호환성·배포 규칙](#7-검증호환성배포-규칙)
8. [신규 게임 시작용 마스터 프롬프트](#8-신규-게임-시작용-마스터-프롬프트)
9. [공통 모듈 변경 기록 방식](#9-공통-모듈-변경-기록-방식)

---

## 1. 공통 모듈의 목적과 경계

**목적:** 게임마다 새로 만드는 `메뉴/물리키/터치/화면/모달/프레임/오디오/진동/저장/QA`를 재사용해 새 게임에서는 **게임 규칙·아이템·콤보·피버·콘텐츠 튜닝**에 집중한다. 독립 앱의 `applicationId`, 저장 namespace, 리소스와 출시 히스토리는 공유하지 않는다.

| 분류 | 공통으로 유지할 것 | 새 게임에서 교체할 것 |
|---|---|---|
| **실행 기반** | Android 생명주기, 시스템 키 보호, 키 반복 차단, 터치 안전 게이트, 프레임 제한 | 게임별 입력 의미·조작 간격·갱신 속도 |
| **화면/UX** | Canvas 스케일, 그리기-터치 좌표 일치, 모달 확인/취소, 화면 상태 관리 | 게임 HUD/월드/버튼 레이아웃 및 문구 |
| **오디오/FX** | SoundPool·진동 래퍼, 백그라운드 정지, FX 품질 제어 | 게임별 음색·이펙트·연출 트리거 |
| **데이터** | 버전형 스냅샷·CRC32·1회 정산·호환 디코더 패턴 | 실제 게임 상태/기록·저장 키·이전 버전 마이그레이션 |
| **보상/성장** | 지급 이벤트·중복 보상 방지·인벤토리/피버 라이프사이클 **추상 계약(예정)** | 아이템 종류·효과·콤보 주기·피버 발동/효과/점수·XP 기준 |
| **진단·릴리스** | JVM 테스트/CI/APK 메타데이터 검사/실기 체크리스트 | 앱 ID·버전·게임별 acceptance 시나리오 |

**공통화 판별 원칙:** 타워의 `floors`, `stackW`, `PERFECT`, `MAGNET` 값을 몰라도 동작할 수 있으면 공통으로 이식·추출한다. 이 값에 직접 접근해야 한다면 아직 타워게임 결합 코드다. 아이템/피버의 **처리 골격**은 공통화할 수 있으나 **효과·규칙·수치는 공통 모듈에 넣지 않는다.**

### 공통 모듈의 핵심 불변식

1. 사용자가 한 번 의도해 입력한 명령은 **최대 한 번만** 실행한다. 시스템 전용 키는 Android가 처리한다.
2. 버튼이 그려지는 사각형과 터치 판정 사각형은 같은 레이아웃 계약을 사용한다. 게임 월드와 조작 도크는 겹치지 않는다.
3. 확인형 모달은 명시적 `OK` 전에 자동 종료되지 않는다. **모달 확인 키/터치는 플레이 명령으로 재사용하지 않는다.**
4. 피버·이벤트·쿨타임은 **실제 활성 플레이 시간**만 센다. 앱 포커스 상실/일시정지/모달 중 시간이 새지 않는다.
5. 저장·복원·REWIND로 같은 보상을 다시 지급하거나 이미 정산한 세션을 중복 정산하지 않는다.
6. 이펙트 품질을 낮추어도 판정·점수·아이템 경제는 달라지지 않는다.
7. 물리키만 또는 터치만 사용해 **모든 화면에서 나올 수 있고 정상 종료할 수 있어야** 한다.

---

## 2. 공통화 과정의 변경 히스토리

공통 코드 자체가 출시된 이력이 아니라, **Tower Builder 내부에서 범용 기반이 발전한 이력**이다. 게임별 기능 변경과 타워 전체 연혁은 [Tower Builder README](../README.md)에서 기록한다.

| 버전 | 공통 기반 관점에서 남긴 성과 | 현재 상태 |
|---|---|---|
| v0.1.0 | Canvas UI, Android 4.4 대응, 키 조작, 기록, 빌드 작업 흐름 | 앱 내부 구현 |
| v0.2.5 | SoundPool·진동, 이펙트 예산과 품질 단계 | 타워 연출에 결합 |
| v0.3.0~v0.4.0 | 성장/업적/미션/코인/스킨의 이벤트-정산 패턴 | 콘텐츠·규칙 결합 |
| v1.0.0 | 체크포인트 CRC32·자동 저장·중복 정산 방지·AUTO FX | 범용 계약 추출 필요 |
| v1.0.1~v1.0.2 | 아이템 피드백, 명시적 확인 모달, 미확인 안내 복원 | 아이템 문구·종류에 결합 |
| v1.0.3~v1.0.4 | 물리키 게이트, 포커스 자동 일시정지, 터치/프레임 진단 | 일부 순수 Java 이식 가능 |
| v1.0.5~v1.0.9 | 피버 시간/상태/저장, 탭 예약, 콤보 정책 단일화, 기록 확장 | **타워용 FEVER와 분리 필요** |
| v1.1.0 | 시스템 키 OS 반환, 백그라운드 오디오 정지, ADB 검사 | 장치 기반 재사용 후보 |
| v1.1.1 | 메뉴·모달·보관함 등의 전체 듀얼 입력과 입력 액션 라우팅 | `TowerTouchMap` 추출 필요 |
| v1.1.2 | 월드/도크 안전영역 분리, 동일 hitbox, 겹침 회귀 검사 | 레이아웃 프로필 분리 필요 |

**주의:** 위 이력에 ‘공통 기반’이라고 쓰여 있어도 `arcade-common` 독립 모듈이 이미 만들어졌다는 뜻은 아니다.

---

## 3. 실제 소스의 재사용 가능 수준

**레벨 정의**

- **① 직접 이식 후보:** Android에 의존하지 않는 비교적 범용 클래스. 패키지명·정책 상수와 테스트만 맞춰 재사용 가능.
- **② 어댑터/추출 필요:** 좋은 구현 패턴은 있으나 현재 `Tower*`/타워 좌표·저장·리소스에 묶여 있음. 게임 중립 인터페이스를 먼저 만드는 것을 권장.
- **③ 본게임 전용:** 다른 게임에서는 기능을 복사하지 말고 새로 작성.

### 3.1 ① 직접 이식하기 쉬운 순수 Java 모듈

| 현재 클래스 | 공통으로 가져갈 기능 | 이식 시 확인 사항 |
|---|---|---|
| `core/PhysicalKeyGate.java` | `KEY_DOWN` 중복·롱프레스 오작동 방지, `KEY_UP` 재입력 허용 | 새 게임의 키 매핑과 포커스 상실 reset 연계 |
| `core/DeviceKeyPolicy.java` | 볼륨·통화·미디어·HOME 등 시스템 키를 OS에 전달 | 대상 기기/Android 키코드 정책 확인 |
| `ui/FramePacer.java` | 플레이/결과 애니메이션/모달/정적 메뉴에 따라 프레임 간격 제어, dt 상한 | 33ms/160ms/180ms 값은 게임 성능에 맞춰 조정 |
| `ui/FrameDiagnostics.java` | 실제 플레이 FPS·프레임 스파이크·최대 지연 측정 | 게임 상태 판단값은 새 앱이 전달 |
| `ui/TouchInputGate.java` | `DOWN→UP` 1회, 드래그/멀티터치/뷰포트 밖 터치 배제 | **320×480 크기와 19px 이동 임계값은 현재 하드코딩** — 새 좌표계에 맞춰 수정 |
| `game/FeverTapBuffer.java` | 짧은 쿨다운 중 탭 1회 예약, 상태 전환 시 취소 | **피버 전용 이름/200ms 창**을 일반 액션 큐 정책으로 변경해야 함 |

`TouchInputGate`, `FeverTapBuffer`는 알고리즘은 순수하지만 현재의 게임 디자인 수치와 시나리오가 있어 **설정 가능하게 만드는 추가 작업**이 필요하다.

### 3.2 ② 구조를 추출해야 재사용 가능한 모듈

| 현재 클래스/파일 | 재사용할 설계 | 현재 결합 요소 / 필요한 분리 |
|---|---|---|
| `MainActivity.java` | 화면 상태기계·생명주기·키/터치 공통 액션 처리·모달 우선순위 | `TowerEngine`, `TowerRecords`, 숫자키 메뉴 분기 → `GameHost`/`InputAction`로 분리 |
| `ui/TowerView.java` | 320×480 Canvas 스케일·letterbox·고정 저할당 렌더링·버튼 페인팅 | 타워·아이템·상점·레코드가 한 View에 있음 → 게임별 Scene renderer 분리 |
| `ui/TowerTouchMap.java` | **보여지는 버튼과 같은 좌표로 hit test**, 화면별 입력 경로 검사 | 각 화면의 버튼 상수가 타워에 맞춰짐 → `ScreenActionMap`/레이아웃 데이터로 전환 |
| `ui/TowerUiLayout.java` | 게임 월드와 조작 도크 분리, safe area/offset 계약 | y87..379 / -41px는 타워 전용 → 게임별 `LayoutProfile` |
| `core/TowerAudio.java` | 우선순위·사운드/진동·앱 lifecycle 관리 | 타워 WAV/`TowerRecords` 직접 의존 → `FeedbackPlayer` + 게임별 효과 매핑 |
| `core/TowerRecords.java` | SharedPreferences 설정/기록 저장, 1회 정산, 저장 스키마 추가·마이그레이션 | 키 이름/통계/체크포인트가 타워 전용 → 게임별 `SaveAdapter` |
| `game/TowerCheckpoint.java` | **버전 + MAGIC + CRC32** 검사, 손상 복원, 이전 버전 디코드 | `TowerEngine/ItemSystem/Fever` 바이너리 직렬화 직접 호출 → `SnapshotCodec<T>` |
| `effects/TowerQualityController.java` | 지속 저FPS 시 코스메틱 품질 자동 하향 | `TowerImpact.LOW` 결합 → 범용 quality enum/상수로 변경 |
| `effects/TowerImpact.java` | 고정 크기 이펙트 배열, 짧은 화면 타격감, 파티클 예산 | 탑 좌표·타워 판정·자석 전용 → 이벤트/좌표 변환/프리셋 분리 |
| `ui/TowerItemFeedback.java` | 확인형 보상 모달, 수신·사용·불가 안내 우선순위 | 아이템 종류/텍스트/슬롯 결합 → 일반 `RewardDialog` + 게임 설명 provider |
| `game/TowerProgress.java` / `TowerEvolution.java` | XP·마스터리·랭크·뱃지·미션/컬렉션/코인 지급 | 달성 조건·랭크/테마/미션 수치 결합 → `ProgressRuleSet`와 분리 |
| `game/TowerComboSchedule.java` | **하나의 보상 정책을 아이템/FEVER/화면에서 공동 참조** | 자연 PERFECT/3콤보 주기 결합 → 게임별 `RewardPolicy` 인터페이스 |
| `game/TowerFever.java` | 타이머·발동·중단·종료 확인·한 번만 지급·통계·저장 | 자석/층/`TowerEngine` 결합 → `FeverLifecycle` + 게임별 `FeverEffect` |
| `game/TowerItemSystem.java` | 슬롯·인벤토리·유효성 검사·보상 중복 방지 | `TowerEngine`·5개 효과가 직접 묶임 → `ItemInventory` + `ItemEffect` |
| `ui/TowerComboExperience.java` | 다음 보상 HUD·발동 배너 | 타워 콤보 문구·색/연출 → 이벤트 기반 `MilestonePresenter` |

### 3.3 ③ 복제하지 말고 새로 만들 영역

| 타워 전용 소스 | 이유 |
|---|---|
| `game/TowerEngine.java` | 이동 블록·수평 겹침·절단·카메라·탑 충돌 판정 자체가 핵심 게임 규칙 |
| `game/TowerChallenges.java` | PRECISION/SKYLINE이라는 타워 목표와 조건 |
| 5개 기존 `ItemEffect`의 구체적 실행 | `armShield()`, `expand()`, `armAlign()`, `rewind()` 등의 타워 메서드에 의존 |
| FEVER **자석 중앙 정렬**과 타워 ×3 점수 산식 | 새 게임에 자석이 의미 없을 수 있음 |
| 6종 타워 스킨·타워 컬렉션·층수 랭크 기준 | 콘텐츠·달성 목표가 본게임 전용 |

**판별 기준:** 입력 장치·렌더링·생명주기·저장·진단처럼 *게임 규칙을 몰라도* 동작하면 공통. `floors`, `stackW`, `PERFECT`, `MAGNET`, `TowerEngine`을 알아야 하면 현재는 게임별 결합 코드이다.

---

## 4. 신규 게임을 위한 권장 아키텍처

아래는 **향후 추출할 권장 구조**다. 이 문서가 포함된 프로젝트 ZIP에 그대로 존재하는 소스 트리는 아니다.

```text
wine-arcade/
├── arcade-common/                 # 재사용 기반 모듈 [추출 예정]
│   ├── input/                     # 물리키 gate, 시스템 키 정책, 터치/액션 라우팅
│   ├── screen/                    # Scene 전환, ModalManager, 버튼 hitbox, safe layout
│   ├── runtime/                   # FramePacer, FrameDiagnostics, lifecycle pause
│   ├── feedback/                  # 효과음/진동 호출, 이펙트 품질 정책
│   ├── persistence/               # versioned SnapshotCodec, CRC32, 설정 저장
│   ├── reward/                    # RewardPolicy, Inventory, Progress, Achievement
│   └── event/                     # 게임 이벤트 전달 (판정/보상/모달/종료)
├── tower-builder/                 # 본게임 어댑터
│   ├── TowerEngine, TowerRules, TowerChallenges
│   ├── TowerRewardPolicy, TowerItems, TowerMagnetFever
│   ├── TowerScenes, TowerAudioPresets, TowerSaveAdapter
│   └── tower-specific tests
└── new-game/                      # 다음 게임별 독립 프로젝트/앱
    ├── NewGameEngine, NewGameRules
    ├── NewGameRewardPolicy, NewGameItems, NewGameFever
    ├── NewGameScenes, NewGameAudioPresets, NewGameSaveAdapter
    └── game-specific tests
```

### 4.1 공통과 게임 전용의 계약 (설계 예시, 현재 구현된 API 아님)

```java
// 설계 인터페이스 예시: 실제 arcade-common 소스에는 아직 존재하지 않음.
interface GameEngine {
    void reset();
    void update(float activePlayDt);
    GameEvent apply(GameAction action);
    GameSnapshot snapshot();
    void restore(GameSnapshot snapshot);
}

interface RewardPolicy {
    Reward decide(GameEvent event, GameSession session);
}

interface ItemEffect {
    String id();
    boolean canUse(GameSession session);
    GameEvent apply(GameSession session);
}

interface FeverPolicy {
    boolean shouldStart(GameEvent event, GameSession session);
    float durationSeconds();
}

interface FeverEffect {
    GameEvent transform(GameAction action, GameSession session);
}
```

이 계약의 목적은 **완전히 다른 게임 엔진**도 공통 메뉴/입력/저장/피드백 흐름을 재사용하도록 하는 것이다.

### 4.2 서로 다른 게임의 예시 정책

| 정책 축 | Tower Builder (현재) | 다음 게임의 임의 예시 *(미구현)* |
|---|---|---|
| 핵심 액션 | 움직이는 블록 배치 | 회전 타이밍 맞추기 |
| 연속 성공 | 자연 PERFECT 콤보 | 목표 영역 연속 적중 |
| 보상 주기 | 2/3/4 = 아이템/미스터리/피버 | 예: 3/5/8 또는 게이지 기반 |
| 아이템 | 방어·폭 확장·정렬·감속·되돌리기 | 예: 재시도권·시간 연장·속도 고정 |
| FEVER | 12초 자석 중앙 정렬/×3 | 예: 8초 넓은 판정 구역/×2 |
| 결과 화면 | 층수·타워 점수·FEVER 층수 | 예: 연속 적중 수·라운드 최고 기록 |
| 장식 | 타워 외형/층 컬렉션 | 예: 회전판/배경/이펙트 테마 |

**권장 공통화 대상은 ‘사용자가 연속 성공했다’라는 이벤트에 보상 정책을 적용하는 흐름이지, Tower Builder의 2/3/4 숫자나 자석 효과 자체가 아니다.**

### 4.3 반드시 유지할 계층 분리

```text
[물리키] ─┐
          ├─> [공통 InputAction] ─> [GameEngine (게임별)] ─> [GameEvent]
[터치] ───┘                                     │                │
                                              │                ├─> RewardPolicy (게임별)
                                              │                ├─> FeverPolicy/Effect (게임별)
                                              │                ├─> Progress/Achievement (규칙 교체)
                                              │                └─> FX / Sound / UI (공통 장치 + 게임별 프리셋)
                                              └──> SaveAdapter / SnapshotCodec

중요: UI는 보상을 '보여 주기만' 한다. 보상의 원본 지급·중복 처리·저장은 엔진/세션에서 수행한다.
```

### 4.4 게임 이벤트 최소 표준 (제안)

- `SESSION_STARTED`, `ACTION_ACCEPTED`, `ACTION_REJECTED`
- `SUCCESS`, `PERFECT`, `COMBO_CHANGED`, `MILESTONE_REACHED`
- `REWARD_GRANTED`, `ITEM_USED`, `FEVER_STARTED`, `FEVER_ENDED`
- `MODAL_OPENED`, `MODAL_ACKNOWLEDGED`, `PAUSED`, `RESUMED`
- `SESSION_ENDED`, `SESSION_SETTLED`, `CHECKPOINT_SAVED`

어떤 게임에는 PERFECT 자체가 없을 수 있으므로 이벤트는 게임이 **선택적으로** 발행한다. `REWARD_GRANTED`를 화면에서 다시 처리해 중복 지급하지 않는다.

---

## 5. 새 게임 이식 및 튜닝 절차

### 5.1 신규 게임을 시작할 때의 7단계

1. **프로젝트 독립화:** 별도 `applicationId`, 프로젝트명, 저장 namespace, 사운드 리소스 이름을 정한다. 기존 타워 앱을 덮어쓰지 않는다.
2. **공통 코드 추출:** `PhysicalKeyGate`, `DeviceKeyPolicy`, `FramePacer`, `FrameDiagnostics`, `TouchInputGate`부터 복사·테스트하고, 화면/저장/오디오 어댑터를 분리한다.
3. **핵심 게임 엔진 교체:** `TowerEngine`의 블록 물리·좌표·PERFECT 판정을 제거하고 새 `GameEngine`을 작성한다. 화면 그리기와 게임 판정을 분리한다.
4. **보상 규칙 정의:** 성공 기준, 콤보 증가 조건, 중복 지급 방지, 아이템 지급 횟수, 미스터리 유효성 등을 새 게임 정책으로 만든다.
5. **아이템·FEVER 게임별 구현:** `ItemEffect`의 유효성·소모 조건과 `FeverPolicy`(시작·시간·효과·종료)를 새로 작성한다. 타워 자석/3배/12초를 기본 복사하지 않는다.
6. **전체 화면/입력 대응:** 모든 화면의 물리키와 터치 버튼을 같은 액션으로 연결하고, 팝업 우선순위·월드/도크 안전영역·화면 비율을 점검한다.
7. **회귀/실기 검증 후 릴리스:** 저장 v1 마이그레이션, 아이템·피버 중단/복원, 중복 입력, 저FPS, Android 4.4/일반폰 APK 확인 후 버전을 올린다.

### 5.2 게임별 설정표를 먼저 작성

새 게임 착수 시 아래 표를 채우면 **공통 엔진과 게임별 규칙의 경계가 흔들리지 않는다.**

| 결정 항목 | 새 게임에서 반드시 정할 사항 |
|---|---|
| 게임 이름·앱 ID | 제품명, `applicationId`, 저장 prefix |
| 주 조작 | 키 매핑, 단일 터치/연타/스와이프 여부 |
| 판정 | 성공/실패/퍼펙트 조건과 허용 오차 |
| 세션 | 시작·일시정지·클리어·게임오버·이어하기 |
| 콤보 | 어떤 성공에서 +1, 언제 0이 되는지, 보상 조건 |
| 아이템 | 종류, 무료 지급/소모, 유효성/슬롯/보관함 |
| 미스터리 | 확률/대상/유효하지 않은 효과 제외 정책 |
| FEVER | 발동 트리거, 지속, 강력한 효과, 점수, 콤보 처리 |
| 밸런스 | 1회 플레이 길이, 게임 속도, 보상 빈도, 실패율 목표 |
| 성장 | XP, 레벨, 랭크, 미션, 컬렉션, 스킨 |
| 연출 | 음향·진동·파티클·프레임 품질 예산 |
| 기기 대응 | F480K 물리키 + 일반 스마트폰 터치 + 팝업 확인 |
| 저장 | 버전형 체크포인트와 이전 기록 유지/마이그레이션 |
| 출시 확인 | JVM 회귀, 소스 검사, 실제 APK, 실기 2종 |

### 5.3 회귀 검증 불변식 (모든 새 게임에서 재사용)

- 같은 사용자 **입력 1회는 게임 액션 최대 1회**. 키 반복/터치 이동/확인 후 연쇄 입력을 막는다.
- 모달이 열린 동안 보상·시간·점수는 잘못 진행되지 않고, 확인 입력은 팝업에서 **소비**된다.
- 동일 마일스톤 보상은 재시작·REWIND·저장 복구 상황에서도 **중복 지급되지 않는다**.
- 일시정지/백그라운드 복귀로 진행 시간이 갑자기 증가하지 않는다.
- UI 파티클 품질이 낮아져도 게임 규칙/스코어는 바뀌지 않는다.
- 탑/게임 월드와 버튼의 그리기 영역 및 터치 영역이 겹치지 않는다.
- 키패드 전용 화면과 터치 전용 화면 사이에 **빠져나올 수 없는 경로**가 없어야 한다.
- 앱 패키지 ID와 저장 키를 타 게임과 공유하지 않는다.

---

## 6. 공통 모듈 개발·추출 우선순위

새 게임에 공통 기반을 적용하려면 아래 순서로 **실제 `arcade-common`을 별도 모듈로 추출**해야 한다. 이 섹션은 구현 계획이며, **아직 완료되지 않았다.**

| 단계 | 추출 영역 | 소스 시작점 | 완료 판단 기준 |
|---|---|---|---|
| P1 | 순수 입력/프레임 | `PhysicalKeyGate`, `DeviceKeyPolicy`, `TouchInputGate`, `FramePacer`, `FrameDiagnostics` | 타워 이름 없이 단독 JVM 테스트 통과; 두 입력 수단 일치 |
| P2 | 화면/모달/액션 | `MainActivity`, `TowerView`, `TowerTouchMap`, `TowerUiLayout`, `TowerItemFeedback` | 공통 호스트+게임별 Scene/액션 맵 분리; 팝업 확인 입력 소비 |
| P3 | 오디오·FX 예산 | `TowerAudio`, `TowerQualityController`, `TowerImpact` | 게임별 사운드·이펙트 프리셋 주입, 백그라운드 정지 |
| P4 | 저장·정산 | `TowerCheckpoint`, `TowerRecords` | `SnapshotCodec<T>`, 게임별 namespace, v1~v3 이전 스냅샷 호환 |
| P5 | 보상·아이템·피버 | `TowerComboSchedule`, `TowerItemSystem`, `TowerFever`, `TowerProgress` | 게임별 `RewardPolicy`, `ItemEffect`, `FeverPolicy/Effect` 교체 가능 |
| P6 | 두 번째 게임 적용 | 완전히 다른 게임 엔진 1개 | 공통 코드 수정 없이(또는 최소 수정) 새로운 규칙·아이템·피버 적용 |

### 외부 API가 아닌 '제안'임을 명시할 것

- `GameEngine`, `RewardPolicy`, `ItemEffect`, `FeverPolicy`, `FeverEffect`는 **문서상의 인터페이스 예시**다. Java 소스 코드에 같은 클래스/메서드가 구현돼 있다고 간주하지 않는다.
- 추출 과정에서 기존 Tower Builder를 깨뜨리지 않도록 **타워 게임을 첫 번째 소비 앱**으로 만든다.
- 타워의 **5종 아이템, 2/3/4 보상, 12초 자석 FEVER, ×3 점수**는 공통 모듈 상수로 정의하지 않는다.
- 새로운 게임별 정책은 설정으로 처리 가능한 영역과 별도의 효과 구현이 필요한 영역을 구분한다. `ItemEffect.apply()`처럼 물리 규칙을 바꾸는 처리는 게임별 구현체에서 수행한다.

---

## 7. 검증·호환성·배포 규칙

- 최소 대상: **Android 4.4 / API 19**. 와인폰 물리키와 일반 스마트폰 터치 모두 테스트한다.
- Java 8 소스 호환, Android 빌드 JDK 17, Canvas 기반 320×480 기준 화면 및 비율 유지 방식은 **타워에서 검증한 시작점**이지 모든 새 게임에 강제되는 화면 크기·UI 정책은 아니다.
- 공통 모듈 테스트는 Android SDK 없이 실행할 수 있는 순수 JVM 검사와, Android API 빌드/실기 검사를 분리한다.
- 코드·UI 정책 테스트가 성공해도 실제 APK가 빌드되었거나 F480K/일반 스마트폰에서 실행된 것이라고 표현하지 않는다.
- `SoundPool`·진동·설정·체크포인트·시스템 키 처리의 API19 호환은 실제 Android SDK 빌드와 기기에서 확인한다.
- 앱 ID, 서명, 저장 키, 기록 스키마, 업데이트 버전은 프로젝트마다 독립 관리한다. 도메인 모듈을 복사할 때 기존 기록과 충돌시키지 않는다.

### 공통 모듈을 사용하는 게임의 필수 테스트

- 키 `DOWN`→`UP`, 길게 누르기, BACK/볼륨/미디어 키, 폴더/포커스 상실과 복귀.
- 단일 터치·드래그·멀티터치·뷰포트 밖 터치; 그림 버튼과 같은 hitbox; 세로 화면 비율 변화.
- 메뉴·일시정지·결과·보관함·상점·설정·확인/취소 등 **어떤 화면에서도 터치만으로 탈출 가능**.
- 아이템·피버 중단/재개, 모달 확인 시 입력 소비, 키 및 터치의 중복 이벤트 제거.
- 체크포인트 버전/CRC 손상/이전 버전 복원/REWIND 및 정산 1회 보장.
- 실제 APK의 `applicationId`, `versionName`, `minSdk`, permission을 검증하고, 두 종류의 기기로 실기 인수 테스트.

**타워 원본 테스트/빌드 명령:** [Tower Builder README — 빌드·검증](../README.md#8-빌드검증호환성) 및 프로젝트 루트 `tests/test-engine.sh`, `tests/verify-project.py`, `.github/workflows/build-apk.yml` 참고.

---

## 8. 신규 게임 시작용 마스터 프롬프트

아래 템플릿은 새로운 게임 개발 대화에서 **공통 기반은 유지하되 게임 규칙을 독립 설계**하도록 하기 위한 시작 프롬프트다. `[]`를 새 게임에 맞게 바꾼다.

```text
프로젝트: Wine Arcade X 신규 게임 — [게임명]
참고 소스: Tower Builder v1.1.2, docs/ARCADE_COMMON_README.md의 공통/도메인 경계 및 README.md의 타워 고유 규칙

목표:
- LG Wine Smart F480K(Android 4.4/API19) 물리키와 일반 안드로이드 스마트폰
  터치를 함께 지원하는 독립 오프라인 캐주얼 게임을 제작한다.
- 프로젝트/패키지/저장 영역을 별도로 만들고 기존 Tower Builder는 변경하지 않는다.
- 기존 Canvas, 키·터치 안전 처리, 생명주기, 프레임 진단, 오디오/진동,
  팝업 확인, 체크포인트, 테스트/CI의 공통 패턴은 최대한 재사용한다.

[새 게임의 고유 규칙]
- 한 번의 주 조작: [...]
- 성공/실패/판정: [...]
- 핵심 점수/목표/게임오버: [...]
- 1회 플레이 목표 길이와 난이도: [...]

[게임별 보상 튜닝]
- 콤보 증가·초기화 조건: [...]
- 콤보 보상(아이템/미스터리/피버): [...]
- 아이템 종류와 각 효과/유효 조건: [...]
- 피버 시작 조건/지속 시간/플레이 효과/점수/종료 정산: [...]
- 성장, 미션, 랭크, 스킨: [...]
- 연출·효과음·진동: [...]

[구현/품질 기준]
1) 공통 코드 후보와 새 게임 전용 코드를 먼저 구분하여 설계한다.
2) 물리키+터치 입력을 동일 액션으로 연결하며, 모든 화면과 팝업에
   보이는 버튼과 실제 터치 동작을 제공한다.
3) 게임 월드와 조작 버튼을 겹치지 않게 배치한다.
4) 콤보 보상 중복 지급, 아이템 소모, 피버 시간/중단/재개,
   모달 확인 입력의 오작동, 저장/이어하기를 테스트한다.
5) 기능 구현 후 실행 가능한 전체 프로젝트를 ZIP 루트 형태로 제공한다.
6) Java 테스트/구조 검사 결과와 APK/실기 미검증 항목을 분리 보고한다.
7) 새 게임 고유의 규칙·밸런스 변경 히스토리를 새 README에 누적한다.

단계: 핵심 게임 MVP → 아이템/보상 → 피버/연출 → 성장/콘텐츠
      → 안정화/저장 → 듀얼 입력/안전영역 → 실기 검증

Tower Builder의 PERFECT 오차 4px, 2/3/4 보상 주기, 5종 아이템,
12초 자석 FEVER, 3배 점수는 새 게임에서 요청하지 않는 이상 복사하지 않는다.
```

---

## 9. 공통 모듈 변경 기록 방식

기술적으로 공통 모듈을 분리한 뒤에는 **공통 코드 변경과 각 게임의 변경을 따로 기록**한다. 아직 공통 모듈 배포 버전은 없다. v1.1.2는 타워 게임 버전이지 `arcade-common` 버전이 아니다.

```markdown
## arcade-common vX.Y.Z — [날짜]
- 공통 기능/고친 문제: [입력, 화면, 모달, 진단, 오디오, 저장 등]
- 변경한 API/인터페이스: [호환/비호환, 마이그레이션]
- 영향받는 게임: [Tower Builder, New Game ...]
- 게임별 구현 책임: [별도 아이템/FEVER/보상 정책의 변경 필요 여부]
- 검증: [공통 JVM/Android 빌드/F480K/일반 스마트폰]
- 남은 제약: [실기 미검증/제조사별 차이/성능]
```

**공통 모듈의 릴리스 원칙:** 게임 고유 기능은 `arcade-common`에 추가하지 말고, 인터페이스·기반 모듈만 공통 릴리스한다. 새로운 게임에서 필요한 아이템·보상·피버 수정은 **그 게임의 README/CHANGELOG에만** 기록한다.

---

**다음 작업:** 실제 `arcade-common`을 추출한 뒤, Tower Builder를 첫 번째 사용처로 재연결하고 두 번째 게임에 적용해 모듈의 재사용성을 검증한다.
