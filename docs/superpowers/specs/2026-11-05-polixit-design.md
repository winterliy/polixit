# Polixit 통합 WorldEdit 설계 초안

## 의도

GLAP1(Paper 26.3) 서버에서 WorldEdit의 선택·편집 기능과 Motopoly의 자연스러운 지형 생성을 단일 플러그인으로 제공한다. 별도 Motopoly 애드온 없이 WorldEdit Bukkit 구현 안에 지형 생성 기능을 통합한다.

## 목표

- WorldEdit 7.4.x Bukkit/Paper 구현을 기반으로 기존 WorldEdit 기능을 보존한다.
- Motopoly의 지형 분석·생성 알고리즘, 식생·광석 배치, 연결 지대, 개인 설정, info를 통합한다.
- WorldEdit의 기존 선택(//pos1, //pos2, wand)을 사용한다.
- 생성 결과는 WorldEdit EditSession 기록에 포함해 //undo와 //redo에 연결한다.
- Java + Gradle + Kotlin 환경을 구성한다. 서버 진입점과 Paper 내부 API 연동은 Java로 유지한다.
- Java 25, Paper 26.3 / GLAP1에서 실행되는 단일 Bukkit 플러그인 jar를 만든다.
- 소스와 릴리스를 winterliy/polixit에 공개한다.

## 비목표

- Fabric, NeoForge, Sponge, CLI 플랫폼에 지형 생성을 제공하지 않는다.
- 별도 Polixit/Motopoly 런타임 플러그인을 요구하지 않는다.
- Paper 26.3 외 서버 및 Java 25 미만 호환성을 약속하지 않는다.
- 수식 기반 WorldEdit 생성 기능을 제거하지 않는다.
- GLAP1 서버 구동기 자체를 수정하거나 jar를 자동 번들링하지 않는다.

## 제약 및 확인 사항

- 기반은 EngineHub/WorldEdit의 version/7.4.x이며 GPLv3이다. 이 브랜치에는 Bukkit용 26.3 adapter가 있다.
- GLAP1은 Paper 26.3, Java 25 이상이다. Motopoly 3.1.0은 MIT 라이선스, Paper 26.3/Java 25 대상이다.
- WorldEdit은 이미 //gen을 /generate의 별칭으로 사용한다.
- winterliy/polixit 저장소는 아직 없으므로, EngineHub/WorldEdit을 fork해 이름을 polixit으로 바꾸고 upstream 원격을 보존한다.
- Polixit 플러그인 식별자를 사용하며, 기존 WorldEdit jar를 제거하고 Polixit jar로 교체한다. 두 플러그인을 동시에 설치하지 않는다. 기존 WorldEdit 설정 폴더의 자동 이동은 첫 릴리스 범위 밖이다.

## 접근 방식

1. **WorldEdit 포크에 직접 통합 (선택)**: Bukkit 모듈에 Motopoly 생성 기능을 통합하고 WorldEdit의 선택·명령·기록 체계를 쓴다. 요청한 단일 플러그인과 기존 기능 보존을 충족한다.
2. **WorldEdit API 별도 애드온**: 원본 업데이트 추적은 쉽지만, 애드온이 아닌 통합본을 원한다는 요청과 맞지 않는다.
3. **WorldEdit 일부 기능을 재작성한 새 플러그인**: 초기 코드는 적지만 WorldEdit에 흡수되지 않고 기존 기능 호환성이 낮다.

## 명령과 선택

- //pos1, //pos2 및 WorldEdit wand를 그대로 사용한다. Motopoly 독립 선택 저장소와 황금 삽은 옮기지 않는다.
- //gen [y]는 Motopoly 방식의 지형 생성이다. Y 생략 시 선택 지점 Y 범위, 지정 시 해당 높이의 평지를 만든다.
- 기존 수식 생성은 //generate와 //g로 유지한다. //gen 축약 별칭은 지형 생성에 재할당한다. 이 호환성 변경을 도움말과 문서에 알린다.
- //terrain config 및 //terrain info에서 Motopoly 설정과 분석을 제공하고, 개인 설정을 유지한다.
- //undo와 //redo는 WorldEdit 기록을 사용한다. 별도 /mt 명령은 제공하지 않는다.

## 구현 구성

- EngineHub/WorldEdit 소스를 포크한 winterliy/polixit에 반영한다. GPLv3 라이선스와 저작권 고지를 보존한다. plugin.yml, 문서, 사용자 메시지를 Polixit으로 브랜딩한다.
- Motopoly MIT 코드를 Bukkit 전용 내부 terrain 패키지로 옮기고 원저작권 및 MIT 고지를 보존한다.
- Kotlin Gradle 플러그인은 Bukkit 모듈에 적용하고 Kotlin은 내부 설정과 작업 조율에 사용한다.
- Java 25 타깃은 배포 Bukkit 모듈과 필요한 26.3 adapter에 한정한다. 공통 멀티플랫폼 모듈은 불필요하게 올리지 않는다.
- terrain 계산은 비동기로 수행하고 월드 접근·블록 쓰기는 서버 스레드에서 제한량씩 적용한다.
- 대량 변경은 EditSession을 통해 적용한다. LocalSession에 보관되는 WorldEdit 기록은 Motopoly의 packed-position/palette 구조를 활용하도록 확장해 메모리 사용량을 제한한다. 작업 실패·취소 시 부분 변경을 복구한다.
- 버전 태그 기반 GitHub Release에 단일 플러그인 jar와 대응 소스를 제공하고 GPLv3 배포 조건을 따른다.

## 검증 기준

- 전체 Gradle 빌드와 WorldEdit 테스트, Motopoly TerrainGenerator/OreTable 회귀 테스트가 통과한다.
- Paper 26.3/GLAP1에서 플러그인 로드, 선택, //gen, //generate, 설정·분석, undo/redo, 취소·실패 처리를 확인한다.
- 배포 jar는 WorldEdit 또는 Motopoly 별도 플러그인을 요구하지 않으며, GPLv3 및 MIT 고지와 대응 소스 경로를 포함한다.
