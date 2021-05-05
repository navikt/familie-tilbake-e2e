package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingsresultatstype
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingsstatus
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingssteg
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.domene.dto.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.dto.Venteårsak
import no.nav.familie.tilbake.e2e.domene.builder.BehandleFaktaStegBuilder
import no.nav.familie.tilbake.e2e.domene.builder.BehandleForeldelseStegBuilder
import no.nav.familie.tilbake.e2e.domene.builder.BehandleVilkårsvurderingStegBuilder
import no.nav.familie.tilbake.e2e.domene.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.domene.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.domene.dto.BehandlingPåVentDto
import no.nav.familie.tilbake.e2e.domene.dto.HenleggDto
import no.nav.familie.tilbake.e2e.domene.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.domene.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.klient.OpprettKravgrunnlagBuilder
import no.nav.familie.tilbake.e2e.klient.OpprettTilbakekrevingBuilder
import no.nav.familie.tilbake.e2e.klient.Vent
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import javax.validation.constraints.Max
import kotlin.random.Random

class Saksbehandler(private val familieTilbakeKlient: FamilieTilbakeKlient,
                    private val opprettTilbakekrevingBuilder: OpprettTilbakekrevingBuilder,
                    private val opprettKravgrunnlagBuilder: OpprettKravgrunnlagBuilder,
                    var gjeldendeBehandling: GjeldendeBehandling? = null) {

    /*OPPRETT-metoder*/

    fun opprettTilbakekreving(eksternFagsakId: String,
                              fagsystem: Fagsystem,
                              ytelsestype: Ytelsestype,
                              varsel: Boolean,
                              verge: Boolean, ): String? {
        val request = opprettTilbakekrevingBuilder.opprettTilbakekrevingRequest(eksternFagsakId = eksternFagsakId,
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

    fun opprettKravgrunnlagUtenBehandling(status: KodeStatusKrav,
                                          fagsystem: Fagsystem,
                                          ytelsestype: Ytelsestype,
                                          eksternFagsakId: String,
                                          @Max(6)
                                          antallPerioder: Int,
                                          under4rettsgebyr: Boolean,
                                          muligforeldelse: Boolean) {
        gjeldendeBehandling = GjeldendeBehandling(eksternBehandlingId = Random.nextInt(1000000, 9999999).toString(),
                                                  fagsystem = fagsystem,
                                                  ytelsestype = ytelsestype,
                                                  eksternFagsakId = eksternFagsakId,
                                                  eksternBrukId = null)

        opprettKravgrunnlag(status = status,
                            antallPerioder = antallPerioder,
                            under4rettsgebyr = under4rettsgebyr,
                            muligforeldelse = muligforeldelse)
    }

    fun opprettKravgrunnlag(status: KodeStatusKrav,
                            @Max(29)
                            antallPerioder: Int,
                            under4rettsgebyr: Boolean,
                            muligforeldelse: Boolean,
                            periodeLengde: Int? = 3) {
        assertTrue(
                gjeldendeBehandling != null && gjeldendeBehandling?.fagsystem != null,
                "Fagsystem ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        assertTrue(
                gjeldendeBehandling != null && gjeldendeBehandling?.ytelsestype != null,
                "Ytelsestype ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        assertTrue(
                gjeldendeBehandling != null && gjeldendeBehandling?.eksternFagsakId != null,
                "EksternFagsakId ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")
        assertTrue(
                gjeldendeBehandling != null && gjeldendeBehandling?.eksternBehandlingId != null,
                "EksternBehandlingId ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling")

        val request = opprettKravgrunnlagBuilder.opprettKravgrunnlag(status = status,
                                                                     ytelsestype = gjeldendeBehandling?.ytelsestype!!,
                                                                     eksternFagsakId = gjeldendeBehandling?.eksternFagsakId!!,
                                                                     eksternBehandlingId = gjeldendeBehandling?.eksternBehandlingId!!,
                                                                     kravgrunnlagId = gjeldendeBehandling?.kravgrunnlagId,
                                                                     vedtakId = gjeldendeBehandling?.vedtakId,
                                                                     antallPerioder = antallPerioder,
                                                                     under4rettsgebyr = under4rettsgebyr,
                                                                     muligforeldelse = muligforeldelse,
                                                                     periodeLengde = periodeLengde!!)
        gjeldendeBehandling!!.kravgrunnlagId = request.detaljertKravgrunnlag?.kravgrunnlagId
        gjeldendeBehandling!!.vedtakId = request.detaljertKravgrunnlag?.vedtakId
        familieTilbakeKlient.opprettKravgrunnlag(kravgrunnlag = request)
        println("Sendt inn $status kravgrunnlag med eksternFagsakId: ${gjeldendeBehandling!!.eksternFagsakId}, på ytelsestype: ${gjeldendeBehandling!!.fagsystem}")
    }

    fun opprettStatusmelding(
            status: KodeStatusKrav) {
        assertTrue(
                gjeldendeBehandling != null && gjeldendeBehandling?.vedtakId != null,
                "VedtakId ikke definert. Minst ett kravgrunnlag må være innsendt før Statusmelding kan sendes")
        assertTrue(
                gjeldendeBehandling != null && gjeldendeBehandling?.fagsystem != null,
                "Fagsystem ikke definert. Minst ett kravgrunnlag må være innsendt før Statusmelding kan sendes")
        assertTrue(
                gjeldendeBehandling != null && gjeldendeBehandling?.eksternFagsakId != null,
                "EksternFagsakId ikke definert. Minst ett kravgrunnlag må være innsendt før Statusmelding kan sendes")
        assertTrue(
                gjeldendeBehandling != null && gjeldendeBehandling?.eksternBehandlingId != null,
                "EksternBehandlingId ikke definert. Minst ett kravgrunnlag må være innsendt før Statusmelding kan sendes")
        val request = opprettKravgrunnlagBuilder.opprettStatusmelding(vedtakId = gjeldendeBehandling?.vedtakId!!,
                                                                      kodeStatusKrav = status,
                                                                      ytelsestype = gjeldendeBehandling?.ytelsestype!!,
                                                                      eksternFagsakId = gjeldendeBehandling?.eksternFagsakId!!,
                                                                      eksternBehandlingId = gjeldendeBehandling?.eksternBehandlingId!!)
        familieTilbakeKlient.opprettStatusmelding(statusmelding = request)
        println("Sendt inn $status statusmelding på eksternFagsakId: ${gjeldendeBehandling!!.eksternFagsakId}")
    }

    /*HENT-metoder*/

    fun hentBehandlingId(fagsystem: Fagsystem, eksternFagsakId: String, eksternBrukId: String?) {
        familieTilbakeKlient.hentFagsak(fagsystem, eksternFagsakId)?.behandlinger?.forEach {
            if (it.eksternBrukId.toString() == eksternBrukId) {
                gjeldendeBehandling?.behandlingId = it.behandlingId.toString()
                // return it.behandlingId.toString()
                return
            }
        }
        throw Exception("Fantes ikke noen behandling med eksternBrukId $eksternBrukId på kombinasjonen eksternFagsakId $eksternFagsakId og fagsystem $fagsystem")
    }

    /*HANDLING-metoder*/

    fun behandleFakta(hendelsestype: Hendelsestype, hendelsesundertype: Hendelsesundertype) {
        val hentFaktaResponse = familieTilbakeKlient.hentFakta(gjeldendeBehandling?.behandlingId!!)
        familieTilbakeKlient.behandleSteg(stegdata = BehandleFaktaStegBuilder(hentFaktaResponse = hentFaktaResponse!!,
                                                                              hendelsestype = hendelsestype,
                                                                              hendelsesundertype = hendelsesundertype).build(),
                                          behandlingId = gjeldendeBehandling?.behandlingId!!)
    }

    fun behandleForeldelse(beslutning: Foreldelsesvurderingstype) {
        val hentForeldelseResponse = familieTilbakeKlient.hentForeldelse(gjeldendeBehandling?.behandlingId!!)
        assertTrue(
                hentForeldelseResponse != null,
                "Kunne ikke hente foreldelse som skulle behandles")
        familieTilbakeKlient.behandleSteg(stegdata = BehandleForeldelseStegBuilder(hentForeldelseResponse = hentForeldelseResponse!!,
                                                                                   beslutning = beslutning).build(),
                                          behandlingId = gjeldendeBehandling?.behandlingId!!)
    }

    fun behandleVilkårsvurdering(vilkårvurderingsresultat: Vilkårsvurderingsresultat,
                                 aktsomhet: Aktsomhet? = null,
                                 særligeGrunner: List<SærligGrunn> = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET),
                                 beløpErIBehold: Boolean = true,
                                 andelTilbakekreves: BigDecimal? = null,
                                 beløpTilbakekreves: BigDecimal? = null,
                                 tilbakekrevSmåbeløp: Boolean? = null) {
        val hentVilkårsvurderingResponse = familieTilbakeKlient.hentVilkårsvurdering(gjeldendeBehandling?.behandlingId!!)
        assertTrue(
                hentVilkårsvurderingResponse != null,
                "Kunne ikke hente vilkårsvurdering som skulle behandles")
        familieTilbakeKlient.behandleSteg(stegdata = BehandleVilkårsvurderingStegBuilder(hentVilkårsvurderingResponse = hentVilkårsvurderingResponse!!,
                                                                                         vilkårvurderingsresultat = vilkårvurderingsresultat,
                                                                                         aktsomhet = aktsomhet,
                                                                                         særligeGrunner = særligeGrunner,
                                                                                         beløpErIBehold = beløpErIBehold,
                                                                                         andelTilbakekreves = andelTilbakekreves,
                                                                                         beløpTilbakekreves = beløpTilbakekreves,
                                                                                         tilbakekrevSmåbeløp = tilbakekrevSmåbeløp).build(),
                                          behandlingId = gjeldendeBehandling?.behandlingId!!)
    }

    /** fun behandleForeslåVedtak */

    fun behandleSteg(stegdata: Any, behandlingId: String) {
        familieTilbakeKlient.behandleSteg(stegdata, behandlingId)
    }

    fun settBehandlingPåVent(årsak: Venteårsak, frist: LocalDate) {
        familieTilbakeKlient.settBehandlingPåVent(BehandlingPåVentDto(venteårsak = årsak,
                                                                      tidsfrist = frist),
                                                  behandlingId = gjeldendeBehandling?.behandlingId!!)
        erBehandlingPåVent(årsak)
    }

    fun taBehandlingAvVent() {
        familieTilbakeKlient.taBehandlingAvVent(gjeldendeBehandling?.behandlingId!!)
    }

    fun henleggBehandling(behandlingId: String, behandlingsresultat: Behandlingsresultatstype) {
        /*Denne vil kun fungere i autotest for behandling opprettet med manueltOpprettet = TRUE ettersom automatisk opprettede behandlinger ikke kan henlegges manuelt før etter 6 dager.
        Når manueltOpprettet Tilbakekreving er implementert i familie-tilbake kan denne metoden brukes i en test.
         */
        val data = HenleggDto(behandlingsresultatstype = behandlingsresultat,
                              begrunnelse = "Dette er en automatisk begrunnelse generert av autotest")
        if (behandlingsresultat == Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_MED_BREV) {
            data.fritekst = "Dette er en automatisk fritekst generert av autotest"
        }
        familieTilbakeKlient.henleggBehandling(behandlingId, data)
    }

    /*SJEKK-metoder*/

    fun erBehandlingPåVent(venteårsak: Venteårsak) {
        Vent.til(
                {
                    familieTilbakeKlient.hentBehandling(gjeldendeBehandling?.behandlingId!!)?.behandlingsstegsinfo?.any {
                        it.behandlingsstegstatus == Behandlingsstegstatus.VENTER && it.venteårsak == venteårsak
                    }
                },
                30, "Behandling kom aldri i vent med årsak $venteårsak")
        println("Behandling med behandlingId ${gjeldendeBehandling?.behandlingId!!} er bekreftet på vent med årsak $venteårsak")
    }

    fun erBehandlingISteg(behandlingssteg: Behandlingssteg,
                          behandlingsstegstatus: Behandlingsstegstatus) {
        Vent.til(
                {
                    familieTilbakeKlient.hentBehandling(gjeldendeBehandling?.behandlingId!!)?.behandlingsstegsinfo?.any {
                        it.behandlingssteg == behandlingssteg && it.behandlingsstegstatus == behandlingsstegstatus
                    }
                },
                30, "Behandlingen kom aldri i status $behandlingsstegstatus i steg $behandlingssteg")
        println("Behandling med behandlingsId ${gjeldendeBehandling?.behandlingId!!} er bekreftet i status $behandlingsstegstatus i steg $behandlingssteg")
    }

    fun erBehandlingAvsluttet(behandlingId: String, resultat: Behandlingsresultatstype) {
        Vent.til(
                { familieTilbakeKlient.hentBehandling(behandlingId)?.status == Behandlingsstatus.AVSLUTTET },
                30, "Behandlingen fikk aldri status AVSLUTTET")
        val behandling = familieTilbakeKlient.hentBehandling(behandlingId)
        val henlagttyper = listOf(Behandlingsresultatstype.HENLAGT,
                                  Behandlingsresultatstype.HENLAGT_FEILOPPRETTET,
                                  Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_MED_BREV,
                                  Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_UTEN_BREV,
                                  Behandlingsresultatstype.HENLAGT_TEKNISK_VEDLIKEHOLD,
                                  Behandlingsresultatstype.HENLAGT_KRAVGRUNNLAG_NULLSTILT)
        val iverksatttyper = listOf(Behandlingsresultatstype.INGEN_TILBAKEBETALING,
                                    Behandlingsresultatstype.DELVIS_TILBAKEBETALING,
                                    Behandlingsresultatstype.FULL_TILBAKEBETALING)
        when (resultat) {
            in henlagttyper -> {
                assertTrue(
                        behandling!!.erBehandlingHenlagt,
                        "Behandling var i status AVSLUTTET med resultat $resultat men erBehandlingHenlagt verdi var FALSE")
                assertTrue(behandling.behandlingsstegsinfo.filter {
                    it.behandlingssteg != Behandlingssteg.VARSEL
                }.all {
                    it.behandlingsstegstatus == Behandlingsstegstatus.AVBRUTT
                },
                           "Behandling var i status AVSLUTTET med resultat $resultat men alle behandlingsstegene var ikke i status AVBRUTT")
                assertTrue(
                        behandling.resultatstype == Behandlingsresultatstype.HENLAGT,
                        "Forventet resultat: HENLAGT, Behandlingens resultat: ${behandling.resultatstype}")
            }
            in iverksatttyper -> {
                assertTrue(
                        behandling!!.behandlingsstegsinfo.all {
                            it.behandlingsstegstatus == Behandlingsstegstatus.UTFØRT || it.behandlingsstegstatus == Behandlingsstegstatus.AUTOUTFØRT
                        },
                        "Behandlingen var i status AVSLUTTET med resultat $resultat men alle behandlingsstegene var ikke UTFØRT/AUTOUTFØRT")
                assertTrue(
                        behandling.resultatstype == resultat,
                        "Forventet resultat: $resultat, Behandlingens resultat: ${behandling.resultatstype}")
            }
            else -> {
                throw Exception("Behandling var i status AVSLUTTET men resultattypen angitt var ${behandling!!.resultatstype} som ikke er et gyldig resultat for en avsluttet behandling")
            }
        }
        println("Behandling med behandlingsId $behandlingId er bekreftet avsluttet med resultat $resultat")
    }
}

/* STATE-object */

class GjeldendeBehandling(var fagsystem: Fagsystem?,
                          var ytelsestype: Ytelsestype?,
                          var eksternFagsakId: String?,
                          var eksternBehandlingId: String?,
                          var eksternBrukId: String?,
                          var behandlingId: String? = null,
                          var vedtakId: BigInteger? = null,
                          var kravgrunnlagId: BigInteger? = null)
