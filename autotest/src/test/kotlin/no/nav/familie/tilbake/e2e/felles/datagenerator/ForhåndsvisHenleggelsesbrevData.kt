package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.ForhåndsvisningHenleggelsesbrevDto
import java.util.UUID

class ForhåndsvisHenleggelsesbrevData(val behandlingId: String) {

    fun lag(): ForhåndsvisningHenleggelsesbrevDto {
        return ForhåndsvisningHenleggelsesbrevDto(behandlingId = UUID.fromString(behandlingId),
                                                  fritekst = "Fritekst fra Autotest")
    }
}
