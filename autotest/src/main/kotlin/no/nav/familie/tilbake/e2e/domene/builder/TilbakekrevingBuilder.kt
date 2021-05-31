package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingstype
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Faktainfo
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Varsel
import no.nav.familie.kontrakter.felles.tilbakekreving.Verge
import no.nav.familie.kontrakter.felles.tilbakekreving.Vergetype
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

class TilbakekrevingBuilder(eksternFagsakId: String,
                            eksternBehandlingId: String? = null,
                            fagsystem: Fagsystem,
                            ytelsestype: Ytelsestype,
                            varsel: Boolean,
                            verge: Boolean) {

    private val request =
        OpprettTilbakekrevingRequest(fagsystem = fagsystem,
                                     ytelsestype = ytelsestype,
                                     eksternFagsakId = eksternFagsakId,
                                     eksternId = eksternBehandlingId ?: Random.nextInt(1000000, 9999999).toString(),
                                     personIdent = "12345678901",
                                     saksbehandlerIdent = "Z994824",
                                     behandlingstype = Behandlingstype.TILBAKEKREVING,
                                     språkkode = Språkkode.NB,
                                     enhetId = "0106",
                                     enhetsnavn = "NAV Fredrikstad",
                                     revurderingsvedtaksdato = LocalDate.now().minusDays(35),
                                     faktainfo = Faktainfo(revurderingsårsak = "Nye opplysninger",
                                                           revurderingsresultat = "Endring i ytelsen",
                                                           tilbakekrevingsvalg = utledTilbakekrevingsvalg(varsel),
                                                           konsekvensForYtelser = setOf("Reduksjon av ytelsen", "Feilutbetaling")),
                                     varsel = utledVarsel(varsel),
                                     manueltOpprettet = false,
                                     verge = utledVerge(verge))

    private fun utledTilbakekrevingsvalg(varsel: Boolean): Tilbakekrevingsvalg {
        return if (varsel) Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL
        else Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL
    }

    private fun utledVarsel(varsel: Boolean): Varsel? {
        return if (varsel) {
            Varsel(varseltekst = "Automatisk varseltekst fra Autotest",
                   sumFeilutbetaling = BigDecimal(8124),
                   perioder = arrayListOf(
                           Periode(fom = LocalDate.now().minusMonths(5).withDayOfMonth(1),
                                   tom = LocalDate.now()
                                           .minusMonths(5)
                                           .withDayOfMonth(LocalDate.now().minusMonths(5).lengthOfMonth())),
                           Periode(fom = LocalDate.now().minusMonths(3).withDayOfMonth(1),
                                   tom = LocalDate.now()
                                           .minusMonths(3)
                                           .withDayOfMonth(LocalDate.now().minusMonths(3).lengthOfMonth()))))
        } else null
    }

    private fun utledVerge(verge: Boolean): Verge? {
        return if (verge) {
            Verge(gyldigFom = LocalDate.now().minusYears(2).withDayOfMonth(1),
                  gyldigTom = LocalDate.now().plusYears(1).withDayOfMonth(15),
                  vergetype = Vergetype.ADVOKAT,
                  navn = "Jens Pettersen",
                  organisasjonsnummer = "987654321")
        } else null
    }

    fun build(): OpprettTilbakekrevingRequest {
        return request
    }
}
