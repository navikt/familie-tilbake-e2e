package no.nav.familie.tilbake.e2e.familie_tilbake.dto

import java.time.LocalDate

data class BehandlingPåVentDto(val venteårsak: Venteårsak,
                               val tidsfrist: LocalDate)
