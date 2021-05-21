package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingssteg
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.domene.dto.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.dto.Venteårsak
import no.nav.familie.tilbake.e2e.domene.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.domene.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.domene.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpprettTilbakekrevingBA(@Autowired val familieTilbakeKlient: FamilieTilbakeKlient) {

    val fagsystem = Fagsystem.BA
    val ytelsestype = Ytelsestype.BARNETRYGD

    lateinit var saksbehandler: Saksbehandler

    @BeforeEach
    fun setup() {
        saksbehandler = Saksbehandler(familieTilbakeKlient = familieTilbakeKlient)
    }

    @Test
    fun `tilbakekrevingsbehandling med varsel, gjenoppta, kravgrunnlag med foreldelse, ikke foreldet, vilkårsvurdering simpel uaktsomhet delvis tilbakebetaling småbeløp`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(eksternFagsakId = eksternFagsakId,
                                                                fagsystem = fagsystem,
                                                                ytelsestype = ytelsestype,
                                                                varsel = true,
                                                                verge = false)
        saksbehandler.opprettStatusmelding(KodeStatusKrav.SPER)
        // saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        saksbehandler.erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

        saksbehandler.taBehandlingAvVent()
        saksbehandler.erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                          antallPerioder = 4,
                                          under4rettsgebyr = false,
                                          muligforeldelse = true)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
        saksbehandler.erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER,
                                               aktsomhet = Aktsomhet.SIMPEL_UAKTSOMHET,
                                               særligeGrunner = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET, SærligGrunn.ANNET),
                                               andelTilbakekreves = BigDecimal(40),
                                               tilbakekrevSmåbeløp = true)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleForeslåVedtak()
        saksbehandler.erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)
    }

    @Test
    fun `tilbakekrevingsbehandling uten varsel med NY kravgrunnlag, SPER melding, ENDR melding, behandling av Fakta, vilkårsvurdering grov uaktsomhet full tilbakekreving`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(eksternFagsakId = eksternFagsakId,
                                                                fagsystem = fagsystem,
                                                                ytelsestype = ytelsestype,
                                                                varsel = false,
                                                                verge = false)
        saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        saksbehandler.erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                          antallPerioder = 2,
                                          under4rettsgebyr = false,
                                          muligforeldelse = false)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        saksbehandler.opprettStatusmelding(KodeStatusKrav.SPER)
        saksbehandler.erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettStatusmelding(KodeStatusKrav.ENDR)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        saksbehandler.settBehandlingPåVent(Venteårsak.AVVENTER_DOKUMENTASJON, LocalDate.now().plusWeeks(3))
        // TODO: saksbehandler.erBehandlingPåVent()

        saksbehandler.taBehandlingAvVent()
        saksbehandler.erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)

        saksbehandler.erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
        saksbehandler.erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                                               aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                                               andelTilbakekreves = BigDecimal(0),
                                               særligeGrunner = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET,
                                                                       SærligGrunn.STØRRELSE_BELØP,
                                                                       SærligGrunn.ANNET))
        saksbehandler.erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleForeslåVedtak()
        saksbehandler.erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)
    }

    @Test
    fun `tilbakekreving med alle perioder foreldet`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(eksternFagsakId = eksternFagsakId,
                                                                fagsystem = fagsystem,
                                                                ytelsestype = Ytelsestype.BARNETRYGD,
                                                                varsel = false,
                                                                verge = false)
        saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        saksbehandler.erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                          antallPerioder = 2,
                                          under4rettsgebyr = false,
                                          muligforeldelse = true)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleForeldelse(Foreldelsesvurderingstype.FORELDET)
        saksbehandler.erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.AUTOUTFØRT)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleForeslåVedtak()
        saksbehandler.erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)
    }

    @Test
    fun `kravgrunnlag uten at behandling opprettes først`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        saksbehandler.opprettKravgrunnlagUtenBehandling(status = KodeStatusKrav.NY,
                                                        fagsystem = Fagsystem.BA,
                                                        ytelsestype = Ytelsestype.BARNETRYGD,
                                                        eksternFagsakId = eksternFagsakId,
                                                        antallPerioder = 1,
                                                        under4rettsgebyr = false,
                                                        muligforeldelse = false)
        //TODO: Fortsette å utvide testen når funk kommer i familie-tilbake
    }

    @Test
    fun `tilbakekreving behandles så langt det er mulig før iverksetting, vilkårsvurdering god tro, så AVSL-melding og henleggelse`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(eksternFagsakId = eksternFagsakId,
                                                                fagsystem = fagsystem,
                                                                ytelsestype = Ytelsestype.BARNETRYGD,
                                                                varsel = true,
                                                                verge = false)
        saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        saksbehandler.erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

        saksbehandler.taBehandlingAvVent()
        saksbehandler.erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                          antallPerioder = 2,
                                          under4rettsgebyr = false,
                                          muligforeldelse = false)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
        saksbehandler.erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.GOD_TRO,
                                               beløpTilbakekreves = BigDecimal(4400.0))
        saksbehandler.erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleForeslåVedtak()
        saksbehandler.erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

        saksbehandler.opprettStatusmelding(KodeStatusKrav.AVSL)
        // saksbehandler.erBehandlingAvsluttet(behandlingId, Behandlingsresultatstype.HENLAGT_KRAVGRUNNLAG_NULLSTILT)
    }

    @Test
    fun `tilbakekrevingsbehandling uten varsel, tilleggsfrist for foreldelse, uaktsomhet forsett`() {
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(eksternFagsakId = eksternFagsakId,
                                                                fagsystem = fagsystem,
                                                                ytelsestype = ytelsestype,
                                                                varsel = false,
                                                                verge = false)
        saksbehandler.hentBehandlingId(fagsystem, eksternFagsakId, eksternBrukId)
        saksbehandler.erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                          antallPerioder = 1,
                                          under4rettsgebyr = false,
                                          muligforeldelse = true)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleForeldelse(Foreldelsesvurderingstype.TILLEGGSFRIST)
        saksbehandler.erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                                               aktsomhet = Aktsomhet.FORSETT)
        saksbehandler.erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

        saksbehandler.behandleForeslåVedtak()
        saksbehandler.erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)
    }
}
