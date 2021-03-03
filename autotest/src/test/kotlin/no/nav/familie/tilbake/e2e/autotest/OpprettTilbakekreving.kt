package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Faktainfo
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Varsel
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpprettTilbakekreving(@Autowired private val familieTilbakeKlient: FamilieTilbakeKlient) {

    @Test
    fun `Skal opprette tilbakekrevingsbehandling`() {
        val fagsystem = Fagsystem.BA
        val eksternFagsakId = "fs1234"

        val opprettRespons = familieTilbakeKlient.opprettTilbakekreving(fagsystem = fagsystem,
                                                                      ytelsestype = Ytelsestype.BARNETRYGD,
                                                                      eksternId = "beh12345",
                                                                      eksternFagsakId = eksternFagsakId,
                                                                      revurderingsvedtaksdato = LocalDate.now(),
                                                                      personIdent = "123456000001",
                                                                      varsel = Varsel(varseltekst = "Ytelsen må tilbakekreves",
                                                                                      sumFeilutbetaling = BigDecimal(2222),
                                                                                      perioder = arrayListOf()),
                                                                      faktainfo = Faktainfo(revurderingsårsak = "Ny søknad",
                                                                                            revurderingsresultat = "Ytelsen opphøres",
                                                                                            Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL,
                                                                                            konsekvensForYtelser = setOf("Opphør av ytelsen",
                                                                                                                         "Ytelsen redusert")));

        assertTrue(opprettRespons.status == Ressurs.Status.SUKSESS, "Opprett behandling skulle hatt status SUKSESS")
        val behandlingId = opprettRespons.data.toString()

        val fagsakRespons = familieTilbakeKlient.hentFagsak(fagsystem = fagsystem, eksternFagsakId = eksternFagsakId);
        assertTrue(fagsakRespons.status == Ressurs.Status.SUKSESS, "Fagsaken skulle ha blitt opprettet")

        val behandlingRespons = familieTilbakeKlient.hentBehandling(behandlingId = behandlingId)
        assertTrue(behandlingRespons.status == Ressurs.Status.SUKSESS, "Behandlingen skulle ha blitt opprettet")
    }
}
