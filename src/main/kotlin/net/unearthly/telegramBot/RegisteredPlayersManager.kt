package net.unearthly.telegramBot

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import net.unearthly.telegramBot.cast
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID

class RegisteredPlayersManager() {
    var config: YamlConfiguration = YamlConfiguration.loadConfiguration(File(TelegramBotManager.instance.dataFolder.path, "players.yml"))

    private val map = mutableMapOf<ChatId.Id, User>()
    private val section = config.getConfigurationSection("players")

    init {
        section?.getKeys(false)?.forEach { value ->
            var id = value.toLongOrNull() ?: run {
                println("Error to load telegram id: $value")
                return@forEach
            }
            val user = User(UUID.fromString(section.getString("${value}.uuid") ?: return@forEach))

            map[ChatId.Id(id)] = user
        }

    }

    fun isRegistered(uuid: UUID): Boolean {
        return map.values.any { it.uuid == uuid }
    }

    fun registerPlayer(userId: ChatId, message: Message) {
        val text = message.text ?: return
        if (!text.startsWith("/link")) return

        val arg = text.substringAfter("/link").trim()
        val offlinePlayer = Bukkit.getOfflinePlayer(arg)
        val id = userId.cast<ChatId.Id>()?.id ?: return

        when {
            offlinePlayer.hasPlayedBefore() -> {
                TelegramBotManager.bot.sendMessage(userId, "Игрок не найден")
                return
            }
            this.isRegistered(offlinePlayer.uniqueId) -> {
                TelegramBotManager.bot.sendMessage(userId, "Ник игрока уже привьязан к другому акаунту телеграм!")
                return
            }
            section?.get("$id.uuid") != null -> {
                TelegramBotManager.bot.sendMessage(userId, "К вашему телеграм акаунту привзьязан игровой акаунт. /unlink - для отвьязки")
                return
            }
        }

        section?.set("${id}.uuid", offlinePlayer.uniqueId.toString())
        map[ChatId.Id(id)] = User(offlinePlayer.uniqueId)
    }

    fun unRegisterPlayer(uuid: UUID) {

    }
}
