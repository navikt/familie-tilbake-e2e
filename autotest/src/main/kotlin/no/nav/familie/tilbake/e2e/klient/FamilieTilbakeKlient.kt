package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.tilbake.e2e.domene.Behandling
import no.nav.familie.tilbake.e2e.domene.Fagsak
import no.nav.familie.tilbake.e2e.domene.VersjonInfo
import no.nav.familie.tilbake.e2e.domene.steg.Fakta
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagMelding
import no.nav.tilbakekreving.status.v1.EndringKravOgVedtakstatus
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.io.StringWriter
import java.net.URI
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

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
        assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "Opprett behandling skulle hatt status SUKSESS. Istedet fikk den ${response.status} med melding ${response.melding}")
        return response.data
    }

    fun opprettKravgrunnlag(kravgrunnlag: DetaljertKravgrunnlagMelding) {
        val xml = jaxbObjectToXML(kravgrunnlag, DetaljertKravgrunnlagMelding::class.java)
        val response: HttpStatus = postForEntity(OPPRETT_KRAVGRUNNLAG_URI, xml)
        assertTrue(response.is2xxSuccessful,
                             "Opprett kravgrunnlag skulle hatt status 200 OK. Istedet fikk den $response")
    }

    fun opprettStatusmelding(statusmelding: EndringKravOgVedtakstatus) {
        val xml = jaxbObjectToXML(statusmelding, EndringKravOgVedtakstatus::class.java)
        val response: HttpStatus = postForEntity(OPPRETT_STATUSMELDING_URI, xml)
        assertTrue(response.is2xxSuccessful,
            "Opprett statusmelding skulle hatt status 200 OK. Istede fikk den $response")
    }

    private fun jaxbObjectToXML(melding: Any, returntype: Class<*>): String {
        val jaxbContext = JAXBContext.newInstance(returntype)
        val jaxcMarshaller = jaxbContext.createMarshaller()
        jaxcMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        val sw = StringWriter()
        sw.use { jaxcMarshaller.marshal(melding, sw) }
        return sw.toString()
    }

    fun hentFagsak(fagsystem: Fagsystem, eksternFagsakId: String): Fagsak? {
        val uri = URI.create("$FAGSAK_URL_V1?fagsystem=$fagsystem&fagsak=$eksternFagsakId")
        val response: Ressurs<Fagsak> = getForEntity(uri)
        assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }

    fun hentBehandling(behandlingId: String): Behandling? {
        val uri = URI.create("$BEHANDLING_URL_V1/$behandlingId")
        val response: Ressurs<Behandling> = getForEntity(uri)
        assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }

    /*HENT-metoder for stegene*/
    private fun hentFakta(behandlingId: String): Fakta? {
        val uri = URI.create("$BEHANDLING_BASE/$behandlingId/fakta/v1")
        val response: Ressurs<Fakta> = getForEntity(uri)
        assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }
    //Mangler Foreldelse, Vilkårsvurdering, ForeslåVedtak, og FattVedtak/to-trinn

    fun hentVersjonInfo(): VersjonInfo? {
        val uri = URI.create("$VERSION_URL")
        val response: Ressurs<VersjonInfo> = getForEntity(uri)
        assertTrue(response.status == Ressurs.Status.SUKSESS,
                             "GET feilet. Status ${response.status}, feilmelding: ${response.melding}")
        return response.data
    }
}
