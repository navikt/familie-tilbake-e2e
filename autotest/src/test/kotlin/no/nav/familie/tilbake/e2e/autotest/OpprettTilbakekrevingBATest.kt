package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Regelverk
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingsårsakstype
import no.nav.familie.kontrakter.felles.tilbakekreving.Vergetype
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.felles.Saksbehandler
import no.nav.familie.tilbake.e2e.felles.Scenario
import no.nav.familie.tilbake.e2e.klienter.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.klienter.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.klienter.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.klienter.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingsresultatstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingssteg
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Dokumentmalstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsestype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsesundertype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.KodeStatusKrav
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Venteårsak
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpprettTilbakekrevingBATest(@Autowired val familieTilbakeKlient: FamilieTilbakeKlient) {

    private lateinit var saksbehandler: Saksbehandler
    private lateinit var scenario: Scenario

    private val fagsystem: Fagsystem = Fagsystem.BA
    private val ytelsestype: Ytelsestype = Ytelsestype.BARNETRYGD

    @BeforeEach
    fun setup() {
        saksbehandler = Saksbehandler(familieTilbakeKlient = familieTilbakeKlient)
        scenario = Scenario(
            eksternFagsakId = Random.nextInt(1000000, 9999999).toString(),
            eksternBehandlingId = Random.nextInt(1000000, 9999999).toString(),
            fagsystem = fagsystem,
            ytelsestype = ytelsestype,
            personIdent = "12345678901",
            enhetId = "0106",
            enhetsnavn = "NAV Fredrikstad"
        )
    }

    @Test
    fun `Tilbakekreving med varsel, kravgrunnlag uten foreldelse, vilkårsvurdering forsett, full tilbakebetaling, revurdering, ingen tilbakekreving`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = true,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 3,
                under4rettsgebyr = false,
                muligforeldelse = false,
                periodelengde = 6
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            // Ikke mulig foreldelse, steget skal derfor være autoutført
            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                aktsomhet = Aktsomhet.FORSETT,
                andelTilbakekreves = BigDecimal(100),
                særligeGrunner = listOf(
                    SærligGrunn.GRAD_AV_UAKTSOMHET,
                    SærligGrunn.STØRRELSE_BELØP,
                    SærligGrunn.ANNET
                )
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            beregn()

            endreAnsvarligSaksbehandler(Saksbehandler.SAKSBEHANDLER_IDENT)
            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)

            opprettRevurdering(scenario, Behandlingsårsakstype.REVURDERING_OPPLYSNINGER_OM_VILKÅR)

            erRevurderingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFaktaRevurdering(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)

            erRevurderingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erRevurderingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurderingRevurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.GOD_TRO,
                beløpErIBehold = false
            )
            erRevurderingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandlerRevurdering(Saksbehandler.SAKSBEHANDLER_IDENT)
            behandleForeslåVedtakRevurdering()
            erRevurderingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandlerRevurdering(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtakRevurdering(godkjent = true)
            erRevurderingAvsluttet(resultat = Behandlingsresultatstype.INGEN_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving med varsel, kravgrunnlag med foreldelse, ikke foreldet, vilkårsvurdering simpel uaktsomhet full tilbakebetaling småbeløp`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = true,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 4,
                under4rettsgebyr = false,
                muligforeldelse = true
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                andelTilbakekreves = BigDecimal(100),
                særligeGrunner = listOf(
                    SærligGrunn.GRAD_AV_UAKTSOMHET,
                    SærligGrunn.STØRRELSE_BELØP,
                    SærligGrunn.ANNET
                )
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)
            endreAnsvarligSaksbehandler(Saksbehandler.SAKSBEHANDLER_IDENT)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving uten varsel, kravgrunnlag, SPER-melding, ENDR-melding, vilkårsvurdering simpel uaktsomhet 22-15 6 ledd, ingen tilbakekreving`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = false,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 4,
                periodelengde = 2,
                under4rettsgebyr = true,
                muligforeldelse = false
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            opprettStatusmelding(KodeStatusKrav.SPER)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettStatusmelding(KodeStatusKrav.ENDR)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            settBehandlingPåVent(Venteårsak.AVVENTER_DOKUMENTASJON, LocalDate.now().plusWeeks(3))
            erBehandlingPåVent(Venteårsak.AVVENTER_DOKUMENTASJON)

            taBehandlingAvVent()
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            // Ikke mulig foreldelse, foreldelsessteget skal derfor være autoutført
            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER,
                aktsomhet = Aktsomhet.SIMPEL_UAKTSOMHET,
                særligeGrunner = listOf(SærligGrunn.TID_FRA_UTBETALING),
                tilbakekrevSmåbeløp = false
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = false)
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.TILBAKEFØRT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.SAKSBEHANDLER_IDENT)
            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.INGEN_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving behandles så langt det er mulig før iverksetting, vilkårsvurdering god tro, så AVSL-melding og henleggelse`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = true,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 2,
                under4rettsgebyr = false,
                muligforeldelse = false
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.GOD_TRO,
                beløpTilbakekreves = BigDecimal(4400.0)
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            opprettStatusmelding(KodeStatusKrav.AVSL)
            erBehandlingAvsluttet(Behandlingsresultatstype.HENLAGT)
        }
    }

    @Test
    fun `Tilbakekreving med alle perioder foreldet`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = false,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 2,
                under4rettsgebyr = false,
                muligforeldelse = true
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            // Alle perioder er foreldet, vilkårsvurdering skal derfor være autoutført
            behandleForeldelse(Foreldelsesvurderingstype.FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingISteg(Behandlingssteg.IVERKSETT_VEDTAK, Behandlingsstegstatus.KLAR)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.INGEN_TILBAKEBETALING)
        }
    }

    @Test
    fun `Opprett tilbakekrevingsbehandling manuelt, kravgrunnlag uten foreldelse, vilkårsvurdering forsett, full tilbakebetaling`() {
        with(saksbehandler) {
            val detaljertMelding = opprettKravgrunnlagForManueltOpprettelse(
                scenario = scenario,
                status = KodeStatusKrav.NY,
                antallPerioder = 2,
                under4rettsgebyr = false,
                muligforeldelse = false
            )

            Thread.sleep(5_000)

            kanBehandlingOpprettesManuelt(scenario)
            oppretManuellBehandling(scenario = scenario, detaljertMelding = detaljertMelding)

            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            // Ikke mulig foreldelse, steget skal derfor være autoutført
            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                aktsomhet = Aktsomhet.FORSETT,
                andelTilbakekreves = BigDecimal(100),
                særligeGrunner = listOf(
                    SærligGrunn.GRAD_AV_UAKTSOMHET,
                    SærligGrunn.STØRRELSE_BELØP,
                    SærligGrunn.ANNET
                )
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            beregn()

            endreAnsvarligSaksbehandler(Saksbehandler.SAKSBEHANDLER_IDENT)
            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving uten varsel, tilleggsfrist for foreldelse, uaktsomhet forsett`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = false,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 1,
                under4rettsgebyr = false,
                muligforeldelse = true
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.TILLEGGSFRIST)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                aktsomhet = Aktsomhet.FORSETT
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving med verge, vilkårsvurdering grov uaktsomhet, delvis tilbakekreving`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = false,
                verge = true
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 1,
                under4rettsgebyr = false,
                muligforeldelse = true
            )

            // Behandlingen opprettes med verge, så steget er autoutført og behandlingen er på FAKTA-steget
            erBehandlingISteg(Behandlingssteg.VERGE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            // Alle perioder er foreldet, vilkårsvurdering skal derfor være autoutført
            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                særligeGrunner = listOf(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.ANNET),
                andelTilbakekreves = BigDecimal(50)
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.DELVIS_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving oppretter og fjerner verge, vilkårsvurdering grov uaktsomhet, delvis tilbakekreving`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = false,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 1,
                under4rettsgebyr = false,
                muligforeldelse = false
            )

            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            opprettVerge()

            erBehandlingISteg(Behandlingssteg.VERGE, Behandlingsstegstatus.KLAR)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.TILBAKEFØRT)

            behandleVerge(type = Vergetype.ADVOKAT, navn = "Advokat Advokatesen", orgNr = "987654321")

            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)

            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                særligeGrunner = listOf(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.ANNET),
                andelTilbakekreves = BigDecimal(50)
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            fjernVerge()

            erBehandlingISteg(Behandlingssteg.VERGE, Behandlingsstegstatus.TILBAKEFØRT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.DELVIS_TILBAKEBETALING, vergeFjernet = true)
        }
    }

    @Test
    @Disabled("Kan bare kjøres lokalt, med mocket IntegrasjonerClient i familie-tilbake")
    fun `Manuell bestilling og forhåndsvisning av brev`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = true,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 1,
                under4rettsgebyr = false,
                muligforeldelse = true
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            bestillBrev(Dokumentmalstype.INNHENT_DOKUMENTASJON)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            taBehandlingAvVent()
            bestillBrev(Dokumentmalstype.VARSEL)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            taBehandlingAvVent()
            bestillBrev(Dokumentmalstype.KORRIGERT_VARSEL)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            forhåndsvisVarselbrev(LocalDate.now())

            forhåndsvisHenleggelsesbrev()

            hentJournaldokument()

            taBehandlingAvVent()

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                aktsomhet = Aktsomhet.FORSETT,
                andelTilbakekreves = BigDecimal(100),
                særligeGrunner = listOf(
                    SærligGrunn.GRAD_AV_UAKTSOMHET,
                    SærligGrunn.STØRRELSE_BELØP,
                    SærligGrunn.ANNET
                )
            )

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving institusjon fagsak, med varsel, kravgrunnlag med foreldelse, ikke foreldet, vilkårsvurdering simpel uaktsomhet full tilbakebetaling småbeløp`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = true,
                verge = true,
                institusjon = true
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 3,
                periodelengde = 5,
                under4rettsgebyr = false,
                muligforeldelse = true
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                andelTilbakekreves = BigDecimal(100),
                særligeGrunner = listOf(
                    SærligGrunn.GRAD_AV_UAKTSOMHET,
                    SærligGrunn.STØRRELSE_BELØP,
                    SærligGrunn.ANNET
                )
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)
            endreAnsvarligSaksbehandler(Saksbehandler.SAKSBEHANDLER_IDENT)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving under EØS-regelverket med varsel, kravgrunnlag uten foreldelse, vilkårsvurdering forsett, full tilbakebetaling, revurdering, ingen tilbakekreving`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario.copy(regelverk = Regelverk.EØS),
                varsel = true,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 3,
                under4rettsgebyr = false,
                muligforeldelse = false,
                periodelengde = 6
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            // Ikke mulig foreldelse, steget skal derfor være autoutført
            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                aktsomhet = Aktsomhet.FORSETT,
                andelTilbakekreves = BigDecimal(100),
                særligeGrunner = listOf(
                    SærligGrunn.GRAD_AV_UAKTSOMHET,
                    SærligGrunn.STØRRELSE_BELØP,
                    SærligGrunn.ANNET
                )
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            beregn()

            endreAnsvarligSaksbehandler(Saksbehandler.SAKSBEHANDLER_IDENT)
            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)

            opprettRevurdering(scenario, Behandlingsårsakstype.REVURDERING_OPPLYSNINGER_OM_VILKÅR)

            erRevurderingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFaktaRevurdering(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)

            erRevurderingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erRevurderingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurderingRevurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.GOD_TRO,
                beløpErIBehold = false
            )
            erRevurderingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandlerRevurdering(Saksbehandler.SAKSBEHANDLER_IDENT)
            behandleForeslåVedtakRevurdering()
            erRevurderingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandlerRevurdering(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtakRevurdering(godkjent = true)
            erRevurderingAvsluttet(resultat = Behandlingsresultatstype.INGEN_TILBAKEBETALING)
        }
    }

    @Test
    fun `Oppretter uavsluttet delvis tilbakekreving for bruk ved utvikling`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = false,
                verge = false
            )
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(
                status = KodeStatusKrav.NY,
                antallPerioder = 1,
                under4rettsgebyr = false,
                muligforeldelse = false
            )

            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            opprettVerge()

            erBehandlingISteg(Behandlingssteg.VERGE, Behandlingsstegstatus.KLAR)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.TILBAKEFØRT)

            behandleVerge(type = Vergetype.ADVOKAT, navn = "Advokat Advokatesen", orgNr = "987654321")

            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)

            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                særligeGrunner = listOf(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.ANNET),
                andelTilbakekreves = BigDecimal(50)
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            fjernVerge()

            erBehandlingISteg(Behandlingssteg.VERGE, Behandlingsstegstatus.TILBAKEFØRT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)
        }
    }
}
