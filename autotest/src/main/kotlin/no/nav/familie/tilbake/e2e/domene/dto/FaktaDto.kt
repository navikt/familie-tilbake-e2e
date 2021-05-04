package no.nav.familie.tilbake.e2e.domene.dto

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto
import java.time.LocalDate

/**
 * DTO-er relatert til behandle steg
 */
data class FaktaDto(@JsonProperty("@type")
                    val type: String = "FAKTA",
                    val feilutbetaltePerioder: List<VurdertFaktaFeilutbetaltPeriodeDto>,
                    var begrunnelse: String? = null)

data class VurdertFaktaFeilutbetaltPeriodeDto(val periode: PeriodeDto,
                                              var hendelsestype: Hendelsestype? = null,
                                              var hendelsesundertype: Hendelsesundertype? = null)

/**
 * DTO-er relatert til hentFakta
 */
data class HentFaktaDto(val varsletBeløp: Int,
                        val totalFeilutbetaltPeriode: PeriodeDto,
                        val feilutbetaltePerioder: Set<FaktaFeilutbetaltPeriodeDto>,
                        val totaltFeilutbetaltBeløp: Int,
                        val revurderingsvedtaksdato: LocalDate,
                        val begrunnelse: String,
                        val faktainfo: FaktainfoDto)

data class FaktaFeilutbetaltPeriodeDto(val periode: PeriodeDto,
                                       val feilutbetaltBeløp: Int?,
                                       var hendelsestype: Hendelsestype? = null,
                                       var hendelsesundertype: Hendelsesundertype? = null)

data class FaktainfoDto(val revurderingsårsak: String,
                        val revurderingsresultat: String,
                        val tilbakekrevingsvalg: Tilbakekrevingsvalg,
                        val konsekvensForYtelser: Set<String>)

/**
 * Felleskomponenter
 */

enum class Hendelsestype {

    BA_ANNET,
    EF_ANNET,
    KS_ANNET
}

enum class Hendelsesundertype {
    ANNET_FRITEKST
}

enum class Tilbakekrevingsvalg {
    OPPRETT_TILBAKEKREVING_MED_VARSEL,
    OPPRETT_TILBAKEKREVING_UTEN_VARSEL,
    IGNORER_TILBAKEKREVING
}
