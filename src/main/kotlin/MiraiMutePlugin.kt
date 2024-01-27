package io.github.ice_forever

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.info
import java.util.*

object MiraiMutePlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.ice_forever.MiraiMutePlugin",
        name = "简单的复读禁言插件",
        version = "1.1.4",
    ) {
        author("github.com/ice-forever")
        info(
            """
            forked from [JOYACEpoor/miraimuteplugin](https://github.com/JOYACEpoor/miraimuteplugin)
            本插件仓库链接：github.com/ice-forever/MiraiMutePlugin
        """.trimIndent()
        )
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        MiraiMutePluginData.reload()
        MiraiMutePluginConfig.reload()

        if (MiraiMutePluginConfig.clearTime > -1 && MiraiMutePluginConfig.clearTime < 24) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR, MiraiMutePluginConfig.clearTime)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    MiraiMutePluginData.mutableMap.clear()
                }
            }, Date(calendar.timeInMillis), 86400000)
        }

        for (_groupId in MiraiMutePluginConfig.groupList) {
            var num = 1
            var preMessage = ""
            var preSenderId: Long = 0
            MiraiMutePlugin.globalEventChannel().filter { it is GroupMessageEvent && it.group.id == _groupId }
                .subscribeAlways<GroupMessageEvent> {
                    if (group.botPermission > MemberPermission.MEMBER) {
                        if (sender.id == preSenderId && message.serializeToMiraiCode() == preMessage) {
                            num++
                            if (num >= MiraiMutePluginConfig.frequency) {
                                if (group.botPermission > sender.permission) {
                                    if (MiraiMutePluginData.mutableMap.containsKey(sender.id)) {
                                        if (MiraiMutePluginData.mutableMap.getValue(sender.id) * MiraiMutePluginConfig.timer > MiraiMutePluginConfig.max) {
                                            MiraiMutePluginData.mutableMap[sender.id] = MiraiMutePluginConfig.max
                                        } else {
                                            MiraiMutePluginData.mutableMap[sender.id] =
                                                MiraiMutePluginData.mutableMap.getValue(sender.id) * MiraiMutePluginConfig.timer
                                        }
                                    } else {
                                        MiraiMutePluginData.mutableMap[sender.id] = MiraiMutePluginConfig.min
                                    }
                                    sender.mute(MiraiMutePluginData.mutableMap.getValue(sender.id))
                                }
                                num = 0
                            }
                        } else {
                            preSenderId = sender.id
                            preMessage = message.serializeToMiraiCode()
                            num = 1
                        }
                    }
                }
        }
    }
}