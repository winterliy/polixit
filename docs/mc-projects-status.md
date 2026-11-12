# Minecraft 프로젝트 통합 현황

마지막 갱신: 2026-11-09 (세션 종료 시점)

## 규칙

- 브랜치는 Minecraft 버전 이름만 사용합니다(`26.3`, `26.2`, `1.21.11` …). `main` 없음, 기본 브랜치는 `26.3`. 2026-11-09 다섯 저장소(polixit·gesturing·maple·ledge·ledge-fabric) 정리 완료.
- 세부 기능이 끝날 때마다 commit/push합니다. 릴리스는 전체가 끝난 뒤 프로젝트별 통합 릴리스 1개로 냅니다.
- jar 이름: `[mod]-[version]-[paper|fabric|forge]-[mc버전].jar` (릴리스 이름도 같은 형식).
- 진행 순서: 최신 버전 → 과거 버전. 26.3을 먼저 마무리합니다.
- 구버전 로더: 플러그인은 Paper가 있으면 Paper(1.7.10 PaperSpigot, 1.8.8, 1.12.2+), 1.5.2는 CraftBukkit/Spigot.
  모드는 Legacy Fabric(1.7.10/1.8.9/1.12.2), 1.5.2는 Forge. 로직 흐름은 Fabric판을 따릅니다.
- 참고 프로젝트: polixit→WorldEdit·Motopoly, gesturing→GSit, maple→CustomImages·ImageFrame·ImageCanvas, ledge→Dynmap·Xaero.

## 버전 대상

26.3 → 26.2 → 26.1.2 → 1.21.11 → 1.20.6 → 1.19.4 → 1.18.2 → 1.17.1 → 1.16.5 → 1.15.2 → 1.14.4 → 1.13.2 → 1.12.2 → 1.8.9 → 1.7.10 → 1.5.2

## 현황표

범례: ✅ 인게임 확인 · 🔨 빌드만 · ⏳ 진행 중 · — 미착수

| 버전 | polixit P/F | gesturing P/F | maple P/F | ledge P/F |
|---|---|---|---|---|
| 26.3 | ✅/✅ | ✅/✅ | ✅/✅ | ✅/✅ |
| 26.2 | 🔨/— | 🔨/🔨 | 🔨/🔨 | 🔨/🔨 |
| 26.1.2 | 🔨/— | 🔨/🔨 | 🔨/🔨 | 🔨/🔨 |
| 1.21.11 | 🔨/— | 🔨/🔨 | 🔨/🔨 | 🔨/— |
| 1.20.6 | —/— | 🔨/🔨 | 🔨/🔨 | 🔨/— |
| 1.19.4 | —/— | 🔨/🔨 | 🔨/🔨 | 🔨/— |
| 1.18.2 | —/— | 🔨/🔨 | 🔨/🔨 | 🔨/— |
| 1.17.1 | —/— | 🔨/🔨 | 🔨/🔨 | 🔨/— |
| 1.16.5 | —/— | 🔨/🔨 | 🔨/🔨 | —/— |
| 1.15.2 | —/— | 🔨/🔨 | 🔨/🔨 | —/— |
| 1.14.4 | —/— | 🔨/🔨 | 🔨/🔨 | —/— |
| 1.13.2 | —/— | 🔨/— | 🔨/— | —/— |
| 1.12.2 | —/— | 🔨/🔨 | 🔨/🔨 | —/— |
| 1.8.9 | —/— | 🔨/🔨 | 🔨/🔨 | —/— |
| 1.7.10 | —/— | —/🔨 | —/— | —/— |
| 1.5.2 | —/— | —/— | —/— | —/— |

P = Paper/Bukkit 플러그인, F = Fabric/Legacy Fabric/Forge 모드

## 26.3 기록

### polixit (브랜치 `26.3`)
- Fabric·Paper 실제 클라이언트로 `//pos1`, `//pos2`, `//terrain info`, `//gen 80`, `//undo`, `//redo` 확인. 서버 오류 없음.
- 커밋 `d5f6bb37c`(README 검증 기록).

### gesturing (브랜치 `26.3`)
- 자세 명령을 GSit 방식(패킷 전용 복제 캐릭터)으로 교체: 침대·일어나기 창·화면 어두워짐 없음.
- `/lay` 방향 생략 시 보는 방향, `/stand` 시 원래 위치 복귀(한 블록 내려가던 문제 해결).
- Paper·Fabric 모두 `/sit`, `/lay`, `/legsup`, `/bellyflop`, `/spin`, `/crawl` 화면 확인. Fabric GameTest 3개 통과.
- 커밋 `c5ea76a`.
- 미확인: 두 명의 실제 플레이어가 필요한 플레이어 위 앉기, 다른 플레이어 시점.

### maple (Paper: `maple` 브랜치 `26.3` `faa8e5e`, Fabric: `ledge-fabric` 브랜치 `26.3` `cc1ae52`)
- Paper·Fabric 실제 클라이언트: HTTPS `/image load` 저장, HTTP 거부, `/image set` 실제 우클릭 2회로 2×2 배치, 화면 표시, `/image del`·`/image reload`·서버 재시작 복구 확인.
- 수정: 마지막 우클릭의 보조 손 이벤트가 지도를 회전시키던 문제, Maple 액자 회전·꺼내기 방지, HTTP 거부를 다운로드 안내 전에 처리, Fabric 투명 픽셀 처리(Paper와 동일).
- 예정: Fabric 코드를 `maple` 저장소로 합치는 작업(사용자 지시 maple→maple).

### ledge (브랜치 `26.3`, 최신 `4007c84`)
- Fabric 지도 화면: 26.3 API 변경(blit/outline 인자, 마우스 버튼 1부터, 새 키 코드, 텍스트 입력 활성화)으로 깨져 있던 타일 표시·클릭·드래그·입력·인쇄를 수정.
- 추가: GUI 축소, 우클릭 메뉴(포인트/영역 추가, 이름·색 수정, 채우기↔아웃라인, 삭제), Esc 단계별 닫기, 부드러운 드래그, Cmd/Ctrl ± 확대, 스크롤 감도 완화, 한 줄 좌표(접기), ↻ 갱신, Xaero식 줌 대체 타일, 지도만 인쇄(선택 영역 그대로, 배경 포함), Shift 가로/세로 직선, 화면 내 안내 메시지.
- 서버: Chunky 로직 내장 사전 생성(`/ledge pregen start <world> <radius> [square|circle] [cx cz]`, pause/continue/cancel/status, 재시작 후 이어서), 미로드 청크는 리전 파일 직접 읽기(26.x `dimensions/` 구조·새 팔레트 형식), Folia 스케줄러 호환(같은 jar로 Paper·Folia), 타일 작업 병렬화, 빠른 PNG 인코더, 3D 가짜 절벽·켈프 검은 점 수정.
- 성능(로컬 측정): 신규 줌2 타일 36개 14.6초 → 2.9초. 메인 스레드 청크 로드 없음.
- Rust/C++: 프로파일 결과 남은 CPU는 Java 이미지 API·PNG·NBT였고, 압축 해제는 이미 네이티브 zlib. Java 쪽 개선이 더 효과적이라 네이티브 모듈은 보류.
- 미확인: 실제 Folia 서버 구동, 여러 플레이어 동시 접속 시 TPS.

## 환경 메모

- 테스트 클라이언트: polixit 개발 클라이언트(`worldedit-fabric/run`, 오프라인 `PolixitTester`, `pauseOnLostFocus:false`).
- 채팅 입력은 `/` 키 + 클립보드 붙여넣기(한글 입력기 우회).
- `classes.jsa` 경고: Gradle JDK 25의 서명 없는 CDS 파일을 `~/.gradle/jdks-cds-backup/`으로 이동해 해결.


## 2026-11-09 작업 요약

- 26.3: 4개 프로젝트 Paper·Fabric 인게임 검증 완료(위 기록 참고). 브랜치 정리 완료.
- 저장소 정리: ledge-fabric·ledge-glap1·kode·motopoly·nakwonbot 아카이브(motopoly만 공개), 나머지 비아카이브는 전부 공개.
  공개 전 전체 기록 비밀값 패턴 검사(이상 없음). aemil 기본 브랜치 main→master, 나머지 브랜치 정리.
- 라이선스: gesturing·polixit·ledge·ledge-glap1·GLAP = GPL-3.0, 그 외(maple, novi, INAZINA, aemil, penetra, winterliy, motopoly, ledge-fabric, kode, nakwonbot) = MIT.
- 모든 저장소(아카이브 포함): `*.md`, `README*` git 추적 해제 + .gitignore 추가(로컬 파일 유지). gesturing은 평문 `NOTICE`로 GSit 고지 유지.
  단, novi/GLAP 등 기본 브랜치 외의 원격 브랜치에는 아직 미적용.
- maple: Fabric 코드를 `maple/fabric/`로 병합(`1ff16b1`), MIT.
- polixit: 업스트림 version/7.4.x의 테스트 커밋 1개 cherry-pick.
- gesturing 이식 브랜치(모두 push 완료): 26.2 `09d07e0`, 26.1.2 `ae296fd`, 1.21.11 `b11d83d`, 1.20.6 `f9d6ca4`, 1.19.4 `4091189`, 1.18.2 `92ed143`
  (일부는 이후 NOTICE·md 정리 커밋이 얹혀 해시가 바뀜. `git log origin/<버전>` 확인).
  검증 수준: Paper 서버 기동·플러그인 로드 + Fabric 서버 기동(1.20.6 이상은 GameTest 통과). 실제 클라이언트 확인은 안 함 → 🔨.
- 사고: 다른 세션/자동화가 gesturing PR #1~#3(26.2/26.1.2/1.21.11 → 26.3)을 병합 → revert로 복구함. 이후 PR 없이 직접 push, push마다 PR 생성 여부 확인.

## 내일 할 일 (순서대로)

1. ~~Gesturing 1.17.1~~ 완료(`c4a4972`, Paper는 Java 17로 기동 테스트: `SMOKE_JAVA=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java`; Fabric 1.17의 fabric.mod.json 의존 id는 `fabric`). 아래는 당시 메모:: Paper 개발 번들 `1.17.1-R0.1-SNAPSHOT`, paperweight 1.7.7, 툴체인 Java 17(경고: 1.17은 Java 16 요구, release 16 시도).
   CraftBukkit 패키지 `v1_17_R1`. 알려진 컴파일 오류: `ServerLevel.addFreshEntity(entity, SpawnReason)` 2-인자 없음(→ `addFreshEntity(entity)` 또는 Bukkit 경로),
   `player.getWorld().getSimulationDistance()` 없음(1.18 이전; 고정값 사용). Fabric API 0.46.1+1.17(1.17.1용 별도 확인: `0.46.1+1.17`), 명령 API v1, Fabric Loader 0.20.1은 Java 17 필요 여부 확인.
2. **Gesturing 1.16.5 이하(1.16.5, 1.15.2, 1.14.4, 1.13.2, 1.12.2, 1.8.9→Paper 1.8.8, 1.7.10, 1.5.2)**: Paper 개발 번들이 1.17.1 미만에는 없음.
   방식 결정 필요: ① Paper API jar만 컴파일하고 NMS는 리플렉션(버전별 이름) ② 포즈(lay/bellyflop/legsup/spin)는 제외하고 sit/stand/crawl만 Bukkit API로 구현.
   기본 방침: 2번(API-only)로 시작, 문서에 제한 명시. Fabric은 1.16.5(Fabric 정식), 1.14.4~1.15.2(Fabric), 1.13.2 이하는 Legacy Fabric(1.12.2/1.8.9/1.7.10), 1.5.2는 Forge.
3. **Polixit, Maple, Ledge**: 같은 순서로 26.2 → 26.1.2 → 1.21.11 → … 이식. (Gesturing에서 얻은 패턴: Java 21 툴체인+release 낮추기, paperweight 1.7.7(1.18.2 이하), 번들 패킷 없음 시 개별 전송, 1.19.4 이하 GameTest 제외.)
4. **릴리스**: 마크 버전별로 릴리스 1개, 안에 paper·fabric jar 둘 다. 이름 `[mod]-[version]-[paper|fabric|forge]-[mc].jar`.
   산출물 이름 정리 필요(paperweight 2.x는 `name.jar`, 1.7.x는 `name.jar`=리오브·`-dev.jar` 제외, Fabric은 `gesturing-fabric-0.3.2.jar`) → 복사해서 이름 변경 + SHA256 기록.
   Ledge jar는 `~/Desktop/Ledge-build`에도 복사. 기존 Maple 릴리스 `maple-0.2.0-fabric-paper-26.3`와 Ledge 프리릴리스의 옛 에셋 이름 정리.
5. **남은 정리**: novi·GLAP 등의 다른 원격 브랜치 md 추적 해제, novi/aemil에 남은 PR 없음 확인, 현황표를 인게임 검증이 생기는 대로 ✅로 갱신,
   Gesturing 플레이어-위-앉기·다른 시점 확인(두 명 필요), 다중 접속 TPS 확인.
6. **주의**: PR 만들지 말고 직접 push. push 후 `gh pr list`로 자동 PR/병합 확인. 사용자 실제 서버·월드·mods 폴더 건드리지 말 것. 커밋에 4개 trailer.


## 구버전(≤1.16.5) 이식 메모 — 2026-11-09 추가

- **방식 결정(사용자 확정)**: Paper 개발 번들이 없는 1.16.5 이하는 Paper가 만든 Spigot 매핑 서버 jar(`cache/patched_<ver>.jar`)에 맞춰 컴파일하고, 버전별 NMS 패키지(`nms/v1_16_R3` 등)를 새로 씀. 포즈 명령(lay 등)까지 유지.
  서버 jar는 git에 넣지 않음(`libs/`는 .gitignore). 받는 법: `fill.papermc.io`에서 해당 버전 paper jar → `java -jar`로 1회 실행(Java 17이면 `-DPaper.IgnoreJavaVersion=true`) → `cache/patched_<ver>.jar` → `gesturing/libs/server-<ver>.jar`.
  임시 보관 위치: `$CLAUDE_JOB_DIR/tmp/legacy/jars/` (세션이 정리되면 사라질 수 있음 → 다시 받기).
- **Java 8 바이트코드 필수**: 1.5.2~1.15.2(그리고 1.16.5) 서버는 Java 8/11. Jabel(`com.pkware.jabel:jabel-javac-plugin:1.0.1-2`)로 Java 17 문법 소스를 Java 8로 컴파일(`sourceCompatibility 17` + `release 8`). record는 직접 일반 클래스로 변환, Java 9+ API(`List.of`, `Stream.toList`, `Optional.isEmpty/orElseThrow()`, `List.copyOf`, `Map.copyOf`)는 대체.
  **테스트는 실제 서버 Java로**: Java 11은 `$CLAUDE_JOB_DIR/tmp/legacy/jdk11` (adoptium 11), 스모크 스크립트 `SMOKE_JAVA=<java> [SMOKE_FLAGS=-DPaper.IgnoreJavaVersion=true] smoke-paper.sh <mc> <port> <jar>`. Java 8은 아직 못 구함(Zulu 8 필요).
- **미해결 주의**: 1.17.1 브랜치는 Java 17 바이트코드라서 Java 16로 돌리는 1.17.1 서버에서는 로드 실패 가능 → 릴리스 전에 1.17.1은 Java 16(release 16)으로 다시 빌드할 것. 1.18.2/1.19.4는 Java 17 서버라 문제 없음.
- Fabric 1.16.5: 의존 id는 `fabric`, API 0.42.0+1.16, GameTest 제외, 명령 API v1, damage는 `hurtTime` 폴링. 필드 접근형(`yRot`, `removed`), `remove()`로 대체.
- 다음: Gesturing 1.15.2 → 1.14.4 → 1.13.2 → 1.12.2 → 1.8.8 → 1.7.10 → 1.5.2. 1.13 이하는 NMS가 크게 달라짐(Flattening 이전 블록 데이터, 패킷 구조), 1.8 이하는 DataWatcher/NPC 방식이 완전히 다름.


## 2026-11-09 (저녁) Gesturing 1.15.2 ~ 1.12.2 진행

- Paper: 1.15.2 `8cf12a7`, 1.14.4 `4523f4a`, 1.13.2 `d6b9f15`+제거 커밋, 1.12.2 `e21b4a0`+`2ac97b7`. 모두 Java 8 바이트코드(Jabel). 1.12.2는 실제 Java 8(Zulu 8)로 기동 확인, 나머지는 Java 11.
- Fabric: 1.15.2·1.14.4 완료(Mojang 매핑, 서버 기동·월드 생성 확인; 이 두 버전은 Log4j 설정이 깨져 모드 로드 로그는 못 읽음).
  **1.13.2·1.12.2 브랜치에는 fabric 모듈이 없음**(Legacy Fabric 이식 전).
- 버전별 차이 메모: 1.13은 Pose 시스템 없음(`/lay`=PacketPlayOutBed+패킷 NPC, `/bellyflop`=스윔 플래그, `/crawl` 미지원), 1.12는 블록 데이터 API 없음(MaterialData·구형 sendBlockChange),
  `/bellyflop`은 겉날개 플래그, 서버 엔티티 `tick()` 대신 `B_()`, 일부 서버 인자는 `nogui`(대시 없음, 1.14 이하).
- **Legacy Fabric 이식 준비물**: `~/Desktop/gesturing-lf-skeleton/` (빌드되는 빈 뼈대: `net.fabricmc.fabric-loom-remap` + `legacy-looming` 1.16-SNAPSHOT, yarn 1.12.2+build.604, LFAPI `1.13.2+1.12.2`, Jabel Java 8).
  yarn 매핑 이름으로 GesturingFabric/PoseNpc/SeatGeometry를 새로 써야 함(Mojang 이름 코드와 호환 안 됨). yarn jar는 `fabric/.gradle/loom-cache/minecraftMaven/...1.12.2...yarn...jar`에서 javap로 조회.
  Legacy Fabric은 1.5.2·1.7.10·1.8.9·1.13.2도 지원(yarn 있음; LFAPI는 1.12.2/1.8.9/1.7.10 등) → 1.5.2도 Forge 대신 LF로 가능해 보임(미확인), 사용자 방침(Forge)과 다르니 그때 확인.
- Java 8 JDK 위치: `$CLAUDE_JOB_DIR/tmp/legacy/jdk8`, Java 11: `.../jdk11` (세션 정리 시 재다운로드: adoptium 11, azul zulu 8).
- 남은 Gesturing: Legacy Fabric(1.13.2, 1.12.2) → 1.8.8/1.8.9(Paper+LF) → 1.7.10 → 1.5.2.


## 2026-11-09 (밤) Gesturing 1.12.2 ~ 1.7.10 진행 결과

- 1.12.2: Paper `b45b28e`(+Fabric `2337ed4`), Legacy Fabric 모드 포함. **/crawl, /spin 없음**(1.12에 자세 없음/riptide 없음). /bellyflop은 겉날개 플래그로 대체.
- 1.13.2: Paper `6d8c810`. **Fabric 없음**: Legacy Fabric API가 1.13.2용은 없음(LFAPI 지원 목록: 1.12.2, 1.11.2, 1.10.2, 1.9.4, 1.8.9, 1.8, 1.7.10, 1.6.4) → 모드를 하려면 Mixin으로 서버 틱/명령 훅을 직접 만들어야 함. /crawl 제거.
- 1.8.9 브랜치: Paper 1.8.8 서버용 플러그인(`1017ea1`) + Legacy Fabric 1.8.9 모드(`5b67c97`). **/sit, /lay, /stand만**(1.8엔 구름·겉날개·riptide·crawl 없음 → /bellyflop /legsup /spin /crawl 제외). Paper엔 1.8.9 서버가 없어서(1.8.8이 마지막, 같은 프로토콜) 플러그인은 1.8.8 기준.
- 1.7.10: Legacy Fabric 모드만(`d95a907`), /sit(화살 좌석), /lay. **플러그인 없음**: 1.7.10 서버 jar를 못 구함(Paper에 1.7.10 없음, Spigot BuildTools 실행은 자동 모드 권한이 막음).
  재개하려면 사용자가 허용하거나 jar를 직접 제공: `java -jar BuildTools.jar --rev 1.7.10` (Java 8) → `spigot-1.7.10-*.jar`를 `gesturing/libs/server-1.7.10.jar`로. 1.5.2도 동일(`--rev 1.5.2`; CraftBukkit 1.5.2는 BuildTools 미지원일 수 있음 → 다른 입수 경로 필요).
- 1.5.2: **미착수**. Legacy Fabric 1.5.2 yarn은 있으나 LFAPI 없음(Mixin 필요), 서버 jar 없음. 사용자 방침은 Forge(1.5.2) — LF 대신 Forge 1.5.2 MDK/ModLoader 환경이 필요. 1.5.2는 패킷 이름이 `PacketNN…` 형식이라 전부 새로 써야 함.
- LF 작업 폴더: `~/Desktop/gesturing-lf`(현재 1.7.10 설정). 1.12.2/1.8.9 모드는 각 브랜치의 `fabric/`에 있음. 새 LF 버전은 gradle.properties의 minecraft_version/yarn_build/fabric_version만 바꿔 빌드.
- 인게임(실제 클라이언트) 검증은 아직 전부 미실시.


## 2026-11-10 새벽 — Maple 이식, 릴리스 준비

- **Maple Paper**: 26.2 ~ 1.8.9 전 브랜치 push 완료(각 버전 서버 기동·플러그인 로드 확인, 1.16.5 이하는 Java 8로). 1.8.9 브랜치는 Paper 1.8.8 서버 jar로 컴파일.
  버전 차이 처리: 1.19.4 이하는 Java 17 호환(가상 스레드·getFirst 제거), 1.15.2 이하는 Adventure 대신 ChatColor+Spigot 채팅 API, 1.13.2는 isAir 제거, 1.12.2는 Material.MAP 내구도=맵 id, 1.8은 오프핸드 필터 제거.
  1.19.4~1.17.1 첫 커밋에는 빌드 실패 상태가 섞였고 이후 수정 커밋으로 고쳤음(`f019f30`, `800db8c`, `98e231f`).
- **Maple Fabric**: 26.3 ✅, 26.2(서버 기동·모드 로드 확인), 26.1.2·1.21.11·1.20.6(컴파일만) 완료. **1.19.4 이하는 미완**: 브랜치의 fabric 소스가 26.x 세대라서 1.20.5 이전(NBT, 정수 map id, MapColor API, 스레드 API 등)에 맞춰 `ImageMapPainter`·`FabricImageService`를 고쳐야 함. 변환 스크립트: `$CLAUDE_JOB_DIR/tmp/maple-fabric-port.py <mc> <fabric api> <java> [jabel]`.
- **Maple 1.7.10, 1.5.2**: 서버 jar 없음(Gesturing과 같은 이유) → 미착수.
- **Gesturing 릴리스용 jar**: `~/Desktop/Gesturing-release/<버전>/`. 첫 빌드는 두 루프가 겹쳐 jar가 섞였음 → 겹침 없이 재빌드(로그 `$CLAUDE_JOB_DIR/tmp/gesturing-release-build2.log`). `git clean -x`는 로컬 `.md`를 지우므로 사용 금지.
- 아직 안 한 것: Polixit·Ledge 구버전 이식 전부, Gesturing 1.13.2 Mixin 모드, 1.5.2(Forge) 전부, 1.7.10/1.5.2 플러그인, 모든 릴리스 발행.

## 2026-11-10 — 릴리스 발행 현황

- **Gesturing**: 15개 릴리스 발행(`gesturing-0.3.2-fabric-paper-<mc>`; 1.13.2는 `…-paper-1.13.2`, 1.7.10은 `…-fabric-1.7.10`). jar는 `~/Desktop/Gesturing-release/<mc>/`, 각 jar의 api-version/minecraft 메타데이터를 브랜치와 대조 완료.
- **Maple**: 26.2 ~ 1.8.9 13개 릴리스 발행(`maple-0.2.0-fabric-paper-<mc>` / Fabric 없는 버전은 `maple-0.2.0-paper-<mc>`). jar는 `~/Desktop/Maple-release/<mc>/`.
  **Maple 26.3은 미갱신**: 기존 `maple-0.2.0-fabric-paper-26.3` 릴리스가 옛 커밋(`d469ff6`)과 옛 jar를 가리킴. 현재 `26.3`(`778f86d`)로 맞추려면 태그 강제 이동이 필요한데 자동 모드가 막았음 → 사용자가 허용하거나 직접 처리(예: 릴리스 삭제 후 재생성 승인) 필요.
- **Polixit, Ledge**: 릴리스 없음(이식 미착수). Ledge는 기존 프리릴리스 v0.1.0-alpha.1~4가 있음.
- 릴리스 노트에는 "실제 클라이언트 미확인"을 명시함.


## 2026-11-10 — Ledge 이식·릴리스, Maple 26.3 재릴리스

- Maple 26.3 릴리스는 사용자 승인으로 삭제 후 재생성(현재 26.3 커밋·새 jar 2개).
- **Ledge**: Paper 플러그인 26.2 ~ 1.17.1 이식·push(`66968ee` 26.2, `cab2d82` 26.1.2, `7d4bef2` 1.21.11, `6400cdc` 1.20.6, `35414a9` 1.19.4, `01390d1` 1.18.2, `cab1775` 1.17.1). 각 버전 서버에서 플러그인 로드 확인.
  클라이언트 모드(지도 화면)는 26.3/26.2/26.1.2만: 1.21.11 이하는 26.x GUI API(`GuiGraphicsExtractor` 등)에 묶인 화면 코드를 버전별로 새로 써야 해서 제외(웹 지도만 제공).
  1.19.4 이하는 Folia 스케줄러가 없어 `TaskHandle`+Bukkit 스케줄러로 대체(`Schedulers.java`).
  릴리스 8개 발행(프리릴리스): `ledge-0.1.0-alpha.4-fabric-paper-<mc>`(26.x) / `…-paper-<mc>`(1.21.11~1.17.1). jar는 `~/Desktop/Ledge-release/<mc>/`와 `~/Desktop/Ledge-build/`(옛 `ledge-fabric.jar`, `ledge-glap1.jar`는 그대로 둠).
- **Ledge 1.16.5 이하 미착수 이유**: 청크 저장 형식이 1.18 이전과 다름(`AnvilRegionReader`는 1.18+ 형식만), `World#getMinHeight` 없음(1.16 이하), Java 8 대상(Jabel) 필요.
- **Polixit**: 아직 시작하지 않음(WorldEdit 포크, Fabric+Bukkit 모듈 빌드가 길다).


## 2026-11-10 — Polixit 이식·릴리스

- **Polixit Paper 플러그인**: 26.2(`604a6b5a0`), 26.1.2(`dac539259`), 1.21.11(`400dfad67`) 완료, 서버 로드 확인. 기존 어댑터(adapter-26.2/26.1/1.21.11)를 연결만 하면 됨.
  1.21.11은 Java 21 대상: piston 0.6.0 → 0.5.11(0.6.0 어노테이션 프로세서가 Java 25 필요), `_` 이름 없는 변수 → `ignored`, `ScopedValue` → `ThreadLocal`(Request.java), Kotlin jvmTarget 21, `primaryJavaRelease = 21`.
- **Polixit Fabric**: 26.3만. `worldedit-core-mc`가 26.3 전용 API(`ServerboundPunchPacket`, `SamplerContext`, 나무 피처 클래스 등)를 써서 26.2 이하는 별도 이식 필요.
- **Polixit 1.20.6 이하 미착수**: 업스트림 어댑터가 `version/7.3.x`에는 1.21.4+, `version/7.2.x`에는 1.17.1·1.18.2·1.19.4·1.20·1.20.2·1.20.4·legacy만 있음(1.20.6용 없음). 옛 세대 어댑터를 가져오고 코어 API 호환(BukkitImplAdapter 변화)을 맞춰야 함. 1.16.5 이하는 업스트림 `archive/1.16.5` 등을 기반으로 폴릭시트 터레인 커밋(약 20개)을 체리픽하는 방식이 현실적.
- **Polixit 릴리스** 4개 발행: `polixit-0.2.0-fabric-paper-26.3`, `polixit-0.2.0-paper-{26.2,26.1.2,1.21.11}`. jar는 `~/Desktop/Polixit-release/`.
- **주의**: Polixit은 포크라 `gh release create`가 원본(EngineHub/WorldEdit)을 대상으로 잡음 → 반드시 `-R winterliy/polixit`.
- 남은 일: Polixit ≤1.20.6, Ledge ≤1.16.5, Maple Fabric ≤1.19.4, Gesturing 1.13.2 Mixin 모드, 1.5.2(Forge) 전부, 1.7.10/1.5.2 플러그인(서버 jar 필요).


## 2026-11-10 (새벽 2시경) — Maple Fabric 1.19.4 ~ 1.14.4 완료

- Maple Fabric: 1.19.4 `cadc631`, 1.18.2 `46d5937`, 1.17.1 `459ba62`, 1.16.5 `489c275`, 1.15.2 `efc8733`, 1.14.4 `edfc25e`. 각 버전 Fabric 개발 서버에서 모드 로드·서버 기동 확인(1.17.1은 Java 17 런타임).
  1.20.5 이전은 지도 id가 NBT의 int라서 `MapId`/`MapRefs` 래퍼 도입; 1.19.x는 `MaterialColor`, 1.18 이하는 `TextComponent`/`sendMessage(…, NIL_UUID)`와 command API v1, 1.15 이하는 `DimensionType`·`MinecraftServer.getFile`, 1.16.5 이하는 Jabel(Java 8)·record→class·`MapItemSavedData.colors` 직접 기록.
- Maple 릴리스 6개(1.19.4~1.14.4)를 Paper 전용에서 `maple-0.2.0-fabric-paper-<mc>` 통합 릴리스로 삭제 후 재생성(내가 이번 세션에 만든 것만).
- **Maple Fabric 남음**: 1.13.2(Mixin), 1.12.2·1.8.9·1.7.10(Legacy Fabric yarn) — Gesturing LF 이식 방식(`~/Desktop/gesturing-lf`) 재사용.
- 백그라운드 개발 서버 종료는 `lsof -ti tcp:<port>`로 PID를 찾아 `kill <pid>`(pkill은 자동 모드가 막음).


## 2026-11-10 (새벽 2시 반) — Maple Legacy Fabric 1.12.2 / 1.8.9

- **Maple Legacy Fabric 1.12.2**(`f83726d`), **1.8.9**(`41f7325`) 모드 추가, 서버 로드 확인(1.12.2는 `/image reload` 콘솔 실행까지 확인, 1.8.9는 로드만 확인). Legacy Fabric에 use-entity 이벤트가 없어 `ServerPlayNetworkHandler.onPlayerInteractEntity`에 믹신을 걸어 액자 선택을 처리. 소스: `~/Desktop/maple-lf`(현재 1.7.10 설정으로 남아 있음; 각 브랜치 `fabric/`가 정본).
  이 두 버전의 릴리스(`maple-0.2.0-paper-1.12.2`, `…-paper-1.8.9`)는 아직 Paper 전용 → Fabric 포함 형식으로 재생성 필요.
- **Maple 1.7.10 Fabric**: 시도했으나 중단. 1.7.10 yarn은 액자 좌표(`field_5328…`)·상호작용 액션(`class_2062`)이 미매핑이고 서버 스레드 실행기(`submit`)가 없어 틱 이벤트로 큐를 직접 돌려야 함.
- **1.13.2 Fabric(Maple·Gesturing)**: Legacy Fabric API가 1.13.2용이 없어 서버 시작·틱·명령 훅을 전부 믹신으로 직접 만들어야 함(미착수).

## 2026-11-10 — 방침 확정(사용자 지시)

- **pass(안 함)**: 1.7.10·1.5.2 Bukkit 플러그인 전부(서버 jar 없음). 클라이언트 실검증과 Gesturing 플레이어-위-앉기/다중접속 TPS 테스트(26.3 외는 형식상 제공이므로 별도 테스트 안 함).
- **보류 — 나중에 할 일**:
  1. 1.13.2 Fabric 모드(Gesturing·Maple): Legacy Fabric API가 없어 서버 시작·틱·명령 훅을 믹신으로 직접 구현.
  2. Ledge 1.16.5 이하 Paper 플러그인(청크 형식 대응, Java 8) 및 Ledge 클라이언트 모드 1.21.11 이하(화면 코드를 버전별로 재작성).
  3. Polixit 1.20.6 이하 Paper 플러그인(옛 세대 어댑터 + 업스트림 archive 브랜치에 터레인 커밋 이식) 및 Polixit Fabric 26.2 이하(core-mc 별도 이식).
  4. 1.7.10·1.5.2 모드(Gesturing 1.7.10은 완료; Maple 1.7.10 Fabric, 1.5.2 Forge 전부)는 별도 지시 전까지 보류.
- 현재 제공 범위는 위 표와 릴리스 목록이 최종본. 추가 작업 지시 전까지 새 이식은 하지 않음.

## 2026-11-10 — 방침 수정(사용자 지시, 위 "방침 확정"보다 우선)

- **pass(안 함)**: 실제 클라이언트 검증(26.3 외는 형식상 제공이므로 별도 테스트 안 함)만.
- **보류로 되돌림(나중에 할 일)**:
  1. 1.7.10·1.5.2 Bukkit 플러그인 전부(서버 jar 확보가 선행 조건: Spigot BuildTools는 1.8 미만 미지원, 공개 미러 없음).
  2. 1.7.10·1.5.2 모드(Maple 1.7.10 Fabric, 1.5.2 Forge 전부).
  3. Gesturing 플레이어 위 앉기(sit on player) 확인: 두 명의 플레이어 필요.
  4. 다중 접속 TPS 확인.
- **보류 전체 목록**: 1.13.2 Fabric(Gesturing·Maple), Ledge ≤1.16.5 Paper 및 클라이언트 모드 ≤1.21.11, Polixit ≤1.20.6 Paper 및 Fabric ≤26.2, 위 1~4.

## 2026-11-10 — 1.7.10·1.5.2 방침 재확정(사용자 지시, 위 항목보다 우선)

- **1.7.10, 1.5.2는 모드만 만든다(보류).** Bukkit 플러그인은 만들지 않음(서버 jar 없음).
- 보류 범위:
  - 1.7.10 모드: Maple(Legacy Fabric 1.7.10; Gesturing 1.7.10은 이미 완료). 막힌 점: 1.7.10 yarn에서 액자 좌표(`field_5328…`)·상호작용 액션(`class_2062`)이 미매핑, 서버 스레드 실행기 없음(틱 이벤트로 큐 직접 구현), Polixit·Ledge는 별도 대형 작업.
  - 1.5.2 모드: Gesturing·Maple (Forge 1.5.2 또는 Legacy Fabric 1.5.2+Mixin). Legacy Fabric 1.5.2는 yarn만 있고 API 없음, 패킷이 `PacketNN…` 형식이라 새로 작성.
- **보류 전체 목록(최종)**: 1.13.2 Fabric(Gesturing·Maple), Ledge ≤1.16.5 Paper 및 클라이언트 모드 ≤1.21.11, Polixit ≤1.20.6 Paper 및 Fabric ≤26.2, 1.7.10·1.5.2 모드, Gesturing 플레이어 위 앉기·다중 접속 TPS 확인.
- pass: 실제 클라이언트 검증(26.3 외)만.

## 2026-11-10 — 보류 6번(Gesturing 확인 2건) 결과
- 환경: Paper 1.21.11 + gesturing 1.21.11 브랜치 jar, mineflayer 봇(프로토콜 1.21.11까지 지원, 26.3/26.2는 미지원이라 26.3 직접 검증 불가). 봇 스크립트: $CLAUDE_JOB_DIR/tmp/bots/{test,t3}.js
- 다중 접속 TPS: 봇 4명 접속·이동·앉기 중 TPS 19.9~20.0 (1m/5m/15m) 유지, 예외/ERROR 없음.
- 플레이어 위 앉기: 봇이 다른 봇을 우클릭 → armor_stand 탑승 확인(서버 `execute on vehicle`), 대상 텔레포트 시 같은 좌표로 따라옴, 웅크리기로 해제 확인. 두 봇이 상호작용 거리 밖이면 앉기 안 됨(정상).
- 한계: 1.21.11에서만 검증. 26.3 및 다른 버전·실제 클라이언트는 미검증.

## 2026-11-10 밤 ~ 11-11 00:10 — 보류 작업 진행 (사용자 지시: 6번 → 1~5번 연속 진행, 릴리스는 마지막에 한꺼번에)
### 완료 (모두 커밋·push)
- **6번 Gesturing 확인**: 플레이어 위 앉기 — 26.3 실제 클라이언트 2개(테스트 전용 자동 조작 모드)로 앉기·걷는 중 따라가기·웅크려 내리기 확인, 1.21.11 봇으로도 확인. 다중 접속 TPS — 봇 4명 19.9~20.0.
- **1번 1.13.2 Fabric**: Gesturing `28b04f6`, Maple `c8388ab` (Mixin 훅, LFAPI 없음). 개발 서버 + 프로덕션 Legacy Fabric 서버(Java 8)에서 동작 확인.
- **Gesturing 버그 수정**(점검 중 발견):
  - Fabric 1.14.4/1.15.2/1.16.5: 엔티티 데이터 ID가 1.17+ 값으로 하드코딩 → /lay·/bellyflop·/legsup·/spin 실패. 게임에서 accessor를 읽도록 수정(`4f81c39`, `7135202`, `0661ce7`), setPose 이름 리플렉션도 시그니처 탐색으로.
  - Fabric 1.12.2: 이름 기반 리플렉션(배포 jar에서 실패) → 타입 탐색(`74bedbd`).
  - Paper 1.13.2~1.18.2: `/lay <방향>` setRotation 예외 → 텔레포트로(각 브랜치 커밋).
  - Paper 1.12.2: NMS vehicle 필드 이름("au") → 타입 탐색, NPC 누수 해결(`4e83b73`).
  - Paper 1.8.9~1.21.11 전 버전 봇 재점검 오류 0건(26.x는 봇 미지원).
- **Legacy Fabric 릴리스 jar 점검**: 서버에 없는 참조 검사기(ASM) — Gesturing 1.12.2/1.8.9/1.7.10, Maple 1.12.2/1.8.9 이상 없음.
- **2번 Ledge**:
  - Paper 1.17.1 버그 수정: 미로드 청크 투명(1.18 이전 리전 형식 미지원 → LegacyChunk 추가 `8bd0a1b`), 스폰 근처 투명(스냅샷 높이 한 칸 위 → 아래로 탐색).
  - Paper 이식: 1.16.5 `0f0eebe`(+`ffdd38c`), 1.15.2, 1.14.4 `6b22d4d`, 1.13.2 `4447881`, 1.12.2 `8120028`, 1.8.9 `fa07197`. 각 버전 서버에서 미로드/로드 청크 타일 렌더 확인.
  - 클라이언트 모드: 1.21.11 `d0ef226`, 1.20.6 `569591f`, 1.19.4 `9cd7666`, 1.18.2 `818c597`, 1.17.1 `faff0e9`. 각 버전 실제 클라이언트로 지도 화면 스크린샷 확인.
### 남은 일
- Ledge 클라이언트 1.16.5 이하(Java 8·구 렌더 API, 후순위).
- 3번 Polixit ≤1.20.6(업스트림 WorldEdit 7.3.10/7.2.x에 터레인 커밋 이식, 진행 시작) 및 Fabric ≤26.2.
- 4번 Maple 1.7.10, 5번 1.5.2 모드.
- 마지막: 모든 수정 jar로 릴리스 일괄 교체 + Fabric API 내장(jar-in-jar; Fabric Loader는 내장 불가).
- **추가(사용자 지시 11-11)**: ledge 이슈 #2 "Spread Square Map to Full Fill" — 클라이언트 지도 화면이 정사각형 틀(3x3 타일)인데 화면 전체를 채우도록 변경. 대상: 클라이언트가 있는 모든 브랜치(26.3, 26.2, 26.1.2, 1.21.11, 1.20.6, 1.19.4, 1.18.2, 1.17.1).

## 2026-11-11 00:35 — 진행 현황 정리 (작업 목록 9개 기준)
### 작업 목록
1. [완료] Gesturing 26.3 포즈 수정 검증(Paper+Fabric)
2. [완료] Maple 26.3 실행 테스트
3. [완료] Ledge 26.3 전체 테스트
4. [완료] 4개 저장소 브랜치 정리
5. [진행 중] 4개 프로젝트 구버전 이식 — 아래 "남은 일" 참고
6. [대기] 4개 프로젝트 최종 릴리스 일괄 교체(+ Fabric 모드에 Fabric API 내장)
7. [완료] Gesturing 플레이어 위 앉기(걷는 중 따라가기 포함, 26.3 실제 클라이언트) + 다중 접속 TPS
8. [진행 중] 보류 1~5: 1번(1.13.2 Fabric) 완료, 2번(Ledge 구버전) Paper 1.8.9까지·클라이언트 1.17.1까지 완료, 3~5번 남음
9. [거의 완료] Ledge 이슈 #2(전체 화면 지도) — 26.3만 적용(다른 버전은 수정하지 않기로 함)

### 오늘(11-11 00시대) Ledge 26.3 변경
- 커밋·push 완료: `5ff358c` 전체 화면 지도(반투명 버튼, 3D는 화면에 닿는 마름모 범위만 요청·그림, 캐시 256), 3D 부드러운 이동, 플레이어 얼굴 마커 + 마우스 옆 닉네임, +확대/−축소(버튼·휠·Ctrl 단축키), 월드 버튼 길이 축소. `cde8d09` 버튼·좌표를 왼쪽 위 한 줄로.
- **커밋 전(작업 트리에 있음)**: 좌표 박스를 글자 길이에 맞춤, 독립 닫기(×) 블록으로 메뉴 줄 전체 닫기 → `+`로 다시 열기, 버전 `0.1.0-alpha.5`, Fabric API 모듈 4개 jar-in-jar 내장(fabric-api 의존성 제거). Fabric API 없이 Loader+Ledge만으로 실행 확인됨.
- 사용자 지시 대기: "26.3 commit push release" → 위 변경 커밋·push 후 `ledge-0.1.0-alpha.5-fabric-paper-26.3` 프리릴리스 발행 예정.
- "불러오는 중" 회색 박스(화면 가장자리 미수신 타일 자리)는 사용자 지시로 그대로 둠.
- 이슈 #2는 커밋에 `Refs #2`만 연결, 닫지 않음(사용자 확인 후 닫기).

### 남은 일
- Ledge 26.3 위 변경 커밋·push·릴리스(alpha.5).
- Ledge 클라이언트 1.16.5 이하(Java 8·구 렌더 API, 후순위).
- 3번 Polixit ≤1.20.6(WorldEdit 7.3.10 / 7.2.x에 터레인 커밋 이식) 및 Fabric ≤26.2.
- 4번 Maple 1.7.10, 5번 1.5.2 모드.
- 최종: 수정된 jar로 Gesturing·Maple·Ledge 릴리스 일괄 교체 + Fabric 모드 전체에 Fabric API 내장(Fabric Loader는 내장 불가).
- 테스트 도구 위치: `$CLAUDE_JOB_DIR/tmp/{bots,ct263,ct121,ct120,ct119,ct118,ct117,prodfab,refcheck}`, 스크립트 `paper-bot.sh`, `ledge-test.sh`, `dev-fabric.sh`, `prod-fabric.sh`, `build-gesturing.sh`(더티 트리 거부).

## 2026-11-11 01:50 — 1번(최종 릴리스 일괄 교체) 완료
- Fabric 모드 전체(Mojang 매핑 1.14.4~26.3)에 Fabric API 모듈 jar-in-jar 내장, fabric-api 의존 제거 → Fabric Loader만 있으면 됨. 실제 서버(26.3·1.20.6·1.16.5·1.14.4, Fabric API 없음)에서 로딩 확인. Legacy Fabric 1.12.2/1.8.9는 여전히 Legacy Fabric API 필요(1.13.2·1.7.10은 원래 API 없음).
- RefCheck(서버에 없는 참조 검사기) 버그 수정 후 1.7.10~26.3 전체 재검사 → 실제 서버 버그 발견·수정·push:
  Maple 1.14.4/1.15.2(getLevelName, 시작 시 크래시), Maple ≤1.16.5(MaterialColor 색 계산), Maple 1.12.2/1.8.9(FilledMapItem.getMapState + 색), Gesturing 1.8.9(getArmorSlot). 수정 jar 모두 검사 통과, Maple 1.12.2 이미지 배치 개발 서버 확인.
- **Paper 서버 jar(libs/server-1.8.8.jar) 커밋 사고**: Maple 26.2·26.1.2, Gesturing 1.17.1(+26.3 기록)에 들어가 있던 것을 git filter-repo로 전체 기록에서 제거 후 모든 브랜치·태그 강제 push(사용자 지시). GitHub에 고아 커밋이 SHA로 남을 수 있음(완전 삭제는 GitHub 지원 요청 필요). libs/는 .gitignore.
- 릴리스 재생성(각 브랜치 최신 커밋, 태그·첨부 수 검증 완료): Gesturing 15개(`gesturing-0.3.2-fabric-paper-<mc>` 26.3~1.8.9, 1.13.2는 paper→fabric-paper로 교체, `-fabric-1.7.10`), Maple 14개(`maple-0.2.0-fabric-paper-<mc>` 26.3~1.8.9, 1.13.2 교체), Ledge 13개 새 프리릴리스(`ledge-0.1.0-alpha.5-fabric-paper-<mc>` 26.2~1.17.1, `-paper-<mc>` 1.16.5~1.8.9) + 26.3 alpha.5. Ledge alpha.4 릴리스는 그대로 둠.
- 남은 일(시간 순): Maple 1.7.10 모드 → Polixit Fabric ≤26.2 → Ledge 클라이언트 ≤1.16.5 → Polixit Paper 1.20.6~1.17.1 → Polixit Paper ≤1.16.5 → 1.5.2 모드.

---
# 2026-11-10 ~ 11-11 작업 요약 (세션 종료 시점 02:00)

## 끝낸 일
1. **Gesturing 확인(보류 6번)**: 플레이어 위 앉기 — 26.3 실제 클라이언트 2개로 앉기·걷는 중 따라가기·웅크려 내리기 확인(1.21.11 봇으로도 확인). 다중 접속 TPS — 봇 4명 19.9~20.0.
2. **1.13.2 Fabric 모드(보류 1번)**: Gesturing·Maple, Mixin 훅(LFAPI 없음). 프로덕션 Legacy Fabric 서버(Java 8) 확인.
3. **Gesturing 버그 수정**: Fabric 1.14.4~1.16.5 엔티티 데이터 ID, Fabric 1.12.2 이름 리플렉션, Paper 1.13.2~1.18.2 `/lay <방향>`, Paper 1.12.2 NPC 실패·누수, Fabric 1.8.9 getArmorSlot. Paper 1.8.9~1.21.11 봇 점검 오류 0건.
4. **Ledge(보류 2번 일부)**: Paper 1.17.1 미로드/스폰 타일 투명 버그 수정, Paper 1.16.5·1.15.2·1.14.4·1.13.2·1.12.2·1.8.9 이식, 클라이언트 모드 1.21.11·1.20.6·1.19.4·1.18.2·1.17.1 이식(각 버전 실제 클라이언트 스크린샷 확인).
5. **Ledge 이슈 #2(26.3만)**: 전체 화면 지도, 왼쪽 위 한 줄 툴바(맨 왼쪽 ×/+ 토글), 픽셀 아이콘(지구본·새로고침·돋보기·프린터·플레이어 머리), 좌표 X/Z만, 알림은 하단 안내 줄, 3D 부드러운 이동·화면에 닿는 타일만 요청, 플레이어 얼굴 마커+닉네임, +확대/−축소(확대 3단계 추가). 이슈는 `Refs #2`만 연결, 닫지 않음.
6. **최종 릴리스 일괄 교체**: Fabric API jar-in-jar 내장(1.14.4~26.3), 실제 서버 호환성 검사(RefCheck) 후 Maple·Gesturing 서버 전용 버그 수정, 릴리스 43개 재생성·검증(Gesturing 15, Maple 14, Ledge alpha.5 14).
7. **사고 처리**: 공개 저장소에 커밋됐던 Paper 서버 jar를 Maple·Gesturing 전체 기록에서 제거하고 강제 push.

## 남은 일 (예상 시간이 짧은 순, 어림값)
1. Maple 1.7.10 Fabric 모드 — 약 2~3시간 (1.7.10 yarn 미매핑, 서버 실행기 직접 구현)
2. Polixit Fabric 26.2·26.1.2·1.21.11 — 약 3~4시간 (core-mc가 26.3 전용 API)
3. Ledge 클라이언트 1.16.5 이하 — 약 4~5시간 (Java 8, 구 렌더·입력 API)
4. Polixit Paper 1.20.6~1.17.1 — 약 4~6시간 (WorldEdit 7.3.10 / 7.2.x에 터레인 기능 이식)
5. Polixit Paper 1.16.5 이하(1.13.2·1.12.2·1.8.9 포함) — 6시간+ (WorldEdit 6.x 구조 차이)
6. 1.5.2 모드(Gesturing·Maple) — 가늠 어려움 (Legacy Fabric 미지원, Forge 1.5.2 등 빌드 환경부터)
- Pass(하지 않기로 함): 26.3 외 버전의 실제 클라이언트 수동 검증.

## 알아둘 점
- Ledge 이슈 #2 변경은 26.3에만 적용(다른 버전은 수정하지 않기로 함). 이슈는 사용자가 확인 후 닫기.
- Ledge `alpha.4` 릴리스들은 지우지 않고 남겨 둠.
- 기록에서 지운 서버 jar는 GitHub에 고아 커밋(SHA 직접 접근)으로 잠시 남을 수 있음 → 완전 삭제는 GitHub 지원 요청.
- Legacy Fabric 1.12.2·1.8.9 모드는 Legacy Fabric API 별도 설치 필요(1.13.2·1.7.10은 API 불필요).
- 테스트 도구(작업 임시 폴더 `$CLAUDE_JOB_DIR/tmp`): `bots/`(mineflayer), `ct263`·`ct117~ct121`(클라이언트 자동 테스트 모드), `prodfab/`(프로덕션 Fabric 서버), `refcheck/RefCheck`(서버에 없는 참조 검사), `fapi/bundle.py`(Fabric API 내장), 스크립트 `paper-bot.sh`, `ledge-test.sh`, `dev-fabric.sh`, `prod-fabric2.sh`, `release-all.sh`. 세션 정리 시 사라질 수 있음.
- 모든 저장소: 커밋하지 않은 변경 없음, push 안 된 커밋 없음, 테스트 서버 모두 종료.

- 2026-11-11 02:05: 사용자 지시로 작업 임시 폴더(`$CLAUDE_JOB_DIR/tmp`, 24GB)와 각 저장소의 `.gradle`·`fabric/.gradle`·`fabric/run`·`build` 삭제. 위에 적은 테스트 도구·스크립트·구버전 JDK·서버 jar는 더 이상 없음(다음 작업 때 다시 준비). 저장소 `libs/server-1.8.8.jar`(gitignore)는 유지.

## 2026-11-12 (새벽) 남은 작업 1~4 진행 결과

- **Maple 1.7.10 Fabric**: 새 브랜치 `1.7.10`(`90bd6c9`), 릴리스 `maple-0.2.0-fabric-1.7.10`. dev 서버 기동·`/image load` 확인.
- **Polixit Fabric 26.2·26.1.2·1.21.11**: 업스트림 26.2/26.3 core-mc 변경을 되돌려 이식. 1.21.11은 fabric-loom-remap + Mojang 매핑, CUI common jar를 리맵 가능하게 복사.
  Fabric jar에 `"minecraft": "<버전>"` 조건 추가(26.3 포함). 1.21.11·26.1.2는 실제 Fabric 서버 + 봇으로 //pos1·//pos2·//terrain info·//gen·//undo 확인, 26.2는 서버 기동만.
  릴리스 `polixit-0.2.0-fabric-paper-{26.3,26.2,26.1.2,1.21.11}`로 재발행.
- **Ledge 클라이언트 1.16.5·1.15.2·1.14.4·1.13.2·1.12.2·1.8.9**: 각 브랜치 `fabric/`. 1.15 이하 GuiGraphics shim, 1.12/1.8.9는 LWJGL2 호환 계층(ModernScreen/Glfw/NativeImage) + Legacy Fabric API, 1.13.2는 믹신(커스텀 페이로드·틱·minecraft:register).
  확인: 1.16.5·1.15.2·1.14.4·1.12.2 모드 로드, 1.13.2·1.8.9 빌드만. 실제 지도 화면은 미확인. 릴리스 `ledge-0.1.0-alpha.5-fabric-paper-<버전>`.
- **Polixit Paper 1.20.6·1.19.4·1.18.2·1.17.1**: 새 브랜치. 1.20.6은 WorldEdit 7.3.10, 나머지는 7.2.20 기반 + Polixit 지형 기능. 각 Paper 서버에서 봇으로 지형 명령 전부 확인.
  릴리스 `polixit-0.2.0-paper-<버전>`. `gradle/gradle-daemon-jvm.properties`(toolchainVersion=21) + Gradle 8.10.2로 기본 Java 27에서도 `./gradlew` 그대로 빌드됨.
- **남은 일(사용자가 "continue"라고 하면 진행)**: 5. Polixit Paper ≤1.16.5, 6. 1.5.2 모드(Gesturing·Maple).

## 2026-11-13 남은 작업 5·6 결과

- **Polixit Paper 1.16.5·1.15.2·1.14.4·1.13.2**: WorldEdit 7.2.20 + 업스트림 레거시 어댑터, Java 8(Jabel, Kotlin JVM 1.8), Guava·SnakeYAML·Kotlin 런타임 번들/재배치. 네 버전 모두 Java 8 Paper 서버에서 봇으로 //pos1·//pos2·//terrain info·//gen 80·//undo·//redo 확인. 브랜치 4개(같은 커밋 `9dc74a085`), 릴리스 `polixit-0.2.0-paper-<버전>`.
- **Polixit Paper 1.12.2·1.8.9(Paper 1.8.8)**: WorldEdit 6.1.9(upstream legacy/1.12.2)를 Gradle 8.10으로 재구성 + 지형 기능 이식(1.13 이전 블록 = 재질+데이터 계층, 단순 식생, 바닐라 샘플링 없음). 두 서버에서 같은 봇 테스트 통과. 브랜치 `1.12.2`·`1.8.9`(`f63c4958e`), 릴리스 동일 형식.
- **Gesturing 1.5.2**: Legacy Fabric 1.5.2(API 없음 → 믹신). /sit·/stand만(/lay 불가). 개발 서버에서 로드·명령 등록 확인, 플레이어 테스트 없음(1.5.2 테스트 클라이언트 없음). 브랜치 `1.5.2`, 릴리스 `gesturing-0.3.2-fabric-1.5.2`.
- **Maple 1.5.2**: 같은 방식, Gson jar-in-jar. 개발 서버에서 /image load·reload 확인, 액자 배치는 플레이어 미검증. 브랜치 `1.5.2`, 릴리스 `maple-0.2.0-fabric-1.5.2`.
- 보류 목록의 작업 1~6 모두 끝남.
