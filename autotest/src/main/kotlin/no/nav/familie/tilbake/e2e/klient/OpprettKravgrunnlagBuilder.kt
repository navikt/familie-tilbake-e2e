package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.tilbake.e2e.domene.KodeFagomraade
import no.nav.familie.tilbake.e2e.domene.KodeStatusKrav
import no.nav.familie.tilbake.e2e.domene.Kravgrunnlag
import javax.validation.constraints.Max

class OpprettKravgrunnlagBuilder {
    fun requestBuilder(
        status: KodeStatusKrav,
        fagområde: KodeFagomraade,
        eksternFagsakId: String,
        eksternBehandlingId: String,
        @Max(29)
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
