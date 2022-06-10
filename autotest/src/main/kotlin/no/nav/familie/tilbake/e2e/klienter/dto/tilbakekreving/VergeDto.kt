package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

import no.nav.familie.kontrakter.felles.tilbakekreving.Vergetype

data class VergeDto(
    val ident: String? = null,
    val orgNr: String? = null,
    val type: Vergetype,
    val navn: String,
    val begrunnelse: String?
)
