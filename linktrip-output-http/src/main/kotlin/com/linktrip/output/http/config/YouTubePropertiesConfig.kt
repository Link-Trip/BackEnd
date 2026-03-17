package com.linktrip.output.http.config

import com.linktrip.output.http.properties.YouTubeProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(YouTubeProperties::class)
class YouTubePropertiesConfig
