package no.nav.familie.tilbake.e2e.klienter

import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service

@Service
class MockServerKlient {

    val restOperations = RestTemplateBuilder().additionalInterceptors(MdcValuesPropagatingClientInterceptor()).build()
}
