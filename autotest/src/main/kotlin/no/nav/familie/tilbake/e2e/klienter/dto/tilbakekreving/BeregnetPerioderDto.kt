package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

import no.nav.familie.tilbake.e2e.klienter.dto.felles.PeriodeDto
import java.math.BigDecimal

/**
 * DTO-er relatert til beregning
 */
data class BeregnetPerioderDto(val beregnetPerioder: List<BeregnetPeriodeDto>)

data class BeregnetPeriodeDto(val periode: PeriodeDto, val feilutbetaltBeløp: BigDecimal)

/**
 * DTO-er relatert til beregningsresultat
 */
data class BeregningsresultatDto(val beregningsresultatsperioder: List<BeregningsresultatsperiodeDto>,
                                 val vedtaksresultat: Vedtaksresultat
)

data class BeregningsresultatsperiodeDto(val periode: PeriodeDto,
                                         val vurdering: String? = null,
                                         val feilutbetaltBeløp: BigDecimal,
                                         val andelAvBeløp: BigDecimal? = null,
                                         val renteprosent: BigDecimal? = null,
                                         val tilbakekrevingsbeløp: BigDecimal? = null,
                                         val tilbakekrevesBeløpEtterSkatt: BigDecimal? = null)

enum class Vedtaksresultat(val navn: String) {
    FULL_TILBAKEBETALING("Tilbakebetaling"),
    DELVIS_TILBAKEBETALING("Delvis tilbakebetaling"),
    INGEN_TILBAKEBETALING("Ingen tilbakebetaling");
}
