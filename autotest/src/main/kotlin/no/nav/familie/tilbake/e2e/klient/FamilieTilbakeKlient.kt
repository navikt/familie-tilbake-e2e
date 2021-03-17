package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.*
import no.nav.familie.tilbake.e2e.util.JsonRest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class FamilieTilbakeKlient(@Value("\${FAMILIE_TILBAKE_API_URL}") private val familieTilbakeApiUrl: String,
                           private val restOperations: RestOperations,
                           private val rest: JsonRest,
                           private val opprettTilbakekrevingBuilder: OpprettTilbakekrevingBuilder) : AbstractRestClient(restOperations,
                                                                                            "familie-tilbake") {
    private final val API_URL: String = "$familieTilbakeApiUrl/api"
    private final val VERSION_URL: URI = URI.create("$API_URL/info")
    private final val BEHANDLING_URL_V1: URI = URI.create("$API_URL/behandling/v1")
    private final val FAGSAK_URL_V1: URI = URI.create("$API_URL/fagsak/v1")

    fun hentVersjonInfo(): Ressurs<VersjonInfo> {
        val uri = URI.create("$VERSION_URL")
        return getForEntity(uri)
    }

    fun opprettTilbakekreving(
        eksternFagsakId: String,
        fagsystem: Fagsystem,
        ytelsestype: Ytelsestype,
        varsel: Boolean,
        verge: Boolean
    ): String? {
        val request = opprettTilbakekrevingBuilder.requestBuilder(
            eksternFagsakId,
            fagsystem,
            ytelsestype,
            varsel,
            verge
        )
        return rest.postOgVerifiser(BEHANDLING_URL_V1, request, Ressurs.Status.SUKSESS)
    }

    fun hentFagsak(fagsystem: Fagsystem, eksternFagsakId: String): Ressurs<Fagsak> {
        val uri = URI.create("$FAGSAK_URL_V1?fagsystem=$fagsystem&fagsak=$eksternFagsakId")
        return getForEntity(uri)
    }

    fun hentBehandling(behandlingId: String): Ressurs<Behandling> {
        val uri = URI.create("$BEHANDLING_URL_V1/$behandlingId")
        return getForEntity(uri)
    }

    fun hentBehandlingId(fagsystem: Fagsystem, eksternFagsakId: String, eksternBrukId: String?): String {
        hentFagsak(fagsystem, eksternFagsakId).data?.behandlinger?.forEach {
            if (it.eksternBrukId.toString() == eksternBrukId) {
                return it.behandlingId.toString()
            }
        }
        throw Exception("Fantes ikke noen behandling med eksternBrukId $eksternBrukId på kombinasjonen eksternFagsakId $eksternFagsakId og fagsystem $fagsystem")
    }

    fun behandlingPåVent(behandlingId: String, venteårsak: Venteårsak): Boolean {
        hentBehandling(behandlingId).data?.behandlingsstegsinfo?.forEach {
            if (it.behandlingsstegstatus == Behandlingsstegstatus.VENTER){
                if (it.venteårsak == venteårsak){
                    return true
                }
                throw Exception("Behandling $behandlingId var på vent men med årsak: ${it.venteårsak}. Forventet $venteårsak")
            }
        }
        return false
    }
    fun behandlingISteg(behandlingId: String, behandlingssteg: Behandlingssteg, behandlingsstegstatus: Behandlingsstegstatus): Boolean {
        hentBehandling(behandlingId).data?.behandlingsstegsinfo?.forEach {
            if (it.behandlingssteg == behandlingssteg && it.behandlingsstegstatus == behandlingsstegstatus){
                return true;
            }
        }
        return false
    }
}
