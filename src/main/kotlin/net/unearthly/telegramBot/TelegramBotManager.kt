package net.unearthly.telegramBot

import com.charleskorn.kaml.Yaml
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import java.io.File
import net.unearthly.telegramBot.command.onCommand
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.uuid.Uuid
import kotlinx.serialization.decodeFromString
import kotlin.Boolean

//IDK is there comfortable to place the variable
private val chatsWithSupport = mutableMapOf<ChatId.Id, Support>()

lateinit var bot: Bot
    private set

lateinit var config: BotConfig
    private set

fun main() {
    //here we initialization the config
    val file = File("config.yml")

    if (!file.exists()) {
        file.createNewFile()
    }

    config = Yaml.default.decodeFromString<BotConfig>(file.readText())

    bot = bot {
        token = config.token

        dispatch {
            command("start") {

            }

            message {
                val message = this.message.text ?: return@message
                val chatId = ChatId.fromId(this.message.chat.id)

                if (message.startsWith('/')) {
                    onCommand(this.message)
                }

                val user = chatsWithSupport.entries.find { it.value.whoResponse == chatId }

                if (user != null) {
                    bot.sendMessage(user.key, this.message.text ?: return@message)
                    return@message
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

                if (config.moderators.contains(this.message.chat.username)) {
                    val buttons = InlineKeyboardMarkup.create(
                        listOf(InlineKeyboardButton.CallbackData("Close ticket", "close_ticket")),
                    )

                    chatsWithSupport[chatId] = Support(Uuid.random())
                    this.bot.sendMessage(chatId, "You started chat with support team, all your messages will be automatically send to the moderator!", replyMarkup = buttons)
                }

                val buttons = getWhoNeedSupport(0)

                this.bot.sendMessage(chatId, "Select the user who you want to response:", replyMarkup = buttons)
            }

            callbackQuery("starting_chat") {
                val supportChatId = ChatId.fromId(this.callbackQuery.message?.chat?.id ?: return@callbackQuery)
                val user = chatsWithSupport.entries.find { it.value.whoResponse == supportChatId } ?: return@callbackQuery
                val messages = user.value.messages

                if (!messages.isNotEmpty()) {
                    messages.forEach { this.bot.sendMessage(supportChatId, it) }
                }
            }

            callbackQuery {
                val supportChatId = ChatId.fromId(this.callbackQuery.message?.chat?.id ?: return@callbackQuery)

                if (this.callbackQuery.data.startsWith("page:")) {
                    val page = this.callbackQuery.data.substringAfter("page:").toIntOrNull() ?: 0
                    val whoNeedSupport = getWhoNeedSupport(page)

                    this.bot.editMessageText(
                        supportChatId,
                        this.callbackQuery.message?.messageId ?: return@callbackQuery,
                        text = this.callbackQuery.message?.text ?: return@callbackQuery,
                        replyMarkup = whoNeedSupport
                    )

                    this.bot.answerCallbackQuery(this.callbackQuery.id)
                }

            }
        }
    }

    bot.startPolling()
}

fun getWhoNeedSupport(page: Int) : InlineKeyboardMarkup {
    val buttonsPerPages = 8
    val activeTickets = chatsWithSupport.entries.toList()

    val totalPages = (if (activeTickets.isEmpty()) 1 else (activeTickets.size + buttonsPerPages - 1) / buttonsPerPages) - 1
    val start = page * buttonsPerPages
    val end = minOf(start + buttonsPerPages, activeTickets.size)

    val inlineButtons = mutableListOf<List<InlineKeyboardButton>>()

    for (i in start until end) {
        val (chatId, _) = activeTickets[i]

        inlineButtons.add(listOf(InlineKeyboardButton.CallbackData("${bot.getChat(chatId).get().username}", "starting_chat")))
    }

    if (page > 0) {
        inlineButtons.add(listOf(InlineKeyboardButton.CallbackData("Back", "page:${page-1}")))
    }

    if (page > totalPages) {
        inlineButtons.add(listOf(InlineKeyboardButton.CallbackData("Next", "page:${page+1}")))
    }

    return InlineKeyboardMarkup.create(inlineButtons)
}