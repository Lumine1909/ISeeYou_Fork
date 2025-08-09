package cn.xor7.xiaohei.icu.listeners.anticheat

import cn.xor7.xiaohei.icu.plugin
import cn.xor7.xiaohei.icu.utils.*
import io.github.lumine1909.recorderapi.api.recorder.Recorder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.io.File
import java.util.*

lateinit var antiCheatListener: AntiCheatListener

class AntiCheatListener : Listener {
    private val suspiciousPhotographers: MutableMap<UUID, SuspiciousPhotographer> = mutableMapOf()

    init {
        antiCheatListener = this
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(
            plugin,
            {
                if (!module.recordSuspicious.enable) return@runAtFixedRate
                val current = System.currentTimeMillis()
                val recordSuspiciousMills = module.recordSuspicious.lengthMillis
                suspiciousPhotographers.entries.removeIf {
                    if (current - it.value.lastTagged > recordSuspiciousMills) {
                        it.value.photographer.removePhotographer()
                        return@removeIf true
                    } else false
                }
            },
            1, 20 * 60,
        )
    }

    fun onAntiCheatAction(player: Player) {
        val suspiciousPhotographer = suspiciousPhotographers[player.uniqueId]
        if (suspiciousPhotographer == null) {
            val photographer =
                createAntiCheatPhotographer(player, createRecordFile(module.recordSuspicious.path, player))
            suspiciousPhotographers[player.uniqueId] = SuspiciousPhotographer(
                photographer = photographer,
                name = player.name,
                lastTagged = System.currentTimeMillis(),
            )
            photographer.startRecording()
        } else {
            suspiciousPhotographers[player.uniqueId] = suspiciousPhotographer.copy(
                lastTagged = System.currentTimeMillis(),
            )
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        suspiciousPhotographers.remove(e.player.uniqueId)?.photographer?.removePhotographer()
    }

    private fun createAntiCheatPhotographer(player: Player, file: File): Recorder =
        createPhotographer(
            player.getPhotographerName("sus_"),
            player,
            file,
        ) ?: throw RuntimeException(
            "Error when create photographer for suspicious player: {name:${player.name},UUID:${player.uniqueId}}",
        )
}

data class SuspiciousPhotographer(
    val photographer: Recorder,
    val name: String,
    val lastTagged: Long,
)