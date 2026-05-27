package net.unearthly.telegramBot.command

import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import net.unearthly.telegramBot.RegisteredPlayersManager
import net.unearthly.telegramBot.command.CommandManager.getCommand
import net.unearthly.telegramBot.command.CommandManager.isValidCommand
import java.util.UUID
import kotlin.uuid.Uuid

//basically class for other command
interface Command {
    // if user doesn't link account
    fun execute(message: Message)
    fun execute(uuid: UUID, message: Message)
    fun execute(uuid: UUID, message: Message, args: Array<out String>)
}

object CommandManager {
    private val commands = mutableMapOf<String, Command>()

    fun isValidCommand(command: String): Boolean = commands.containsKey(command)

    fun getCommand(command: String): Command? = commands[command]
}

fun onCommand(message: Message): Boolean {
    val text = message.text ?: return false

    if (!isValidCommand(text)) {
        return false
    }

    val command = getCommand(text) ?: return false

//    RegisteredPlayersManager().isRegistered()

    return true
}