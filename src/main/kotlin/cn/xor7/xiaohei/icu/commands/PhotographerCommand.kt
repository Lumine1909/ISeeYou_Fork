package cn.xor7.xiaohei.icu.commands

import cn.xor7.xiaohei.icu.utils.*
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player

fun registerPhotographerCommand() = commandTree("photographer") {
    literalArgument("create") {
        withPermission(perms.photographer.create)
        stringArgument("name") {
            playerArgument("player", optional = true) {
                anyExecutor { sender, args ->
                    val isPlayer = sender is Player
                    val name: String by args
                    val playerArg: Player? by args
                    val player = playerArg ?: run {
                        if (!isPlayer) {
                            sender.sendError("Must specify a player when use this command in console")
                            return@anyExecutor
                        }
                        return@run sender
                    }

                    if (name.length !in 4..16) {
                        sender.sendError("Name must be between 4 and 16 characters")
                        return@anyExecutor
                    }

                    if (!ALLOWED_CHARACTERS.matches(name)) {
                        sender.sendError("Photographer name contains invalid chars. only supports number, letter and underscore")
                        return@anyExecutor
                    }

                    getPhotographer(name)?.run {
                        sender.sendError("Photographer with name '$name' already exists")
                        return@anyExecutor
                    }

                    val photographer = createPhotographer(name, player, createRecordFile(module.path, player)) ?: run {
                        sender.sendError("Failed to create photographer with name '$name'")
                        return@anyExecutor
                    }
                    sender.sendSuccess("Photographer '${photographer.name}' created for ${player.name}")
                }
            }
        }
    }
    literalArgument("remove") {
        withPermission(perms.photographer.remove)
        stringArgument("name") {
            replaceSuggestions(
                ArgumentSuggestions.strings {
                    allPhotographers.map { it.name }.toTypedArray()
                },
            )
            anyExecutor { sender, args ->
                val name: String by args
                val photographer = getPhotographer(name) ?: run {
                    sender.sendError("Photographer with name '$name' not found")
                    return@anyExecutor
                }

                photographer.removePhotographer(false)
                sender.sendSuccess("Photographer '${photographer.name}' removed")
            }
        }
    }
    literalArgument("list") {
        withPermission(perms.photographer.list)
        anyExecutor { sender, _ ->
            allPhotographers.also {
                sender.sendInfo("Photographers (${it.size}):")
                val photographerList = it.joinToString("\n - ")
                sender.sendInfo(photographerList)
            }
        }
    }
}