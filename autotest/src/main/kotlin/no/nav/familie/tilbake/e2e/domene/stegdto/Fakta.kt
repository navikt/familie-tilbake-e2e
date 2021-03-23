package no.nav.familie.tilbake.e2e.domene.stegdto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.domene.Periode
import java.time.LocalDate

data class Fakta(
    val varsletBeløp: Int,
    val totalFeilutbetaltPeriode: Periode,
    val feilutbetaltePerioder: Set<FeilutbetaltPeriode>,
    val totaltFeilutbetaltBeløp: Int,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val revurderingsvedtaksdato: LocalDate,
    val begrunnelse: String,
    val faktainfo: Faktainfo
)

data class FaktaSteg(
    @JsonProperty("@type")
    val type: String = "FAKTA",
    val feilutbetaltePerioder: List<FeilutbetaltStegPeriode>,
    var begrunnelse: String? = null,
) {

    fun addFaktaVurdering(hendelse: Hendelsestype, underhendelse: Hendelsesundertype) {
        this.feilutbetaltePerioder.forEach {
            it.hendelsestype = hendelse
            it.hendelsesundertype = underhendelse
        }
        this.begrunnelse = "Generisk begrunnelse lagt til av autotest"
    }

}

data class FeilutbetaltPeriode(
    val periode: Periode,
    val feilutbetaltBeløp: Int?,
    var hendelsestype: Hendelsestype? = null,
    var hendelsesundertype: Hendelsesundertype? = null
)

data class FeilutbetaltStegPeriode(
    val periode: Periode,
    var hendelsestype: Hendelsestype? = null,
    var hendelsesundertype: Hendelsesundertype? = null
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
