package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

import java.util.UUID

data class BestillBrevDto(
    val behandlingId: UUID,
    val brevmalkode: Dokumentmalstype,
    val fritekst: String
)

enum class Dokumentmalstype {
    INNHENT_DOKUMENTASJON,
    FRITEKSTBREV,
    VARSEL,
    KORRIGERT_VARSEL;
}
