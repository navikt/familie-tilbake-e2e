package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.klienter.dto.felles.PeriodeDto
import java.time.LocalDate

/**
 * DTO-er relatert til behandle steg
 */
data class ForeldelseDto(@JsonProperty("@type")
                         val type: String = "FORELDELSE",
                         val foreldetPerioder: List<VurdertForeldelsesperiodeDto>)

data class VurdertForeldelsesperiodeDto(val periode: PeriodeDto,
                                        var begrunnelse: String = "Default begrunnelse fra Autotest",
                                        var foreldelsesvurderingstype: Foreldelsesvurderingstype = Foreldelsesvurderingstype.IKKE_FORELDET,
                                        var foreldelsesfrist: LocalDate? = null,
                                        var oppdagelsesdato: LocalDate? = null)

/**
 * DTO-er relatert til hentForeldelse
 */
data class HentForeldelseDto(val foreldetPerioder: Set<ForeldelsePeriodeDto>)

data class ForeldelsePeriodeDto(val periode: PeriodeDto,
                                val feilutbetaltBeløp: Int,
                                var begrunnelse: String?,
                                var foreldelsesvurderingstype: Foreldelsesvurderingstype?,
                                var foreldelsesfrist: LocalDate? = null,
                                var oppdagelsesdato: LocalDate? = null)

/**
 * Felleskomponenter
 */
enum class Foreldelsesvurderingstype {

    IKKE_VURDERT,
    FORELDET,
    IKKE_FORELDET,
    TILLEGGSFRIST
}
