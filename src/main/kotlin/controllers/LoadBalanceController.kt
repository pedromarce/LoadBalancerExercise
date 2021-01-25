package controllers

import config.Configuration
import config.Resolution
import domain.Provider
import services.LoadBalanceService
import services.LoadBalancerServiceRandom
import services.LoadBalancerServiceRoundRobin

class LoadBalanceController(
    private val resolution: Resolution = Configuration.properties.resolution,
    private val loadBalanceServiceInject: LoadBalanceService? = null
) {

    private val OK = "Ok"
    private val ERROR = "Error : "

    val loadBalanceService = loadBalanceServiceInject ?: when (resolution) {
        Resolution.Random -> LoadBalancerServiceRandom()
        Resolution.RoundRobin -> LoadBalancerServiceRoundRobin()
    }

    fun register(provider: Provider): String {
        return try {
            loadBalanceService.register(provider)
            OK
        } catch (e: RuntimeException) {
            ERROR + e.message
        }

    }

    fun configure(id: String, status: Boolean): String {
        return try {
            loadBalanceService.configure(id, status)
            OK
        } catch (e: RuntimeException) {
            ERROR + e.message
        }

    }

    fun get(): String {
        return try {
            loadBalanceService.get().get()
        } catch (e: RuntimeException) {
            ERROR + e.message
        }

    }

}