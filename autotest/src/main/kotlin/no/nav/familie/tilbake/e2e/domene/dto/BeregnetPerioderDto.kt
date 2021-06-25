package no.nav.familie.tilbake.e2e.domene.dto

import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto
import java.math.BigDecimal

data class BeregnetPerioderDto(val beregnetPerioder: List<BeregnetPeriodeDto>)

data class BeregnetPeriodeDto(val periode: PeriodeDto, val feilutbetaltBeløp: BigDecimal)
