package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.Behandlingssteg
import no.nav.familie.tilbake.e2e.domene.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.domene.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.Venteårsak
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.klient.OpprettKravgrunnlagBuilder
import no.nav.familie.tilbake.e2e.klient.OpprettTilbakekrevingBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import javax.validation.constraints.Max
import kotlin.random.Random

class Saksbehandler(private val familieTilbakeKlient: FamilieTilbakeKlient,
                    private val opprettTilbakekrevingBuilder: OpprettTilbakekrevingBuilder,
                    private val opprettKravgrunnlagBuilder: OpprettKravgrunnlagBuilder,
                    var gjeldendeBehandling: GjeldendeBehandling? = null
) {

    fun opprettTilbakekreving(
            eksternFagsakId: String,
            fagsystem: Fagsystem,
            ytelsestype: Ytelsestype,
            varsel: Boolean,
            verge: Boolean
    ): String? {
        val request = opprettTilbakekrevingBuilder.opprettTilbakekrevingRequest(
                eksternFagsakId = eksternFagsakId,
                fagsystem = fagsystem,
                ytelsestype = ytelsestype,
                varsel = varsel,
                verge = verge
        )
        val eksternBrukId = familieTilbakeKlient.opprettTilbakekreving(request)
        println("Opprettet behandling med eksternFagsakId: $eksternFagsakId og eksternBrukId: $eksternBrukId")

        gjeldendeBehandling = GjeldendeBehandling(eksternFagsakId = eksternFagsakId,
                                                  fagsystem = fagsystem,
                                                  ytelsestype = ytelsestype,
                                                  eksternBehandlingId = request.eksternId,
                                                  eksternBrukId = eksternBrukId)
        return eksternBrukId
    }

    fun opprettKravgrunnlagUtenBehandling(
            status: KodeStatusKrav,
            fagsystem: Fagsystem,
            ytelsestype: Ytelsestype,
            eksternFagsakId: String,
            @Max(6)
            antallPerioder: Int,
            under4rettsgebyr: Boolean,
            muligforeldelse: Boolean
    ) {
        gjeldendeBehandling = GjeldendeBehandling(eksternBehandlingId = Random.nextInt(1000000, 9999999).toString(),
            fagsystem = fagsystem,
            ytelsestype = ytelsestype,
            eksternFagsakId = eksternFagsakId,
            eksternBrukId = null)

        opprettKravgrunnlag(
                status = status,
                antallPerioder = antallPerioder,
                under4rettsgebyr = under4rettsgebyr,
                muligforeldelse = muligforeldelse
        )
    }

    fun opprettKravgrunnlag(
            status: KodeStatusKrav,
            @Max(29)
            antallPerioder: Int,
            under4rettsgebyr: Boolean,
            muligforeldelse: Boolean
    ) {
        assertTrue(gjeldendeBehandling != null && gjeldendeBehandling?.fagsystem != null,
                             "Fagsystem ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        assertTrue(gjeldendeBehandling != null && gjeldendeBehandling?.ytelsestype != null,
                             "Ytelsestype ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        assertTrue(gjeldendeBehandling != null && gjeldendeBehandling?.eksternFagsakId != null,
                             "EksternFagsakId ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        assertTrue(gjeldendeBehandling != null && gjeldendeBehandling?.eksternBehandlingId != null,
                             "EksternBehandlingId ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")

        val request = opprettKravgrunnlagBuilder.opprettKravgrunnlag(
                status = status,
                fagområde = gjeldendeBehandling?.fagsystem!!,
                ytelsestype = gjeldendeBehandling?.ytelsestype!!,
                eksternFagsakId = gjeldendeBehandling?.eksternFagsakId!!,
                eksternBehandlingId = gjeldendeBehandling?.eksternBehandlingId!!,
                kravgrunnlagId =  gjeldendeBehandling?.kravgrunnlagId,
                vedtakId =  gjeldendeBehandling?.vedtakId,
                antallPerioder = antallPerioder,
                under4rettsgebyr = under4rettsgebyr,
                muligforeldelse = muligforeldelse
        )
        gjeldendeBehandling!!.kravgrunnlagId = request.detaljertKravgrunnlagMelding.detaljertKravgrunnlag.kravgrunnlagId
        gjeldendeBehandling!!.vedtakId = request.detaljertKravgrunnlagMelding.detaljertKravgrunnlag.vedtakId
        familieTilbakeKlient.opprettKravgrunnlag(kravgrunnlag = request)
    }

    fun hentBehandlingId(fagsystem: Fagsystem, eksternFagsakId: String, eksternBrukId: String?): String {
        familieTilbakeKlient.hentFagsak(fagsystem, eksternFagsakId)?.behandlinger?.forEach {
            if (it.eksternBrukId.toString() == eksternBrukId) {
                gjeldendeBehandling?.behandlingId = it.behandlingId.toString()
                return it.behandlingId.toString()
            }
        }
        throw Exception("Fantes ikke noen behandling med eksternBrukId $eksternBrukId på kombinasjonen eksternFagsakId $eksternFagsakId og fagsystem $fagsystem")
    }

    fun erBehandlingPåVent(behandlingId: String, venteårsak: Venteårsak): Boolean {
        familieTilbakeKlient.hentBehandling(behandlingId)?.behandlingsstegsinfo?.forEach {
            if (it.behandlingsstegstatus == Behandlingsstegstatus.VENTER) {
                if (it.venteårsak == venteårsak) {
                    return true
                }
                throw Exception("Behandling $behandlingId var på vent men med årsak: ${it.venteårsak}. Forventet $venteårsak")
            }
        }
        return false
    }

    fun erBehandlingISteg(behandlingId: String,
                          behandlingssteg: Behandlingssteg,
                          behandlingsstegstatus: Behandlingsstegstatus): Boolean {
        familieTilbakeKlient.hentBehandling(behandlingId)?.behandlingsstegsinfo?.forEach {
            if (it.behandlingssteg == behandlingssteg && it.behandlingsstegstatus == behandlingsstegstatus) {
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
        var behandlingId: String? = null,
        var vedtakId: Int? = null,
        var kravgrunnlagId: Int? = null
)

