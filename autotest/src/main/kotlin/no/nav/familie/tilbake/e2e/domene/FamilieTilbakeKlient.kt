package no.nav.familie.tilbake.e2e.domene

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.tilbake.e2e.domene.dto.AvsnittDto
import no.nav.familie.tilbake.e2e.domene.dto.BehandlingDto
import no.nav.familie.tilbake.e2e.domene.dto.FagsakDto
import no.nav.familie.tilbake.e2e.domene.dto.VersjonInfoDto
import no.nav.familie.tilbake.e2e.domene.dto.HentFaktaDto
import no.nav.familie.tilbake.e2e.domene.dto.HentForeldelseDto
import no.nav.familie.tilbake.e2e.domene.dto.BehandlingPåVentDto
import no.nav.familie.tilbake.e2e.domene.dto.BestillBrevDto
import no.nav.familie.tilbake.e2e.domene.dto.ForhåndsvisningVedtaksbrevPdfDto
import no.nav.familie.tilbake.e2e.domene.dto.HenleggDto
import no.nav.familie.tilbake.e2e.domene.dto.HentVilkårsvurderingDto
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagMelding
import no.nav.tilbakekreving.status.v1.EndringKravOgVedtakstatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.io.StringWriter
import java.net.URI
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

@Service
class FamilieTilbakeKlient(@Value("\${FAMILIE_TILBAKE_API_URL}") private val familieTilbakeApiUrl: String,
                           @Qualifier("azure") private val restOperations: RestOperations)
    : AbstractRestClient(restOperations, "familie-tilbake") {

    /* OPPRETT-tjenester */

    fun opprettTilbakekreving(opprettTilbakekrevingRequest: OpprettTilbakekrevingRequest): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/v1")

        return postForEntity(uri, opprettTilbakekrevingRequest)
    }

    fun opprettKravgrunnlag(kravgrunnlag: DetaljertKravgrunnlagMelding): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/autotest/opprett/kravgrunnlag/")
        val xml = jaxbObjectToXML(kravgrunnlag, DetaljertKravgrunnlagMelding::class.java)

        return postForEntity(uri, xml)
    }

    fun opprettStatusmelding(statusmelding: EndringKravOgVedtakstatus): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/autotest/opprett/statusmelding/")
        val xml = jaxbObjectToXML(statusmelding, EndringKravOgVedtakstatus::class.java)

        return postForEntity(uri, xml)
    }

    private fun jaxbObjectToXML(melding: Any, returntype: Class<*>): String {
        val jaxbContext = JAXBContext.newInstance(returntype)
        val jaxcMarshaller = jaxbContext.createMarshaller()
        jaxcMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        val sw = StringWriter()
        sw.use { jaxcMarshaller.marshal(melding, sw) }

        return sw.toString()
    }

    /*HENT-tjenester*/

    fun hentFagsak(fagsystem: Fagsystem, eksternFagsakId: String): Ressurs<FagsakDto> {
        val uri = URI.create("$familieTilbakeApiUrl/api/fagsystem/$fagsystem/fagsak/$eksternFagsakId/v1")

        return getForEntity(uri)
    }

    fun hentBehandling(behandlingId: String): Ressurs<BehandlingDto> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/v1/$behandlingId")

        return getForEntity(uri)
    }

    fun hentVersjonInfo(): Ressurs<VersjonInfoDto> {
        val uri = URI.create("$familieTilbakeApiUrl/api/info")

        return getForEntity(uri)
    }

    /*HENT-tjenester for behandlingsteg*/

    fun hentFakta(behandlingId: String): Ressurs<HentFaktaDto> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/fakta/v1")

        return getForEntity(uri)
    }

    fun hentForeldelse(behandlingId: String): Ressurs<HentForeldelseDto> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/foreldelse/v1")

        return getForEntity(uri)
    }

    fun hentVilkårsvurdering(behandlingId: String): Ressurs<HentVilkårsvurderingDto> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/vilkarsvurdering/v1")

        return getForEntity(uri)
    }

    fun hentVedtaksbrevtekst(behandlingId: String): Ressurs<List<AvsnittDto>> {
        val uri = URI.create("$familieTilbakeApiUrl/api/dokument/vedtaksbrevtekst/$behandlingId")

        return getForEntity(uri)
    }

    // TODO: FattVedtak/to-trinn

    /* BEHANDLE- og SETT-tjenester*/

    fun behandleSteg(stegdata: Any, behandlingId: String): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/steg/v1")

        return postForEntity(uri, stegdata)
    }

    fun settBehandlingPåVent(data: BehandlingPåVentDto, behandlingId: String): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/vent/v1")

        return putForEntity(uri, data)
    }

    fun taBehandlingAvVent(behandlingId: String): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/gjenoppta/v1")

        return putForEntity(uri, "")
    }

    fun henleggBehandling(behandlingId: String, data: HenleggDto): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/henlegg/v1")

        return putForEntity(uri, data)
    }

    // Andre tjenester

    fun bestillBrev(data: BestillBrevDto): Ressurs<Any> {
        val uri = URI.create("$familieTilbakeApiUrl/api/dokument/bestill")

        return postForEntity(uri, data)
    }

    fun forhåndsvisVedtaksbrev(data: ForhåndsvisningVedtaksbrevPdfDto): Ressurs<ByteArray> {
        val uri = URI.create("$familieTilbakeApiUrl/api/dokument/forhandsvis-vedtaksbrev")

        return postForEntity(uri, data)
    }
}
