<h1>
    <img src="worldedit-logo.svg" alt="Polixit" width="400" />
</h1>

**Polixit combines WorldEdit's in-game map editing with Motopoly-style terrain generation.**

Polixit supports **GLAP1 / Paper 26.3** and **Fabric 26.3** with Java 25. Install the Paper plugin jar in the server's `plugins` folder, or install the Fabric mod jar in the `mods` folder. Remove any separate WorldEdit jar; Polixit replaces it on either platform.

Fabric distribution: `worldedit-fabric/build/libs/polixit-fabric-mc26.3-0.2.0-dist.jar`. The Fabric mod id is `polixit`; existing WorldEdit Fabric installations should remove the old jar. Fabric terrain generation uses captured native surface heights and procedural shaping, so it does not include Paper's vanilla terrain sampler.

### Terrain commands

* `//gen [y]` generates terrain in the current WorldEdit cuboid selection. Omit `y` to use the selection's vertical range, or provide a height for a flat target.
* `//terrain info` analyzes the terrain surrounding the selection.
* `//terrain config` shows your terrain settings. Use `//terrain config <distance|round|rough|step> [value]` to change a setting; omit the value to restore the server default. Player settings are saved per UUID.
* Formula-based generation remains available as `//generate` and `//g`.

`//pos1 [x] [y] [z]` and `//pos2 [x] [y] [z]` set selection points from explicit coordinates; omit all coordinates to use the player's current position. WorldEdit's wand, selections, undo, and redo continue to work as usual.

The integrated Motopoly terrain code retains its MIT notice in `worldedit-bukkit/src/main/resources/MOTOPOLY-LICENSE.txt`. WorldEdit remains licensed under GPLv3.

This repository contains the Polixit source code. Follow [COMPILING.md](COMPILING.md) to build it. Release artifacts: `worldedit-bukkit/build/libs/polixit-0.2.0-dist.jar` and `worldedit-fabric/build/libs/polixit-fabric-mc26.3-0.2.0-dist.jar`.

### 검증 결과 (2026-11-08)

실제 Fabric 26.3 클라이언트(offline, `PolixitTester`)를 격리 테스트 서버(`worldedit-fabric/run`, 포트 25566 / Paper 26.3 build 218, 포트 25570)에 접속시켜 채팅창에서 직접 명령을 입력해 확인했습니다. 블록 변화는 `clone ... filtered minecraft:air`로 선택 영역의 공기 블록 수를 세어 판정했습니다.

| 항목 | Fabric 26.3 | Paper 26.3 |
|---|---|---|
| `//pos1`, `//pos2` 좌표 인자 | 통과 | 통과 |
| `//terrain info` | 통과 | 통과 |
| `//gen 80` (3000블록 이상, 여러 틱) | 통과: 3680블록 변경 | 통과: 6761블록 변경 |
| `//undo` 원본 복원 | 통과: 공기 17개 → 601개(원본) | 통과: 공기 0개 → 1302개(원본) |
| `//redo` 재적용 | 통과: 601개 → 17개 | 통과: 1302개 → 0개 |
| 서버 로그 오류 | 없음(클라이언트 인증 401 경고만 있음) | 없음 |

- `//terrain config`의 설정 변경 명령은 이번 검증 범위에 포함하지 않았습니다.
- 선택 영역 안에 플레이어가 있으면 생성된 블록에 끼어 사망합니다. 첫 Fabric 시도에서 확인했습니다. 생성 도중 플레이어를 안전한 위치로 옮기는 처리는 추가하지 않았습니다.
- 다른 플레이어가 동시에 접속한 상태와 Paper 1.x 이전 버전은 확인하지 않았습니다.

---

The upstream WorldEdit project is **a Minecraft map editor that runs in-game**:

* With selections, schematics, copy and paste, brushes, and scripting!
* Use it in creative, survival in single player or on your server.
* Use it on your Minecraft server to fix griefing and mistakes.

Java Edition required. WorldEdit is compatible with NeoForge, Fabric, Bukkit, Spigot, Paper, and Sponge.

## Download WorldEdit

This place contains the Java code for WorldEdit, but if you want to just use WorldEdit, get the mod or plugin from Modrinth:

https://modrinth.com/plugin/worldedit/versions

Edit the Code
---------

Want to add new features to WorldEdit or fix bugs yourself? You can get the game running, with WorldEdit, from the code here, without any additional outside steps, by doing the following *four* things:

1. Download WorldEdit's source code and put it somewhere. We recommend you use something called Git if you already know how to use it, but [you can also just download a .zip file](https://github.com/EngineHub/WorldEdit/archive/master.zip). (If you plan on contributing the changes, you will need to figure out Git.)
2. Install any version of Java greater than or equal to 21.
   * Note that if you do _not_ install JDK 21 exactly, Gradle will download it for you on first run. However, it is still required to have some form of Java installed for Gradle to start at all.
3. Open terminal / command prompt / bash and navigate to the directory where you put the source code.
4. Run **one** of these following commands:
   * Mac OS X / Linux: `./gradlew :worldedit-fabric:runClient`
   * Windows - Command Prompt: `gradlew :worldedit-fabric:runClient`
   * Windows - PowerShell: `.\gradlew :worldedit-fabric:runClient`

🎉 That's it. 🎉 It takes a long time to actually transform WorldEdit into a mod. If it succeeds, **the Minecraft game will open and you can create a single player world with WorldEdit**.

When you make changes to the code, you have to restart the game by re-running the command for your changes to take effect. If there are errors in your Java syntax, the command will fail.

For additional information about compiling WorldEdit, see [COMPILING.md](COMPILING.md).

### Using a Java IDE

To edit WorldEdit in a Java IDE, follow these steps:

1. Download and install [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/).
2. In the IDE, open the folder that you saved WorldEdit's code in. This creates a new project in IDEA.

That's pretty much it.

If you want to be able to run the game also, follow these instructions:

1. Go to Run -> Edit Configurations.
2. Add a Gradle task:
   1. Choose `worldedit-fabric` for the project.
   2. For the tasks, type in `runClient`
3. Click OK
4. Under the Run menu again, go to "Debug [your new task]".

### Speeding up the Edit-Test-Edit-Test Cycle

It's a little annoying have to restart the game to test your changes. The best way to reduce the time is to run the server instead (using `runServer` instead of `runClient`) and then reconnect to the server after restarting it.

Submitting Your Changes
------------

WorldEdit is open source (specifically licensed under GPL v3), so note that your contributions will also be open source. The best way to submit a change is to create a fork on GitHub, put your changes there, and then create a "pull request" on our WorldEdit repository.

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for important guidelines to follow.

Links
-----

* [Visit our website](https://enginehub.org/)
* [Discord](https://discord.gg/enginehub)
* [Issue tracker](https://github.com/EngineHub/WorldEdit/issues)
* [Continuous integration](https://builds.enginehub.org) [![Build Status](https://ci.enginehub.org/app/rest/builds/buildType:bt10,branch:master/statusIcon.svg)](https://ci.enginehub.org/viewType.html?buildTypeId=bt10&guest=1)
* [End-user documentation](https://worldedit.enginehub.org/en/latest/)

Supporters
----------

[![YourKit Logo](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com/)

YourKit supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.
YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/),
[YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/),
and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).
