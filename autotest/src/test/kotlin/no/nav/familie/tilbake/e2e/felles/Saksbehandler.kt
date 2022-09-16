package no.nav.familie.tilbake.e2e.felles

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingsårsakstype
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettManueltTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Vergetype
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleFaktaData
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleFatteVedtakData
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleForeldelseData
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleForeslåVedtakData
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleVergeData
import no.nav.familie.tilbake.e2e.felles.datagenerator.BehandleVilkårsvurderingData
import no.nav.familie.tilbake.e2e.felles.datagenerator.BestillBrevData
import no.nav.familie.tilbake.e2e.felles.datagenerator.ForhåndsvisHenleggelsesbrevData
import no.nav.familie.tilbake.e2e.felles.datagenerator.ForhåndsvisVarselbrevData
import no.nav.familie.tilbake.e2e.felles.datagenerator.ForhåndsvisVedtaksbrevBuilder
import no.nav.familie.tilbake.e2e.felles.datagenerator.KravgrunnlagData
import no.nav.familie.tilbake.e2e.felles.datagenerator.StatusmeldingData
import no.nav.familie.tilbake.e2e.felles.datagenerator.TilbakekrevingData
import no.nav.familie.tilbake.e2e.felles.utils.LogiskPeriodeUtil.utledLogiskPeriodeFraKravgrunnlag
import no.nav.familie.tilbake.e2e.felles.utils.Vent
import no.nav.familie.tilbake.e2e.klienter.FamilieHistorikkKlient
import no.nav.familie.tilbake.e2e.klienter.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.klienter.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.klienter.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.klienter.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.klienter.dto.historikkinnslag.TilbakekrevingHistorikkinnslagstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.BehandlingPåVentDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingsresultatstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingsstatus
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingssteg
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Dokumentmalstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsestype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsesundertype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.HenleggDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.InstitusjonDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.KodeStatusKrav
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.OpprettRevurderingDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Venteårsak
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.VergeDto
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagMelding
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class Saksbehandler(
    private val familieTilbakeKlient: FamilieTilbakeKlient,
    private val familieHistorikkKlient: FamilieHistorikkKlient? = null
) {

    companion object {

        const val SAKSBEHANDLER_IDENT = "Z994619" // Denne må oppdateres når bruker går ut i IDA
        const val BESLUTTER_IDENT = "Z994623" // Denne må oppdateres når bruker går ut i IDA
    }

    private lateinit var gjeldendeBehandling: GjeldendeBehandling

    fun opprettTilbakekreving(
        scenario: Scenario,
        varsel: Boolean,
        verge: Boolean,
        saksbehandlerIdent: String = SAKSBEHANDLER_IDENT,
        sumFeilutbetaling: BigDecimal? = null,
        institusjon: Boolean = false
    ) {
        val data = TilbakekrevingData(
            eksternFagsakId = scenario.eksternFagsakId,
            eksternBehandlingId = scenario.eksternBehandlingId,
            fagsystem = scenario.fagsystem,
            ytelsestype = scenario.ytelsestype,
            personIdent = scenario.personIdent,
            enhetId = scenario.enhetId,
            enhetsnavn = scenario.enhetsnavn,
            varsel = varsel,
            verge = verge,
            sumFeilutbetaling = sumFeilutbetaling,
            saksbehandlerIdent = saksbehandlerIdent,
            institusjon = institusjon
        ).lag()

        val eksternBrukId =
            requireNotNull(familieTilbakeKlient.opprettTilbakekreving(data = data).data) { "Det oppstod en feil under opprettelse av tilbakekreving" }

        val behandlingId = hentBehandlingId(
            fagsystem = scenario.fagsystem,
            eksternFagsakId = scenario.eksternFagsakId,
            eksternBrukId = eksternBrukId
        )

        gjeldendeBehandling = GjeldendeBehandling(
            eksternFagsakId = scenario.eksternFagsakId,
            eksternBrukId = eksternBrukId,
            behandlingId = behandlingId,
            eksternBehandlingId = scenario.eksternBehandlingId,
            fagsystem = scenario.fagsystem,
            ytelsestype = scenario.ytelsestype,
            personIdent = scenario.personIdent,
            enhetId = scenario.enhetId,
            enhetsnavn = scenario.enhetsnavn,
            harVerge = verge,
            institusjon = data.institusjon?.let { InstitusjonDto(organisasjonsnummer = it.organisasjonsnummer, navn = it.navn) }
        )

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_OPPRETTET)
        if (varsel) {
            lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_PÅ_VENT)
            lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VARSELBREV_SENDT)
            if (verge) {
                lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VARSELBREV_SENDT_TIL_VERGE)
            }
        }

        println("Opprettet behandling med eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId} og " + "eksternBrukId: ${gjeldendeBehandling.eksternBrukId}")
    }

    fun oppretManuellBehandling(scenario: Scenario, detaljertMelding: DetaljertKravgrunnlagMelding) {
        val manuellTilbakekrevingRequest = OpprettManueltTilbakekrevingRequest(
            eksternFagsakId = scenario.eksternFagsakId,
            ytelsestype = scenario.ytelsestype,
            eksternId = scenario.eksternBehandlingId
        )
        familieTilbakeKlient.opprettManuellBehandling(manuellTilbakekrevingRequest)

        Thread.sleep(2_500)

        familieTilbakeKlient.publiserFagsystembehandling(manuellTilbakekrevingRequest)

        Thread.sleep(10_000)

        val fagsak = familieTilbakeKlient.hentFagsak(fagsystem = scenario.fagsystem, eksternFagsakId = scenario.eksternFagsakId)

        assertNotNull(fagsak, "Fagsak ikke opprettet")
        assertTrue(fagsak.data!!.behandlinger.size == 1, "Fagsak har ingen behandlinger")

        val behandling = fagsak.data!!.behandlinger.elementAt(0)

        val institusjon =
            fagsak.data!!.institusjon?.let { InstitusjonDto(organisasjonsnummer = it.organisasjonsnummer, navn = it.navn) }

        gjeldendeBehandling = GjeldendeBehandling(
            eksternFagsakId = scenario.eksternFagsakId,
            eksternBehandlingId = scenario.eksternBehandlingId,
            fagsystem = scenario.fagsystem,
            ytelsestype = scenario.ytelsestype,
            personIdent = scenario.personIdent,
            enhetId = scenario.enhetId,
            enhetsnavn = scenario.enhetsnavn,
            eksternBrukId = behandling.eksternBrukId.toString(),
            behandlingId = behandling.behandlingId.toString(),
            harVerge = false,
            kravgrunnlagId = detaljertMelding.detaljertKravgrunnlag.kravgrunnlagId,
            vedtakId = detaljertMelding.detaljertKravgrunnlag.vedtakId,
            feilutbetaltePerioder = utledLogiskPeriodeFraKravgrunnlag(detaljertKravgrunnlag = detaljertMelding.detaljertKravgrunnlag),
            institusjon = institusjon
        )

        println("Opprettet behandling med eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId} og " + "eksternBrukId: ${gjeldendeBehandling.eksternBrukId}")
    }

    fun opprettKravgrunnlag(
        status: KodeStatusKrav,
        antallPerioder: Int = 2,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean,
        periodelengde: Int = 3,
        skattProsent: BigDecimal = BigDecimal.ZERO,
        sumFeilutbetaling: BigDecimal = BigDecimal(20000),
        medJustering: Boolean? = false
    ) {
        val data = KravgrunnlagData(
            status = status,
            ytelsestype = requireNotNull(gjeldendeBehandling.ytelsestype) {
                "Ytelsestype er ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagForManueltOpprettelse."
            },
            eksternFagsakId = requireNotNull(gjeldendeBehandling.eksternFagsakId) {
                "EksternFagsakId  er ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagForManueltOpprettelse."
            },
            eksternBehandlingId = requireNotNull(gjeldendeBehandling.eksternBehandlingId) {
                "EksternBehandlingId er ikke definert. Opprett behandling først eller bruk opprettKravgrunnlagForManueltOpprettelse."
            },
            antallPerioder = antallPerioder,
            under4rettsgebyr = under4rettsgebyr,
            muligforeldelse = muligforeldelse,
            periodeLengde = periodelengde,
            personIdent = gjeldendeBehandling.personIdent,
            enhetId = gjeldendeBehandling.enhetId,
            skattProsent = skattProsent,
            sumFeilutbetaling = sumFeilutbetaling,
            medJustering = medJustering!!,
            institusjon = gjeldendeBehandling.institusjon
        ).lag()

        familieTilbakeKlient.opprettKravgrunnlag(kravgrunnlag = data)

        gjeldendeBehandling.apply {
            kravgrunnlagId = data.detaljertKravgrunnlag.kravgrunnlagId
            vedtakId = data.detaljertKravgrunnlag.vedtakId
            feilutbetaltePerioder = utledLogiskPeriodeFraKravgrunnlag(data.detaljertKravgrunnlag)
        }

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.KRAVGRUNNLAG_MOTTATT)
        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_GJENOPPTATT)

        println("Sendte inn $status kravgrunnlag med eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId} på ytelsestype: ${gjeldendeBehandling.ytelsestype}")
    }

    fun opprettKravgrunnlagForManueltOpprettelse(
        scenario: Scenario,
        status: KodeStatusKrav,
        antallPerioder: Int = 2,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean,
        periodelengde: Int = 3,
        skattProsent: BigDecimal = BigDecimal.ZERO,
        sumFeilutbetaling: BigDecimal = BigDecimal(20000),
        medJustering: Boolean = false
    ): DetaljertKravgrunnlagMelding {
        val data = KravgrunnlagData(
            status = status,
            ytelsestype = scenario.ytelsestype,
            eksternFagsakId = scenario.eksternFagsakId,
            eksternBehandlingId = scenario.eksternBehandlingId,
            antallPerioder = antallPerioder,
            under4rettsgebyr = under4rettsgebyr,
            muligforeldelse = muligforeldelse,
            periodeLengde = periodelengde,
            personIdent = scenario.personIdent,
            enhetId = scenario.enhetId,
            skattProsent = skattProsent,
            sumFeilutbetaling = sumFeilutbetaling,
            medJustering = medJustering
        ).lag()

        familieTilbakeKlient.opprettKravgrunnlag(kravgrunnlag = data)

        println("Sendte inn $status kravgrunnlag med eksternFagsakId: ${scenario.eksternFagsakId} på ytelsestype: ${scenario.ytelsestype}")
        return data
    }

    fun opprettRevurdering(scenario: Scenario, behandlingsårsakstype: Behandlingsårsakstype) {
        val respons = familieTilbakeKlient.opprettRevurdering(
            OpprettRevurderingDto(
                ytelsestype = scenario.ytelsestype,
                originalBehandlingId = UUID.fromString(
                    gjeldendeBehandling.behandlingId
                ),
                årsakstype = behandlingsårsakstype
            )
        )
        assertTrue(respons.status == Ressurs.Status.SUKSESS, "Kallet for å opprette revurdering feilet")
        val revurderingEksternBrukId = respons.data

        val fagsak = familieTilbakeKlient.hentFagsak(fagsystem = scenario.fagsystem, eksternFagsakId = scenario.eksternFagsakId)

        assertTrue(fagsak.data!!.behandlinger.size == 2, "Fagsak har ikke revurderingen")

        val revurdering = fagsak.data!!.behandlinger.find { it.eksternBrukId.toString() == revurderingEksternBrukId }

        assertNotNull(
            revurdering,
            "Fann ikke revurderingen med eksterBrukId $revurderingEksternBrukId på fagsak ${scenario.eksternFagsakId}"
        )

        gjeldendeBehandling.apply {
            gjeldendeBehandling.revurderingEksternId = revurderingEksternBrukId
            gjeldendeBehandling.revurderingBehandlingId = revurdering!!.behandlingId.toString()
        }

        println("Opprettet revurdering med eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId} og " + "eksternBrukId: ${gjeldendeBehandling.revurderingEksternId}")
    }

    fun kanBehandlingOpprettesManuelt(scenario: Scenario) {
        val kanOppretteManueltResponse = familieTilbakeKlient.kanBehandlingOpprettesManuelt(
            ytelsestype = scenario.ytelsestype,
            eksternFagsakId = scenario.eksternFagsakId
        )

        assertTrue(kanOppretteManueltResponse.status == Ressurs.Status.SUKSESS, "Kallet kan opprette feilet")
        assertTrue(kanOppretteManueltResponse.data!!.kanBehandlingOpprettes, "Kan ikke opprette behandling manuelt")
    }

    fun opprettStatusmelding(status: KodeStatusKrav) {
        val data = StatusmeldingData(
            status = status,
            kravgrunnlagVedtakId = requireNotNull(gjeldendeBehandling.vedtakId) { "VedtakId er ikke definert. Opprett kravgrunnlag først." },
            ytelsestype = requireNotNull(gjeldendeBehandling.ytelsestype) { "Ytelsestype er ikke definert. Opprett kravgrunnlag først." },
            eksternFagsakId = requireNotNull(gjeldendeBehandling.eksternFagsakId) { "EksternFagsakId er ikke definert. Opprett kravgrunnlag først." },
            eksternBehandlingId = requireNotNull(gjeldendeBehandling.eksternBehandlingId) { "EksternBehandlingId er ikke definert. Opprett kravgrunnlag først." }
        ).lag()

        familieTilbakeKlient.opprettStatusmelding(statusmelding = data)

        println("Sendte inn statusmelding $status på eksternFagsakId: ${gjeldendeBehandling.eksternFagsakId}")
    }

    fun hentBehandlingId(fagsystem: Fagsystem, eksternFagsakId: String, eksternBrukId: String?): String {
        familieTilbakeKlient.hentFagsak(fagsystem, eksternFagsakId).data?.behandlinger?.forEach {
            if (it.eksternBrukId.toString() == eksternBrukId) {
                return it.behandlingId.toString()
            }
        }
        throw Exception("Fant ingen behandling med eksternBrukId: $eksternBrukId med eksternFagsakId: $eksternFagsakId" + " og fagsystem: $fagsystem")
    }

    fun behandleFakta(hendelsestype: Hendelsestype, hendelsesundertype: Hendelsesundertype) {
        behandleFakta(gjeldendeBehandling.behandlingId, hendelsestype, hendelsesundertype)
    }

    fun behandleFaktaRevurdering(hendelsestype: Hendelsestype, hendelsesundertype: Hendelsesundertype) {
        assertNotNull(gjeldendeBehandling.revurderingBehandlingId, "Revurdering må være opprettet!")
        behandleFakta(gjeldendeBehandling.revurderingBehandlingId!!, hendelsestype, hendelsesundertype)
    }

    fun behandleFakta(behandlingId: String, hendelsestype: Hendelsestype, hendelsesundertype: Hendelsesundertype) {
        val hentFaktaResponse =
            requireNotNull(familieTilbakeKlient.hentFakta(behandlingId).data) { "Kunne ikke hente data for behandling av fakta" }
        val data = BehandleFaktaData(
            hentFaktaResponse = hentFaktaResponse,
            hendelsestype = hendelsestype,
            hendelsesundertype = hendelsesundertype
        ).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.FAKTA_VURDERT)

        println("Behandling $behandlingId ble behandlet i steg fakta: hendelsestype $hendelsestype, " + "hendelsesundertype $hendelsesundertype")
    }

    fun behandleForeldelse(beslutning: Foreldelsesvurderingstype) {
        val hentForeldelseResponse =
            requireNotNull(familieTilbakeKlient.hentForeldelse(gjeldendeBehandling.behandlingId).data) { "Kunne ikke hente data for behandling av foreldelse" }
        val data = BehandleForeldelseData(hentForeldelseResponse = hentForeldelseResponse, beslutning = beslutning).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.FORELDELSE_VURDERT)

        println("Behandling ${gjeldendeBehandling.behandlingId} ble behandlet i steg foreldelse: beslutning $beslutning")
    }

    fun behandleVilkårsvurdering(
        vilkårvurderingsresultat: Vilkårsvurderingsresultat,
        aktsomhet: Aktsomhet? = null,
        særligeGrunner: List<SærligGrunn> = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET),
        beløpErIBehold: Boolean = true,
        andelTilbakekreves: BigDecimal? = null,
        beløpTilbakekreves: BigDecimal? = null,
        tilbakekrevSmåbeløp: Boolean? = null,
        ileggRenter: Boolean = false
    ) {
        behandleVilkårsvurdering(
            gjeldendeBehandling.behandlingId,
            vilkårvurderingsresultat,
            aktsomhet,
            særligeGrunner,
            beløpErIBehold,
            andelTilbakekreves,
            beløpTilbakekreves,
            tilbakekrevSmåbeløp,
            ileggRenter
        )
    }

    fun behandleVilkårsvurderingRevurdering(
        vilkårvurderingsresultat: Vilkårsvurderingsresultat,
        aktsomhet: Aktsomhet? = null,
        særligeGrunner: List<SærligGrunn> = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET),
        beløpErIBehold: Boolean = true,
        andelTilbakekreves: BigDecimal? = null,
        beløpTilbakekreves: BigDecimal? = null,
        tilbakekrevSmåbeløp: Boolean? = null,
        ileggRenter: Boolean = false
    ) {
        assertNotNull(gjeldendeBehandling.revurderingBehandlingId, "Revurdering må være opprettet!")
        behandleVilkårsvurdering(
            gjeldendeBehandling.revurderingBehandlingId!!,
            vilkårvurderingsresultat,
            aktsomhet,
            særligeGrunner,
            beløpErIBehold,
            andelTilbakekreves,
            beløpTilbakekreves,
            tilbakekrevSmåbeløp,
            ileggRenter
        )
    }

    fun behandleVilkårsvurdering(
        behandlingId: String,
        vilkårvurderingsresultat: Vilkårsvurderingsresultat,
        aktsomhet: Aktsomhet? = null,
        særligeGrunner: List<SærligGrunn> = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET),
        beløpErIBehold: Boolean = true,
        andelTilbakekreves: BigDecimal? = null,
        beløpTilbakekreves: BigDecimal? = null,
        tilbakekrevSmåbeløp: Boolean? = null,
        ileggRenter: Boolean = false
    ) {
        val hentVilkårsvurderingResponse =
            requireNotNull(familieTilbakeKlient.hentVilkårsvurdering(behandlingId).data) { "Kunne ikke hente data for behandling av vilkår" }
        val data = BehandleVilkårsvurderingData(
            hentVilkårsvurderingResponse = hentVilkårsvurderingResponse,
            vilkårvurderingsresultat = vilkårvurderingsresultat,
            aktsomhet = aktsomhet,
            særligeGrunner = særligeGrunner,
            beløpErIBehold = beløpErIBehold,
            andelTilbakekreves = andelTilbakekreves,
            beløpTilbakekreves = beløpTilbakekreves,
            tilbakekrevSmåbeløp = tilbakekrevSmåbeløp,
            ileggRenter = ileggRenter,
            ytelsestype = gjeldendeBehandling.ytelsestype
        ).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VILKÅRSVURDERING_VURDERT)

        println("Behandling $behandlingId ble behandlet i steg vilkårsvurdering: " + "vilkårvurderingsresultat $vilkårvurderingsresultat")
    }

    fun behandleForeslåVedtak() {
        behandleForeslåVedtak(gjeldendeBehandling.behandlingId)
    }

    fun behandleForeslåVedtakRevurdering() {
        assertNotNull(gjeldendeBehandling.revurderingBehandlingId, "Revurdering må være opprettet!")
        behandleForeslåVedtak(gjeldendeBehandling.revurderingBehandlingId!!)
    }

    fun behandleForeslåVedtak(behandlingId: String) {
        val hentVedtakbrevtekstResponse =
            requireNotNull(familieTilbakeKlient.hentVedtaksbrevtekst(behandlingId).data) { "Kunne ikke hente data for behandling av foreslå vedtak" }
        val data = BehandleForeslåVedtakData(hentVedtakbrevtekstResponse = hentVedtakbrevtekstResponse).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.FORESLÅ_VEDTAK_VURDERT)
        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_SENDT_TIL_BESLUTTER)

        println("Behandling $behandlingId ble behandlet i steg foreslå vedtak")
    }

    fun behandleFatteVedtak(godkjent: Boolean) {
        behandleFatteVedtak(gjeldendeBehandling.behandlingId, godkjent)
    }

    fun behandleFatteVedtakRevurdering(godkjent: Boolean) {
        assertNotNull(gjeldendeBehandling.revurderingBehandlingId, "Revurdering må være opprettet!")
        behandleFatteVedtak(gjeldendeBehandling.revurderingBehandlingId!!, godkjent)
    }

    fun behandleFatteVedtak(behandlingId: String, godkjent: Boolean) {
        val hentTotrinnsvurderingerResponse =
            requireNotNull(familieTilbakeKlient.hentTotrinnsvurderinger(behandlingId).data) { "Kunne ikke hente data for behandling av fatte vedtak" }
        val data = BehandleFatteVedtakData(
            hentTotrinnsvurderingerResponse = hentTotrinnsvurderingerResponse,
            godkjent = godkjent
        ).lag()

        familieTilbakeKlient.behandleSteg(behandlingId = behandlingId, data = data)

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

        println("Behandling $behandlingId ble behandlet i steg fatte vedtak: " + if (godkjent) "vedtak GODKJENT" else "vedtak UNDERKJENT")
    }

    fun behandleVerge(type: Vergetype, navn: String, ident: String? = null, orgNr: String? = null) {
        if (type == Vergetype.ADVOKAT) {
            requireNotNull(orgNr) { "Organisasjonsnummer er obligatarisk for type Advokat" }
        } else {
            requireNotNull(ident) { "Fødselsnummer er obligatarisk for type $type" }
        }
        val data = BehandleVergeData(
            verge = VergeDto(
                type = type,
                navn = navn,
                begrunnelse = "Begrunnelse fra autotest",
                ident = ident,
                orgNr = orgNr
            )
        )

        familieTilbakeKlient.behandleSteg(behandlingId = gjeldendeBehandling.behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VERGE_OPPRETTET)

        println("Behandling ${gjeldendeBehandling.behandlingId} ble behandlet i steg Verge")
    }

    fun settBehandlingPåVent(venteårsak: Venteårsak, tidsfrist: LocalDate) {
        val data = BehandlingPåVentDto(venteårsak = venteårsak, tidsfrist = tidsfrist)

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
        val data = HenleggDto(
            behandlingsresultatstype = behandlingsresultat,
            begrunnelse = "Dette er en automatisk begrunnelse generert av autotest"
        )
        if (behandlingsresultat == Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_MED_BREV) {
            data.fritekst = "Dette er en automatisk fritekst generert av autotest"
        }

        familieTilbakeKlient.henleggBehandling(behandlingId = gjeldendeBehandling.behandlingId, data = data)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.BEHANDLING_HENLAGT)

        println("Behandling ${gjeldendeBehandling.behandlingId} ble henlagt: $behandlingsresultat")
    }

    fun opprettVerge() {
        familieTilbakeKlient.opprettVerge(gjeldendeBehandling.behandlingId)

        println("Startet opprettelse av verge på behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun fjernVerge() {
        familieTilbakeKlient.fjernVerge(gjeldendeBehandling.behandlingId)

        lagreHistorikkinnslag(TilbakekrevingHistorikkinnslagstype.VERGE_FJERNET)

        println("Fjernet verge på behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun hentVerge() {
        val verge = familieTilbakeKlient.hentVerge(gjeldendeBehandling.behandlingId)
    }

    fun erBehandlingPåVent(venteårsak: Venteårsak) {
        Vent.til({
            familieTilbakeKlient.hentBehandling(gjeldendeBehandling.behandlingId).data?.behandlingsstegsinfo?.any {
                it.behandlingsstegstatus == Behandlingsstegstatus.VENTER && it.venteårsak == venteårsak
            }
        }, 30, "Behandling kom aldri i vent med årsak $venteårsak")
        println("Behandling ${gjeldendeBehandling.behandlingId} er bekreftet på vent med årsak $venteårsak")
    }

    fun erBehandlingISteg(behandlingssteg: Behandlingssteg, behandlingsstegstatus: Behandlingsstegstatus) {
        erBehandlingISteg(gjeldendeBehandling.behandlingId, behandlingssteg, behandlingsstegstatus)
    }

    fun erRevurderingISteg(behandlingssteg: Behandlingssteg, behandlingsstegstatus: Behandlingsstegstatus) {
        assertNotNull(gjeldendeBehandling.revurderingBehandlingId, "Revurdering må være opprettet!")
        erBehandlingISteg(gjeldendeBehandling.revurderingBehandlingId!!, behandlingssteg, behandlingsstegstatus)
    }

    fun erBehandlingISteg(behandlingId: String, behandlingssteg: Behandlingssteg, behandlingsstegstatus: Behandlingsstegstatus) {
        Vent.til({
            familieTilbakeKlient.hentBehandling(behandlingId).data?.behandlingsstegsinfo?.any {
                it.behandlingssteg == behandlingssteg && it.behandlingsstegstatus == behandlingsstegstatus
            }
        }, 30, "Behandlingen kom aldri i status $behandlingsstegstatus i steg $behandlingssteg")
        println("Behandling $behandlingId er bekreftet i " + "status $behandlingsstegstatus i steg $behandlingssteg")
    }

    fun erBehandlingAvsluttet(resultat: Behandlingsresultatstype, vergeFjernet: Boolean? = false) {
        erBehandlingAvsluttet(gjeldendeBehandling.behandlingId, resultat, vergeFjernet)
    }

    fun erRevurderingAvsluttet(resultat: Behandlingsresultatstype, vergeFjernet: Boolean? = false) {
        assertNotNull(gjeldendeBehandling.revurderingBehandlingId, "Revurdering må være opprettet!")
        erBehandlingAvsluttet(gjeldendeBehandling.revurderingBehandlingId!!, resultat, vergeFjernet)
    }

    fun erBehandlingAvsluttet(behandlingId: String, resultat: Behandlingsresultatstype, vergeFjernet: Boolean? = false) {
        Vent.til(
            { familieTilbakeKlient.hentBehandling(behandlingId).data?.status == Behandlingsstatus.AVSLUTTET },
            30,
            "Behandlingen fikk aldri status AVSLUTTET"
        )
        val behandling = requireNotNull(familieTilbakeKlient.hentBehandling(behandlingId).data)
        val henlagttyper = listOf(
            Behandlingsresultatstype.HENLAGT,
            Behandlingsresultatstype.HENLAGT_FEILOPPRETTET,
            Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_MED_BREV,
            Behandlingsresultatstype.HENLAGT_FEILOPPRETTET_UTEN_BREV,
            Behandlingsresultatstype.HENLAGT_TEKNISK_VEDLIKEHOLD,
            Behandlingsresultatstype.HENLAGT_KRAVGRUNNLAG_NULLSTILT
        )
        val iverksatttyper = listOf(
            Behandlingsresultatstype.INGEN_TILBAKEBETALING,
            Behandlingsresultatstype.DELVIS_TILBAKEBETALING,
            Behandlingsresultatstype.FULL_TILBAKEBETALING
        )
        when (resultat) {
            in henlagttyper -> {
                assertTrue(
                    behandling.erBehandlingHenlagt,
                    "Behandling var i status AVSLUTTET med resultat $resultat men erBehandlingHenlagt verdi var FALSE"
                )
                assertTrue(
                    behandling.behandlingsstegsinfo.filter {
                        it.behandlingssteg != Behandlingssteg.VARSEL
                    }.all {
                        it.behandlingsstegstatus == Behandlingsstegstatus.AVBRUTT
                    },
                    "Behandling var i status AVSLUTTET med resultat $resultat men alle behandlingsstegene var ikke i status AVBRUTT"
                )
                assertTrue(
                    behandling.resultatstype == Behandlingsresultatstype.HENLAGT,
                    "Forventet resultat: HENLAGT, Behandlingens resultat: ${behandling.resultatstype}"
                )
            }
            in iverksatttyper -> {
                assertTrue(
                    behandling.behandlingsstegsinfo.all {
                        it.behandlingsstegstatus == Behandlingsstegstatus.UTFØRT || it.behandlingsstegstatus == Behandlingsstegstatus.AUTOUTFØRT || (vergeFjernet!! && it.behandlingssteg == Behandlingssteg.VERGE && it.behandlingsstegstatus == Behandlingsstegstatus.TILBAKEFØRT)
                    },
                    "Behandlingen var i status AVSLUTTET med resultat $resultat men alle behandlingsstegene var ikke UTFØRT/AUTOUTFØRT"
                )
                assertTrue(
                    behandling.resultatstype == resultat,
                    "Forventet resultat: $resultat, Behandlingens resultat: ${behandling.resultatstype}"
                )
            }
            else -> {
                throw Exception("Behandling var i status AVSLUTTET men resultattypen angitt var ${behandling.resultatstype}" + " som ikke er et gyldig resultat for en avsluttet behandling")
            }
        }

        println("Behandling $behandlingId er bekreftet avsluttet med resultat $resultat")
    }

    fun endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler: String) {
        endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler, gjeldendeBehandling.behandlingId)
    }

    fun endreAnsvarligSaksbehandlerRevurdering(nyAnsvarligSaksbehandler: String) {
        assertNotNull(gjeldendeBehandling.revurderingBehandlingId, "Revurdering må være opprettet!")
        endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler, gjeldendeBehandling.revurderingBehandlingId!!)
    }

    fun endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler: String, behandlingId: String) {
        Vent.til({
            familieTilbakeKlient.endreAnsvarligSaksbehandler(
                behandlingId = behandlingId,
                nyAnsvarligSaksbehandler = nyAnsvarligSaksbehandler
            ).status == Ressurs.Status.SUKSESS
        }, 30, "Kunne ikke endre saksbehandler")
    }

    fun bestillBrev(dokumentmalstype: Dokumentmalstype) {
        val data = BestillBrevData(behandlingId = gjeldendeBehandling.behandlingId, dokumentmalstype = dokumentmalstype).lag()

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
            else -> RuntimeException("Malen $dokumentmalstype er ikke støttet")
        }

        println("Bestilte brev $dokumentmalstype for behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun forhåndsvisVedtaksbrev() {
        val data = ForhåndsvisVedtaksbrevBuilder(
            behandlingId = gjeldendeBehandling.behandlingId,
            perioder = gjeldendeBehandling.feilutbetaltePerioder?.map { it.periode }!!
        ).lag()

        familieTilbakeKlient.forhåndsvisVedtaksbrev(data = data)

        println("Forhåndsviste vedtaksbrev for behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun forhåndsvisVarselbrev(vedtaksdato: LocalDate) {
        val hentBehandlingResponse = familieTilbakeKlient.hentBehandling(behandlingId = gjeldendeBehandling.behandlingId).data

        val data = ForhåndsvisVarselbrevData(
            behandlendeEnhetId = gjeldendeBehandling.enhetId,
            behandlendeEnhetsNavn = gjeldendeBehandling.enhetsnavn,
            eksternFagsakId = gjeldendeBehandling.eksternFagsakId,
            fagsystem = gjeldendeBehandling.fagsystem,
            perioder = requireNotNull(gjeldendeBehandling.feilutbetaltePerioder?.map { it.periode }),
            ident = gjeldendeBehandling.personIdent,
            saksbehandlerIdent = requireNotNull(hentBehandlingResponse?.ansvarligSaksbehandler),
            språkkode = Språkkode.NB,
            vedtaksdato = vedtaksdato,
            verge = gjeldendeBehandling.harVerge,
            sumFeilutbetaling = requireNotNull(
                gjeldendeBehandling.feilutbetaltePerioder?.sumOf {
                    it.feilutbetaltBeløp
                }
            ).toLong(),
            ytelsestype = gjeldendeBehandling.ytelsestype
        ).lag()

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
        familieTilbakeKlient.hentJournaldokument(
            behandlingId = gjeldendeBehandling.behandlingId,
            journalpostId = "jpId",
            dokumentId = "id"
        )

        println("Journaldokument hentet for behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun beregn() {
        val data =
            requireNotNull(gjeldendeBehandling.feilutbetaltePerioder?.map { it.periode }) { "Autotest mangler informasjon om perioder å beregne feilutbetalinger for" }

        familieTilbakeKlient.beregn(behandlingId = gjeldendeBehandling.behandlingId, data = data).data

        val beregnedePerioder = familieTilbakeKlient.beregnResultat(behandlingId = gjeldendeBehandling.behandlingId).data

        val forventetFeilutbetaling = gjeldendeBehandling.feilutbetaltePerioder?.sumOf { it.feilutbetaltBeløp }?.setScale(2)
        val beregnetFeilutbetaling = beregnedePerioder?.beregningsresultatsperioder?.sumOf { it.feilutbetaltBeløp }?.setScale(2)

        assertTrue(
            beregnetFeilutbetaling == forventetFeilutbetaling,
            "Forventet resultat: $forventetFeilutbetaling, beregnet resultat: $beregnetFeilutbetaling"
        )

        println("Beregnet feilutbetaling for behandling ${gjeldendeBehandling.behandlingId}")
    }

    fun verifiserHistorikkinnslag() {
        val historikkinnslag = requireNotNull(
            familieHistorikkKlient?.hentHistorikkinnslag(
                applikasjon = "FAMILIE_TILBAKE",
                behandlingId = gjeldendeBehandling.eksternBrukId
            )?.data
        ) { "Kunne ikke hente historikkinnsag" }

        // Sjekker at det finnes historikkinnslag med forventet tittel
        val historikkinnslagTitler = historikkinnslag.map { it.tittel }

        gjeldendeBehandling.historikkinnslag.forEach {
            assertTrue(it.tittel in historikkinnslagTitler)
        }

        println("Verifiserte historikkinnslag for ${gjeldendeBehandling.behandlingId}")
    }

    private fun lagreHistorikkinnslag(innslag: TilbakekrevingHistorikkinnslagstype) {
        gjeldendeBehandling.historikkinnslag.add(innslag)
    }
}
