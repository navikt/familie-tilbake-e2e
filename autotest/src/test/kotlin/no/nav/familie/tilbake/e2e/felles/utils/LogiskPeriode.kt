package no.nav.familie.tilbake.e2e.felles.utils

import no.nav.familie.tilbake.e2e.familie_tilbake.dto.felles.Periode
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.felles.PeriodeDto
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto
import java.math.BigDecimal
import java.time.YearMonth
import java.util.SortedMap

data class LogiskPeriode(val periode: PeriodeDto, val feilutbetaltBeløp: BigDecimal)

object LogiskPeriodeUtil {

    fun utledLogiskPeriodeFraKravgrunnlag(detaljertKravgrunnlag: DetaljertKravgrunnlagDto): List<LogiskPeriode> {
        return utledLogiskPeriode(
            detaljertKravgrunnlag.tilbakekrevingsPeriode.associate { tilbakekrevingsPeriode ->
                Periode(fom = tilbakekrevingsPeriode.periode.fom,
                        tom = tilbakekrevingsPeriode.periode.tom) to
                        tilbakekrevingsPeriode.tilbakekrevingsBelop.sumOf { it.belopTilbakekreves }
            }.toSortedMap())
    }

    fun utledLogiskPeriode(feilutbetalingPrPeriode: SortedMap<Periode, BigDecimal>): List<LogiskPeriode> {
        var førsteMåned: YearMonth? = null
        var sisteMåned: YearMonth? = null
        var logiskPeriodeBeløp = BigDecimal.ZERO
        val resultat = mutableListOf<LogiskPeriode>()
        for ((periode, feilutbetaltBeløp) in feilutbetalingPrPeriode) {
            if (førsteMåned == null && sisteMåned == null) {
                førsteMåned = periode.fom
                sisteMåned = periode.tom
            } else {
                if (harOppholdMellom(sisteMåned!!, periode.fom)) {
                    resultat.add(LogiskPeriode(PeriodeDto(førsteMåned!!, sisteMåned),
                                               feilutbetaltBeløp = logiskPeriodeBeløp))
                    førsteMåned = periode.fom
                    logiskPeriodeBeløp = BigDecimal.ZERO
                }
                sisteMåned = periode.tom
            }
            logiskPeriodeBeløp = logiskPeriodeBeløp.add(feilutbetaltBeløp)
        }
        if (BigDecimal.ZERO.compareTo(logiskPeriodeBeløp) != 0) {
            resultat.add(LogiskPeriode(periode = PeriodeDto(førsteMåned!!, sisteMåned!!),
                                       feilutbetaltBeløp = logiskPeriodeBeløp))
        }
        return resultat.toList()
    }

    private fun harOppholdMellom(måned1: YearMonth, måned2: YearMonth): Boolean {
        require(måned2 > måned1) { "måned2 må være etter måned1" }
        return måned1.plusMonths(1) != måned2
    }
}
