package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.Kravgrunnlag
import javax.validation.constraints.Max

class OpprettKravgrunnlagBuilder {

    fun opprettKravgrunnlag(
        status: KodeStatusKrav,
        fagområde: Fagsystem,
        ytelsestype: Ytelsestype,
        eksternFagsakId: String,
        eksternBehandlingId: String,
        @Max(29)
        kravgrunnlagId: Int?,
        vedtakId: Int?,
        antallPerioder: Int,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean
    ): Kravgrunnlag? {

//        var request: Kravgrunnlag(
//        TODO
//        )
        return null
    }
}
