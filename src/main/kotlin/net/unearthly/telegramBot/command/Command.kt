package net.unearthly.telegramBot.command

import com.github.kotlintelegrambot.entities.Message
import java.util.UUID

//basically class for other command
interface Command {
    // if user doesn't link account
    fun execute(message: Message)
    fun execute(message: Message, args: List<String>)
    fun execute(uuid: UUID, message: Message)
    fun execute(uuid: UUID, message: Message, args: List<String>)
}

object CommandManager {
    private val commands = mutableMapOf<String, Command>()

    fun isValidCommand(command: String): Boolean = commands.containsKey(command)

    fun getCommand(command: String): Command? = commands[command]
}

fun onCommand(message: Message): Boolean {
    val text = (message.text ?: return false).lowercase()
    if (!text.startsWith("/")) return false // Telegram commands usually start with '/'

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

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TelegramCommand(val name: String)