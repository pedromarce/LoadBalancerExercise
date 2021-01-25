package services

import config.Configuration
import domain.Provider
import org.junit.Test
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class LoadBalanceTest {

    @Test
    fun initializeRandomService() {
        val loadBalanceService = LoadBalancerServiceRandom()
        assertNotNull(loadBalanceService)
    }

    @Test
    fun initializeRoundRobinService() {
        val loadBalanceService = LoadBalancerServiceRoundRobin()
        assertNotNull(loadBalanceService)
    }

    @Test
    fun registerProvider() {
        val loadBalanceService = LoadBalancerServiceRandom()
        val provider = Provider()
        loadBalanceService.register(provider)
        assertEquals(provider.get(), loadBalanceService.get().get())
        assert(loadBalanceService.providersActive() == 1)
    }

    @Test
    fun getRoundRobin() {
        val loadBalanceService = LoadBalancerServiceRoundRobin()
        repeat(4) { loadBalanceService.register(Provider()) }
        val firstProvider = loadBalanceService.get()
        assertNotEquals(firstProvider, loadBalanceService.get())
        assertNotEquals(firstProvider, loadBalanceService.get())
        assertNotEquals(firstProvider, loadBalanceService.get())
        assertEquals(firstProvider, loadBalanceService.get())
    }

    @Test
    fun getRandom() {
        val loadBalanceService = LoadBalancerServiceRandom()
        val sourceProviders = ArrayList<Provider>()
        val testProviders = ArrayList<Provider>()
        val maxProviders = Configuration.properties.limits.providers
        repeat(maxProviders) {
            val provider = Provider()
            sourceProviders.add(provider)
            loadBalanceService.register(provider) }
        repeat(maxProviders) { testProviders.add(loadBalanceService.get()) }
        assert(testProviders.filterIndexed { idx, provider -> provider !== sourceProviders[idx] }
            .isNotEmpty())
    }

    @Test
    fun disableProvider() {
        val loadBalanceService = LoadBalancerServiceRoundRobin()
        repeat(2) { loadBalanceService.register(Provider()) }
        val firstProvider = loadBalanceService.get()
        loadBalanceService.configure(firstProvider.get(), false)
        repeat (2) { assertNotEquals(firstProvider, loadBalanceService.get())}
    }

    @Test
    fun enableProvider() {
        val loadBalanceService = LoadBalancerServiceRoundRobin()
        repeat(2) { loadBalanceService.register(Provider()) }
        val firstProvider = loadBalanceService.get()
        loadBalanceService.configure(firstProvider.get(), false)
        repeat (2) { assertNotEquals(firstProvider, loadBalanceService.get())}
        loadBalanceService.configure(firstProvider.get(), true)
        loadBalanceService.get()
        assertEquals(firstProvider, loadBalanceService.get())
    }

    @Test
    fun getErrorConfigure() {
        val loadBalanceService = LoadBalancerServiceRoundRobin()
        repeat(3) { loadBalanceService.register(Provider()) }
        assertFails("Error : Provider not available") { loadBalanceService.configure("not:_existing", true) }
    }

    @Test
    fun getErrorGet() {
        val loadBalanceService = LoadBalancerServiceRoundRobin()
        assertFails("Error : No provider available") { loadBalanceService.get() }
    }

    @Test
    fun getErrorGetAllDisabled() {
        val loadBalanceService = LoadBalancerServiceRoundRobin()
        val provider = Provider()
        loadBalanceService.register(provider)
        loadBalanceService.configure(provider.get(),false)
        assertFails("Error : No provider available") { loadBalanceService.get() }
    }

    @Test
    fun getHealthCheckDisable() {
        val loadBalanceService = LoadBalancerServiceRoundRobin(0)
        val provider = Provider { false }
        loadBalanceService.register(provider)
        assert(loadBalanceService.providersActive() > 0)
        loadBalanceService.checkHealth()
        assert(loadBalanceService.providersActive() == 0)
    }

    @Test
    fun getHealthCheckEnablesAfter2HealthTrue() {
        val loadBalanceService = LoadBalancerServiceRoundRobin(0)
        val provider = Mockito.mock(Provider::class.java)
        Mockito.`when`(provider.get()).thenReturn("uuid")
        Mockito.`when`(provider.check()).thenReturn(false)
        loadBalanceService.register(provider)
        assert(loadBalanceService.providersActive() > 0)
        loadBalanceService.checkHealth()
        assert(loadBalanceService.providersActive() == 0)
        Mockito.`when`(provider.check()).thenReturn(true)
        Mockito.`when`(provider.isHealthy()).thenReturn(false)
        loadBalanceService.checkHealth()
        assert(loadBalanceService.providersActive() == 0)
        Mockito.`when`(provider.isHealthy()).thenReturn(true)
        loadBalanceService.checkHealth()
        assert(loadBalanceService.providersActive() > 0)
    }

}