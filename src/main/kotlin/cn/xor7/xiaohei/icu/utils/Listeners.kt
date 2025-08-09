package cn.xor7.xiaohei.icu.utils

import cn.xor7.xiaohei.icu.listeners.InstantReplayListener
import cn.xor7.xiaohei.icu.listeners.SimpleRecordListener
import cn.xor7.xiaohei.icu.listeners.anticheat.*
import cn.xor7.xiaohei.icu.plugin
import org.bukkit.event.Listener
import kotlin.reflect.KClass

private val listeners = mutableMapOf<KClass<out Listener>, Any>()

@Suppress("UNCHECKED_CAST")
fun <T : Listener> registerListener(listener: T) {
    val type = listener::class as KClass<T>
    listeners[type] = listener
    plugin.server.pluginManager.registerEvents(listener, plugin)
}

fun <T : Listener> registerListenerIfAbsent(listener: T) {
    val type = listener::class as KClass<*>
    if (listeners.containsKey(type)) return
    registerListener(listener)
}

fun registerOrUpdateListeners() {
    registerListenerIfAbsent(SimpleRecordListener())

    if (module.instantReplay.enable) registerListenerIfAbsent(InstantReplayListener())

    module.recordSuspicious.apply {
        if (enable) registerListenerIfAbsent(AntiCheatListener())
        else return

        if (grim) registerListenerIfAbsent(GrimACListener())
        if (lightAntiCheat) registerListenerIfAbsent(LightAntiCheatListener())
        if (matrix) registerListenerIfAbsent(MatrixListener())
        if (negativity) registerListenerIfAbsent(NegativityListener())
        if (spartan) registerListenerIfAbsent(SpartanListener())
        if (themis) registerListenerIfAbsent(ThemisListener())
        if (vulcan) registerListenerIfAbsent(VulcanListener())
    }
}