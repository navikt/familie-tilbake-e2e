package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.Behandlingssteg
import no.nav.familie.tilbake.e2e.domene.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.domene.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.Venteårsak
import no.nav.familie.tilbake.e2e.domene.steg.dto.BehandlingPåVent
import no.nav.familie.tilbake.e2e.domene.steg.dto.FaktaSteg
import no.nav.familie.tilbake.e2e.domene.steg.dto.FeilutbetaltStegPeriode
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.klient.OpprettKravgrunnlagBuilder
import no.nav.familie.tilbake.e2e.klient.OpprettTilbakekrevingBuilder
import no.nav.familie.tilbake.e2e.klient.Vent
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigInteger
import java.time.LocalDate
import javax.validation.constraints.Max
import kotlin.random.Random

class Saksbehandler(
    private val familieTilbakeKlient: FamilieTilbakeKlient,
    private val opprettTilbakekrevingBuilder: OpprettTilbakekrevingBuilder,
    private val opprettKravgrunnlagBuilder: OpprettKravgrunnlagBuilder,
    var gjeldendeBehandling: GjeldendeBehandling? = null,
) {

    /*OPPRETT-metoder*/

    fun opprettTilbakekreving(
        eksternFagsakId: String,
        fagsystem: Fagsystem,
        ytelsestype: Ytelsestype,
        varsel: Boolean,
        verge: Boolean,
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
        muligforeldelse: Boolean,
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
        muligforeldelse: Boolean,
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
            kravgrunnlagId = gjeldendeBehandling?.kravgrunnlagId,
            vedtakId = gjeldendeBehandling?.vedtakId,
            antallPerioder = antallPerioder,
            under4rettsgebyr = under4rettsgebyr,
            muligforeldelse = muligforeldelse
        )
        gjeldendeBehandling!!.kravgrunnlagId = request.detaljertKravgrunnlag?.kravgrunnlagId
        gjeldendeBehandling!!.vedtakId = request.detaljertKravgrunnlag?.vedtakId
        familieTilbakeKlient.opprettKravgrunnlag(kravgrunnlag = request)
        println("Sendt inn $status kravgrunnlag med eksternFagsakId: ${gjeldendeBehandling!!.eksternFagsakId}, på ytelsestype: ${gjeldendeBehandling!!.fagsystem}")
    }

    fun opprettStatusmelding(
        status: KodeStatusKrav,
    ){
        assertTrue(gjeldendeBehandling != null && gjeldendeBehandling?.vedtakId != null,
            "VedtakId ikke definert. Minst ett kravgrunnlag må være innsendt før Statusmelding kan sendes")
        assertTrue(gjeldendeBehandling != null && gjeldendeBehandling?.fagsystem != null,
            "Fagsystem ikke definert. Minst ett kravgrunnlag må være innsendt før Statusmelding kan sendes")
        assertTrue(gjeldendeBehandling != null && gjeldendeBehandling?.eksternFagsakId != null,
            "EksternFagsakId ikke definert. Minst ett kravgrunnlag må være innsendt før Statusmelding kan sendes")
        assertTrue(gjeldendeBehandling != null && gjeldendeBehandling?.eksternBehandlingId != null,
            "EksternBehandlingId ikke definert. Minst ett kravgrunnlag må være innsendt før Statusmelding kan sendes")
        val request = opprettKravgrunnlagBuilder.opprettStatusmelding(
            vedtakId = gjeldendeBehandling?.vedtakId!!,
            kodeStatusKrav = status,
            fagområde = gjeldendeBehandling?.fagsystem!!,
            eksternFagsakId = gjeldendeBehandling?.eksternFagsakId!!,
            eksternBehandlingId = gjeldendeBehandling?.eksternBehandlingId!!
        )
        familieTilbakeKlient.opprettStatusmelding(statusmelding = request)
        println("Sendt inn $status statusmelding på eksternFagsakId: ${gjeldendeBehandling!!.eksternFagsakId}")
    }

    /*HENT-metoder*/

    fun hentBehandlingId(fagsystem: Fagsystem, eksternFagsakId: String, eksternBrukId: String?): String {
        familieTilbakeKlient.hentFagsak(fagsystem, eksternFagsakId)?.behandlinger?.forEach {
            if (it.eksternBrukId.toString() == eksternBrukId) {
                gjeldendeBehandling?.behandlingId = it.behandlingId.toString()
                return it.behandlingId.toString()
            }
        }
        throw Exception("Fantes ikke noen behandling med eksternBrukId $eksternBrukId på kombinasjonen eksternFagsakId $eksternFagsakId og fagsystem $fagsystem")
    }

    fun hentBehandlingssteg(stegtype: Behandlingssteg, behandlingId: String): Any? {
        when (stegtype) {
            Behandlingssteg.FAKTA -> {
                val feilutbetaltePerioderList: MutableList<FeilutbetaltStegPeriode> = mutableListOf()
                    familieTilbakeKlient.hentFakta(behandlingId)?.feilutbetaltePerioder?.forEach {
                    feilutbetaltePerioderList.add(FeilutbetaltStegPeriode(periode = it.periode))
                }
                return FaktaSteg(feilutbetaltePerioder = feilutbetaltePerioderList)
            }
            Behandlingssteg.FORELDELSE -> {
                //TODO
                return null
            }
            Behandlingssteg.VILKÅRSVURDERING -> {
                //TODO
                return null
            }
            Behandlingssteg.FORESLÅ_VEDTAK -> {
                //TODO
                return null
            }
            Behandlingssteg.FATTE_VEDTAK -> {
                //TODO
                return null
            }
            Behandlingssteg.VERGE -> {
                //TODO
                return null
            }
            else -> {
                throw Exception("Behandlingssteg $stegtype kan ikke behandles av saksbehandler")
            }
        }
    }

    /*HANDLING-metoder*/

    fun behandleSteg(stegdata: Any, behandlingId: String){
        familieTilbakeKlient.behandleSteg(stegdata, behandlingId)
    }

    fun settBehandlingPåVent(behandlingId: String, årsak: Venteårsak, frist: LocalDate){
        familieTilbakeKlient.settBehandlingPåVent(BehandlingPåVent(
            behandlingId = behandlingId,
            venteårsak = årsak,
            tidsfrist = frist
        ))
        erBehandlingPåVent(behandlingId, årsak)
    }

    fun taBehandlingAvVent(behandlingId: String){
        familieTilbakeKlient.taBehandlingAvVent(behandlingId)
    }

    /*SJEKK-metoder*/

    fun erBehandlingPåVent(behandlingId: String, venteårsak: Venteårsak) {
        Vent.til( {familieTilbakeKlient.hentBehandling(behandlingId)?.behandlingsstegsinfo?.any {
            it.behandlingsstegstatus == Behandlingsstegstatus.VENTER && it.venteårsak == venteårsak
        } },
            30, "Behandling kom aldri i vent med årsak $venteårsak")
        println("Behandling med behandlingId $behandlingId er bekreftet på vent med årsak $venteårsak")
    }

    fun erBehandlingISteg(behandlingId: String, behandlingssteg: Behandlingssteg, behandlingsstegstatus: Behandlingsstegstatus) {
        Vent.til( { familieTilbakeKlient.hentBehandling(behandlingId)?.behandlingsstegsinfo?.any {
            it.behandlingssteg == behandlingssteg && it.behandlingsstegstatus == behandlingsstegstatus
        } },
            30, "Behandlingen kom aldri i status $behandlingsstegstatus i steg $behandlingssteg")
        println("Behandling med behandlingsId $behandlingId er bekreftet i status $behandlingsstegstatus i steg $behandlingssteg")
    }
}

/*STATE-object*/
class GjeldendeBehandling(
    var fagsystem: Fagsystem?,
    var ytelsestype: Ytelsestype?,
    var eksternFagsakId: String?,
    var eksternBehandlingId: String?,
    var eksternBrukId: String?,
    var behandlingId: String? = null,
    var vedtakId: BigInteger? = null,
    var kravgrunnlagId: BigInteger? = null,
)

