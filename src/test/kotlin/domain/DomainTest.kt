package domain

import config.Configuration
import kotlin.test.*

class DomainTest {

    @Test
    fun initLoadBalancer() {
        val loadBalancer = LoadBalancer()
        assertNotNull(loadBalancer)
    }

    @Test
    fun registerLoadBalancer() {
        val provider = Provider()
        val loadBalancer = LoadBalancer()
        loadBalancer.register(provider)
        assertEquals(provider, loadBalancer.get(provider.get()))
    }

    @Test
    fun registerLoadBalancerConfiguredLimit() {
        val loadBalancer = LoadBalancer()
        repeat(Configuration.properties.limits.providers) {loadBalancer.register(Provider())}
        val provider = Provider()
        assertFails { loadBalancer.register(provider) }
    }

    @Test
    fun getProvider() {
        val provider = Provider()
        val loadBalancer = LoadBalancer()
        loadBalancer.register(provider)
        assertEquals(provider, loadBalancer.get(provider.get()))
    }

    @Test
    fun testProviderHealth() {
        val provider = Provider()
        assertTrue { provider.isHealthy() }
        provider.unhealthy()
        assertFalse { provider.isHealthy() }
        provider.healthy()
        assertFalse { provider.isHealthy() }
        provider.healthy()
        assertTrue { provider.isHealthy() }
    }
}
