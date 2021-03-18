package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.*
import no.nav.familie.tilbake.e2e.domene.steg.Fakta
import no.nav.familie.tilbake.e2e.util.JsonRest
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI
import javax.validation.constraints.Max
import kotlin.random.Random

@Service
class FamilieTilbakeKlient(@Value("\${FAMILIE_TILBAKE_API_URL}") private val familieTilbakeApiUrl: String,
                           restOperations: RestOperations,
                           private val opprettTilbakekrevingBuilder: OpprettTilbakekrevingBuilder,
                           private val opprettKravgrunnlagBuilder: OpprettKravgrunnlagBuilder) : JsonRest(restOperations) {
    private final val API_URL: String = "$familieTilbakeApiUrl/api"
    private final val VERSION_URL: URI = URI.create("$API_URL/info")
    private final val BEHANDLING_BASE: URI = URI.create("$API_URL/behandling")
    private final val BEHANDLING_URL_V1: URI = URI.create("$BEHANDLING_BASE/v1")
    private final val FAGSAK_URL_V1: URI = URI.create("$API_URL/fagsak/v1")

    private final val AUTOTEST_API: URI = URI.create("$API_URL/autotest")
    private final val OPPRETT_KRAVGRUNNLAG_URI: URI = URI.create("$AUTOTEST_API/opprett/kravgrunnlag")
    private final val OPPRETT_STATUSMELDING_URI: URI = URI.create("$AUTOTEST_API/opprett/statusmelding")

    private lateinit var gjeldndeBehandling: GjeldendeBehandling

    /*OPPRETT METODER*/

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
            eksternFagsakId = eksternFagsakId,
            eksternBehandlingId = gjeldndeBehandling.eksternBehandlingId,
            fagsystem = fagsystem,
            ytelsestype = ytelsestype,
            varsel = varsel,
            verge = verge
        )
        gjeldndeBehandling.eksternBehandlingId = request.eksternId
        gjeldndeBehandling.eksternBrukId = postOgHentData(BEHANDLING_URL_V1, request, Ressurs.Status.SUKSESS)
        return gjeldndeBehandling.eksternBrukId
    }

    fun opprettKravgrunnlagUtenBehandling(
        status: KodeStatusKrav,
        fagsystem: Fagsystem,
        ytelsestype: Ytelsestype,
        eksternFagsakId: String,
        @Max(29)
        antallPerioder: Int,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean
    ): GjeldendeBehandling {
        gjeldndeBehandling.eksternBehandlingId = Random.nextInt(1000000, 9999999).toString()
        gjeldndeBehandling.fagsystem = fagsystem
        gjeldndeBehandling.ytelsestype = ytelsestype
        gjeldndeBehandling.eksternFagsakId = eksternFagsakId
        opprettKravgrunnlag(
            status, antallPerioder, under4rettsgebyr, muligforeldelse
        )
        return gjeldndeBehandling
    }

    fun opprettKravgrunnlag(
        status: KodeStatusKrav,
        @Max(29)
        antallPerioder: Int,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean
    ) {
        assertTrue(gjeldndeBehandling.fagsystem != null, "Fagsystem ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        assertTrue(gjeldndeBehandling.ytelsestype != null, "Ytelsestype ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        assertTrue(gjeldndeBehandling.eksternFagsakId != null, "EksternFagsakId ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        assertTrue(gjeldndeBehandling.eksternBehandlingId != null, "EksternBehandlingId ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        val request = opprettKravgrunnlagBuilder.requestBuilder(
            status = status,
            fagområde = gjeldndeBehandling.fagsystem!!,
            ytelsestype = gjeldndeBehandling.ytelsestype!!,
            eksternFagsakId = gjeldndeBehandling.eksternFagsakId!!,
            eksternBehandlingId = gjeldndeBehandling.eksternBehandlingId!!,
            kravgrunnlagId = gjeldndeBehandling.kravgrunnlagId,
            vedtakId = gjeldndeBehandling.vedtakId,
            antallPerioder = antallPerioder,
            under4rettsgebyr = under4rettsgebyr,
            muligforeldelse = muligforeldelse
        )
        postOgVerifiser(OPPRETT_KRAVGRUNNLAG_URI, request!!, Ressurs.Status.SUKSESS)
    }

    /*HENT METODER*/

    fun hentVersjonInfo(): VersjonInfo? {
        val uri = URI.create("$VERSION_URL")
        return getOgHentData(uri)
    }

    private fun hentFagsak(fagsystem: Fagsystem, eksternFagsakId: String): Fagsak? {
        val uri = URI.create("$FAGSAK_URL_V1?fagsystem=$fagsystem&fagsak=$eksternFagsakId")
        return getOgHentData(uri)
    }

    fun hentBehandling(behandlingId: String): Behandling? {
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

    private fun hentFakta(behandlingId: String): Fakta? {
        val uri = URI.create("$BEHANDLING_BASE/$behandlingId/fakta/v1")
        return getOgHentData(uri)
    }

    /*BEKREFTELSES METODER (aka sjekker)*/

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
    var vedtakId: Int?,
    var kravgrunnlagId: Int?
)
