# AADisplay

[![AADisplay](https://img.shields.io/badge/AADisplay-Project-blue?logo=github)](https://github.com/Nitsuya/AADisplay)
[![GitHub Release](https://img.shields.io/github/v/release/Xposed-Modules-Repo/io.github.nitsuya.aa.display)](https://github.com/Xposed-Modules-Repo/io.github.nitsuya.aa.display/releases)
![Xposed Module](https://img.shields.io/badge/Xposed-Module-blue)
[![License](https://img.shields.io/github/license/nitsuya/AADisplay)](https://github.com/nitsuya/AADisplay/blob/main/LICENSE)
![Android SDK min 31](https://img.shields.io/badge/Android%20SDK-%3E%3D%2031-brightgreen?logo=android)
![Android SDK target 33](https://img.shields.io/badge/Android%20SDK-target%2033-brightgreen?logo=android)

让Android Auto显示任意应用, 基于VirtualDisplay的套娃方案, Star! Star! Star! 

仅支持A12+, 需LSPosed, 部分ROM可能存在崩的可能性, 请自备救机的常识.

理论可以支持到A10+, 由于没有设备和过多精力维护, 所以没有支持, 如有能力自行PR.

与本项目无关的问题请勿提交Issue, 必关! 例如: Magisk/LSPosed环境安装, AA无法连接/使用 等等相关的问题.

-----

## 使用方法
- LSPosed开启本模块,勾选System Framework和Android Auto,安装你喜爱的启动器,并在模块设置中填写包名.
- 建议设置Dpi,会对使用应用上下文绘制UI的叼毛应用得到改善.
- 添加Properties AA相关参数,可对AA的配置进行Hook修改,仅支持基础类型参数.
- 自动绕过Android Auto对AA应用本身安装来源的检测.
- Root权限仅对用户配置的Shell命令执行使用,若无需求可不给.

## 免责声明
- 使用本模块即代表自愿承担一切后果, 包括但不限于 设备损坏, 驾车事故.
- 任何由本项目衍生出的项目, 本项目不承担任何责任.
- 本项目保证永久开源, 欢迎提交 Issue 或者 PR, 但请不要提交用于非法用途的功能.
- 开发者可能在任何时间**停止更新**或**删除项目**

## Thanks

### 库

[AOSP](https://source.android.com/)

[YAFM](https://github.com/duzhaokun123/YAFM)

[DexKit](https://github.com/LuckyPray/DexKit)

[Hide-My-Applist](https://github.com/Dr-TSNG/Hide-My-Applist)

[HiddenApi](https://github.com/RikkaW/HiddenApi)

[LSPosed](https://github.com/LSPosed/LSPosed)

[xposed](https://forum.xda-developers.com/xposed)

[Material](https://material.io/)

[QAuxiliary](https://github.com/cinit/QAuxiliary)

[ViewBindingUtil](https://github.com/matsudamper/ViewBindingUtil)

