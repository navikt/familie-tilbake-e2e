package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.familie_historikk.FamilieHistorikkKlient
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Behandlingssteg
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.KodeStatusKrav
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Venteårsak
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.familie_tilbake.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Behandlingsresultatstype
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
class OpprettTilbakekrevingOSTest(@Autowired val familieTilbakeKlient: FamilieTilbakeKlient) {

    private lateinit var saksbehandler: Saksbehandler
    private lateinit var scenario: Scenario

    private val fagsystem: Fagsystem = Fagsystem.EF
    private val ytelsestype: Ytelsestype = Ytelsestype.OVERGANGSSTØNAD

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
    fun `Tilbakekreving med varsel, gjenoppta, kravgrunnlag med foreldelse, vilkårsvurdering simpel uaktsomhet delvis tilbakebetaling`() {
        with(saksbehandler) {
            saksbehandler.opprettTilbakekreving(scenario = scenario,
                                                varsel = true,
                                                verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 4,
                                under4rettsgebyr = false,
                                muligforeldelse = true,
                                periodelengde = 4)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.EF_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER,
                                     aktsomhet = Aktsomhet.SIMPEL_UAKTSOMHET,
                                     særligeGrunner = listOf(SærligGrunn.STØRRELSE_BELØP, SærligGrunn.ANNET),
                                     andelTilbakekreves = BigDecimal.valueOf(60.0))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.DELVIS_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving uten varsel, SPER melding, ENDR melding, vilkårsvurdering god tro`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = false,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 4,
                                under4rettsgebyr = false,
                                muligforeldelse = false)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            opprettStatusmelding(KodeStatusKrav.SPER)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettStatusmelding(KodeStatusKrav.ENDR)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            // Ikke mulig foreldelse, steget skal derfor være autoutført
            behandleFakta(Hendelsestype.EF_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            settBehandlingPåVent(Venteårsak.AVVENTER_DOKUMENTASJON, LocalDate.now().plusWeeks(3))
            erBehandlingPåVent(Venteårsak.AVVENTER_DOKUMENTASJON)

            taBehandlingAvVent()
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.GOD_TRO,
                                     beløpErIBehold = true,
                                     beløpTilbakekreves = BigDecimal.valueOf(2400))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.DELVIS_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving med varsel, skatt, vilkårsvurdering forsett med renter`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = true,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 3,
                                skattProsent = BigDecimal(10),
                                under4rettsgebyr = false,
                                muligforeldelse = false)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.EF_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER,
                                     aktsomhet = Aktsomhet.FORSETT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            beregn()

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

}
