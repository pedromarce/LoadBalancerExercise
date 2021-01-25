package services

import domain.Provider

interface ILoadBalancerService {

    fun get(): Provider

    fun register(provider: Provider)

    fun configure(id: String, status: Boolean)
}