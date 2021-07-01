package no.nav.familie.tilbake.e2e.klienter.dto.felles

import java.time.LocalDate
import java.time.YearMonth

data class Periode(val fom: YearMonth, val tom: YearMonth) : Comparable<Periode> {

    constructor(fom: LocalDate, tom: LocalDate) : this(YearMonth.from(fom), YearMonth.from(tom))

    init {
        require(tom >= fom) { "Til-og-med-måned før fra-og-med-måned: $fom > $tom" }
    }

    companion object {

        val COMPARATOR: Comparator<Periode> = Comparator.comparing(Periode::fom).thenComparing(Periode::tom)
    }

    override fun compareTo(other: Periode): Int {
        return COMPARATOR.compare(this, other)
    }
}
