package cn.xor7.xiaohei.icu.listeners

import cn.xor7.xiaohei.icu.utils.*
import io.github.lumine1909.recorderapi.api.recorder.Recorder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.io.File

class SimpleRecordListener : Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (!module.shouldRecordPlayer(player)) return

        val photographer = createSimplePhotographer(player, createRecordFile(module.path, player))
        photographer.startRecording()
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val photographer = player.getRecordPhotographer() ?: return
        photographer.forEach { it.removePhotographer() }
    }

    private fun createSimplePhotographer(player: Player, file: File): Recorder =
        createPhotographer(
            player.getPhotographerName(),
            player,
            file,
        ) ?: throw RuntimeException(
            "Error when create photographer for player: {name:${player.name},UUID:${player.uniqueId}}",
        )
}