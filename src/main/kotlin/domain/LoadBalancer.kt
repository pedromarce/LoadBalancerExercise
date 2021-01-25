package domain

import config.Configuration

class LoadBalancer {

    private val providers = HashMap<String, Provider>()

    fun get(id: String): Provider? {
        return providers[id]
    }

    fun register(provider: Provider): Boolean {
        if (providers.size == Configuration.properties.limits.providers) {
            throw RuntimeException("Maximum number of providers")
        }
        providers.putIfAbsent(provider.get(), provider)
        return true
    }

    fun providers(): Collection<Provider> {
        return providers.values
    }

}
