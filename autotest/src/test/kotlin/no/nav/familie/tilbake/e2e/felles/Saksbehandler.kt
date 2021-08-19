package no.nav.familie.tilbake.e2e.felles

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.tilbake.e2e.klienter.FamilieHistorikkKlient
import no.nav.familie.tilbake.e2e.klienter.dto.historikkinnslag.TilbakekrevingHistorikkinnslagstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingsresultatstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingsstatus
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingssteg
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.KodeStatusKrav
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Venteårsak
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleForeldelseData
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleForeslåVedtakData
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleVilkårsvurderingData
import no.nav.familie.tilbake.e2e.klienter.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsestype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsesundertype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.BehandlingPåVentDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.HenleggDto
import no.nav.familie.tilbake.e2e.klienter.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.klienter.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.klienter.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleFaktaData
import no.nav.familie.tilbake.e2e.felles.datagenerator.BestillBrevData
import no.nav.familie.tilbake.e2e.felles.datagenerator.ForhåndsvisHenleggelsesbrevData
import no.nav.familie.tilbake.e2e.felles.datagenerator.ForhåndsvisVarselbrevData
import no.nav.familie.tilbake.e2e.felles.datagenerator.ForhåndsvisVedtaksbrevBuilder
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleFatteVedtakData
import no.nav.familie.tilbake.e2e.felles.datagenerator.KravgrunnlagData
import no.nav.familie.tilbake.e2e.felles.datagenerator.StatusmeldingData
import no.nav.familie.tilbake.e2e.felles.datagenerator.TilbakekrevingData
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Dokumentmalstype
import no.nav.familie.tilbake.e2e.felles.utils.LogiskPeriodeUtil.utledLogiskPeriodeFraKravgrunnlag
import no.nav.familie.tilbake.e2e.felles.utils.Vent
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import java.time.LocalDate

class Saksbehandler(private val familieTilbakeKlient: FamilieTilbakeKlient,
                    private val familieHistorikkKlient: FamilieHistorikkKlient? = null) {


    companion object {

        const val SAKSBEHANDLER_IDENT = "Z994619" // Denne må oppdateres når bruker går ut i IDA
        const val BESLUTTER_IDENT = "Z994623" // Denne må oppdateres når bruker går ut i IDA
    }

    private lateinit var gjeldendeBehandling: GjeldendeBehandling

    fun opprettTilbakekreving(scenario: Scenario,
                              varsel: Boolean,
                              verge: Boolean,
                              saksbehandlerIdent: String = SAKSBEHANDLER_IDENT,
                              sumFeilutbetaling: BigDecimal? = null) {
        val data = TilbakekrevingData(eksternFagsakId = scenario.eksternFagsakId,
                                      eksternBehandlingId = scenario.eksternBehandlingId,
                                      fagsystem = scenario.fagsystem,
                                      ytelsestype = scenario.ytelsestype,
                                      personIdent = scenario.personIdent,
                                      enhetId = scenario.enhetId,
                                      enhetsnavn = scenario.enhetsnavn,
                                      varsel = varsel,
                                      verge = verge,
                                      sumFeilutbetaling = sumFeilutbetaling,
                                      saksbehandlerIdent = saksbehandlerIdent!!).lag()

        val eksternBrukId = requireNotNull(familieTilbakeKlient.opprettTilbakekreving(data = data).data)
        { "Det oppstod en feil under opprettelse av tilbakekreving" }

        val behandlingId = hentBehandlingId(fagsystem = scenario.fagsystem,
                                            eksternFagsakId = scenario.eksternFagsakId,
                                            eksternBrukId = eksternBrukId)

        gjeldendeBehandling = GjeldendeBehandling(eksternFagsakId = scenario.eksternFagsakId,
                                                  eksternBrukId = eksternBrukId,
                                                  behandlingId = behandlingId,
                                                  eksternBehandlingId = scenario.eksternBehandlingId,
                                                  fagsystem = scenario.fagsystem,
                                                  ytelsestype = scenario.ytelsestype,
                                                  personIdent = scenario.personIdent,
                                                  enhetId = scenario.enhetId,
                                                  enhetsnavn = scenario.enhetsnavn,
                                                  harVerge = verge)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_OPPRETTET)
        if (varsel) {
            lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_PÅ_VENT)
            lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VARSELBREV_SENDT)
            if (verge) {
                lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VARSELBREV_SENDT_TIL_VERGE)
            }
        }

        println("Opprettet behandling med eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId} og " +
                        "eksternBrukId: ${gjeldendeBehandling.eksternBrukId}")
    }

    /** TODO: Fullfør implementasjon når funksjonalitet for å opprette manuell behandling er implementert i familie-tilbake
    fun opprettKravgrunnlagUtenBehandling(status: KodeStatusKrav,
                                          fagsystem: Fagsystem,
                                          ytelsestype: Ytelsestype,
                                          eksternFagsakId: String,
                                          antallPerioder: Int,
                                          under4rettsgebyr: Boolean,
                                          muligforeldelse: Boolean) {

        gjeldendeBehandling = GjeldendeBehandling(fagsystem = fagsystem,
                                                  ytelsestype = ytelsestype,
                                                  eksternFagsakId = eksternFagsakId,
                                                  eksternBehandlingId = Random.nextInt(1000000, 9999999).toString())

        opprettKravgrunnlag(status = status,
                            antallPerioder = antallPerioder,
                            under4rettsgebyr = under4rettsgebyr,
                            muligforeldelse = muligforeldelse)
    }*/

    fun opprettKravgrunnlag(status: KodeStatusKrav,
                            antallPerioder: Int = 2,
                            under4rettsgebyr: Boolean,
                            muligforeldelse: Boolean,
                            periodelengde: Int = 3,
                            skattProsent: BigDecimal = BigDecimal.ZERO,
                            sumFeilutbetaling: BigDecimal = BigDecimal(20000)) {

        val data = KravgrunnlagData(status = status,
                                    ytelsestype = requireNotNull(gjeldendeBehandling.ytelsestype)
                                    { "Ytelsestype ikke definert. " +
                                            "Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling." },
                                    eksternFagsakId = requireNotNull(gjeldendeBehandling.eksternFagsakId)
                                    { "EksternFagsakId ikke definert. " +
                                            "Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling." },
                                    eksternBehandlingId = requireNotNull(gjeldendeBehandling.eksternBehandlingId)
                                    { "EksternBehandlingId ikke definert. " +
                                            "Opprett behandling først eller bruk opprettKravgrunnlagUtenBehandling." },
                                    personIdent = gjeldendeBehandling.personIdent,
                                    enhetId = gjeldendeBehandling.enhetId,
                                    antallPerioder = antallPerioder,
                                    under4rettsgebyr = under4rettsgebyr,
                                    muligforeldelse = muligforeldelse,
                                    periodeLengde = periodelengde,
                                    skattProsent = skattProsent,
                                    sumFeilutbetaling = sumFeilutbetaling).lag()

        familieTilbakeKlient.opprettKravgrunnlag(kravgrunnlag = data)

        gjeldendeBehandling.apply {
            kravgrunnlagId = data.detaljertKravgrunnlag.kravgrunnlagId
            vedtakId = data.detaljertKravgrunnlag.vedtakId
            feilutbetaltePerioder = utledLogiskPeriodeFraKravgrunnlag(data.detaljertKravgrunnlag)
        }

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.KRAVGRUNNLAG_MOTTATT)
        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_GJENOPPTATT)

        println("Sendte inn $status kravgrunnlag med eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId} " +
                        "på ytelsestype: ${gjeldendeBehandling.fagsystem}")
    }

    fun opprettStatusmelding(status: KodeStatusKrav) {
        val data = StatusmeldingData(status = status,
                                     kravgrunnlagVedtakId = requireNotNull(gjeldendeBehandling.vedtakId)
                                     { "VedtakId ikke definert. Opprett kravgrunnlag først." },
                                     ytelsestype = requireNotNull(gjeldendeBehandling.ytelsestype)
                                     { "Ytelsestype ikke definert. Opprett kravgrunnlag først." },
                                     eksternFagsakId = requireNotNull(gjeldendeBehandling.eksternFagsakId)
                                     { "EksternFagsakId ikke definert. Opprett kravgrunnlag først." },
                                     eksternBehandlingId = requireNotNull(gjeldendeBehandling.eksternBehandlingId)
                                     { "EksternBehandlingId ikke definert. Opprett kravgrunnlag først." }).lag()

        familieTilbakeKlient.opprettStatusmelding(statusmelding = data)

        println("Sendte inn statusmelding $status på eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId}")
    }

    fun hentBehandlingId(fagsystem: Fagsystem, eksternFagsakId: String, eksternBrukId: String?): String {
        familieTilbakeKlient.hentFagsak(fagsystem, eksternFagsakId).data?.behandlinger?.forEach {
            if (it.eksternBrukId.toString() == eksternBrukId) {
                return it.behandlingId.toString()
            }
        }
        throw Exception("Fant ingen behandling med eksternBrukId: $eksternBrukId med eksternFagsakId: $eksternFagsakId" +
                                " og fagsystem: $fagsystem")
    }

    fun behandleFakta(hendelsestype: Hendelsestype, hendelsesundertype: Hendelsesundertype) {
        val hentFaktaResponse =
            requireNotNull(familieTilbakeKlient.hentFakta(gjeldendeBehandling.behandlingId).data)
            { "Kunne ikke hente data for behandling av fakta" }
        val data = BehandleFaktaData(hentFaktaResponse = hentFaktaResponse,
                                     hendelsestype = hendelsestype,
                                     hendelsesundertype = hendelsesundertype).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.FAKTA_VURDERT)

        println("Behandling ${gjeldendeBehandling.behandlingId} ble behandlet i steg fakta: hendelsestype $hendelsestype, " +
                        "hendelsesundertype $hendelsesundertype")
    }

    fun behandleForeldelse(beslutning: Foreldelsesvurderingstype) {
        val hentForeldelseResponse =
            requireNotNull(familieTilbakeKlient.hentForeldelse(gjeldendeBehandling.behandlingId).data)
            { "Kunne ikke hente data for behandling av foreldelse" }
        val data = BehandleForeldelseData(hentForeldelseResponse = hentForeldelseResponse,
                                          beslutning = beslutning).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.FORELDELSE_VURDERT)

        println("Behandling ${gjeldendeBehandling.behandlingId} ble behandlet i steg foreldelse: beslutning $beslutning")
    }

    fun behandleVilkårsvurdering(vilkårvurderingsresultat: Vilkårsvurderingsresultat,
                                 aktsomhet: Aktsomhet? = null,
                                 særligeGrunner: List<SærligGrunn> = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET),
                                 beløpErIBehold: Boolean = true,
                                 andelTilbakekreves: BigDecimal? = null,
                                 beløpTilbakekreves: BigDecimal? = null,
                                 tilbakekrevSmåbeløp: Boolean? = null,
                                 ileggRenter: Boolean = false) {
        val hentVilkårsvurderingResponse =
            requireNotNull(familieTilbakeKlient.hentVilkårsvurdering(gjeldendeBehandling.behandlingId).data)
            { "Kunne ikke hente data for behandling av vilkår" }
        val data = BehandleVilkårsvurderingData(hentVilkårsvurderingResponse = hentVilkårsvurderingResponse,
                                                vilkårvurderingsresultat = vilkårvurderingsresultat,
                                                aktsomhet = aktsomhet,
                                                særligeGrunner = særligeGrunner,
                                                beløpErIBehold = beløpErIBehold,
                                                andelTilbakekreves = andelTilbakekreves,
                                                beløpTilbakekreves = beløpTilbakekreves,
                                                tilbakekrevSmåbeløp = tilbakekrevSmåbeløp,
                                                ileggRenter = ileggRenter,
                                                ytelsestype = gjeldendeBehandling.ytelsestype).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VILKÅRSVURDERING_VURDERT)

        println("Behandling ${gjeldendeBehandling.behandlingId} ble behandlet i steg vilkårsvurdering: " +
                        "vilkårvurderingsresultat $vilkårvurderingsresultat")
    }

    fun behandleForeslåVedtak() {
        val hentVedtakbrevtekstResponse =
            requireNotNull(familieTilbakeKlient.hentVedtaksbrevtekst(gjeldendeBehandling.behandlingId).data)
            { "Kunne ikke hente data for behandling av foreslå vedtak" }
        val data = BehandleForeslåVedtakData(hentVedtakbrevtekstResponse = hentVedtakbrevtekstResponse).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.FORESLÅ_VEDTAK_VURDERT)
        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_SENDT_TIL_BESLUTTER)

        println("Behandling ${gjeldendeBehandling.behandlingId} ble behandlet i steg foreslå vedtak")
    }

    fun behandleFatteVedtak(godkjent: Boolean) {
        val hentTotrinnsvurderingerResponse =
            requireNotNull(familieTilbakeKlient.hentTotrinnsvurderinger(gjeldendeBehandling.behandlingId).data)
            { "Kunne ikke hente data for behandling av fatte vedtak" }
        val data = BehandleFatteVedtakData(hentTotrinnsvurderingerResponse = hentTotrinnsvurderingerResponse,
                                           godkjent = godkjent).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId, data = data)

        if (godkjent) {
            lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VEDTAK_FATTET)
            lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_SENDT_TIL_BESLUTTER)
            lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VEDTAKSBREV_SENDT)
            if (gjeldendeBehandling.harVerge) {
                lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VEDTAKSBREV_SENDT_TIL_VERGE)
            }
            lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_AVSLUTTET)
        } else {
            lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_SENDT_TILBAKE_TIL_SAKSBEHANDLER)
        }

        println("Behandling ${gjeldendeBehandling.behandlingId} ble behandlet i steg fatte vedtak: " +
                        if(godkjent) "vedtak GODKJENT" else "vedtak UNDERKJENT")
    }

    fun settBehandlingPåVent(venteårsak: Venteårsak, tidsfrist: LocalDate) {
        val data = BehandlingPåVentDto(venteårsak = venteårsak,
                                       tidsfrist = tidsfrist)

        familieTilbakeKlient.settBehandlingPåVent(behandlingId = gjeldendeBehandling.behandlingId, data = data)
        erBehandlingPåVent(venteårsak)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_PÅ_VENT)

        println("Behandling ${gjeldendeBehandling.behandlingId} satt på vent: $venteårsak med frist $tidsfrist")
    }

    fun taBehandlingAvVent() {
        familieTilbakeKlient.taBehandlingAvVent(gjeldendeBehandling.behandlingId)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_GJENOPPTATT)

        println("Behandling ${gjeldendeBehandling.behandlingId} tatt av vent")
    }

    fun henleggBehandling(behandlingsresultat: Behandlingsresultatstype) {
        /* Denne vil kun fungere i autotest for behandling opprettet med manueltOpprettet = TRUE ettersom automatisk opprettede
         behandlinger ikke kan henlegges manuelt før etter 6 dager.
         Når manueltOpprettet Tilbakekreving er implementert i familie-tilbake kan denne metoden brukes i en test.
         */
        val data = HenleggDto(behandlingsresultatstype = behandlingsresultat,
                              begrunnelse = "Dette er en automatisk begrunnelse generert av autotest")
        if (behandlingsresultat == Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_MED_BREV) {
            data.fritekst = "Dette er en automatisk fritekst generert av autotest"
        }

        familieTilbakeKlient.henleggBehandling(behandlingId = gjeldendeBehandling.behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_HENLAGT)

        println("Behandling ${gjeldendeBehandling.behandlingId} ble henlagt: $behandlingsresultat")
    }

    fun erBehandlingPåVent(venteårsak: Venteårsak) {
        Vent.til(
                {
                    familieTilbakeKlient.hentBehandling(gjeldendeBehandling.behandlingId).data?.behandlingsstegsinfo?.any {
                        it.behandlingsstegstatus == Behandlingsstegstatus.VENTER && it.venteårsak == venteårsak
                    }
                },
                30, "Behandling kom aldri i vent med årsak $venteårsak")
        println("Behandling ${gjeldendeBehandling.behandlingId} er bekreftet på vent med årsak $venteårsak")
    }

    fun erBehandlingISteg(behandlingssteg: Behandlingssteg,
                          behandlingsstegstatus: Behandlingsstegstatus
    ) {
        Vent.til(
                {
                    familieTilbakeKlient.hentBehandling(gjeldendeBehandling.behandlingId).data?.behandlingsstegsinfo?.any {
                        it.behandlingssteg == behandlingssteg && it.behandlingsstegstatus == behandlingsstegstatus
                    }
                },
                30, "Behandlingen kom aldri i status $behandlingsstegstatus i steg $behandlingssteg")
        println("Behandling ${gjeldendeBehandling.behandlingId} er bekreftet i " +
                        "status $behandlingsstegstatus i steg $behandlingssteg")
    }

    fun erBehandlingAvsluttet(resultat: Behandlingsresultatstype) {
        Vent.til(
            { familieTilbakeKlient.hentBehandling(gjeldendeBehandling.behandlingId).data?.status == Behandlingsstatus.AVSLUTTET },
            30, "Behandlingen fikk aldri status AVSLUTTET")
        val behandling = requireNotNull(familieTilbakeKlient.hentBehandling(gjeldendeBehandling.behandlingId).data)
        val henlagttyper = listOf(
            Behandlingsresultatstype.HENLAGT,
            Behandlingsresultatstype.HENLAGT_FEILOPPRETTET,
            Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_MED_BREV,
            Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_UTEN_BREV,
            Behandlingsresultatstype.HENLAGT_TEKNISK_VEDLIKEHOLD,
            Behandlingsresultatstype.HENLAGT_KRAVGRUNNLAG_NULLSTILT)
        val iverksatttyper = listOf(
            Behandlingsresultatstype.INGEN_TILBAKEBETALING,
            Behandlingsresultatstype.DELVIS_TILBAKEBETALING,
            Behandlingsresultatstype.FULL_TILBAKEBETALING)
        when (resultat) {
            in henlagttyper -> {
                assertTrue(behandling.erBehandlingHenlagt,
                           "Behandling var i status AVSLUTTET med resultat $resultat men erBehandlingHenlagt verdi var FALSE")
                assertTrue(behandling.behandlingsstegsinfo.filter {
                    it.behandlingssteg != Behandlingssteg.VARSEL
                }.all {
                    it.behandlingsstegstatus == Behandlingsstegstatus.AVBRUTT
                },
                           "Behandling var i status AVSLUTTET med resultat $resultat men alle behandlingsstegene var ikke i status AVBRUTT")
                assertTrue(behandling.resultatstype == Behandlingsresultatstype.HENLAGT,
                           "Forventet resultat: HENLAGT, Behandlingens resultat: ${behandling.resultatstype}")
            }
            in iverksatttyper -> {
                assertTrue(behandling.behandlingsstegsinfo.all {
                    it.behandlingsstegstatus == Behandlingsstegstatus.UTFØRT ||
                    it.behandlingsstegstatus == Behandlingsstegstatus.AUTOUTFØRT },
                           "Behandlingen var i status AVSLUTTET med resultat $resultat men alle behandlingsstegene var ikke UTFØRT/AUTOUTFØRT")
                assertTrue(behandling.resultatstype == resultat,
                           "Forventet resultat: $resultat, Behandlingens resultat: ${behandling.resultatstype}")
            }
            else -> {
                throw Exception("Behandling var i status AVSLUTTET men resultattypen angitt var ${behandling.resultatstype}" +
                                        " som ikke er et gyldig resultat for en avsluttet behandling")
            }
        }

        println("Behandling ${gjeldendeBehandling.behandlingId} er bekreftet avsluttet med resultat $resultat")
    }

    fun endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler: String) {
        Vent.til({ familieTilbakeKlient
            .endreAnsvarligSaksbehandler(behandlingId = gjeldendeBehandling.behandlingId,
                                         nyAnsvarligSaksbehandler = nyAnsvarligSaksbehandler).status == Ressurs.Status.SUKSESS },
                 30, "Kunne ikke endre saksbehandler")
    }

    fun bestillBrev(dokumentmalstype: Dokumentmalstype) {
        val data = BestillBrevData(behandlingId = gjeldendeBehandling.behandlingId,
                                   dokumentmalstype = dokumentmalstype).lag()

        familieTilbakeKlient.bestillBrev(data = data)

        when (dokumentmalstype) {
            Dokumentmalstype.INNHENT_DOKUMENTASJON -> {
                lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.INNHENT_DOKUMENTASJON_BREV_SENDT)
                if (gjeldendeBehandling.harVerge) {
                    lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.INNHENT_DOKUMENTASJON_BREV_SENDT_TIL_VERGE)
                }
            }
            Dokumentmalstype.VARSEL -> {
                lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VARSELBREV_SENDT)
                if (gjeldendeBehandling.harVerge) {
                    lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VARSELBREV_SENDT_TIL_VERGE)
                }
            }
            Dokumentmalstype.KORRIGERT_VARSEL -> {
                lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.KORRIGERT_VARSELBREV_SENDT)
                if (gjeldendeBehandling.harVerge) {
                    lagreHistorikkinnslag((TilbakekrevingHistorikkinnslagstype.KORRIGERT_VARSELBREV_SENDT_TIL_VERGE))
                }
            }
        }

        println("Bestilte brev $dokumentmalstype for behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun forhåndsvisVedtaksbrev() {
        val data = ForhåndsvisVedtaksbrevBuilder(behandlingId = gjeldendeBehandling.behandlingId,
                                                 perioder = gjeldendeBehandling.feilutbetaltePerioder?.map { it.periode }!!).lag()

        familieTilbakeKlient.forhåndsvisVedtaksbrev(data = data)

        println("Forhåndsviste vedtaksbrev for behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun forhåndsvisVarselbrev(vedtaksdato: LocalDate) {
        val hentBehandlingResponse = familieTilbakeKlient.hentBehandling(behandlingId = gjeldendeBehandling.behandlingId).data

        val data = ForhåndsvisVarselbrevData(behandlendeEnhetId = gjeldendeBehandling.enhetId,
                                             behandlendeEnhetsNavn = gjeldendeBehandling.enhetsnavn,
                                             eksternFagsakId = gjeldendeBehandling.eksternFagsakId,
                                             fagsystem = gjeldendeBehandling.fagsystem,
                                             perioder = requireNotNull(gjeldendeBehandling.feilutbetaltePerioder?.map { it.periode }),
                                             ident = gjeldendeBehandling.personIdent,
                                             saksbehandlerIdent = requireNotNull(hentBehandlingResponse?.ansvarligSaksbehandler),
                                             språkkode = Språkkode.NB,
                                             vedtaksdato = vedtaksdato,
                                             verge = gjeldendeBehandling.harVerge,
                                             sumFeilutbetaling = requireNotNull(gjeldendeBehandling.feilutbetaltePerioder?.sumOf {
                                                 it.feilutbetaltBeløp
                                             }).toLong(),
                                             ytelsestype = gjeldendeBehandling.ytelsestype).lag()

        familieTilbakeKlient.forhåndsvisVarselbrev(data = data)

        println("Forhåndsviste varselbrev for behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun forhåndsvisHenleggelsesbrev() {
        val data = ForhåndsvisHenleggelsesbrevData(behandlingId = gjeldendeBehandling.behandlingId).lag()

        familieTilbakeKlient.forhåndsvisHenleggelsesbrev(data = data)

        println("Forhåndsviste henleggelsesbrev for behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun hentJournaldokument() {
        // TODO: Oppdater med reelle IDer når dette er implementert i familie-tilbake
        familieTilbakeKlient.hentJournaldokument(behandlingId = gjeldendeBehandling.behandlingId,
                                                 journalpostId = "jpId",
                                                 dokumentId = "id")

        println("Journaldokument hentet for behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun beregn() {
        val data = requireNotNull(gjeldendeBehandling.feilutbetaltePerioder?.map { it.periode })
        { "Autotest mangler informasjon om perioder å beregne feilutbetalinger for" }

        familieTilbakeKlient.beregn(behandlingId = gjeldendeBehandling.behandlingId,
                                    data = data).data

        val beregnedePerioder = familieTilbakeKlient.beregnResultat(behandlingId = gjeldendeBehandling.behandlingId).data

        val forventetFeilutbetaling = gjeldendeBehandling.feilutbetaltePerioder?.sumOf { it.feilutbetaltBeløp }?.setScale(2)
        val beregnetFeilutbetaling = beregnedePerioder?.beregningsresultatsperioder?.sumOf { it.feilutbetaltBeløp }?.setScale(2)

        assertTrue(beregnetFeilutbetaling == forventetFeilutbetaling,
                   "Forventet resultat: $forventetFeilutbetaling, beregnet resultat: $beregnetFeilutbetaling")

        println("Beregnet feilutbetaling for behandling ${gjeldendeBehandling.behandlingId}" )
    }

    fun verifiserHistorikkinnslag() {
        val historikkinnslag = requireNotNull(
            familieHistorikkKlient?.hentHistorikkinnslag(applikasjon = "FAMILIE_TILBAKE",
                                                        behandlingId = gjeldendeBehandling.eksternBrukId)?.data)
        { "Kunne ikke hente historikkinnsag" }

        // Sjekker at det finnes historikkinnslag med forventet tittel
        val historikkinnslagTitler = historikkinnslag.map{ it.tittel }

        gjeldendeBehandling.historikkinnslag.forEach {
            assertTrue(it.tittel in historikkinnslagTitler)
        }

        println("Verifiserte historikkinnslag for ${gjeldendeBehandling.behandlingId}" )
    }

    private fun lagreHistorikkinnslag(innslag: TilbakekrevingHistorikkinnslagstype) {
        gjeldendeBehandling.historikkinnslag.add(innslag)
    }
}
