package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.ForhåndsvisningVedtaksbrevPdfDto
import no.nav.familie.tilbake.e2e.domene.dto.PeriodeMedTekstDto
import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto
import java.util.UUID

class ForhåndsvisVedtaksbrevBuilder(behandlingId: String,
                                    perioder: List<PeriodeDto>) {

    private val BEGRUNNELSE = "Automatisk begrunnelse fra Autotest"
    private val request = ForhåndsvisningVedtaksbrevPdfDto(behandlingId = UUID.fromString(behandlingId),
                                                           perioderMedTekst = perioder.map {
                                                               PeriodeMedTekstDto(periode = it,
                                                                                  faktaAvsnitt = BEGRUNNELSE,
                                                                                  foreldelseAvsnitt = BEGRUNNELSE,
                                                                                  vilkårAvsnitt = BEGRUNNELSE,
                                                                                  særligeGrunnerAvsnitt = BEGRUNNELSE,
                                                                                  særligeGrunnerAnnetAvsnitt = BEGRUNNELSE)
                                                           })

    fun build(): ForhåndsvisningVedtaksbrevPdfDto {
        return request
    }
}
