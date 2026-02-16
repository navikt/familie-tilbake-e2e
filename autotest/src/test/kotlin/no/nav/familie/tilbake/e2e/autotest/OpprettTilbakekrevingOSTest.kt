package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Fagsystem
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
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsestype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsesundertype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.KodeStatusKrav
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Venteårsak
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
class OpprettTilbakekrevingOSTest {
    @Autowired
    lateinit var familieTilbakeKlient: FamilieTilbakeKlient

    private lateinit var saksbehandler: Saksbehandler
    private lateinit var scenario: Scenario

    private val fagsystem: Fagsystem = Fagsystem.EF
    private val ytelsestype: Ytelsestype = Ytelsestype.OVERGANGSSTØNAD

    @BeforeEach
    fun setup() {
        saksbehandler = Saksbehandler(familieTilbakeKlient = familieTilbakeKlient)
        scenario = Scenario(
            eksternFagsakId = Random.nextInt(1000000, 9999999).toString(),
            eksternBehandlingId = Random.nextInt(1000000, 9999999).toString(),
            fagsystem = fagsystem,
            ytelsestype = ytelsestype,
            personIdent = "12345678901",
            enhetId = "4489",
            enhetsnavn = "4489 NAY"
        )
    }

    @Test
    fun `Tilbakekreving med varsel, gjenoppta, kravgrunnlag med foreldelse, vilkårsvurdering simpel uaktsomhet delvis tilbakebetaling`() {
        with(saksbehandler) {
            saksbehandler.opprettTilbakekreving(
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
                muligforeldelse = true,
                periodelengde = 4
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER,
                aktsomhet = Aktsomhet.SIMPEL_UAKTSOMHET,
                særligeGrunner = listOf(SærligGrunn.STØRRELSE_BELØP, SærligGrunn.ANNET),
                andelTilbakekreves = BigDecimal.valueOf(60.0)
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
    fun `Tilbakekreving uten varsel, SPER melding, ENDR melding, vilkårsvurdering god tro`() {
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
                under4rettsgebyr = false,
                muligforeldelse = false
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            opprettStatusmelding(KodeStatusKrav.SPER)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettStatusmelding(KodeStatusKrav.ENDR)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            // Ikke mulig foreldelse, steget skal derfor være autoutført
            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            settBehandlingPåVent(Venteårsak.AVVENTER_DOKUMENTASJON, LocalDate.now().plusWeeks(3))
            erBehandlingPåVent(Venteårsak.AVVENTER_DOKUMENTASJON)

            taBehandlingAvVent()
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.GOD_TRO,
                beløpErIBehold = true,
                beløpTilbakekreves = BigDecimal.valueOf(2400)
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
    fun `Tilbakekreving med varsel, skatt, vilkårsvurdering forsett med renter`() {
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
                antallPerioder = 3,
                skattProsent = BigDecimal(10),
                under4rettsgebyr = false,
                muligforeldelse = false
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                aktsomhet = Aktsomhet.FORSETT
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            beregn()

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Opprett dummy-test-data lokalt`() {
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
                antallPerioder = 3,
                skattProsent = BigDecimal(10),
                under4rettsgebyr = false,
                muligforeldelse = false
            )
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)
        }
    }
}
