<p align="center">
  <img src="assets/death-roulette-banner.png" alt="Death Roulette Banner">
</p>

# Death Roulette

<p align="center">
  <a href="https://github.com/IndoGeek/death-roulette/actions">
    <img src="https://img.shields.io/github/actions/workflow/status/IndoGeek/death-roulette/build.yml?style=flat-square&label=build" alt="Build">
  </a>
  <a href="https://github.com/IndoGeek/death-roulette/releases">
    <img src="https://img.shields.io/github/downloads/IndoGeek/death-roulette/total?style=flat-square&label=downloads" alt="Downloads">
  </a>
  <a href="https://github.com/IndoGeek/death-roulette/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/IndoGeek/death-roulette?style=flat-square" alt="License">
  </a>
  <img src="https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=flat-square&logo=minecraft&logoColor=white" alt="Minecraft">
  <img src="https://img.shields.io/badge/Fabric-Loader-DBD0B4?style=flat-square&logo=fabric&logoColor=black" alt="Fabric">
</p>

<p align="center">
  <a href="https://github.com/IndoGeek/death-roulette/stargazers">
    <img src="https://img.shields.io/github/stars/IndoGeek/death-roulette?style=flat-square" alt="Stars">
  </a>
  <a href="https://github.com/IndoGeek/death-roulette/network/members">
    <img src="https://img.shields.io/github/forks/IndoGeek/death-roulette?style=flat-square" alt="Forks">
  </a>
</p>

**Features**

*  Automatic roulette rounds based on Minecraft days
*  Random player or mob selection
*  Configurable chance of selecting a player
*  Separate control over passive and hostile mobs
*  Configurable mob search radius
*  Optional countdown and result sounds
*  Optional titles, action bar messages, and particles
*  Simple .properties configuration
*  Reload configuration without restarting the server
*  Built-in commands for starting, stopping, checking, and testing roulette rounds

**Commands**

The main command is:
```
/roulette
```
Available subcommands:
```
/roulette start
/roulette stop
/roulette status
/roulette reload
/roulette test <days>
```
By default, roulette commands require operator permissions.

**Configuration**

After the first launch, the mod creates:

config/deathroulette.properties

The configuration controls things such as:

* How often roulette runs
* Player selection chance
* Mob search radius
* Passive/hostile mob selection
* Titles and action bar messages
* Particles
* Roulette and death sounds
* Command permissions

After changing the configuration, use:
```
/roulette reload
```
to apply it.

**Installation**

Death Roulette is built for Minecraft 1.20.1 with Fabric.

1. Install Fabric Loader and Fabric API.
2. Download the Death Roulette .jar.
3. Put it in the server’s mods folder.
4. Start the server.

The mod is primarily intended for server-side use.

**Development**

This project is built with Gradle and uses the Fabric toolchain.

Clone the repository and run:
```
./gradlew build
```
The compiled mod will be placed in:
```
build/libs/
```
**License**

Death Roulette is released under the MIT License.

⸻

Made by IndoGeek.
