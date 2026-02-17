package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.http.config.RestTemplateAzure
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.kotlinModule

@SpringBootConfiguration
@ComponentScan(ApplicationConfig.pakkenavn)
@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
@Import(RestTemplateAzure::class)
class ApplicationConfig {

    companion object {

        const val pakkenavn = "no.nav.familie.tilbake"
    }

    @Primary
    @Bean
    fun customizeJackson(): JsonMapperBuilderCustomizer {
        return JsonMapperBuilderCustomizer { builder ->
            builder.addModule(kotlinModule {
                configure(KotlinFeature.KotlinPropertyNameAsImplicitName, true)
            })
        }
    }

    @Bean
    @Primary
    fun restTemplateBuilder(jsonMapper: JsonMapper): RestTemplateBuilder {
        return RestTemplateBuilder({ restTemplate ->
            restTemplate.messageConverters.removeIf { it is JacksonJsonHttpMessageConverter }
            restTemplate.messageConverters.add(JacksonJsonHttpMessageConverter(jsonMapper))
        })
    }
}
