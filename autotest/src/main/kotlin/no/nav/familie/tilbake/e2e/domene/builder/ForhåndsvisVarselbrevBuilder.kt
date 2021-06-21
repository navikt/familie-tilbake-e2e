package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.FeilutbetaltePerioderDto
import no.nav.familie.kontrakter.felles.tilbakekreving.ForhåndsvisVarselbrevRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Verge
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto
import java.time.LocalDate

class ForhåndsvisVarselbrevBuilder(behandlendeEnhetId: String = "0106",
                                   behandlendeEnhetsNavn: String = "NAV Fredrikstad",
                                   eksternFagsakId: String,
                                   fagsystem: Fagsystem,
                                   perioder: List<PeriodeDto>,
                                   ident: String = "31079221655",
                                   saksbehandlerIdent: String = "VL",
                                   språkkode: Språkkode = Språkkode.NB,
                                   vedtaksdato: LocalDate = LocalDate.now(),
                                   verge: Verge? = null,
                                   ytelsestype: Ytelsestype) {

    private val request =
        ForhåndsvisVarselbrevRequest(behandlendeEnhetId = behandlendeEnhetId,
                                     behandlendeEnhetsNavn = behandlendeEnhetsNavn,
                                     eksternFagsakId = eksternFagsakId,
                                     fagsystem = fagsystem,
                                     feilutbetaltePerioderDto =
                                     FeilutbetaltePerioderDto(perioder = perioder.map { Periode(fom = it.fom, tom = it.tom) },
                                                              sumFeilutbetaling = 4000L),
                                     ident = ident,
                                     saksbehandlerIdent = saksbehandlerIdent,
                                     språkkode = språkkode,
                                     varseltekst = "Varseltekst fra Autotest",
                                     vedtaksdato = vedtaksdato,
                                     verge = verge,
                                     ytelsestype = ytelsestype)

    fun build(): ForhåndsvisVarselbrevRequest {
        return request
    }
}
