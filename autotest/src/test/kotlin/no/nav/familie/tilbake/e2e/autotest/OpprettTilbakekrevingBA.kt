package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.Behandlingssteg
import no.nav.familie.tilbake.e2e.domene.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.domene.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.Venteårsak
import no.nav.familie.tilbake.e2e.domene.steg.FaktaSteg
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
    fun `tilbakekrevingsbehandling med varsel`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(
                eksternFagsakId = eksternFagsakId,
                fagsystem = fagsystem,
                ytelsestype = Ytelsestype.BARNETRYGD,
                varsel = true,
                verge = false
        )

        val behandlingId = saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)
        //TODO: Registrere brukerrespons og verifisere neste steg
    }

    @Test
    fun `tilbakekrevingsbehandling uten varsel med NY kravgrunnlag og SPER melding`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(
                eksternFagsakId = eksternFagsakId,
                fagsystem = fagsystem,
                ytelsestype = Ytelsestype.BARNETRYGD,
                varsel = false,
                verge = false
        )

        val behandlingId = saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettKravgrunnlag(KodeStatusKrav.NY,2, false, false)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)
        saksbehandler.opprettStatusmelding(KodeStatusKrav.SPER)
        saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)
    }

    @Test
    fun `kravgrunnlag uten at behandling opprettes først`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        saksbehandler.opprettKravgrunnlagUtenBehandling(
            status = KodeStatusKrav.NY,
            fagsystem = Fagsystem.BA,
            ytelsestype = Ytelsestype.BARNETRYGD,
            eksternFagsakId = eksternFagsakId,
            antallPerioder = 1,
            under4rettsgebyr = false,
            muligforeldelse = false
        )
        //TODO: Fortsette å utvide testen når funk kommer i familie-tilbake
    }

}
