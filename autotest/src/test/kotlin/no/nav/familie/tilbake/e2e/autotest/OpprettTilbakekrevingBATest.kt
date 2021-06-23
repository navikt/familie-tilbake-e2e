package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingsresultatstype
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingssteg
import no.nav.familie.tilbake.e2e.domene.dto.Behandlingsstegstatus
import no.nav.familie.tilbake.e2e.domene.dto.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.dto.Venteårsak
import no.nav.familie.tilbake.e2e.domene.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.domene.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.domene.dto.Vilkårsvurderingsresultat
import no.nav.familie.tilbake.e2e.domene.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.domene.dto.Dokumentmalstype
import no.nav.familie.tilbake.e2e.felles.Saksbehandler
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
class OpprettTilbakekrevingBATest(@Autowired val familieTilbakeKlient: FamilieTilbakeKlient) {

    private val fagsystem = Fagsystem.BA
    private val ytelsestype = Ytelsestype.BARNETRYGD

    private lateinit var saksbehandler: Saksbehandler
    private lateinit var eksternFagsakId: String // TODO: Fjern når alle testere er ryddet
    private lateinit var eksternBrukId: String // TODO: Fjern når alle testere er ryddet

    @BeforeEach
    fun setup() {
        saksbehandler = Saksbehandler(familieTilbakeKlient = familieTilbakeKlient)
    }

    @Test
    fun `Tilbakekreving med varsel, kravgrunnlag uten foreldelse, vilkårsvurdering forsett, full tilbakebetaling`() {
        with(saksbehandler) {
            opprettTilbakekreving(fagsystem = fagsystem,
                                  ytelsestype = ytelsestype,
                                  varsel = true,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 4,
                                under4rettsgebyr = false,
                                muligforeldelse = false)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            // Ikke mulig foreldelse, steget skal derfor være autoutført
            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                                     aktsomhet = Aktsomhet.FORSETT,
                                     andelTilbakekreves = BigDecimal(100),
                                     særligeGrunner = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET,
                                                             SærligGrunn.STØRRELSE_BELØP,
                                                             SærligGrunn.ANNET))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving med varsel, kravgrunnlag med foreldelse, ikke foreldet, vilkårsvurdering simpel uaktsomhet full tilbakebetaling småbeløp`() {
        with(saksbehandler) {
            opprettTilbakekreving(fagsystem = fagsystem,
                                  ytelsestype = ytelsestype,
                                  varsel = true,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 4,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                                     aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                                     andelTilbakekreves = BigDecimal(100),
                                     særligeGrunner = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET,
                                                             SærligGrunn.STØRRELSE_BELØP,
                                                             SærligGrunn.ANNET))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test // TODO: Verifiser innhold i testen
    fun `Tilbakekreving uten varsel, kravgrunnlag, SPER-melding, ENDR-melding, vilkårsvurdering simpel uaktsomhet 22-15 6 ledd, ingen tilbakekreving`() {
        with(saksbehandler) {
            opprettTilbakekreving(fagsystem = fagsystem,
                                  ytelsestype = ytelsestype,
                                  varsel = false,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 4,
                                periodelengde = 2,
                                under4rettsgebyr = false,
                                muligforeldelse = false)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            opprettStatusmelding(KodeStatusKrav.SPER)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettStatusmelding(KodeStatusKrav.ENDR)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            settBehandlingPåVent(Venteårsak.AVVENTER_DOKUMENTASJON, LocalDate.now().plusWeeks(3))
            erBehandlingPåVent(Venteårsak.AVVENTER_DOKUMENTASJON)

            taBehandlingAvVent()
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            // Ikke mulig foreldelse, steget skal derfor være autoutført
            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)


            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER,
                                     aktsomhet = Aktsomhet.SIMPEL_UAKTSOMHET,
                                     særligeGrunner = listOf(SærligGrunn.TID_FRA_UTBETALING),
                                     andelTilbakekreves = BigDecimal(0))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = false)
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.TILBAKEFØRT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.INGEN_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving behandles så langt det er mulig før iverksetting, vilkårsvurdering god tro, så AVSL-melding og henleggelse`() {
        with(saksbehandler) {
            opprettTilbakekreving(fagsystem = fagsystem,
                                  ytelsestype = Ytelsestype.BARNETRYGD,
                                  varsel = true,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 2,
                                under4rettsgebyr = true,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.GOD_TRO,
                                     beløpTilbakekreves = BigDecimal(4400.0))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            opprettStatusmelding(KodeStatusKrav.AVSL)
            erBehandlingAvsluttet(Behandlingsresultatstype.HENLAGT)
        }
    }

    @Test
    fun `Tilbakekreving med alle perioder foreldet`() {
        with(saksbehandler) {
            opprettTilbakekreving(fagsystem = fagsystem,
                                  ytelsestype = Ytelsestype.BARNETRYGD,
                                  varsel = false,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 2,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            // Alle perioder er foreldet, vilkårsvurdering skal derfor være autoutført
            behandleForeldelse(Foreldelsesvurderingstype.FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.AUTOUTFØRT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingISteg(Behandlingssteg.IVERKSETT_VEDTAK, Behandlingsstegstatus.KLAR)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.INGEN_TILBAKEBETALING)
        }
    }

    @Test // TODO: Fortsette å utvide testen når funk kommer i familie-tilbake
    fun `Kravgrunnlag uten at behandling opprettes først`() {
        with(saksbehandler) {
            eksternFagsakId = Random.nextInt(1000000, 9999999).toString() // TODO: Fjern denne
            opprettKravgrunnlagUtenBehandling(status = KodeStatusKrav.NY,
                                              fagsystem = Fagsystem.BA,
                                              ytelsestype = Ytelsestype.BARNETRYGD,
                                              eksternFagsakId = eksternFagsakId,
                                              antallPerioder = 1,
                                              under4rettsgebyr = false,
                                              muligforeldelse = false)
        }
    }

    @Test
    fun `Tilbakekreving uten varsel, tilleggsfrist for foreldelse, uaktsomhet forsett`() {
        with(saksbehandler) {
            opprettTilbakekreving(fagsystem = fagsystem,
                                  ytelsestype = ytelsestype,
                                  varsel = false,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 1,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            behandleForeldelse(Foreldelsesvurderingstype.TILLEGGSFRIST)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                                     aktsomhet = Aktsomhet.FORSETT)
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.FULL_TILBAKEBETALING)
        }
    }

    @Test
    fun `Tilbakekreving med verge, vilkårsvurdering grov uaktsomhet, delvis tilbakekreving`() {
        with(saksbehandler) {
            opprettTilbakekreving(fagsystem = fagsystem,
                                  ytelsestype = ytelsestype,
                                  varsel = false,
                                  verge = true)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 1,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            behandleFakta(Hendelsestype.BA_ANNET, Hendelsesundertype.ANNET_FRITEKST)
            erBehandlingISteg(Behandlingssteg.FORELDELSE, Behandlingsstegstatus.KLAR)

            // Alle perioder er foreldet, vilkårsvurdering skal derfor være autoutført
            behandleForeldelse(Foreldelsesvurderingstype.IKKE_FORELDET)
            erBehandlingISteg(Behandlingssteg.VILKÅRSVURDERING, Behandlingsstegstatus.KLAR)

            behandleVilkårsvurdering(vilkårvurderingsresultat = Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                                     aktsomhet = Aktsomhet.GROV_UAKTSOMHET,
                                     særligeGrunner = listOf(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.ANNET),
                                     andelTilbakekreves = BigDecimal(50))
            erBehandlingISteg(Behandlingssteg.FORESLÅ_VEDTAK, Behandlingsstegstatus.KLAR)

            behandleForeslåVedtak()
            erBehandlingISteg(Behandlingssteg.FATTE_VEDTAK, Behandlingsstegstatus.KLAR)

            endreAnsvarligSaksbehandler(nyAnsvarligSaksbehandler = "nyAnsvarligSaksbehandler")
            behandleFatteVedtak(godkjent = true)
            erBehandlingAvsluttet(resultat = Behandlingsresultatstype.DELVIS_TILBAKEBETALING)
        }
    }

    @Test
    fun `Manuell bestilling og forhåndsvisning av brev`() {
        with(saksbehandler) {
            opprettTilbakekreving(fagsystem = fagsystem,
                                  ytelsestype = ytelsestype,
                                  varsel = true,
                                  verge = false)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            Thread.sleep(10_000)

            taBehandlingAvVent()
            erBehandlingPåVent(Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)

            opprettKravgrunnlag(status = KodeStatusKrav.NY,
                                antallPerioder = 1,
                                under4rettsgebyr = false,
                                muligforeldelse = true)
            erBehandlingISteg(Behandlingssteg.FAKTA, Behandlingsstegstatus.KLAR)

            bestillBrev(Dokumentmalstype.INNHENT_DOKUMENTASJON)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            taBehandlingAvVent()
            bestillBrev(Dokumentmalstype.VARSEL)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            taBehandlingAvVent()
            bestillBrev(Dokumentmalstype.KORRIGERT_VARSEL)
            erBehandlingPåVent(Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING)

            forhåndsvisVarselbrev(LocalDate.now())

            forhåndsvisHenleggelsesbrev()

            hentJournaldokument()

            // TODO: Fullfør behandling til foreslå vedtak
            // forhåndsvisVedtaksbrev()
        }
    }

}
