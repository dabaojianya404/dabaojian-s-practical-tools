## 概述

这个模组为 MC 添加了一些实用的工具。

添加了清理树木工具、清理花草工具、光照更新工具、平整工具、混合工具、黑名单清理工具你可以在工具一栏找到。

在这个仓库中，你可以在alpha或beta分支中看到最新的测试版！

## 工具列表

- 清理树木工具：这个工具右键可以清除选定范围内的树木，手持工具 Shift+鼠标滚轮可以调节选定范围，最大 256x256，清除完毕后会自动更新光照，这个工具可以不用亲自跑到树上右键，最远可以隔着 64 个格子使用，这个工具可以清除一些模组的树，例如[枫](https://www.mcmod.cn/class/11807.html)；
- 清除花草工具：这个工具右键可以清除选定范围内的花草，手持工具 Shift+鼠标滚轮可以调节选定范围，最大 256x256，同样的，最远可以隔着 64 个格子使用；
- 平整工具：这个工具可以清除选中的方块面前的 128x128x50 的区域，然后再在选定的 y 轴基准高度上放置一层草方块，手持工具 Shift+鼠标滚轮可以调整放置的高度；
- 混合工具：包含树木清理工具和清除花草工具的所有功能；
- 光照更新工具：右键使用可更新16x16x16范围内的光照；
- 黑名单清理工具：对着任意方块shift+右键可以选定一个方块，按下右键对准这些方块可以破坏范围内选中的方块，对着天空按下shift+右键可以打开一个2*9物品栏，放物品栏放入方块后，右键会清理范围内的这些方块，手持工具 Shift+鼠标滚轮可以调节选定范围，最大 256x256。

## 其他

- 撤销：从1.1.2版本开始，默认f7撤销上一步操作。可在按键绑定中更改；
- 命令：自1.1.3版本开始，新增/dbjtools指令，用法:/dbjtools [玩家名称] undo  管理员可以用此指令撤销服务器内成员的操作。请注意，最多只能撤销最近10次操作；
- 注意： 自1.1.2版本开始，模组需要[fabric api](https://www.mcmod.cn/class/3124.html)作为前置模组；在清理有关[枫](https://www.mcmod.cn/class/11807.html)模组的内容时，可能会出现未被发现的问题。
# **Overview**

This module adds some practical tools to MC.

Added tree cleaning tools, flower and grass cleaning tools, lighting update tools, leveling tools, blending tools, and blacklist cleaning tools that you can find in the tools section.

In this repository, you can see the latest alpha or beta version in the alpha branch!

# **Tool List**

**Tree Cleaning Tool:** Right click on this tool to clear trees within the selected range. Hold the tool Shift+mouse wheel to adjust the selected range, up to 256x256. After cleaning, the lighting will be automatically updated. This tool can be used up to 64 grids away from the tree without having to right-click on it. It can also clear some mod trees, such as maple trees;

**Clear Flowers and Plants Tool:** Right click on this tool to clear flowers and plants within the selected range. Hold the tool Shift+mouse wheel to adjust the selected range, up to 256x256. Similarly, it can be used up to 64 grids apart;

**Flattening tool:** This tool can clear the 128x128x50 area in front of the selected block, and then place a layer of grass blocks on the selected y-axis reference height. Hold the tool Shift+mouse wheel to adjust the placement height;

**Mixed tools:** including all functions of tree cleaning tools and flower removal tools;

**Lighting update tool:** right-click to update lighting within the range of 16x16x16;

**Blacklist cleaning tool:** Shift and right-click on any block to select a block, press the right-click to align with these blocks to destroy the selected blocks within the range, and press Shift and right-click on the sky to open a 2 \* 9 item bar. After placing the block in the item bar, right-click to clear the blocks within the range. Hold the tool Shift and mouse wheel to adjust the selected range, up to 256x256.

# **Others**

**Revocation:** Starting from version 1.1.2, the default is to undo the previous operation in f7. Can be changed in key binding;

**Command:** Starting from version 1.1.3, the/dbjtools command has been added. Usage:/dbjtools [player name] undo. Administrators can use this command to undo operations by members within the server. Please note that only the last 10 operations can be revoked at most;

**Note:** Starting from version 1.1.2, the Fabric API is required as a pre module for mods; When cleaning up content related to maple mods, there may be undiscovered issues.
