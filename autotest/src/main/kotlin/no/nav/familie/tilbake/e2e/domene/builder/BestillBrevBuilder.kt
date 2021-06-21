package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.BestillBrevDto
import no.nav.familie.tilbake.e2e.domene.dto.Dokumentmalstype
import java.util.UUID

class BestillBrevBuilder(behandlingId: String,
                         dokumentmalstype: Dokumentmalstype) {

    private val request = BestillBrevDto(behandlingId = UUID.fromString(behandlingId),
                                         brevmalkode = dokumentmalstype,
                                         fritekst = "Fritekst fra Autotest")

    fun build(): BestillBrevDto {
        return request
    }
}
