package no.nav.familie.tilbake.e2e.domene.dto

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.domene.dto.felles.Periode
import java.time.LocalDate

/**
 * DTO-er relatert til behandle steg
 */
data class BehandleFaktaDto(@JsonProperty("@type")
                            val type: String = "FAKTA",
                            val feilutbetaltePerioder: List<VurdertFaktaFeilutbetaltPeriode>,
                            var begrunnelse: String? = null)

data class VurdertFaktaFeilutbetaltPeriode(val periode: Periode,
                                           var hendelsestype: Hendelsestype? = null,
                                           var hendelsesundertype: Hendelsesundertype? = null)

/**
 * DTO-er relatert til hentFakta
 */
data class HentFaktaDto(val varsletBeløp: Int,
                        val totalFeilutbetaltPeriode: Periode,
                        val feilutbetaltePerioder: Set<FaktaFeilutbetaltPeriode>,
                        val totaltFeilutbetaltBeløp: Int,
                        val revurderingsvedtaksdato: LocalDate,
                        val begrunnelse: String,
                        val faktainfo: Faktainfo)

data class FaktaFeilutbetaltPeriode(val periode: Periode,
                                    val feilutbetaltBeløp: Int?,
                                    var hendelsestype: Hendelsestype? = null,
                                    var hendelsesundertype: Hendelsesundertype? = null)

data class Faktainfo(val revurderingsårsak: String,
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
