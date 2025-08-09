package cn.xor7.xiaohei.icu.utils

import cn.xor7.xiaohei.icu.plugin
import io.github.lumine1909.recorderapi.api.RecorderAPI
import io.github.lumine1909.recorderapi.api.recorder.Recorder
import io.github.lumine1909.recorderapi.api.recorder.RecorderOption
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import java.util.UUID.randomUUID

val photographers = mutableSetOf<String>()
val photographer2PlayerMap = mutableMapOf<Recorder, Player>()
val player2PhotographersMap = mutableMapOf<UUID, MutableSet<Recorder>>()
val allPhotographers: Collection<Recorder>
    get() = photographers.map { RecorderAPI.MANAGER.getRecorder(it) }

fun getPhotographer(id: String): Recorder? = RecorderAPI.MANAGER.getRecorder(id)

fun createPhotographer(
    id: String,
    player: Player,
    file: File,
    recorderOption: RecorderOption = RecorderOption()
): Recorder? = RecorderAPI.MANAGER.createVirtualRecorder(id, player, file, recorderOption).also {
    it?.let {
        photographers.add(it.name)
        photographer2PlayerMap[it] = player
        player2PhotographersMap.computeIfAbsent(player.uniqueId) {mutableSetOf()}.add(it)
    }
}

fun Recorder.removePhotographer(save: Boolean = true) {
    if (photographer2PlayerMap[this] == null) {
        return
    }
    try {
        this.stopRecording(save)
        this.remove()
    } catch (e: Exception) {
        plugin.logger.warning("Err while removing photographer ${this.name}: ${e.message}")
    }
    photographers.remove(this.name)
    val player = photographer2PlayerMap[this]
    photographer2PlayerMap.remove(this)
    player2PhotographersMap[player?.uniqueId]?.removeIf { it == this }
}

fun removeAllPhotographers() = photographer2PlayerMap.forEach {it.key.removePhotographer(false)}

fun Player.getRecordPhotographer(): MutableSet<Recorder>? {
    return player2PhotographersMap[this.uniqueId]
}

fun Player.getPhotographerName(type: String = ""): String {
    val trimmedPlayerName = this.name
        .replace(".", "_")
        .replace("-", "_")
    val leftPart = (module.photographerPrefix + trimmedPlayerName).run {
        if (length > 6) substring(0, 6) else this
    }
    val rightPart = type + randomUUID().toString().replace("-", "")
    return "${leftPart}_${rightPart}".run {
        if (length > 16) substring(0, 16) else this
    }
}