package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.klienter.dto.felles.PeriodeDto
import java.time.LocalDate

/**
 * DTO-er relatert til behandle steg
 */
data class FaktaDto(
    @JsonProperty("@type")
    val type: String = "FAKTA",
    val feilutbetaltePerioder: List<VurdertFaktaFeilutbetaltPeriodeDto>,
    var begrunnelse: String? = null
)

data class VurdertFaktaFeilutbetaltPeriodeDto(
    val periode: PeriodeDto,
    var hendelsestype: Hendelsestype? = null,
    var hendelsesundertype: Hendelsesundertype? = null
)

/**
 * DTO-er relatert til hentFakta
 */
data class HentFaktaDto(
    val varsletBeløp: Int,
    val totalFeilutbetaltPeriode: PeriodeDto,
    val feilutbetaltePerioder: Set<FaktaFeilutbetaltPeriodeDto>,
    val totaltFeilutbetaltBeløp: Int,
    val revurderingsvedtaksdato: LocalDate,
    val begrunnelse: String,
    val faktainfo: FaktainfoDto
)

data class FaktaFeilutbetaltPeriodeDto(
    val periode: PeriodeDto,
    val feilutbetaltBeløp: Int?,
    var hendelsestype: Hendelsestype? = null,
    var hendelsesundertype: Hendelsesundertype? = null
)

data class FaktainfoDto(
    val revurderingsårsak: String,
    val revurderingsresultat: String,
    val tilbakekrevingsvalg: Tilbakekrevingsvalg,
    val konsekvensForYtelser: Set<String>
)

/**
 * Felleskomponenter
 */

enum class Hendelsestype {
    ANNET,
    BOR_MED_SØKER,
    BOSATT_I_RIKET,
    LOVLIG_OPPHOLD,
    DØDSFALL,
    DELT_BOSTED,
    BARNS_ALDER
}

enum class Hendelsesundertype {

    ANNET_FRITEKST,
    BOR_IKKE_MED_BARN,
    BARN_FLYTTET_FRA_NORGE,
    BRUKER_FLYTTET_FRA_NORGE,
    BARN_BOR_IKKE_I_NORGE,
    BRUKER_BOR_IKKE_I_NORGE,
    UTEN_OPPHOLDSTILLATELSE,
    BARN_DØD,
    BRUKER_DØD,
    ENIGHET_OM_OPPHØR_DELT_BOSTED,
    UENIGHET_OM_OPPHØR_DELT_BOSTED,
    BARN_OVER_18_ÅR,
    BARN_OVER_6_ÅR
}

enum class Tilbakekrevingsvalg {
    OPPRETT_TILBAKEKREVING_MED_VARSEL,
    OPPRETT_TILBAKEKREVING_UTEN_VARSEL,
    IGNORER_TILBAKEKREVING
}
