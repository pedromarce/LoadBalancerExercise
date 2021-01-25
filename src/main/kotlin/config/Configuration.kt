package config

import com.sksamuel.hoplite.ConfigLoader

object Configuration {

    val properties: Properties = ConfigLoader().loadConfigOrThrow("/application.yaml")

}