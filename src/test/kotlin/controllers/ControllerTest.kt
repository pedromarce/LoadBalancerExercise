package controllers

import config.Resolution
import domain.Provider
import kotlinx.coroutines.*
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import services.LoadBalanceService
import services.LoadBalancerServiceRandom
import services.LoadBalancerServiceRoundRobin
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ControllerTest {

    @Test
    fun initControllerFromConfig() {
        val loadBalancerController = LoadBalanceController()
        assertNotNull(loadBalancerController)
    }

    @Test
    fun initControllerForRandom() {
        val loadBalancerController = LoadBalanceController(Resolution.Random)
        assertNotNull(loadBalancerController)
        assert(loadBalancerController.loadBalanceService is LoadBalancerServiceRandom)
    }

    @Test
    fun initControllerForRoundRobin() {
        val loadBalancerController = LoadBalanceController(Resolution.RoundRobin)
        assertNotNull(loadBalancerController)
        assert(loadBalancerController.loadBalanceService is LoadBalancerServiceRoundRobin)
    }

    @Test
    fun registerTest() {
        runBlocking {
            val loadBalancerService = mock(LoadBalanceService::class.java)
            val loadBalancerController = LoadBalanceController(loadBalanceServiceInject = loadBalancerService)
            assertNotNull(loadBalancerController)
            val provider = Provider()
            val result = GlobalScope.async { loadBalancerController.register(provider) }
            assertEquals("Ok", result.await())
            verify(loadBalancerService).register(provider)
        }
    }

    @Test
    fun configureTest() {
        runBlocking {
            val loadBalancerService = mock(LoadBalanceService::class.java)
            val loadBalancerController = LoadBalanceController(loadBalanceServiceInject = loadBalancerService)
            assertNotNull(loadBalancerController)
            val provider = Provider()
            val result = GlobalScope.async { loadBalancerController.configure(provider.get(), true) }
            assertEquals("Ok", result.await())
            verify(loadBalancerService).configure(provider.get(), true)
        }
    }

    @Test
    fun getTest() {
        runBlocking {
            val loadBalancerService = mock(LoadBalanceService::class.java)
            val loadBalancerController = LoadBalanceController(loadBalanceServiceInject = loadBalancerService)
            assertNotNull(loadBalancerController)
            val provider = Provider()
            Mockito.`when`(loadBalancerService.get()).thenReturn(provider)
            val resultProvider = GlobalScope.async { loadBalancerController.get() }
            assertEquals(provider.get(), resultProvider.await())
            verify(loadBalancerService).get()
        }
    }

    @Test
    fun getErrorRegister() {
        runBlocking {
            val loadBalancerService = mock(LoadBalanceService::class.java)
            val loadBalancerController = LoadBalanceController(loadBalanceServiceInject = loadBalancerService)
            assertNotNull(loadBalancerController)
            val provider = Provider()
            Mockito.`when`(loadBalancerService.register(provider)).thenThrow(RuntimeException("error"))
            val result = GlobalScope.async { loadBalancerController.register(provider) }
            assertEquals("Error : error", result.await())
        }
    }

    @Test
    fun getErrorConfigure() {
        runBlocking {
            val loadBalancerService = mock(LoadBalanceService::class.java)
            val loadBalancerController = LoadBalanceController(loadBalanceServiceInject = loadBalancerService)
            assertNotNull(loadBalancerController)
            Mockito.`when`(loadBalancerService.configure("id", true)).thenThrow(RuntimeException("error"))
            val result = GlobalScope.async { loadBalancerController.configure("id", true) }
            assertEquals("Error : error", result.await())
        }
    }

    @Test
    fun getErrorGet() {
        runBlocking {
            val loadBalancerService = mock(LoadBalanceService::class.java)
            val loadBalancerController = LoadBalanceController(loadBalanceServiceInject = loadBalancerService)
            assertNotNull(loadBalancerController)
            Mockito.`when`(loadBalancerService.get()).thenThrow(RuntimeException("error"))
            val result = GlobalScope.async { loadBalancerController.get() }
            assertEquals("Error : error", result.await())
        }
    }

    @Test
    fun getErrorConcurrencyGet() {
        runBlocking {
            val loadBalancerService = LoadBalancerServiceRandom(0, 2) {
                Thread.sleep(300)
                Provider()
            }
            val loadBalancerController = LoadBalanceController(loadBalanceServiceInject = loadBalancerService)
            assertNotNull(loadBalancerController)
            loadBalancerController.register(Provider())
            val resultDeferred: ArrayList<Deferred<String>> = ArrayList()
            repeat (20) {resultDeferred.add(GlobalScope.async {
                loadBalancerController.get()
            })}
            val result = resultDeferred.awaitAll()
            assert(result.any { it == "Error : Too many calls simultaneously" })
        }
    }

}