package net.unearthly.telegramBot.command

import com.github.kotlintelegrambot.entities.Message
import java.util.UUID

//basically class for other command
abstract class Command(val command: String) {
    // if user doesn't link account
    abstract fun execute(message: Message)
    abstract fun execute(message: Message, args: List<String>)
}

object CommandManager {
    private val commands = mutableMapOf<String, Command>()

    fun isValidCommand(command: String): Boolean = commands.containsKey(command)

    fun getCommand(command: String): Command? = commands[command]
}

fun onCommand(message: Message): Boolean {
    val text = (message.text ?: return false).lowercase()
    if (!text.startsWith("/")) return false
    // IDK what I need to write in ragex, and I ask in AI
    val parts = text.split("\\s+".toRegex())
    val commandName = parts[0].lowercase()
    val args = parts.drop(1)

    if (!CommandManager.isValidCommand(commandName)) {
        return false
    }

    val command = CommandManager.getCommand(commandName) ?: return false

    if (args.isNotEmpty()) {
        command.execute(message, args)
    } else {
        command.execute(message)
    }

    return true
}