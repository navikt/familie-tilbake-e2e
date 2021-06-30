package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.klienter.dto.felles.PeriodeDto
import java.time.LocalDate
import kotlin.collections.List

/**
 * DTO-er relatert til behandle steg
 */
data class ForeslåVedtakDto(@JsonProperty("@type")
                            val type: String = "FORESLÅ_VEDTAK",
                            val fritekstavsnitt: FritekstavsnittDto
)

data class FritekstavsnittDto(val oppsummeringstekst: String? = null,
                              val perioderMedTekst: List<PeriodeMedTekstDto>)

data class PeriodeMedTekstDto(val periode: PeriodeDto,
                              val faktaAvsnitt: String? = null,
                              val foreldelseAvsnitt: String? = null,
                              val vilkårAvsnitt: String? = null,
                              val særligeGrunnerAvsnitt: String? = null,
                              val særligeGrunnerAnnetAvsnitt: String? = null)

/**
 * DTO-er relatert til hentVedtaksbrevtekst
 */
data class VedtaksbrevtekstDto(val avsnitt: List<AvsnittDto>)

data class AvsnittDto(val overskrift: String? = null,
                      val underavsnittsliste: List<UnderavsnittDto> = listOf(),
                      val avsnittstype: Avsnittstype? = null,
                      val fom: LocalDate? = null,
                      val tom: LocalDate? = null)

data class UnderavsnittDto(val overskrift: String? = null,
                           val brødtekst: String? = null,
                           val fritekst: String? = null,
                           val fritekstTillatt: Boolean = false,
                           val fritekstPåkrevet: Boolean = false,
                           val underavsnittstype: Underavsnittstype? = null)

enum class Avsnittstype {
    OPPSUMMERING,
    PERIODE,
    TILLEGGSINFORMASJON
}

enum class Underavsnittstype {
    FAKTA,
    FORELDELSE,
    VILKÅR,
    SÆRLIGEGRUNNER,
    SÆRLIGEGRUNNER_ANNET
}
