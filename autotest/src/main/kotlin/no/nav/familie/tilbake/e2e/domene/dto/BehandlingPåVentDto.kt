package no.nav.familie.tilbake.e2e.domene.dto

import java.time.LocalDate

data class BehandlingPåVentDto(val venteårsak: Venteårsak,
                               val tidsfrist: LocalDate)
