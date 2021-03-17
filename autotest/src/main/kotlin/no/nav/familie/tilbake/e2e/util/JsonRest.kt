package no.nav.familie.tilbake.e2e.util

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.springframework.web.client.RestOperations
import org.springframework.http.HttpHeaders
import java.net.URI

open class JsonRest(restOperations: RestOperations): AbstractRestClient(restOperations, "familie-tilbake") {

    /*POST*/
    fun postOgVerifiser(uri: URI, request: Any, expectedStatus: Ressurs.Status): String? {
        val response = post(uri, request)
        assertTrue(response.status == expectedStatus, "Opprett behandling skulle hatt status $expectedStatus. Istede fikk den ${response.status} med melding ${response.melding}")
        return response.data
    }

    private fun post(uri: URI, request: Any): Ressurs<String> {
        return postForEntity(uri, request)
    }

    /*GET*/
    fun <T> getOgHentData(uri: URI): T? {
        val response: Ressurs<T> = getForEntity(uri)
        assertTrue(response.status == Ressurs.Status.SUKSESS, "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }
    fun <T> getOgHentData(uri: URI, request: HttpHeaders): T? {
        val response: Ressurs<T> = getForEntity(uri, request)
        assertTrue(response.status == Ressurs.Status.SUKSESS, "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }
}
