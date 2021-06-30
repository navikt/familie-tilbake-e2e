package no.nav.familie.tilbake.e2e.familie_tilbake.dto

import java.util.UUID

data class ForhåndsvisningVedtaksbrevPdfDto(var behandlingId: UUID,
                                            var oppsummeringstekst: String? = null,
                                            var perioderMedTekst: List<PeriodeMedTekstDto>)
