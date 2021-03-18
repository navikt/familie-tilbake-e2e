package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.Behandling
import no.nav.familie.tilbake.e2e.domene.Venteårsak
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.random.Random

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpprettTilbakekrevingBA(@Autowired private val familieTilbakeKlient: FamilieTilbakeKlient) {

    val fagsystem = Fagsystem.BA

    @Test
    fun `Tilbakekrevingsbehandling med varsel`() {

        val behandling: Behandling? = familieTilbakeKlient.hentBehandling("d51e59dc-f11a-4d74-827c-36ba9f54a416")

        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = familieTilbakeKlient.opprettTilbakekreving(
            eksternFagsakId = eksternFagsakId,
            fagsystem = fagsystem,
            ytelsestype = Ytelsestype.BARNETRYGD,
            varsel = true,
            verge = false
        )

        val behandlingId = familieTilbakeKlient.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        assertTrue(familieTilbakeKlient.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING), "Behandling står ikke på vent og/eller med riktig venteårsak")
        //TODO: Registrere brukerrespons og verifisere neste steg
    }

    @Test
    fun `Tilbakekrevingsbehandling uten varsel`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = familieTilbakeKlient.opprettTilbakekreving(
            eksternFagsakId = eksternFagsakId,
            fagsystem = fagsystem,
            ytelsestype = Ytelsestype.BARNETRYGD,
            varsel = false,
            verge = false
        )

        val behandlingId = familieTilbakeKlient.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        assertTrue(familieTilbakeKlient.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG), "Behandling står ikke på vent og/eller med riktig venteårsak")
        //TODO: Registere kravgrunnlag
    }

}
