package net.unearthly.telegramBot

import kotlinx.serialization.Serializable

@Serializable
data class BotConfig(
    val token: String,
    val owners: List<String> = emptyList(),
    val moderators: List<String> = emptyList()
)


