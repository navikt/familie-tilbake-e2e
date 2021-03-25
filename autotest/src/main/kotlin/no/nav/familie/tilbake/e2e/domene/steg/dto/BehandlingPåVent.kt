package no.nav.familie.tilbake.e2e.domene.steg.dto

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.familie.tilbake.e2e.domene.Venteårsak
import java.time.LocalDate

data class BehandlingPåVent(
    val venteårsak: Venteårsak,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tidsfrist: LocalDate
)
