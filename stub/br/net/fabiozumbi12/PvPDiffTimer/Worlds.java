// 编译桩：PvPDiffTimer 上游源已不可获取（github 404），服务器亦未安装该插件。
// 仅满足 PvPDiffTimerHook 编译期符号解析；不打入产物（运行期 hook 永不启用）。
package br.net.fabiozumbi12.PvPDiffTimer;

import org.bukkit.World;

public class Worlds {
    public static boolean isEnabled(World world) {
        return false;
    }

    public static boolean isNight(World world) {
        return false;
    }
}
