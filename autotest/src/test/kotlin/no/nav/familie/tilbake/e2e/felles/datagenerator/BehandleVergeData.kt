package no.nav.familie.tilbake.e2e.felles.datagenerator

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.VergeDto

class BehandleVergeData(
    @JsonProperty("@type")
    val type: String = "VERGE",
    val verge: VergeDto
)
