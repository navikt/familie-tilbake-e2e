package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.Behandlingssteg
import no.nav.familie.tilbake.e2e.domene.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.domene.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.Venteårsak
import no.nav.familie.tilbake.e2e.domene.Behandlingsresultatstype
import no.nav.familie.tilbake.e2e.domene.steg.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.domene.steg.dto.FaktaSteg
import no.nav.familie.tilbake.e2e.domene.steg.dto.ForeldelseSteg
import no.nav.familie.tilbake.e2e.domene.steg.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.domene.steg.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.domene.steg.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.domene.steg.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.domene.steg.dto.VilkårsvurderingSteg
import no.nav.familie.tilbake.e2e.domene.steg.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.klient.OpprettKravgrunnlagBuilder
import no.nav.familie.tilbake.e2e.klient.OpprettTilbakekrevingBuilder
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
class OpprettTilbakekrevingBA(@Autowired val familieTilbakeKlient: FamilieTilbakeKlient) {

    val fagsystem = Fagsystem.BA
    val ytelsestype = Ytelsestype.BARNETRYGD

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
            antallPerioder = 1,
            under4rettsgebyr = false,
            muligforeldelse = true)

        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        val faktasteg: FaktaSteg = saksbehandler.hentBehandlingssteg(Behandlingssteg.FAKTA, behandlingId) as FaktaSteg
        faktasteg.addFaktaVurdering(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
        saksbehandler.behandleSteg(faktasteg, behandlingId)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

        val foreldelsesteg: ForeldelseSteg = saksbehandler.hentBehandlingssteg(Behandlingssteg.FORELDELSE, behandlingId) as ForeldelseSteg
        foreldelsesteg.addForeldelseVurdering(Foreldelsesvurderingstype.IKKE_FORELDET)
        saksbehandler.behandleSteg(foreldelsesteg, behandlingId)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

        val vilkarsvurderingssteg: VilkårsvurderingSteg = saksbehandler.hentBehandlingssteg(Behandlingssteg.VILKÅRSVURDERING, behandlingId) as VilkårsvurderingSteg
        vilkarsvurderingssteg.addVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                                                  aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                                                  beløpIBehold = true,
                                                  andelTilbakekreves = BigDecimal.TEN,
                                                  særligeGrunner = listOf(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, SærligGrunn.GRAD_AV_UAKTSOMHET, SærligGrunn.ANNET))
        // vilkarsvurderingssteg.addVilkårsvurdering(Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER, Aktsomhet.FORSETT)
        saksbehandler.behandleSteg(vilkarsvurderingssteg, behandlingId)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)
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
            antallPerioder = 2,
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

    @Test
    fun `tilbakekreving med alle perioder foreldet`() {
        /**
         * Opprett behandling
         * Sjekk at behandling settes på vent
         */
        val eksternFagsakId = Random.nextInt(1000000, 9999999).toString()
        val eksternBrukId = saksbehandler.opprettTilbakekreving(
            eksternFagsakId = eksternFagsakId,
            fagsystem = fagsystem,
            ytelsestype = Ytelsestype.BARNETRYGD,
            varsel = false,
            verge = false
        )
        val behandlingId = saksbehandler.hentBehandlingId(
            fagsystem = fagsystem,
            eksternFagsakId = eksternFagsakId,
            eksternBrukId = eksternBrukId
        )
        saksbehandler.erBehandlingPåVent(
            behandlingId = behandlingId,
            venteårsak = Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG
        )

        /**
         * Opprett kravgrunnlag
         * Sjekk at behandling settest til steg FAKTA = KLAR
         */
        saksbehandler.opprettKravgrunnlag(
            status = KodeStatusKrav.NY,
            antallPerioder = 2,
            under4rettsgebyr = false,
            muligforeldelse = true
        )
        saksbehandler.erBehandlingISteg(
            behandlingId = behandlingId,
            behandlingssteg = Behandlingssteg.FAKTA,
            behandlingsstegstatus = Behandlingsstegstatus.KLAR
        )

        /**
         * Behandle fakta
         * Sjekk at behandling settes til steg FORELDELSE = KLAR
         */
        val faktasteg: FaktaSteg = saksbehandler.hentBehandlingssteg(
            stegtype = Behandlingssteg.FAKTA,
            behandlingId = behandlingId
        ) as FaktaSteg
        faktasteg.addFaktaVurdering(
            hendelse = Hendelsestype.BA_ANNET,
            underhendelse = Hendelsesundertype.ANNET_FRITEKST
        )
        saksbehandler.behandleSteg(
            stegdata = faktasteg,
            behandlingId = behandlingId
        )
        saksbehandler.erBehandlingISteg(
            behandlingId = behandlingId,
            behandlingssteg = Behandlingssteg.FORELDELSE,
            behandlingsstegstatus = Behandlingsstegstatus.KLAR
        )

        /**
         * Behandle foreldelse - setter alle perioder til FORELDET
         * Sjekk at behandling hopper over steg VILKÅRSVURDERING
         * Sjekk at behandling sette til steg FORESLÅ_VEDTAK = KLAR
         */
        val foreldelsesteg: ForeldelseSteg = saksbehandler.hentBehandlingssteg(
            stegtype = Behandlingssteg.FORELDELSE,
            behandlingId = behandlingId
        ) as ForeldelseSteg
        foreldelsesteg.addForeldelseVurdering(
            beslutning = Foreldelsesvurderingstype.FORELDET
        )
        saksbehandler.behandleSteg(
            stegdata = foreldelsesteg,
            behandlingId = behandlingId
        )
        saksbehandler.erBehandlingISteg(
            behandlingId = behandlingId,
            behandlingssteg = Behandlingssteg.VILKÅRSVURDERING,
            behandlingsstegstatus = Behandlingsstegstatus.AUTOUTFØRT
        )
        saksbehandler.erBehandlingISteg(
            behandlingId = behandlingId,
            behandlingssteg = Behandlingssteg.FORESLÅ_VEDTAK,
            behandlingsstegstatus = Behandlingsstegstatus.KLAR
        )
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

    @Test
    fun `tilbakekreving behandles så langt det er mulig før iverksetting så AVSL-melding og henleggelse`() {
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
        saksbehandler.taBehandlingAvVent(behandlingId)
        saksbehandler.erBehandlingPåVent(behandlingId, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

        saksbehandler.opprettKravgrunnlag(
            status = KodeStatusKrav.NY,
            antallPerioder = 2,
            under4rettsgebyr = false,
            muligforeldelse = false)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

        val faktasteg: FaktaSteg = saksbehandler.hentBehandlingssteg(Behandlingssteg.FAKTA, behandlingId) as FaktaSteg
        faktasteg.addFaktaVurdering(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
        saksbehandler.behandleSteg(faktasteg, behandlingId)
        saksbehandler.erBehandlingISteg(behandlingId, Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

        //Todo: Legge til behandling av Vilkårsvurdering og Foreslå_Vedtak

        saksbehandler.opprettStatusmelding(KodeStatusKrav.AVSL)
        saksbehandler.erBehandlingAvsluttet(behandlingId, Behandlingsresultatstype.HENLAGT_KRAVGRUNNLAG_NULLSTILT)
    }

}
