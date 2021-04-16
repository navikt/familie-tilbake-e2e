package no.nav.familie.tilbake.e2e.domene.steg.dto

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.domene.Periode
import java.time.LocalDate

data class Foreldelse(
        val foreldetPerioder: Set<ForeldelseFeilutbetaltPeriode>,

    )

data class ForeldelseSteg(
    @JsonProperty("@type")
    val type: String = "FORELDELSE",
    val foreldetPerioder: List<ForeldelseFeilutbetaltStegPeriode>
) {
    fun addForeldelseVurdering(beslutning: Foreldelsesvurderingstype){
        this.foreldetPerioder.forEach {
            it.foreldelsesvurderingstype = beslutning
            it.begrunnelse = "Dette er en automatisk begrunnelse av autotest"
            when (beslutning){
                Foreldelsesvurderingstype.FORELDET -> {
                    it.foreldelsesfrist = LocalDate.now().minusMonths(31)
                }
                Foreldelsesvurderingstype.TILLEGGSFRIST -> {
                    it.foreldelsesfrist = LocalDate.now().minusMonths(31)
                    it.oppdagelsesdato = LocalDate.now().minusMonths(15)
                }
            }
        }
    }
}

data class ForeldelseFeilutbetaltPeriode(
    val periode: Periode,
    val feilutbetaltBeløp: Int,
    var begrunnelse: String?,
    var foreldelsesvurderingstype: Foreldelsesvurderingstype?,
    var foreldelsesfrist: LocalDate? = null,
    var oppdagelsesdato: LocalDate? = null
)

data class ForeldelseFeilutbetaltStegPeriode(
    val periode: Periode,
    var begrunnelse: String = "Default begrunnelse fra Autotest",
    var foreldelsesvurderingstype: Foreldelsesvurderingstype = Foreldelsesvurderingstype.IKKE_FORELDET,
    var foreldelsesfrist: LocalDate? = null,
    var oppdagelsesdato: LocalDate? = null
)

enum class Foreldelsesvurderingstype {
    IKKE_VURDERT,
    FORELDET,
    IKKE_FORELDET,
    TILLEGGSFRIST
}
