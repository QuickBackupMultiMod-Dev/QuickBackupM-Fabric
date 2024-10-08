[![License](https://img.shields.io/github/license/SkyDynamic/QuickBackupM-Fabric.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Issues](https://img.shields.io/github/issues/QuickBackupMultiMod-Dev/QuickBackupM-Fabric.svg)](https://github.com/QuickBackupMultiMod-Dev/QuickBackupM-Fabric/issues)
[![Modrinth](https://img.shields.io/modrinth/dt/DgWBIBY5?label=Modrinth%20Downloads)](https://modrinth.com/mod/quickbackupmulti)
[![CurseForge](https://img.shields.io/curseforge/dt/951047?label=CurseForge%20Downloads)](https://www.curseforge.com/minecraft/mc-mods/quickbackupmulti)
[![Github Release](https://img.shields.io/github/downloads/QuickBackupMultiMod-Dev/QuickBackupM-Fabric/total?label=Github%20Downloads)](https://github.com/QuickBackupMultiMod-Dev/QuickBackupM-Fabric/releases)

<div align="center">
<a><img src="./indexImg.png" width="180" height="180" alt="Logo"></a>
</div>
<div align="center">

# QuickBackupMulti-Fabric

[简体中文](README.MD) | **English**

_✨ Minecraft Backup / Restore Mod ✨_  
Idea come from: [QuickBackupMulti](https://github.com/TISUnion/QuickBackupM)

</div>

> [!WARNING]  
> Now the Mod version is `v2`, and there are great differences backup implementation method compared to the `v1` version
>
> If you use `v1` now, please don't to upgrade to `v2`. Also, if you use `v2` now, please don't downgrade to `v1`

[//]: # (> This mod has poor support in SinglePlay. So use it with great caution. If your save break, This mod will not be responsible for you.)

## Advantage
- Support auto start server after restore backup
- Support schedule backup and custom cron expression.

## How to use
> [!WARNING]  
> It is strictly prohibited to delete all backup files in the backup folder by oneself.
> If you need to delete them, please enter the game and use the command to delete them!

> Please install Fabric Loader before install this mod

Go to [release](https://github.com/SkyDynamic/QuickBackupM-Fabric/releases) to download the latest mod and put the `.jar` file in `mods` dir

## Command
`/qb` or `/quickbackupmulti` can trigger the mod

## todo
- [x] Schedule Backup
- [x] Infinite Slot
- [x] Compare files hash and save what files had change
- [ ] Personal Setting

## LICENSE
This Project follow [Apache-2.0 license](https://www.apache.org/licenses/LICENSE-2.0)
