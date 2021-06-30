package no.nav.familie.tilbake.e2e.familie_tilbake

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.AvsnittDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.BehandlingDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.FagsakDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.VersjonInfoDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.HentFaktaDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.HentForeldelseDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.BehandlingPåVentDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.BeregnetPerioderDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.BeregningsresultatDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.BestillBrevDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.ForhåndsvisningHenleggelsesbrevDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.ForhåndsvisningVedtaksbrevPdfDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.HenleggDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.HentVilkårsvurderingDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.TotrinnsvurderingDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.felles.PeriodeDto
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

    fun opprettTilbakekreving(data: OpprettTilbakekrevingRequest): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/v1")

        return postForEntity(uri, data)
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

    fun hentTotrinnsvurderinger(behandlingId: String): Ressurs<TotrinnsvurderingDto> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/totrinn/v1")

        return getForEntity(uri)
    }

    fun behandleSteg(data: Any, behandlingId: String): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/steg/v1")

        return postForEntity(uri, data)
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

    fun endreAnsvarligSaksbehandler(behandlingId: String, nyAnsvarligSaksbehandler: String): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl//api/autotest/behandling/$behandlingId/endre/saksbehandler/$nyAnsvarligSaksbehandler")

        return putForEntity(uri, "")
    }

    fun bestillBrev(data: BestillBrevDto): Ressurs<Any> {
        val uri = URI.create("$familieTilbakeApiUrl/api/dokument/bestill")

        return postForEntity(uri, data)
    }

    fun forhåndsvisVedtaksbrev(data: ForhåndsvisningVedtaksbrevPdfDto): Ressurs<ByteArray> {
        val uri = URI.create("$familieTilbakeApiUrl/api/dokument/forhandsvis-vedtaksbrev")

        return postForEntity(uri, data)
    }

    fun forhåndsvisVarselbrev(data: ForhåndsvisVarselbrevRequest): ByteArray {
        val uri = URI.create("$familieTilbakeApiUrl/api/dokument/forhandsvis-varselbrev")

        return postForEntity(uri, data)
    }

    fun forhåndsvisHenleggelsesbrev(data: ForhåndsvisningHenleggelsesbrevDto): Ressurs<ByteArray> {
        val uri = URI.create("$familieTilbakeApiUrl/api/dokument/forhandsvis-henleggelsesbrev")

        return postForEntity(uri, data)
    }

    fun hentJournaldokument(behandlingId: String, journalpostId: String, dokumentId: String) {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/journalpost/$behandlingId/dokument/$dokumentId")

        return getForEntity(uri)
    }

    fun beregn(behandlingId: String, data: List<PeriodeDto>): Ressurs<BeregnetPerioderDto> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/beregn/v1")

        return postForEntity(uri, data)
    }

    fun beregnResultat(behandlingId: String): Ressurs<BeregningsresultatDto> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/beregn/resultat/v1")

        return getForEntity(uri)
    }

    // TODO: Implementer manuell opprettelse av behandling
}
