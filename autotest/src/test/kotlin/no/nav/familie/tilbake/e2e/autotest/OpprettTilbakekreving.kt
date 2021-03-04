package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Faktainfo
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Varsel
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.Behandlingsoppsummering
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.UUID

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpprettTilbakekreving(@Autowired private val familieTilbakeKlient: FamilieTilbakeKlient) {

    @Test
    fun `Skal opprette tilbakekrevingsbehandling`() {
        val fagsystem = Fagsystem.BA
        val eksternFagsakId = UUID.randomUUID().toString()
        val eksternBehandlingId = UUID.randomUUID().toString()

        val perioder =
                arrayListOf(
                        Periode(fom = LocalDate.of(2020, Month.JANUARY, 1), tom = LocalDate.of(2020, Month.MARCH, 31)),
                        Periode(fom = LocalDate.of(2020, Month.AUGUST, 1), tom = LocalDate.of(2020, Month.OCTOBER, 30)),
                )
        val opprettRespons = familieTilbakeKlient.opprettTilbakekreving(fagsystem = fagsystem,
                                                                        ytelsestype = Ytelsestype.BARNETRYGD,
                                                                        eksternId = eksternBehandlingId,
                                                                        eksternFagsakId = eksternFagsakId,
                                                                        revurderingsvedtaksdato = LocalDate.now(),
                                                                        personIdent = "12345678901",
                                                                        varsel = Varsel(varseltekst = "Ytelsen må tilbakekreves",
                                                                                        sumFeilutbetaling = BigDecimal(2222),
                                                                                        perioder = perioder),
                                                                        faktainfo = Faktainfo(revurderingsårsak = "Ny søknad",
                                                                                              revurderingsresultat = "Ytelsen opphøres",
                                                                                              Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                                                                                              konsekvensForYtelser = setOf("Opphør av ytelsen",
                                                                                                                           "Ytelsen redusert")));

        assertTrue(opprettRespons.status == Ressurs.Status.SUKSESS, "Opprett behandling skulle hatt status SUKSESS")
        val eksternBrukId = opprettRespons.data.toString()
        println("Har opprettet en behandling med eksternBrukId $eksternBrukId for eksternBehandlingId $eksternBehandlingId på fagsak $eksternFagsakId")

        val fagsakRespons = familieTilbakeKlient.hentFagsak(fagsystem = fagsystem, eksternFagsakId = eksternFagsakId);
        assertTrue(fagsakRespons.status == Ressurs.Status.SUKSESS, "Fagsaken skulle ha blitt opprettet")

        val behandlingsoppsummering = fagsakRespons.data?.behandlinger?.find { behandling: Behandlingsoppsummering -> behandling.eksternBrukId.toString() == eksternBrukId }
        assertNotNull(behandlingsoppsummering, "Skal være behandling med eksternBehandlingId $eksternBehandlingId i fagsak")
        val behandlingId = behandlingsoppsummering?.behandlingId
        println("Behandling er opprettet med id $behandlingId")

        val behandlingRespons = familieTilbakeKlient.hentBehandling(behandlingId = behandlingId.toString())
        assertTrue(behandlingRespons.status == Ressurs.Status.SUKSESS, "Behandlingen skulle ha blitt opprettet")
    }
}
