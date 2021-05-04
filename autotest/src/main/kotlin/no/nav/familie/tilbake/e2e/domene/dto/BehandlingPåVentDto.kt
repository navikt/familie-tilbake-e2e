package no.nav.familie.tilbake.e2e.domene.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class BehandlingPåVent(val venteårsak: Venteårsak,
                            @JsonFormat(pattern = "yyyy-MM-dd")
                            val tidsfrist: LocalDate)
