package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

import java.time.LocalDate

data class BehandlingPåVentDto(val venteårsak: Venteårsak,
                               val tidsfrist: LocalDate)
