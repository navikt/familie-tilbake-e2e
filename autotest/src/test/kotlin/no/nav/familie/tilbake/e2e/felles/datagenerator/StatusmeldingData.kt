package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.dto.KodeStatusKrav
import no.nav.tilbakekreving.status.v1.EndringKravOgVedtakstatus
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus
import no.nav.tilbakekreving.typer.v1.TypeGjelderDto
import java.math.BigInteger

class StatusmeldingData(val kravgrunnlagVedtakId: BigInteger,
                        val status: KodeStatusKrav,
                        val ytelsestype: Ytelsestype,
                        val eksternFagsakId: String,
                        val eksternBehandlingId: String) {

    fun lag(): EndringKravOgVedtakstatus {
        return EndringKravOgVedtakstatus().also { endringKravOgVedtakstatus ->
            endringKravOgVedtakstatus.kravOgVedtakstatus = KravOgVedtakstatus().apply {
                this.vedtakId = kravgrunnlagVedtakId
                this.kodeStatusKrav = status.toString()
                this.kodeFagomraade = utledFagområdeKode(ytelsestype = ytelsestype)
                this.fagsystemId = eksternFagsakId
                this.vedtakGjelderId = "12345678901"
                this.typeGjelderId = TypeGjelderDto.PERSON
                this.referanse = eksternBehandlingId
            }
        }
    }

    private fun utledFagområdeKode(ytelsestype: Ytelsestype): String {
        return when (ytelsestype) {
            Ytelsestype.BARNETRYGD -> "BA"
            Ytelsestype.OVERGANGSSTØNAD -> "EFOG"
            Ytelsestype.BARNETILSYN -> "EFBT"
            Ytelsestype.SKOLEPENGER -> "EFSP"
            Ytelsestype.KONTANTSTØTTE -> "KS"
        }
    }
}
