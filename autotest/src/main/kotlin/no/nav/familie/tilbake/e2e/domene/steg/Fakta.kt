package no.nav.familie.tilbake.e2e.domene.steg

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.domene.Periode
import java.time.LocalDate

data class Fakta(
    val varsletBeløp: Int,
    val totalFeilutbetaltPeriode: Periode,
    val feilutbetaltePerioder: Set<FeilutbetaltePeriode>,
    val totaltFeilutbetaltBeløp: Int,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val revurderingsvedtaksdato: LocalDate,
    val begrunnelse: String,
    val faktainfo: Faktainfo
)

data class FaktaSteg(
    @JsonProperty("@type")
    val type: String = "FAKTA",
    val feilutbetaltePerioder: Set<FeilutbetaltePeriode>? = emptySet(),
    val begrunnelse: String? = null
) {

    fun addFaktaVurdering() {
        //TODO
    }
}

data class FeilutbetaltePeriode(
    val periode: Periode,
    val feilutbetaltBeløp: Int?,
    val hendelsestype: Hendelsestype,
    val hendelsesundertype: Hendelsesundertype
)

data class Faktainfo(
    val revurderingsårsak: String,
    val revurderingsresultat: String,
    val tilbakekrevingsvalg: Tilbakekrevingsvalg,
    val konsekvensForYtelser: Set<String>
)

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
