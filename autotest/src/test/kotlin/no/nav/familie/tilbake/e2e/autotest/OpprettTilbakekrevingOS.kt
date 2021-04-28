package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.Behandlingssteg
import no.nav.familie.tilbake.e2e.domene.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.domene.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.Venteårsak
import no.nav.familie.tilbake.e2e.domene.steg.dto.FaktaSteg
import no.nav.familie.tilbake.e2e.domene.steg.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.domene.steg.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.domene.steg.dto.ForeldelseSteg
import no.nav.familie.tilbake.e2e.domene.steg.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.klient.OpprettKravgrunnlagBuilder
import no.nav.familie.tilbake.e2e.klient.OpprettTilbakekrevingBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import kotlin.random.Random

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpprettTilbakekrevingOS(@Autowired val familieTilbakeKlient: FamilieTilbakeKlient) {

    val fagsystem = Fagsystem.EF
    val ytelsestype = Ytelsestype.OVERGANGSSTØNAD

    lateinit var saksbehandler: Saksbehandler

    @BeforeEach
    fun setup() {
        saksbehandler = Saksbehandler(familieTilbakeKlient = familieTilbakeKlient,
                                      opprettTilbakekrevingBuilder = OpprettTilbakekrevingBuilder(),
                                      opprettKravgrunnlagBuilder = OpprettKravgrunnlagBuilder())
    }

    @Test
    fun `tilbakekrevingsbehandling med varsel, gjenoppta, kravgrunnlag med foreldelse`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(
                eksternFagsakId = eksternFagsakId,
                fagsystem = fagsystem,
                ytelsestype = ytelsestype,
                varsel = true,
                verge = false
        )

        val behandlingId = saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)
        saksbehandler.taBehandlingAvVent(behandlingId)
        saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettKravgrunnlag(
            status = KodeStatusKrav.NY,
            antallPerioder = 4,
            under4rettsgebyr = false,
            muligforeldelse = true,
            periodeLengde = 4,
        )

        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        val faktasteg: FaktaSteg = saksbehandler.hentBehandlingssteg(Behandlingssteg.FAKTA, behandlingId) as FaktaSteg
        faktasteg.addFaktaVurdering(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
        saksbehandler.behandleSteg(faktasteg, behandlingId)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

        val foreldelsesteg: ForeldelseSteg = saksbehandler.hentBehandlingssteg(Behandlingssteg.FORELDELSE, behandlingId) as ForeldelseSteg
        foreldelsesteg.addForeldelseVurdering(Foreldelsesvurderingstype.IKKE_FORELDET)
        saksbehandler.behandleSteg(foreldelsesteg, behandlingId)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)
    }

    @Test
    fun `tilbakekrevingsbehandling uten varsel med NY kravgrunnlag, SPER melding, ENDR melding, behandling av Fakta`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(
                eksternFagsakId = eksternFagsakId,
                fagsystem = fagsystem,
                ytelsestype = ytelsestype,
                varsel = false,
                verge = false
        )

        val behandlingId = saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettKravgrunnlag(
            status = KodeStatusKrav.NY,
            antallPerioder = 4,
            under4rettsgebyr = false,
            muligforeldelse = false)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)
        saksbehandler.opprettStatusmelding(KodeStatusKrav.SPER)
        saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)
        saksbehandler.opprettStatusmelding(KodeStatusKrav.ENDR)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        val faktasteg: FaktaSteg = saksbehandler.hentBehandlingssteg(Behandlingssteg.FAKTA, behandlingId) as FaktaSteg
        faktasteg.addFaktaVurdering(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
        saksbehandler.behandleSteg(faktasteg, behandlingId)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

        saksbehandler.settBehandlingPåVent(behandlingId, Venteårsak.AVVENTER_DOKUMENTASJON, LocalDate.now().plusWeeks(3))
        saksbehandler.taBehandlingAvVent(behandlingId)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

    }

}