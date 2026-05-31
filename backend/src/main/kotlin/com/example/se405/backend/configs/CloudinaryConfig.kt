package com.example.se405.backend.configs

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudinaryConfig(
    @Value("\${cloudinary.cloud-name}") private val cloudName: String,
    @Value("\${cloudinary.api-key}") private val apiKey: String,
    @Value("\${cloudinary.api-secret}") private val apiSecret: String
) {

    @Bean
    fun cloudinary(): Cloudinary {
        val config = HashMap<String, String>()
        config["cloud_name"] = cloudName
        config["api_key"] = apiKey
        config["api_secret"] = apiSecret

        return Cloudinary(config)
    }
}