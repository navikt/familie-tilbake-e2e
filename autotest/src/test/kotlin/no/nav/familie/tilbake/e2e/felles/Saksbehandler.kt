package no.nav.familie.tilbake.e2e.felles

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingsresultatstype
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingsstatus
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingssteg
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.domene.dto.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.dto.Venteårsak
import no.nav.familie.tilbake.e2e.domene.builder.BehandleFaktaStegBuilder
import no.nav.familie.tilbake.e2e.domene.builder.BehandleForeldelseStegBuilder
import no.nav.familie.tilbake.e2e.domene.builder.BehandleForeslåVedtakBuilder
import no.nav.familie.tilbake.e2e.domene.builder.BehandleVilkårsvurderingStegBuilder
import no.nav.familie.tilbake.e2e.domene.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.domene.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.domene.dto.BehandlingPåVentDto
import no.nav.familie.tilbake.e2e.domene.dto.HenleggDto
import no.nav.familie.tilbake.e2e.domene.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.domene.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.domene.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.domene.builder.BestillBrevBuilder
import no.nav.familie.tilbake.e2e.domene.builder.ForhåndsvisHenleggelsesbrevBuilder
import no.nav.familie.tilbake.e2e.domene.builder.ForhåndsvisVarselbrevBuilder
import no.nav.familie.tilbake.e2e.domene.builder.ForhåndsvisVedtaksbrevBuilder
import no.nav.familie.tilbake.e2e.domene.builder.KravgrunnlagBuilder
import no.nav.familie.tilbake.e2e.domene.builder.StatusmeldingBuilder
import no.nav.familie.tilbake.e2e.domene.builder.TilbakekrevingBuilder
import no.nav.familie.tilbake.e2e.domene.dto.Dokumentmalstype
import no.nav.familie.tilbake.e2e.domene.dto.felles.Periode
import no.nav.familie.tilbake.e2e.felles.utils.LogiskPeriode.utledLogiskPeriode
import no.nav.familie.tilbake.e2e.felles.utils.Vent
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

class Saksbehandler(private val familieTilbakeKlient: FamilieTilbakeKlient) {

    private lateinit var gjeldendeBehandling: GjeldendeBehandling

    /*OPPRETT-metoder*/

    fun opprettTilbakekreving(eksternFagsakId: String,
                              fagsystem: Fagsystem,
                              ytelsestype: Ytelsestype,
                              varsel: Boolean,
                              verge: Boolean): String {
        val request = TilbakekrevingBuilder(eksternFagsakId = eksternFagsakId,
                                            fagsystem = fagsystem,
                                            ytelsestype = ytelsestype,
                                            varsel = varsel,
                                            verge = verge).build()

        val eksternBrukId = familieTilbakeKlient.opprettTilbakekreving(data = request).data
        val behandlingId = hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)

        gjeldendeBehandling = GjeldendeBehandling(fagsystem = fagsystem,
                                                  ytelsestype = ytelsestype,
                                                  eksternFagsakId = eksternFagsakId,
                                                  eksternBehandlingId = request.eksternId,
                                                  eksternBrukId = eksternBrukId,
                                                  behandlingId = behandlingId)

        println("Opprettet behandling med eksternFagsakId: $eksternFagsakId og eksternBrukId: $eksternBrukId")

        return requireNotNull(eksternBrukId)
    }

    fun opprettKravgrunnlagUtenBehandling(status: KodeStatusKrav,
                                          fagsystem: Fagsystem,
                                          ytelsestype: Ytelsestype,
                                          eksternFagsakId: String,
                                          antallPerioder: Int,
                                          under4rettsgebyr: Boolean,
                                          muligforeldelse: Boolean) {
        // TODO: Fullfør implementasjon når funksjonalitet kommer


        gjeldendeBehandling = GjeldendeBehandling(fagsystem = fagsystem,
                                                  ytelsestype = ytelsestype,
                                                  eksternFagsakId = eksternFagsakId,
                                                  eksternBehandlingId = Random.nextInt(1000000, 9999999).toString())

        opprettKravgrunnlag(status = status,
                            antallPerioder = antallPerioder,
                            under4rettsgebyr = under4rettsgebyr,
                            muligforeldelse = muligforeldelse)
    }

    fun opprettKravgrunnlag(status: KodeStatusKrav,
                            antallPerioder: Int = 2,
                            under4rettsgebyr: Boolean,
                            muligforeldelse: Boolean,
                            periodelengde: Int = 3) {
        val request = KravgrunnlagBuilder(status = status,
                                          ytelsestype = requireNotNull(gjeldendeBehandling.ytelsestype,
                                                                       { "Ytelsestype ikke definert. Opprett behandling" +
                                                                         " først eller bruk opprettKravgrunnlagUtenBehandling." }),
                                          eksternFagsakId = requireNotNull(gjeldendeBehandling.eksternFagsakId,
                                                                           { "EksternFagsakId ikke definert. Opprett behandling" +
                                                                             " først eller bruk opprettKravgrunnlagUtenBehandling." }),
                                          eksternBehandlingId = requireNotNull(gjeldendeBehandling.eksternBehandlingId,
                                                                               { "EksternBehandlingId ikke definert. Opprett behandling først" +
                                                                                 " eller bruk opprettKravgrunnlagUtenBehandling." }),
                                          kravgrunnlagId = gjeldendeBehandling.kravgrunnlagId,
                                          vedtakId = gjeldendeBehandling.vedtakId,
                                          antallPerioder = antallPerioder,
                                          under4rettsgebyr = under4rettsgebyr,
                                          muligforeldelse = muligforeldelse,
                                          periodeLengde = periodelengde).build()

        familieTilbakeKlient.opprettKravgrunnlag(kravgrunnlag = request)

        gjeldendeBehandling.apply {
            kravgrunnlagId = request.detaljertKravgrunnlag.kravgrunnlagId
            vedtakId = request.detaljertKravgrunnlag.vedtakId
            perioder = utledLogiskPeriode(request.detaljertKravgrunnlag.tilbakekrevingsPeriode.map {
                Periode(fom = it.periode.fom,
                        tom = it.periode.tom)
            })
        }

        println("Sendt inn $status kravgrunnlag med eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId}" +
                        "på ytelsestype: ${gjeldendeBehandling.fagsystem}")
    }

    fun opprettStatusmelding(status: KodeStatusKrav) {
        val request = StatusmeldingBuilder(kodeStatusKrav = status,
                                           vedtakId = requireNotNull(gjeldendeBehandling.vedtakId,
                                                                     { "VedtakId ikke definert. Opprett kravgrunnlag først." }),
                                           ytelsestype = requireNotNull(gjeldendeBehandling.ytelsestype,
                                                                        { "Ytelsestype ikke definert. Opprett kravgrunnlag først." }),
                                           eksternFagsakId = requireNotNull(gjeldendeBehandling.eksternFagsakId,
                                                                            { "EksternFagsakId ikke definert. Opprett kravgrunnlag først." }),
                                           eksternBehandlingId = requireNotNull(gjeldendeBehandling.eksternBehandlingId,
                                                                                { "EksternBehandlingId ikke definert. Opprett kravgrunnlag først." }))
                .build()

        familieTilbakeKlient.opprettStatusmelding(statusmelding = request)

        println("Sendt inn statusmelding $status på eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId}")
    }

    /*HENT-metoder*/

    fun hentBehandlingId(fagsystem: Fagsystem, eksternFagsakId: String, eksternBrukId: String?): String {
        familieTilbakeKlient.hentFagsak(fagsystem, eksternFagsakId).data?.behandlinger?.forEach {
            if (it.eksternBrukId.toString() == eksternBrukId) {
                return it.behandlingId.toString()
            }
        }
        throw Exception("Fant ingen behandling med eksternBrukId: $eksternBrukId med eksternFagsakId: $eksternFagsakId" +
                                " og fagsystem: $fagsystem")
    }

    /*HANDLING-metoder*/

    fun behandleFakta(hendelsestype: Hendelsestype, hendelsesundertype: Hendelsesundertype) {
        val hentFaktaResponse =
            requireNotNull(familieTilbakeKlient.hentFakta(gjeldendeBehandling.behandlingId!!).data)
            { "Kunne ikke hente data for behandling av fakta" }
        val request = BehandleFaktaStegBuilder(hentFaktaResponse = hentFaktaResponse,
                                               hendelsestype = hendelsestype,
                                               hendelsesundertype = hendelsesundertype).build()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId!!, data = request)
    }

    fun behandleForeldelse(beslutning: Foreldelsesvurderingstype) {
        val hentForeldelseResponse =
            requireNotNull(familieTilbakeKlient.hentForeldelse(gjeldendeBehandling.behandlingId!!).data)
            { "Kunne ikke hente data for behandling av foreldelse" }
        val request = BehandleForeldelseStegBuilder(hentForeldelseResponse = hentForeldelseResponse,
                                                    beslutning = beslutning).build()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId!!, data = request)
    }

    fun behandleVilkårsvurdering(vilkårvurderingsresultat: Vilkårsvurderingsresultat,
                                 aktsomhet: Aktsomhet? = null,
                                 særligeGrunner: List<SærligGrunn> = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET),
                                 beløpErIBehold: Boolean = true,
                                 andelTilbakekreves: BigDecimal? = null,
                                 beløpTilbakekreves: BigDecimal? = null,
                                 tilbakekrevSmåbeløp: Boolean? = null) {
        val hentVilkårsvurderingResponse =
            requireNotNull(familieTilbakeKlient.hentVilkårsvurdering(gjeldendeBehandling.behandlingId!!).data)
            { "Kunne ikke hente data for behandling av vilkår" }
        val request = BehandleVilkårsvurderingStegBuilder(hentVilkårsvurderingResponse = hentVilkårsvurderingResponse,
                                                          vilkårvurderingsresultat = vilkårvurderingsresultat,
                                                          aktsomhet = aktsomhet,
                                                          særligeGrunner = særligeGrunner,
                                                          beløpErIBehold = beløpErIBehold,
                                                          andelTilbakekreves = andelTilbakekreves,
                                                          beløpTilbakekreves = beløpTilbakekreves,
                                                          tilbakekrevSmåbeløp = tilbakekrevSmåbeløp).build()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId!!, data = request)
    }

    fun behandleForeslåVedtak() {
        val hentVedtakbrevtekstResponse =
            requireNotNull(familieTilbakeKlient.hentVedtaksbrevtekst(gjeldendeBehandling.behandlingId!!).data)
            { "Kunne ikke hente data for behandling av foreslå vedtak" }
        val request = BehandleForeslåVedtakBuilder(hentVedtakbrevtekstResponse = hentVedtakbrevtekstResponse).build()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId!!, data = request)
    }

    fun settBehandlingPåVent(venteårsak: Venteårsak, tidsfrist: LocalDate) {
        val request = BehandlingPåVentDto(venteårsak = venteårsak,
                                          tidsfrist = tidsfrist)
        familieTilbakeKlient.settBehandlingPåVent(behandlingId = gjeldendeBehandling.behandlingId!!, data = request)
        erBehandlingPåVent(venteårsak)
    }

    fun taBehandlingAvVent() {
        familieTilbakeKlient.taBehandlingAvVent(gjeldendeBehandling.behandlingId!!)
    }

    fun henleggBehandling(behandlingId: String, behandlingsresultat: Behandlingsresultatstype) {
        /* Denne vil kun fungere i autotest for behandling opprettet med manueltOpprettet = TRUE ettersom automatisk opprettede
         behandlinger ikke kan henlegges manuelt før etter 6 dager.
         Når manueltOpprettet Tilbakekreving er implementert i familie-tilbake kan denne metoden brukes i en test.
         */
        val request = HenleggDto(behandlingsresultatstype = behandlingsresultat,
                                 begrunnelse = "Dette er en automatisk begrunnelse generert av autotest")
        if (behandlingsresultat == Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_MED_BREV) {
            request.fritekst = "Dette er en automatisk fritekst generert av autotest"
        }

        familieTilbakeKlient.henleggBehandling(behandlingId = behandlingId, data = request)
    }

    /*SJEKK-metoder*/

    fun erBehandlingPåVent(venteårsak: Venteårsak) {
        Vent.til(
                {
                    familieTilbakeKlient.hentBehandling(gjeldendeBehandling.behandlingId!!).data?.behandlingsstegsinfo?.any {
                        it.behandlingsstegstatus == Behandlingsstegstatus.VENTER && it.venteårsak == venteårsak
                    }
                },
                30, "Behandling kom aldri i vent med årsak $venteårsak")
        println("Behandling med behandlingId ${gjeldendeBehandling.behandlingId!!} er bekreftet på vent med årsak $venteårsak")
    }

    fun erBehandlingISteg(behandlingssteg: Behandlingssteg,
                          behandlingsstegstatus: Behandlingsstegstatus) {
        Vent.til(
                {
                    familieTilbakeKlient.hentBehandling(gjeldendeBehandling.behandlingId!!).data?.behandlingsstegsinfo?.any {
                        it.behandlingssteg == behandlingssteg && it.behandlingsstegstatus == behandlingsstegstatus
                    }
                },
                30, "Behandlingen kom aldri i status $behandlingsstegstatus i steg $behandlingssteg")
        println("Behandling med behandlingsId ${gjeldendeBehandling.behandlingId!!} er bekreftet i status" +
                        " $behandlingsstegstatus i steg $behandlingssteg")
    }

    fun erBehandlingAvsluttet(resultat: Behandlingsresultatstype) {
        Vent.til(
                { familieTilbakeKlient.hentBehandling(gjeldendeBehandling.behandlingId!!).data?.status == Behandlingsstatus.AVSLUTTET },
                30, "Behandlingen fikk aldri status AVSLUTTET")
        val behandling = requireNotNull(familieTilbakeKlient.hentBehandling(gjeldendeBehandling.behandlingId!!).data)
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
                        behandling.erBehandlingHenlagt,
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
                        behandling.behandlingsstegsinfo.all {
                            it.behandlingsstegstatus == Behandlingsstegstatus.UTFØRT || it.behandlingsstegstatus == Behandlingsstegstatus.AUTOUTFØRT
                        },
                        "Behandlingen var i status AVSLUTTET med resultat $resultat men alle behandlingsstegene var ikke UTFØRT/AUTOUTFØRT")
                assertTrue(
                        behandling.resultatstype == resultat,
                        "Forventet resultat: $resultat, Behandlingens resultat: ${behandling.resultatstype}")
            }
            else -> {
                throw Exception("Behandling var i status AVSLUTTET men resultattypen angitt var ${behandling.resultatstype}" +
                                        " som ikke er et gyldig resultat for en avsluttet behandling")
            }
        }
        println("Behandling med behandlingsId ${gjeldendeBehandling.behandlingId} er bekreftet avsluttet med resultat $resultat")
    }

    fun bestillBrev(dokumentmalstype: Dokumentmalstype) {
        val request = BestillBrevBuilder(behandlingId = gjeldendeBehandling.behandlingId!!,
                                         dokumentmalstype = dokumentmalstype).build()

        familieTilbakeKlient.bestillBrev(data = request)
    }

    fun forhåndsvisVedtaksbrev() {
        val request = ForhåndsvisVedtaksbrevBuilder(behandlingId = gjeldendeBehandling.behandlingId!!,
                                                    perioder = gjeldendeBehandling.perioder!!).build()

        familieTilbakeKlient.forhåndsvisVedtaksbrev(data = request)
    }

    fun forhåndsvisVarselbrev(vedtakdato: LocalDate) {
        val hentBehandlingResponse = familieTilbakeKlient.hentBehandling(behandlingId = gjeldendeBehandling.behandlingId!!).data

        val request = ForhåndsvisVarselbrevBuilder(behandlendeEnhetId = hentBehandlingResponse?.enhetskode ?: "0106",
                                                   behandlendeEnhetsNavn = hentBehandlingResponse?.enhetsnavn ?: "NAV Fredrikstad",
                                                   eksternFagsakId = gjeldendeBehandling.eksternFagsakId,
                                                   fagsystem = gjeldendeBehandling.fagsystem,
                                                   perioder = requireNotNull(gjeldendeBehandling.perioder),
                                                   ident = "31079221655", // Kan hentes fra hentFagsak
                                                   saksbehandlerIdent = hentBehandlingResponse?.ansvarligSaksbehandler ?: "VL",
                                                   språkkode = Språkkode.NB,
                                                   vedtakdato = vedtakdato,
                                                   verge = null, // TODO: Implementer sjekk på harVerge i hentBehandlingResponse
                                                   ytelsestype = gjeldendeBehandling.ytelsestype).build()

        familieTilbakeKlient.forhåndsvisVarselbrev(data = request)
    }

    fun forhåndsvisHenleggelsesbrev() {
        val request = ForhåndsvisHenleggelsesbrevBuilder(behandlingId = gjeldendeBehandling.behandlingId!!).build()

        familieTilbakeKlient.forhåndsvisHenleggelsesbrev(data = request)
    }
}
