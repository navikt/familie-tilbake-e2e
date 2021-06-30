package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.familie_historikk.FamilieHistorikkKlient
import no.nav.familie.tilbake.e2e.familie_tilbake.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Behandlingsresultatstype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Behandlingssteg
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Dokumentmalstype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.KodeStatusKrav
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Venteårsak
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.felles.Saksbehandler
import no.nav.familie.tilbake.e2e.felles.Scenario
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import kotlin.random.Random

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HistorikkinnslagTest(@Autowired val familieTilbakeKlient: FamilieTilbakeKlient,
                           @Autowired val familieHistorikkKlient: FamilieHistorikkKlient) {

    private lateinit var saksbehandler: Saksbehandler
    private lateinit var scenario: Scenario

    private val fagsystem: Fagsystem = Fagsystem.BA
    private val ytelsestype: Ytelsestype = Ytelsestype.BARNETRYGD

    @BeforeEach
    fun setup() {
        saksbehandler = Saksbehandler(familieTilbakeKlient = familieTilbakeKlient,
                                      familieHistorikkKlient = familieHistorikkKlient)
        scenario = Scenario(eksternFagsakId = Random.nextInt(1000000, 9999999).toString(),
                            eksternBehandlingId = Random.nextInt(1000000, 9999999).toString(),
                            fagsystem = fagsystem,
                            ytelsestype = ytelsestype,
                            personIdent = "12345678901",
                            enhetId = "0106",
                            enhetsnavn = "NAV Fredrikstad")
    }

    @Test
    fun `Historikkinnslag lagres for tilbakekreving med varsel, verge, mulig foreldelse, bestilling av brev`() {
        with(saksbehandler) {
            opprettTilbakekreving(scenario = scenario,
                                  varsel = true,
                                  verge = true)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            Thread.sleep(10_000)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 4,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            Thread.sleep(10_000)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            Thread.sleep(10_000)

            behandleForeldelse(beslutning = Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            Thread.sleep(10_000)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                                     aktsomhet = Aktsomhet.FORSETT,
                                     andelTilbakekreves = BigDecimal(100),
                                     særligeGrunner = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET,
                                                             SærligGrunn.STØRRELSE_BELØP,
                                                             SærligGrunn.ANNET),
                                     ileggRenter = true)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            Thread.sleep(10_000)

            bestillBrev(Dokumentmalstype.INNHENT_DOKUMENTASJON)
            taBehandlingAvVent()

            Thread.sleep(10_000)

            beregn()
            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            Thread.sleep(10_000)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)

            saksbehandler.verifiesrHistorikkinnslag()
        }
    }
}
