package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.familie_historikk.FamilieHistorikkKlient
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Behandlingsresultatstype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Behandlingssteg
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.KodeStatusKrav
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Venteårsak
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.familie_tilbake.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Dokumentmalstype
import no.nav.familie.tilbake.e2e.felles.Saksbehandler
import no.nav.familie.tilbake.e2e.felles.Scenario
import org.junit.jupiter.api.BeforeEach
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
        scenario = Scenario(eksternFagsakId = Random.nextInt(1000000, 9999999).toString(),
                            eksternBehandlingId = Random.nextInt(1000000, 9999999).toString(),
                            fagsystem = fagsystem,
                            ytelsestype = ytelsestype,
                            personIdent = "12345678901",
                            enhetId = "0106",
                            enhetsnavn = "NAV Fredrikstad")
    }

    @Test
    fun `Tilbakekreving med varsel, kravgrunnlag uten foreldelse, vilkårsvurdering forsett, full tilbakebetaling`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = true,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 4,
                                under4rettsgebyr = false,
                                muligforeldelse = false)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            // Ikke mulig foreldelse, steget skal derfor være autoutført
            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                                     aktsomhet = Aktsomhet.FORSETT,
                                     andelTilbakekreves = BigDecimal(100),
                                     særligeGrunner = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET,
                                                             SærligGrunn.STØRRELSE_BELØP,
                                                             SærligGrunn.ANNET))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            beregn()

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving med varsel, kravgrunnlag med foreldelse, ikke foreldet, vilkårsvurdering simpel uaktsomhet full tilbakebetaling småbeløp`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = true,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 4,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                                     aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                                     andelTilbakekreves = BigDecimal(100),
                                     særligeGrunner = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET,
                                                             SærligGrunn.STØRRELSE_BELØP,
                                                             SærligGrunn.ANNET))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving uten varsel, kravgrunnlag, SPER-melding, ENDR-melding, vilkårsvurdering simpel uaktsomhet 22-15 6 ledd, ingen tilbakekreving`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = false,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 4,
                                periodelengde = 2,
                                under4rettsgebyr = true,
                                muligforeldelse = false)
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
            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)


            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER,
                                     aktsomhet = Aktsomhet.SIMPEL_UAKTSOMHET,
                                     særligeGrunner = listOf(SærligGrunn.TID_FRA_UTBETALING),
                                     tilbakekrevSmåbeløp = false)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = false)
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.TILBAKEFØRT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.INGEN_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving behandles så langt det er mulig før iverksetting, vilkårsvurdering god tro, så AVSL-melding og henleggelse`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = true,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 2,
                                under4rettsgebyr = true,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.GOD_TRO,
                                     beløpTilbakekreves = BigDecimal(4400.0))
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
            opprettTilbakekreving(scenario = scenario,
                                  varsel = false,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 2,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            // Alle perioder er foreldet, vilkårsvurdering skal derfor være autoutført
            behandleForeldelse(Foreldelsesvurderingstype.FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingISteg(Behandlingssteg.IVERKSETT_VEDTAK, Behandlingsstegstatus.KLAR)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.INGEN_TILBAKEBETALING)
        }
    }

    /** TODO: Fortsette å utvikle testen når funk kommer i familie-tilbake
    @Test
    fun `Opprett tilbakekrevingsbehandling manuelt`() {
        with(saksbehandler) {
            opprettKravgrunnlagUtenBehandling(status = KodeStatusKrav.NY,
                                              fagsystem = Fagsystem.BA,
                                              ytelsestype = Ytelsestype.BARNETRYGD,
                                              eksternFagsakId = eksternFagsakId,
                                              antallPerioder = 1,
                                              under4rettsgebyr = false,
                                              muligforeldelse = false)
        }
    }*/

    @Test
    fun `Tilbakekreving uten varsel, tilleggsfrist for foreldelse, uaktsomhet forsett`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = false,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 1,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.TILLEGGSFRIST)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                                     aktsomhet = Aktsomhet.FORSETT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving med verge, vilkårsvurdering grov uaktsomhet, delvis tilbakekreving`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = false,
                                  verge = true)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 1,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            // Alle perioder er foreldet, vilkårsvurdering skal derfor være autoutført
            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                                     aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                                     særligeGrunner = listOf(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.ANNET),
                                     andelTilbakekreves = BigDecimal(50))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.DELVIS_TILBAKEBETALING)
        }
    }

    @Test
    fun `Manuell bestilling og forhåndsvisning av brev`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = true,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 1,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
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

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                                     aktsomhet = Aktsomhet.FORSETT,
                                     andelTilbakekreves = BigDecimal(100),
                                     særligeGrunner = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET,
                                                             SærligGrunn.STØRRELSE_BELØP,
                                                             SærligGrunn.ANNET))

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

}
