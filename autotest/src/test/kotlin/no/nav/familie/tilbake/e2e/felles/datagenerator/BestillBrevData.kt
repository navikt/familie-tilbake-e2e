package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.BestillBrevDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Dokumentmalstype
import java.util.UUID

class BestillBrevData(
    val behandlingId: String,
    val dokumentmalstype: Dokumentmalstype
) {

    fun lag(): BestillBrevDto {
        return BestillBrevDto(
            behandlingId = UUID.fromString(behandlingId),
            brevmalkode = dokumentmalstype,
            fritekst = "Fritekst fra Autotest"
        )
    }
}
