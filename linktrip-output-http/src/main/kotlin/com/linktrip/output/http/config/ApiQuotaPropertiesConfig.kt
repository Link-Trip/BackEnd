package com.linktrip.output.http.config

import com.linktrip.output.http.properties.ApiCostProperties
import com.linktrip.output.http.properties.ApiQuotaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ApiQuotaProperties::class, ApiCostProperties::class)
class ApiQuotaPropertiesConfig
