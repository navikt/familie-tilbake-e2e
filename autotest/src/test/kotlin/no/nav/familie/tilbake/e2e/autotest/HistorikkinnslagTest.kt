package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.felles.Saksbehandler
import no.nav.familie.tilbake.e2e.felles.Scenario
import no.nav.familie.tilbake.e2e.klienter.FamilieHistorikkKlient
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
import kotlin.random.Random

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("Trenger Kafka, så kan bare kjøres lokalt")
class HistorikkinnslagTest(
    @Autowired val familieTilbakeKlient: FamilieTilbakeKlient,
    @Autowired val familieHistorikkKlient: FamilieHistorikkKlient
) {

    private lateinit var saksbehandler: Saksbehandler
    private lateinit var scenario: Scenario

    private val fagsystem: Fagsystem = Fagsystem.BA
    private val ytelsestype: Ytelsestype = Ytelsestype.BARNETRYGD

    @BeforeEach
    fun setup() {
        saksbehandler = Saksbehandler(
            familieTilbakeKlient = familieTilbakeKlient,
            familieHistorikkKlient = familieHistorikkKlient
        )
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
    fun `Historikkinnslag lagres for tilbakekreving med varsel, verge, mulig foreldelse, bestilling av brev`() {
        with(saksbehandler) {
            opprettTilbakekreving(
                scenario = scenario,
                varsel = true,
                verge = true
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

            behandleForeldelse(beslutning = Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(
                vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                aktsomhet = Aktsomhet.FORSETT,
                andelTilbakekreves = BigDecimal(100),
                særligeGrunner = listOf(
                    SærligGrunn.GRAD_AV_UAKTSOMHET,
                    SærligGrunn.STØRRELSE_BELØP,
                    SærligGrunn.ANNET
                ),
                ileggRenter = true
            )
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            bestillBrev(Dokumentmalstype.INNHENT_DOKUMENTASJON)
            taBehandlingAvVent()

            beregn()
            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(Saksbehandler.BESLUTTER_IDENT)
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)

            saksbehandler.verifiserHistorikkinnslag()
        }
    }
}
