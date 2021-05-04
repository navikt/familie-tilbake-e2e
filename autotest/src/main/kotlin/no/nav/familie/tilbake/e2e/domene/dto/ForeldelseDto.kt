package no.nav.familie.tilbake.e2e.domene.dto

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.domene.dto.felles.Periode
import java.time.LocalDate

/**
 * DTO-er relatert til behandle steg
 */
data class ForeldelseStegDto(@JsonProperty("@type")
                             val type: String = "FORELDELSE",
                             val foreldetPerioder: List<VurdertForeldelsesperiode>)

data class VurdertForeldelsesperiode(val periode: Periode,
                                     var begrunnelse: String = "Default begrunnelse fra Autotest",
                                     var foreldelsesvurderingstype: Foreldelsesvurderingstype = Foreldelsesvurderingstype.IKKE_FORELDET,
                                     var foreldelsesfrist: LocalDate? = null,
                                     var oppdagelsesdato: LocalDate? = null)

/**
 * DTO-er relatert til hentForeldelse
 */
data class HentForeldelseDto(val foreldetPerioder: Set<ForeldelsePeriode>)

data class ForeldelsePeriode(val periode: Periode,
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
