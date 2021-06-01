package no.nav.familie.tilbake.e2e.felles.utils

import no.nav.familie.tilbake.e2e.domene.dto.felles.Periode
import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto
import java.time.YearMonth

object LogiskPeriode {

    fun utledLogiskPeriode(perioder: List<Periode>): List<PeriodeDto>{
        var førsteMåned: YearMonth? = null
        var sisteMåned: YearMonth? = null
        val resultat = mutableListOf<PeriodeDto>()
        for (periode in perioder) {
            if (førsteMåned == null && sisteMåned == null) {
                førsteMåned = periode.fom
                sisteMåned = periode.tom
            } else {
                if (harOppholdMellom(sisteMåned!!, periode.fom)) {
                    resultat.add(PeriodeDto(førsteMåned!!, sisteMåned))
                    førsteMåned = periode.fom
                }
                sisteMåned = periode.tom
            }
        }
        resultat.add(PeriodeDto(førsteMåned!!, sisteMåned!!))
        return resultat.toList()
    }

    private fun harOppholdMellom(måned1: YearMonth, måned2: YearMonth): Boolean {
        require(måned2 > måned1) { "dag2 må være etter dag1" }
        return måned1.plusMonths(1) != måned2
    }
}
