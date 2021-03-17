package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.simulering.FagOmrådeKode
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.*
import no.nav.familie.tilbake.e2e.util.JsonRest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI
import java.util.*
import javax.validation.constraints.Max

@Service
class FamilieTilbakeKlient(@Value("\${FAMILIE_TILBAKE_API_URL}") private val familieTilbakeApiUrl: String,
                           restOperations: RestOperations,
                           private val opprettTilbakekrevingBuilder: OpprettTilbakekrevingBuilder) : JsonRest(restOperations) {
    private final val API_URL: String = "$familieTilbakeApiUrl/api"
    private final val VERSION_URL: URI = URI.create("$API_URL/info")
    private final val BEHANDLING_URL_V1: URI = URI.create("$API_URL/behandling/v1")
    private final val FAGSAK_URL_V1: URI = URI.create("$API_URL/fagsak/v1")

    private lateinit var gjeldndeBehandling: GjeldendeBehandling

    fun hentVersjonInfo(): VersjonInfo? {
        val uri = URI.create("$VERSION_URL")
        return getOgHentData(uri)
    }

    fun opprettTilbakekreving(
        eksternFagsakId: String,
        fagsystem: Fagsystem,
        ytelsestype: Ytelsestype,
        varsel: Boolean,
        verge: Boolean
    ): String? {
        gjeldndeBehandling.eksternFagsakId = eksternFagsakId
        gjeldndeBehandling.fagsystem = fagsystem
        gjeldndeBehandling.ytelsestype = ytelsestype
        val request = opprettTilbakekrevingBuilder.requestBuilder(
            eksternFagsakId,
            fagsystem,
            ytelsestype,
            varsel,
            verge
        )
        gjeldndeBehandling.eksternBehandlingId = request.eksternId
        gjeldndeBehandling.eksternBrukId = postOgVerifiser(BEHANDLING_URL_V1, request, Ressurs.Status.SUKSESS)
        return gjeldndeBehandling.eksternBrukId
    }

    fun opprettKravgrunnlag(
        status: KodeStatusKrav,
        @Max(29)
        antallPerioder: Int,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean
    ) {
        //TODO
    }

    private fun hentFagsak(fagsystem: Fagsystem, eksternFagsakId: String): Fagsak? {
        val uri = URI.create("$FAGSAK_URL_V1?fagsystem=$fagsystem&fagsak=$eksternFagsakId")
        return getOgHentData(uri)
    }

    private fun hentBehandling(behandlingId: String): Behandling? {
        val uri = URI.create("$BEHANDLING_URL_V1/$behandlingId")
        return getOgHentData(uri)
    }

    fun hentBehandlingId(fagsystem: Fagsystem, eksternFagsakId: String, eksternBrukId: String?): String {
        hentFagsak(fagsystem, eksternFagsakId)?.behandlinger?.forEach {
            if (it.eksternBrukId.toString() == eksternBrukId) {
                gjeldndeBehandling.behandlingId = it.behandlingId.toString()
                return it.behandlingId.toString()
            }
        }
        throw Exception("Fantes ikke noen behandling med eksternBrukId $eksternBrukId på kombinasjonen eksternFagsakId $eksternFagsakId og fagsystem $fagsystem")
    }

    fun erBehandlingPåVent(behandlingId: String, venteårsak: Venteårsak): Boolean {
        hentBehandling(behandlingId)?.behandlingsstegsinfo?.forEach {
            if (it.behandlingsstegstatus == Behandlingsstegstatus.VENTER){
                if (it.venteårsak == venteårsak){
                    return true
                }
                throw Exception("Behandling $behandlingId var på vent men med årsak: ${it.venteårsak}. Forventet $venteårsak")
            }
        }
        return false
    }
    fun erBehandlingISteg(behandlingId: String, behandlingssteg: Behandlingssteg, behandlingsstegstatus: Behandlingsstegstatus): Boolean {
        hentBehandling(behandlingId)?.behandlingsstegsinfo?.forEach {
            if (it.behandlingssteg == behandlingssteg && it.behandlingsstegstatus == behandlingsstegstatus){
                return true
            }
        }
        return false
    }
}

class GjeldendeBehandling(
    var fagsystem: Fagsystem?,
    var ytelsestype: Ytelsestype?,
    var eksternFagsakId: String?,
    var eksternBehandlingId: String?,
    var eksternBrukId: String?,
    var behandlingId: String?,
    var vedtakId: String?,
    var kravgrunnlagId: String?
)
