package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.http.client.AbstractRestClient
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.tilbake.e2e.domene.Behandling
import no.nav.familie.tilbake.e2e.domene.Fagsak
import no.nav.familie.tilbake.e2e.domene.Kravgrunnlag
import no.nav.familie.tilbake.e2e.domene.VersjonInfo
import no.nav.familie.tilbake.e2e.domene.steg.Fakta
import org.hibernate.validator.internal.util.Contracts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class FamilieTilbakeKlient(@Value("\${FAMILIE_TILBAKE_API_URL}") private val familieTilbakeApiUrl: String,
                           restOperations: RestOperations) : AbstractRestClient(restOperations, "familie-tilbake") {

    private final val API_URL: String = "$familieTilbakeApiUrl/api"
    private final val VERSION_URL: URI = URI.create("$API_URL/info")
    private final val BEHANDLING_BASE: URI = URI.create("$API_URL/behandling")
    private final val BEHANDLING_URL_V1: URI = URI.create("$BEHANDLING_BASE/v1")
    private final val FAGSAK_URL_V1: URI = URI.create("$API_URL/fagsak/v1")

    private final val AUTOTEST_API: URI = URI.create("$API_URL/autotest")
    private final val OPPRETT_KRAVGRUNNLAG_URI: URI = URI.create("$AUTOTEST_API/opprett/kravgrunnlag/")
    private final val OPPRETT_STATUSMELDING_URI: URI = URI.create("$AUTOTEST_API/opprett/statusmelding/")

    fun opprettTilbakekreving(opprettTilbakekrevingRequest: OpprettTilbakekrevingRequest): String? {
        val response: Ressurs<String> = postForEntity(BEHANDLING_URL_V1, opprettTilbakekrevingRequest)
        Contracts.assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "Opprett behandling skulle hatt status SUKSESS. Istedet fikk den ${response.status} med melding ${response.melding}")
        return response.data
    }

    fun opprettKravgrunnlag(kravgrunnlag: Kravgrunnlag) {
        val mapper = jacksonObjectMapper()
        println(mapper.writeValueAsString(kravgrunnlag))
        val response: Ressurs<String> = postForEntity(OPPRETT_KRAVGRUNNLAG_URI, kravgrunnlag)
        Contracts.assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "Opprett behandling skulle hatt status SUKSESS. Istedet fikk den ${response.status} med melding ${response.melding}")
    }

    fun hentFagsak(fagsystem: Fagsystem, eksternFagsakId: String): Fagsak? {
        val uri = URI.create("$FAGSAK_URL_V1?fagsystem=$fagsystem&fagsak=$eksternFagsakId")
        val response: Ressurs<Fagsak> = getForEntity(uri)
        Contracts.assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }

    fun hentBehandling(behandlingId: String): Behandling? {
        val uri = URI.create("$BEHANDLING_URL_V1/$behandlingId")
        val response: Ressurs<Behandling> = getForEntity(uri)
        Contracts.assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }

    private fun hentFakta(behandlingId: String): Fakta? {
        val uri = URI.create("$BEHANDLING_BASE/$behandlingId/fakta/v1")
        val response: Ressurs<Fakta> = getForEntity(uri)
        Contracts.assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }

    fun hentVersjonInfo(): VersjonInfo? {
        val uri = URI.create("$VERSION_URL")
        val response: Ressurs<VersjonInfo> = getForEntity(uri)
        Contracts.assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }
}
