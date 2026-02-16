package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.http.config.INaisProxyCustomizer
import no.nav.familie.http.config.RestTemplateAzure
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate

@SpringBootConfiguration
@ComponentScan(ApplicationConfig.pakkenavn)
@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
@Import(RestTemplateAzure::class)
class ApplicationConfig {

    companion object {

        const val pakkenavn = "no.nav.familie.tilbake.e2e"
    }

    @Bean
    @Profile("local")
    @Primary
    fun restTemplateBuilder(): RestTemplateBuilder {
        return RestTemplateBuilder(LocalINaisProxyCustomiser())
    }

    class LocalINaisProxyCustomiser : INaisProxyCustomizer {

        override fun customize(restTemplate: RestTemplate) {
            // Should do nothing
        }
    }
}
