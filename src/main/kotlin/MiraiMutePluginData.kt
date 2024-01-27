package io.github.ice_forever

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object MiraiMutePluginData : AutoSavePluginData("MiraiMutePluginData") {
    var mutableMap: MutableMap<Long, Int> by value(mutableMapOf())
}