package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

import java.util.UUID

data class ForhåndsvisningHenleggelsesbrevDto(
    val behandlingId: UUID,
    val fritekst: String?
)
