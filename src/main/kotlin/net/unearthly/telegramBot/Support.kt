package net.unearthly.telegramBot

import com.github.kotlintelegrambot.entities.ChatId
import java.util.UUID
import kotlin.uuid.Uuid

data class Support(
    val uuid: Uuid,
    val whoResponse: ChatId.Id? = null,
    val messages: MutableList<String> = mutableListOf(),
)
