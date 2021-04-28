package no.nav.familie.tilbake.e2e.domene.steg.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.domene.Periode
import java.math.BigDecimal

data class Vilkårsvurdering(
    val perioder: Set<VurdertVilkårsvurderingsperiodeDto>,
    val rettsgebyr: Long
)

data class VilkårsvurderingSteg(
    @JsonProperty("@type")
    val type: String = "VILKÅRSVURDERING",
    val vilkårsvurderingsperioder: List<VilkårsvurderingStegPeriode>
) {
    fun addVilkårsvurdering(vilkårvurderingsresultat: Vilkårsvurderingsresultat,
                            aktsomhet: Aktsomhet? = null,
                            erBeløpIBehold: Boolean = true,
                            redusertBeløpSomTilbakekreves: BigDecimal? = null,
                            andelSomTilbakekreves: BigDecimal? = null,
                            særligeGrunner: List<SærligGrunn> = listOf(SærligGrunn.GRAD_AV_UAKTSOMHET),
                            skalTilbakekreveSmåbeløp: Boolean? = null
    ) {
        this.vilkårsvurderingsperioder.forEach {
            it.vilkårsvurderingsresultat = vilkårvurderingsresultat
            it.begrunnelse = "Vilkårsbegrunnelse fra autotest"
            when (vilkårvurderingsresultat) {
                Vilkårsvurderingsresultat.GOD_TRO -> {
                    it.aktsomhetDto = null
                    it.godTroDto?.beløpErIBehold = erBeløpIBehold
                    if (!erBeløpIBehold) {
                        it.godTroDto?.beløpTilbakekreves = BigDecimal.ZERO
                    }
                    if (erBeløpIBehold && redusertBeløpSomTilbakekreves != null) {
                        it.godTroDto?.beløpTilbakekreves = redusertBeløpSomTilbakekreves
                    }
                }
                else -> {
                    it.godTroDto = null
                    it.aktsomhetDto?.aktsomhet = aktsomhet!!
                    when (aktsomhet) {
                        Aktsomhet.FORSETT -> {
                            it.aktsomhetDto?.beløpTilbakekreves = null
                        }
                        Aktsomhet.GROV_UAKTSOMHET,
                        Aktsomhet.SIMPEL_UAKTSOMHET -> {
                            it.aktsomhetDto?.særligeGrunnerBegrunnelse = "Særlige grunner begrunnelse fra autotest"
                            it.aktsomhetDto?.særligeGrunner = særligeGrunner.map { særligGrunn ->
                                SærligGrunnDto(
                                    særligGrunn = særligGrunn,
                                    begrunnelse = if (særligGrunn == SærligGrunn.ANNET) "Særlig grunn annet begrunnelse fra autotest" else null
                                )
                            }
                            if (andelSomTilbakekreves != null) {
                                it.aktsomhetDto?.beløpTilbakekreves = null
                                it.aktsomhetDto?.andelTilbakekreves = andelSomTilbakekreves
                                it.aktsomhetDto?.særligeGrunnerTilReduksjon = true
                            }
                            if (skalTilbakekreveSmåbeløp == false && aktsomhet == Aktsomhet.SIMPEL_UAKTSOMHET) {
                                it.aktsomhetDto?.tilbakekrevSmåbeløp = skalTilbakekreveSmåbeløp
                                it.aktsomhetDto?.beløpTilbakekreves = null
                            }
                        }
                    }
                }
            }
        }
    }
}

data class VurdertVilkårsvurderingsperiodeDto(val periode: Periode,
                                              val feilutbetaltBeløp: BigDecimal,
                                              val hendelsestype: Hendelsestype,
                                              val reduserteBeløper: List<RedusertBeløpDto>?,
                                              val aktiviteter: List<AktivitetDto>,
                                              val vilkårsvurderingsresultatInfo: VurdertVilkårsvurderingsresultatDto? = null,
                                              val begrunnelse: String? = null,
                                              val foreldet: Boolean)

data class VurdertVilkårsvurderingsresultatDto(val vilkårsvurderingsresultat: Vilkårsvurderingsresultat? = null,
                                               val godTro: VurdertGodTroDto? = null,
                                               val aktsomhet: VurdertAktsomhetDto? = null)

data class VurdertGodTroDto(val beløpErIBehold: Boolean,
                            val beløpTilbakekreves: BigDecimal? = null,
                            val begrunnelse: String)

data class VurdertAktsomhetDto(val aktsomhet: Aktsomhet,
                               val ileggRenter: Boolean? = null,
                               val andelTilbakekreves: BigDecimal? = null,
                               val beløpTilbakekreves: BigDecimal? = null,
                               val begrunnelse: String,
                               val særligeGrunner: List<VurdertSærligGrunnDto>? = null,
                               val særligeGrunnerTilReduksjon: Boolean = false,
                               val tilbakekrevSmåbeløp: Boolean = true,
                               val særligeGrunnerBegrunnelse: String? = null)

data class VurdertSærligGrunnDto(val særligGrunn: SærligGrunn,
                                 val begrunnelse: String? = null)

data class RedusertBeløpDto(val trekk: Boolean, val beløp: BigDecimal)

data class AktivitetDto(val aktivitet: String, val beløp: BigDecimal)

data class VilkårsvurderingStegPeriode(
    val periode: Periode,
    var vilkårsvurderingsresultat: Vilkårsvurderingsresultat = Vilkårsvurderingsresultat.GOD_TRO,
    var begrunnelse: String = "Begrunnelse fra autotest",
    var godTroDto: GodTroDto? = null,
    var aktsomhetDto: AktsomhetDto? = null)

data class GodTroDto(var beløpErIBehold: Boolean = true,
                     var beløpTilbakekreves: BigDecimal? = null,
                     val begrunnelse: String = "God tro begrunnelse fra autotest")

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AktsomhetDto(var aktsomhet: Aktsomhet = Aktsomhet.SIMPEL_UAKTSOMHET,
                        val ileggRenter: Boolean? = null,
                        var andelTilbakekreves: BigDecimal? = null,
                        var beløpTilbakekreves: BigDecimal? = null,
                        val begrunnelse: String = "Aktsomhetsbegrunnelse fra autotest",
                        var særligeGrunner: List<SærligGrunnDto>? = null,
                        var særligeGrunnerTilReduksjon: Boolean = false,
                        var tilbakekrevSmåbeløp: Boolean? = null,
                        var særligeGrunnerBegrunnelse: String? = null)


data class SærligGrunnDto(val særligGrunn: SærligGrunn,
                          val begrunnelse: String? = null)

enum class SærligGrunn(val navn: String) {
    GRAD_AV_UAKTSOMHET("Graden av uaktsomhet hos den kravet retter seg mot"),
    HELT_ELLER_DELVIS_NAVS_FEIL("Om feilen helt eller delvis kan tilskrives NAV"),
    STØRRELSE_BELØP("Størrelsen på feilutbetalt beløp"),
    TID_FRA_UTBETALING("Hvor lang tid siden utbetalingen fant sted"),
    ANNET("Annet");
}

interface Vurdering {

    val navn: String
}

enum class Aktsomhet(override val navn: String) : Vurdering {
    FORSETT("Forsett"),
    GROV_UAKTSOMHET("Grov uaktsomhet"),
    SIMPEL_UAKTSOMHET("Simpel uaktsomhet");
}

enum class AnnenVurdering(override val navn: String) : Vurdering {

    GOD_TRO("Handlet i god tro"),
    FORELDET("Foreldet");
}

enum class Vilkårsvurderingsresultat(val navn: String) {
    FORSTO_BURDE_FORSTÅTT("Ja, mottaker forsto eller burde forstått at utbetalingen skyldtes en feil (1. ledd, 1. punkt)"),
    MANGELFULLE_OPPLYSNINGER_FRA_BRUKER("Ja, mottaker har forårsaket feilutbetalingen ved forsett " +
                                                "eller uaktsomt gitt mangelfulle opplysninger (1. ledd, 2 punkt)"),
    FEIL_OPPLYSNINGER_FRA_BRUKER("Ja, mottaker har forårsaket feilutbetalingen ved forsett eller " +
                                         "uaktsomt gitt feilaktige opplysninger (1. ledd, 2 punkt)"),
    GOD_TRO("Nei, mottaker har mottatt beløpet i god tro (1. ledd)"),
    UDEFINERT("Ikke Definert")
}
