package services

import config.Configuration
import domain.Provider

class LoadBalancerServiceRoundRobin (healthCheck: Long = Configuration.properties.health.check,
                                     limitCalls: Int = Configuration.properties.limits.calls,
                                     actionInject: (() -> Provider?)? = null): LoadBalanceService(healthCheck, limitCalls) {

    val action = actionInject ?: {
        val provider = activeProviders.removeFirst()
        activeProviders.add(provider)
        provider
    }

    override fun get(): Provider {
        return get(action)
    }

}