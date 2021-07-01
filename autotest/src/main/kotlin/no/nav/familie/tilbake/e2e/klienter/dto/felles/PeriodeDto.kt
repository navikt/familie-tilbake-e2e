package no.nav.familie.tilbake.e2e.klienter.dto.felles

import java.time.LocalDate
import java.time.YearMonth

data class PeriodeDto(val fom: LocalDate,
                      val tom: LocalDate) {

    constructor(fom: YearMonth, tom: YearMonth) : this(fom.atDay(1), tom.atEndOfMonth())

}
