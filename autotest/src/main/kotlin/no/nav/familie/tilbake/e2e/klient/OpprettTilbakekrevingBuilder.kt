package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.kontrakter.felles.tilbakekreving.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class OpprettTilbakekrevingBuilder {

    fun requestBuilder(
        eksternFagsakId: String,
        fagsystem: Fagsystem,
        ytelsestype: Ytelsestype,
        varsel: Boolean,
        verge: Boolean
    ):OpprettTilbakekrevingRequest {
        val eksternBehandlingId = UUID.randomUUID().toString()
        var tilbakekrevingsvalg: Tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL
        var varselinfo: Varsel? = null
        if (varsel){
            tilbakekrevingsvalg = Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL
            varselinfo = Varsel(
                varseltekst = "Bla bla bla bla bla bla TEST",
                sumFeilutbetaling = BigDecimal(8124),
                perioder = arrayListOf(
                    Periode(
                        fom = LocalDate.now().minusMonths(5).withDayOfMonth(1),
                        tom = LocalDate.now().minusMonths(5).withDayOfMonth(LocalDate.now().minusMonths(5).lengthOfMonth())),
                    Periode(
                        fom = LocalDate.now().minusMonths(3).withDayOfMonth(1),
                        tom = LocalDate.now().minusMonths(3).withDayOfMonth(LocalDate.now().minusMonths(3).lengthOfMonth())
                    )
                )
            )
        }
        var vergeinfo: Verge? = null
        if (verge){
            vergeinfo = Verge(
                gyldigFom = LocalDate.now().minusYears(2).withDayOfMonth(1),
                gyldigTom = LocalDate.now().plusYears(1).withDayOfMonth(15),
                vergetype = Vergetype.ADVOKAT,
                navn = "Jens Pettersen",
                organisasjonsnummer = "987654321"
            )
        }

        return OpprettTilbakekrevingRequest(
            fagsystem = fagsystem,
            ytelsestype = ytelsestype,
            eksternFagsakId = eksternFagsakId,
            eksternId = eksternBehandlingId,
            personIdent = "12345678901",
            behandlingstype = Behandlingstype.TILBAKEKREVING,
            språkkode = Språkkode.NB,
            enhetId = "0106",
            enhetsnavn = "NAV Fredrikstad",
            revurderingsvedtaksdato = LocalDate.now().minusDays(35),
            faktainfo = Faktainfo(revurderingsårsak = "Nye opplysninger",
                revurderingsresultat = "Endring i ytelsen",
                tilbakekrevingsvalg = tilbakekrevingsvalg,
                konsekvensForYtelser = setOf("Reduksjon av ytelsen", "Feilutbetaling")
            ),
            varsel = varselinfo,
            manueltOpprettet = false,
            verge = vergeinfo
        )
    }
}
