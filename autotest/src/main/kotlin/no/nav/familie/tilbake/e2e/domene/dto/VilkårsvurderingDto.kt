package no.nav.familie.tilbake.e2e.domene.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.tilbake.e2e.domene.dto.felles.Periode
import java.math.BigDecimal

data class VilkårsvurderingStegDto(@JsonProperty("@type")
                                   val type: String = "VILKÅRSVURDERING",
                                   val vilkårsvurderingsperioder: List<VilkårsvurderingStegPeriode>)

data class HentVilkårsvurderingDto(val periode: Periode,
                                   val vilkårsvurderingsresultat: Vilkårsvurderingsresultat,
                                   val begrunnelse: String,
                                   val godTroDto: GodTroDto? = null,
                                   val aktsomhetDto: AktsomhetDto? = null)

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

data class RedusertBeløpDto(val trekk: Boolean,
                            val beløp: BigDecimal)

data class AktivitetDto(val aktivitet: String,
                        val beløp: BigDecimal)

data class VilkårsvurderingStegPeriode(val periode: Periode,
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
