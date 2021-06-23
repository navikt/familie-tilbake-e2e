package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.domene.dto.ForhåndsvisningVedtaksbrevPdfDto
import no.nav.familie.tilbake.e2e.domene.dto.PeriodeMedTekstDto
import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto
import java.util.UUID

class ForhåndsvisVedtaksbrevBuilder(val behandlingId: String,
                                    val perioder: List<PeriodeDto>) {

    private val BEGRUNNELSE = "Automatisk begrunnelse fra Autotest"

    fun lag(): ForhåndsvisningVedtaksbrevPdfDto {
        return ForhåndsvisningVedtaksbrevPdfDto(behandlingId = UUID.fromString(behandlingId),
                                                perioderMedTekst = perioder.map {
                                                    PeriodeMedTekstDto(periode = it,
                                                                       faktaAvsnitt = BEGRUNNELSE,
                                                                       foreldelseAvsnitt = BEGRUNNELSE,
                                                                       vilkårAvsnitt = BEGRUNNELSE,
                                                                       særligeGrunnerAvsnitt = BEGRUNNELSE,
                                                                       særligeGrunnerAnnetAvsnitt = BEGRUNNELSE)
                                                })
    }
}
