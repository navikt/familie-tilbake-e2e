package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.FeilutbetaltePerioderDto
import no.nav.familie.kontrakter.felles.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Verge
import no.nav.familie.kontrakter.felles.tilbakekreving.Vergetype
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.klienter.dto.felles.PeriodeDto
import java.time.LocalDate

class ForhåndsvisVarselbrevData(
    val behandlendeEnhetId: String = "0106",
    val behandlendeEnhetsNavn: String = "NAV Fredrikstad",
    val eksternFagsakId: String,
    val fagsystem: Fagsystem,
    val perioder: List<PeriodeDto>,
    val ident: String = "31079221655",
    val saksbehandlerIdent: String = "VL",
    val språkkode: Språkkode = Språkkode.NB,
    val vedtaksdato: LocalDate = LocalDate.now(),
    val verge: Boolean,
    val ytelsestype: Ytelsestype,
    val sumFeilutbetaling: Long
) {

    fun lag(): ForhåndsvisVarselbrevRequest {
        return ForhåndsvisVarselbrevRequest(
            behandlendeEnhetId = behandlendeEnhetId,
            behandlendeEnhetsNavn = behandlendeEnhetsNavn,
            eksternFagsakId = eksternFagsakId,
            fagsystem = fagsystem,
            feilutbetaltePerioderDto = FeilutbetaltePerioderDto(
                sumFeilutbetaling = sumFeilutbetaling,
                perioder = perioder.map {
                    Periode(
                        fom = it.fom,
                        tom = it.tom
                    )
                }
            ),
            ident = ident,
            saksbehandlerIdent = saksbehandlerIdent,
            språkkode = språkkode,
            varseltekst = "Varseltekst fra Autotest",
            vedtaksdato = vedtaksdato,
            verge = utledVerge(verge),
            ytelsestype = ytelsestype
        )
    }

    private fun utledVerge(harVerge: Boolean): Verge? {
        return if (harVerge) {
            Verge(vergetype = Vergetype.ADVOKAT, navn = "Jens Pettersen", organisasjonsnummer = "987654321")
        } else {
            null
        }
    }
}
