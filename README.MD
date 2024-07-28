[![License](https://img.shields.io/github/license/SkyDynamic/QuickBackupM-Fabric.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Issues](https://img.shields.io/github/issues/SkyDynamic/QuickBackupM-Fabric.svg)](https://github.com/SkyDynamic/QuickBackupM-Fabric/issues)
[![Modrinth](https://img.shields.io/modrinth/dt/DgWBIBY5?label=Modrinth%20Downloads)](https://modrinth.com/mod/quickbackupmulti)

<div align="center">
<a><img src="./indexImg.png" width="180" height="180" alt="Logo"></a>
</div>
<div align="center">

# QuickBackupMulti-Fabric

**简体中文** | [English](README_EN.MD)

_✨ MC备份 / 回档模组 ✨_  
重构自MCDR插件: [QuickBackupMulti](https://github.com/TISUnion/QuickBackupM)

</div>

> [!WARNING]  
> 当前Mod大版本为`v2`, 与`v1`的实现代码有很大差异  
> 
> 如果使用的是`v1`版本, 请勿直接更新到`v2`. 使用`v2`的也请勿随意降级到`v1`

[//]: # (> 本 Mod 对于客户端单人游戏兼容性较差，使用时请谨慎，若造成存档损坏本 Mod 不负任何责任)

## 本Mod优势
- 支持回档自动重启服务器, 不再是只备份不回档
- 支持定时备份, 并支持自定义cron表达式

## 使用方式
> [!WARNING]  
> 严禁自行删除备份文件夹内的所有备份文件, 如需删除请进入游戏内进行手动删除! 

> 在使用mod前请确保你已安装Fabric Loader

将本mod放进`mods`文件夹即可

## 指令
`/qb` 或 `/quickbackupmulti`均可触发mod

## todo
- [x] 定时备份
- [x] 无限槽位
- [x] Hash对比并仅备份差异文件
- [ ] 个性化设置

## 许可
本项目遵循 [Apache-2.0 license](https://www.apache.org/licenses/LICENSE-2.0) 许可
