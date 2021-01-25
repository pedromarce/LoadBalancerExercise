package config

data class Properties(val health: Health, val limits: Limits, val resolution: Resolution)

data class Health(val check: Long)

data class Limits (val providers: Int, val calls: Int)

enum class Resolution { Random, RoundRobin}