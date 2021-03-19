package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.Venteårsak
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.klient.OpprettKravgrunnlagBuilder
import no.nav.familie.tilbake.e2e.klient.OpprettTilbakekrevingBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.random.Random

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpprettTilbakekrevingBA(@Autowired val familieTilbakeKlient: FamilieTilbakeKlient) {

    val fagsystem = Fagsystem.BA

    lateinit var saksbehandler: Saksbehandler

    @BeforeEach
    fun setup() {
        saksbehandler = Saksbehandler(familieTilbakeKlient = familieTilbakeKlient,
                                      opprettTilbakekrevingBuilder = OpprettTilbakekrevingBuilder(),
                                      opprettKravgrunnlagBuilder = OpprettKravgrunnlagBuilder())
    }

    @Test
    fun `Tilbakekrevingsbehandling med varsel`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(
                eksternFagsakId = eksternFagsakId,
                fagsystem = fagsystem,
                ytelsestype = Ytelsestype.BARNETRYGD,
                varsel = true,
                verge = false
        )

        val behandlingId = saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        assertTrue(saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING),
                   "Behandling står ikke på vent og/eller med riktig venteårsak")
        //TODO: Registrere brukerrespons og verifisere neste steg
    }

    @Test
    fun `Tilbakekrevingsbehandling uten varsel`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(
                eksternFagsakId = eksternFagsakId,
                fagsystem = fagsystem,
                ytelsestype = Ytelsestype.BARNETRYGD,
                varsel = false,
                verge = false
        )

        val behandlingId = saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        assertTrue(saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG),
                   "Behandling står ikke på vent og/eller med riktig venteårsak")
        saksbehandler.opprettKravgrunnlag(KodeStatusKrav.NY,2, false, false)
        //TODO: Registere kravgrunnlag og verifisere neste steg
    }

}
