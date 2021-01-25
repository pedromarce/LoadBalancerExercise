package domain

import java.util.*

class Provider (val checkInject: () -> Boolean = { true }) {

    private val id: String = UUID.randomUUID().toString()
    private var active: Boolean = true
    private var consecutiveHealth: Int = 2

    fun get(): String {
        return id
    }

    fun status(status: Boolean) {
        active = status
    }

    fun healthy() {
        if (consecutiveHealth < 2) consecutiveHealth++
    }

    fun unhealthy() {
        consecutiveHealth = 0
    }

    fun isHealthy(): Boolean {
        return active && consecutiveHealth > 1
    }

    fun check() : Boolean {
        return checkInject.invoke()
    }

}