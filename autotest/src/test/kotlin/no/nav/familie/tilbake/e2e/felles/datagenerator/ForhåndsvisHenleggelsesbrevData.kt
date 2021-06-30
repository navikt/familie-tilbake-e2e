package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.familie_tilbake.dto.ForhåndsvisningHenleggelsesbrevDto
import java.util.UUID

class ForhåndsvisHenleggelsesbrevData(val behandlingId: String) {

    fun lag(): ForhåndsvisningHenleggelsesbrevDto {
        return ForhåndsvisningHenleggelsesbrevDto(behandlingId = UUID.fromString(behandlingId),
                                                  fritekst = "Fritekst fra Autotest")
    }
}
