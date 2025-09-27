package com.rezonation.config

final case class KafkaConfig(
    bootstrapServers: List[String],
    topic: String
)

object KafkaConfig {
  // val config = deriveConfig[KafkaConfig]
}
