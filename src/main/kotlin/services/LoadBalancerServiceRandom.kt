package services

import config.Configuration
import domain.Provider
import kotlin.random.Random

class LoadBalancerServiceRandom (healthCheck: Long = Configuration.properties.health.check,
                                 limitCalls: Int = Configuration.properties.limits.calls,
                                 actionInject: (() -> Provider?)? = null) : LoadBalanceService(healthCheck, limitCalls) {

    private val action = actionInject ?: {
        activeProviders[Random.nextInt(activeProviders.size)]
    }

    override fun get(): Provider {
        return get(action)
    }

}