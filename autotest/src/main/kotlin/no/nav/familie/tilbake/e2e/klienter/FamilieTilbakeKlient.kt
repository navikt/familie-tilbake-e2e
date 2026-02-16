package no.nav.familie.tilbake.e2e.klienter

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.KanBehandlingOpprettesManueltRespons
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettManueltTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.klienter.dto.HentVilkårsvurderingDto
import no.nav.familie.tilbake.e2e.klienter.dto.felles.PeriodeDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.AvsnittDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.BehandlingDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.BehandlingPåVentDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.BeregnetPerioderDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.BeregningsresultatDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.BestillBrevDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.FagsakDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.ForhåndsvisningHenleggelsesbrevDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.ForhåndsvisningVedtaksbrevPdfDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.HenleggDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.HentFaktaDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.HentForeldelseDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.OpprettRevurderingDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.TotrinnsvurderingDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.VersjonInfoDto
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagMelding
import no.nav.tilbakekreving.status.v1.EndringKravOgVedtakstatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.io.StringWriter
import java.net.URI

@Service
class FamilieTilbakeKlient(
    @param:Value("\${FAMILIE_TILBAKE_API_URL}") private val familieTilbakeApiUrl: String,
    @param:Qualifier("azure") private val restOperations: RestOperations
) :
    AbstractRestClient(restOperations, "familie-tilbake") {

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

    fun behandleSteg(data: Any, behandlingId: String, retry: Int = 1): Ressurs<String> {
        try {
            val uri = URI.create("$familieTilbakeApiUrl/api/behandling/$behandlingId/steg/v1")
            return postForEntity(uri, data)
        } catch (exception: Exception) {
            println("Feilet kall mot behandle steg")
            if (retry > 0) {
                println("Retryer kall mot behandle steg")
                return behandleSteg(data, behandlingId, retry - 1)
            } else {
                throw exception
            }
        }
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
        val uri =
            URI.create("$familieTilbakeApiUrl/api/autotest/behandling/$behandlingId/endre/saksbehandler/$nyAnsvarligSaksbehandler")

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

    fun opprettVerge(behandlingId: String): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/v1/$behandlingId/verge")

        return postForEntity(uri, "")
    }

    fun fjernVerge(behandlingId: String): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/v1/$behandlingId/verge")

        return putForEntity(uri, "")
    }

    fun hentVerge(behandlingId: String): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/v1/$behandlingId/verge")

        return getForEntity(uri)
    }

    fun kanBehandlingOpprettesManuelt(ytelsestype: Ytelsestype, eksternFagsakId: String): Ressurs<KanBehandlingOpprettesManueltRespons> {
        val uri =
            URI.create("$familieTilbakeApiUrl/api/ytelsestype/$ytelsestype/fagsak/$eksternFagsakId/kanBehandlingOpprettesManuelt/v1")

        return getForEntity(uri)
    }

    fun publiserFagsystembehandling(data: OpprettManueltTilbakekrevingRequest): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/autotest/publiser/fagsystemsbehandling")

        return postForEntity(uri, data)
    }

    fun opprettManuellBehandling(data: OpprettManueltTilbakekrevingRequest): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/manuelt/task/v1")

        return postForEntity(uri, data)
    }

    fun opprettRevurdering(data: OpprettRevurderingDto): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/revurdering/v1")

        return postForEntity(uri, data)
    }
}
