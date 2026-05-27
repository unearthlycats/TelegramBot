package net.unearthly.telegramBot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import net.unearthly.telegramBot.command.Command
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TelegramBotManager : JavaPlugin() {

    companion object {
        lateinit var instance: TelegramBotManager
        private set

        lateinit var bot: Bot
            private set
    }

    override fun onEnable() {
        instance = this

        bot = bot {
            try {
                token = config.getString("token")!!
            } catch(e: NullPointerException) {
                e
            }

            dispatch {
                message {
                    val message = this.message.text ?: return@message

                    if (message.first().equals('/', true)) {

                    }
                }
            }
        }
    }

    override fun onDisable() {
    }
}
