package net.unearthly.telegramBot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.HandleCommand
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import jdk.internal.org.jline.utils.InfoCmp
import net.unearthly.telegramBot.command.Command
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import net.unearthly.telegramBot.command.onCommand
import kotlin.uuid.Uuid

class TelegramBotManager : JavaPlugin() {

    private val users = YamlConfiguration.loadConfiguration(File(dataFolder.path, "users.yml"))
    private val chatsWithSupport = mutableMapOf<ChatId.Id, Support>()


    companion object {
        lateinit var instance: TelegramBotManager
        private set

        lateinit var bot: Bot
            private set
    }

    override fun onEnable() {
        instance = this
        this.saveDefaultConfig()

        bot = bot {
            try {
                token = config.getString("token")!!
            } catch(e: NullPointerException) {
                e
            }

            dispatch {
                command("start") {

                }

                message {
                    val message = this.message.text ?: return@message
                    val chatId = ChatId.fromId(this.message.chat.id)

                    if (message.first().equals('/', true)) {
                        onCommand(this.message)
                    }

                    if (chatsWithSupport.containsKey(chatId)) {
                        val user = chatsWithSupport[chatId] ?: return@message

                        if (user.whoResponse != null) {
                            bot.sendMessage(user.whoResponse, message)
                        }

                        user.messages.add(message)
                    }
                }

                command("support") {
                    val text = this.message.text ?: return@command
                    val chatId = ChatId.fromId(this.message.chat.id)

                    if (config.getStringList("support_users").contains(this.message.chat.username)) {
                        chatsWithSupport[chatId] = Support(Uuid.random())
                        this.bot.sendMessage(chatId, "You started chat with support team, all your messages will be automatically send to the moderator!")
                    }

                    var buttons: InlineKeyboardMarkup? = null

                    var addedButtons = 0
                    chatsWithSupport.forEach { (chatId, support) ->
                        if (addedButtons >= 7) return@forEach

                        val whoStarted = this.bot.getChat(chatId).get()
                        buttons = InlineKeyboardMarkup.create(

                        listOf(InlineKeyboardButton.CallbackData(
                            "${whoStarted.firstName} | (${support.messages.size})",
                            "starting_chat"
                        )),

//                        listOf(InlineKeyboardButton.CallbackData(
//                            "Back"
//
//                        ))
                        )

                        addedButtons++
                    }

                    this.bot.sendMessage(chatId, "Select the user who you want to response:", replyMarkup = buttons)
                }

                callbackQuery("starting_chat") {
                    val supportChatId = ChatId.fromId(this.callbackQuery.message?.chat?.id ?: return@callbackQuery)
                    val user = chatsWithSupport.entries.find { it.value.whoResponse == supportChatId } ?: return@callbackQuery

                    val messages = user.value.messages
                    val chatId = user.value.whoResponse ?: return@callbackQuery

                    if (!messages.isEmpty()) {
                        messages.forEach { this.bot.sendMessage(supportChatId, it) }
                    }

                    this.bot.sendMessage(chatId, this.callbackQuery.message?.text ?: return@callbackQuery)
                }
            }
        }

        bot.startPolling()
    }

    override fun onDisable() {
    }
}
