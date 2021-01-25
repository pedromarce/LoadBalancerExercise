package services

import config.Configuration
import domain.LoadBalancer
import domain.Provider
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

abstract class LoadBalanceService(
    healthCheck: Long = Configuration.properties.health.check,
    private val limitCalls: Int = Configuration.properties.limits.calls
) :
    ILoadBalancerService {

    private val loadBalancer = LoadBalancer()
    protected val activeProviders = LinkedList<Provider>()
    private var currentCalls: AtomicInteger = AtomicInteger(0)

    init {
        if (healthCheck > 0) {
            Timer("HealthCheck", true).scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        checkHealth()
                    }
                },
                1000,
                healthCheck
            )
        }
    }

    override fun register(provider: Provider) {
        loadBalancer.register(provider)
        activeProviders.add(provider)
    }

    override fun configure(id: String, status: Boolean) {
        val provider = loadBalancer.get(id) ?: throw RuntimeException("Provider not available")
        if (!status) provider.let {
            it.status(false)
            activeProviders.remove(it)
        }
        else {
            provider.let {
                it.status(true)
                if (!activeProviders.contains(it)) activeProviders.add(it)
            }
        }
    }

    fun providersActive(): Int {
        return activeProviders.size
    }

    fun get(action: () -> Provider?): Provider {
        currentCalls = newCall(currentCalls)
        val provider = action()
        currentCalls.decrementAndGet()
        return provider ?: throw RuntimeException("No provider available")

    }

    @Synchronized
    fun newCall(currentCalls: AtomicInteger): AtomicInteger {
        if (currentCalls.incrementAndGet() > activeProviders.count()  * limitCalls) {
            throw RuntimeException("Too many calls simultaneously")
        }
        return currentCalls
    }

    fun checkHealth() {
        loadBalancer.providers().forEach {
            val healthy = try {
                it.check()
            } catch (e: Exception) {
                false
            }
            if (!healthy) {
                activeProviders.remove(it)
                it.unhealthy()
            } else {
                it.healthy()
            }
            if (it.isHealthy() && !activeProviders.contains(it)) activeProviders.add(it)
        }
    }

}