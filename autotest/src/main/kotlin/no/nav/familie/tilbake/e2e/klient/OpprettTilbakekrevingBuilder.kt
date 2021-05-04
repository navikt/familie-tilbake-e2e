package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingstype
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Faktainfo
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Varsel
import no.nav.familie.kontrakter.felles.tilbakekreving.Verge
import no.nav.familie.kontrakter.felles.tilbakekreving.Vergetype
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

class OpprettTilbakekrevingBuilder {

    fun opprettTilbakekrevingRequest(eksternFagsakId: String,
                                     eksternBehandlingId: String? = null,
                                     fagsystem: Fagsystem,
                                     ytelsestype: Ytelsestype,
                                     varsel: Boolean,
                                     verge: Boolean
    ): OpprettTilbakekrevingRequest {
        val finalEksternBehandlingId = eksternBehandlingId ?: Random.nextInt(1000000, 9999999).toString()

        var tilbakekrevingsvalg: Tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL
        var varselinfo: Varsel? = null
        if (varsel) {
            tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL
            varselinfo = Varsel(varseltekst = "Automatisk varseltekst fra Autotest",
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
        }
        var vergeinfo: Verge? = null
        if (verge) {
            vergeinfo = Verge(gyldigFom = LocalDate.now().minusYears(2).withDayOfMonth(1),
                              gyldigTom = LocalDate.now().plusYears(1).withDayOfMonth(15),
                              vergetype = Vergetype.ADVOKAT,
                              navn = "Jens Pettersen",
                              organisasjonsnummer = "987654321")
        }

        return OpprettTilbakekrevingRequest(fagsystem = fagsystem,
                                            ytelsestype = ytelsestype,
                                            eksternFagsakId = eksternFagsakId,
                                            eksternId = finalEksternBehandlingId,
                                            personIdent = "12345678901",
                                            behandlingstype = Behandlingstype.TILBAKEKREVING,
                                            språkkode = Språkkode.NB,
                                            enhetId = "0106",
                                            enhetsnavn = "NAV Fredrikstad",
                                            revurderingsvedtaksdato = LocalDate.now().minusDays(35),
                                            faktainfo = Faktainfo(revurderingsårsak = "Nye opplysninger",
                                                                  revurderingsresultat = "Endring i ytelsen",
                                                                  tilbakekrevingsvalg = tilbakekrevingsvalg,
                                                                  konsekvensForYtelser = setOf("Reduksjon av ytelsen",
                                                                                               "Feilutbetaling")),
                                            varsel = varselinfo,
                                            manueltOpprettet = false,
                                            verge = vergeinfo)
    }
}
