package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.ForhåndsvisningHenleggelsesbrevDto
import java.util.UUID

class ForhåndsvisHenleggelsesbrevBuilder(behandlingId: String) {

    private val request = ForhåndsvisningHenleggelsesbrevDto(behandlingId = UUID.fromString(behandlingId),
                                                             fritekst = "Fritekst fra Autotest")

    fun build(): ForhåndsvisningHenleggelsesbrevDto {
        return request
    }
}
