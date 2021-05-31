package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.domene.dto.AktsomhetDto
import no.nav.familie.tilbake.e2e.domene.dto.VilkårsvurderingDto
import no.nav.familie.tilbake.e2e.domene.dto.GodTroDto
import no.nav.familie.tilbake.e2e.domene.dto.HentVilkårsvurderingDto
import no.nav.familie.tilbake.e2e.domene.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.domene.dto.SærligGrunnDto
import no.nav.familie.tilbake.e2e.domene.dto.VilkårsvurderingsperiodeDto
import no.nav.familie.tilbake.e2e.domene.dto.Vilkårsvurderingsresultat
import java.math.BigDecimal

class BehandleVilkårsvurderingStegBuilder(hentVilkårsvurderingResponse: HentVilkårsvurderingDto,
                                          vilkårvurderingsresultat: Vilkårsvurderingsresultat,
                                          aktsomhet: Aktsomhet?,
                                          særligeGrunner: List<SærligGrunn>,
                                          beløpErIBehold: Boolean,
                                          andelTilbakekreves: BigDecimal?,
                                          beløpTilbakekreves: BigDecimal?,
                                          tilbakekrevSmåbeløp: Boolean?) {

    private val vilkårsvurderingsperioder: MutableList<VilkårsvurderingsperiodeDto> = mutableListOf()
    private val BEGRUNNELSE = "Dette er en automatisk begrunnelse fra Autotest"

    init {
        require(!(andelTilbakekreves != null && beløpTilbakekreves != null))
        { "Kan ikke sette både andelTilbakekreves og beløpTilbakekreves" }
        require(!(vilkårvurderingsresultat in listOf(Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                                                     Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                                                     Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER) && aktsomhet == null))
        {
            "Må oppgi grad av uaktsomhet når vilkårsvurdering ikke er ${Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT}, " +
            "${Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER} eller ${Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER}"
        }

        hentVilkårsvurderingResponse.perioder.forEach {
            vilkårsvurderingsperioder.add(
                    VilkårsvurderingsperiodeDto(periode = it.periode,
                                                vilkårsvurderingsresultat = vilkårvurderingsresultat,
                                                begrunnelse = BEGRUNNELSE,
                                                godTroDto = when (vilkårvurderingsresultat) {
                                                    Vilkårsvurderingsresultat.GOD_TRO ->
                                                        godTroGenerator(beløpErIBehold = beløpErIBehold,
                                                                        beløpTilbakekreves = beløpTilbakekreves,
                                                                        feilutbetaltBeløp = it.feilutbetaltBeløp)
                                                    else -> null
                                                },
                                                aktsomhetDto = when (vilkårvurderingsresultat) {
                                                    Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                                                    Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                                                    Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER ->
                                                        aktsomhetGenerator(aktsomhet = aktsomhet,
                                                                           andelTilbakekreves = andelTilbakekreves,
                                                                           beløpTilbakekreves = beløpTilbakekreves,
                                                                           tilbakekrevSmåbeløp = tilbakekrevSmåbeløp,
                                                                           særligeGrunner = særligeGrunner,
                                                                           feilutbetaltBeløp = it.feilutbetaltBeløp)
                                                    else -> null
                                                }))
        }
    }

    private fun godTroGenerator(beløpErIBehold: Boolean,
                                beløpTilbakekreves: BigDecimal?,
                                feilutbetaltBeløp: BigDecimal): GodTroDto {
        return GodTroDto(begrunnelse = BEGRUNNELSE,
                         beløpErIBehold = beløpErIBehold,
                         beløpTilbakekreves = if (beløpErIBehold) beløpTilbakekreves
                                                                  ?: feilutbetaltBeløp else BigDecimal.ZERO)
    }

    private fun aktsomhetGenerator(aktsomhet: Aktsomhet?,
                                   andelTilbakekreves: BigDecimal?,
                                   beløpTilbakekreves: BigDecimal?,
                                   tilbakekrevSmåbeløp: Boolean?,
                                   særligeGrunner: List<SærligGrunn>,
                                   feilutbetaltBeløp: BigDecimal): AktsomhetDto? {
        return when (aktsomhet) {
            Aktsomhet.FORSETT -> AktsomhetDto(aktsomhet = aktsomhet,
                                              begrunnelse = BEGRUNNELSE)
            Aktsomhet.GROV_UAKTSOMHET,
            Aktsomhet.SIMPEL_UAKTSOMHET -> AktsomhetDto(aktsomhet = aktsomhet,
                                                        andelTilbakekreves = andelTilbakekreves,
                                                        beløpTilbakekreves = if (andelTilbakekreves == null) beløpTilbakekreves
                                                                                                             ?: feilutbetaltBeløp else null,
                                                        ileggRenter = false,
                                                        begrunnelse = BEGRUNNELSE,
                                                        særligeGrunnerTilReduksjon = (andelTilbakekreves != BigDecimal(
                                                                100.0)),
                                                        tilbakekrevSmåbeløp = tilbakekrevSmåbeløp,
                                                        særligeGrunner = utledSærligeGrunner(særligeGrunner),
                                                        særligeGrunnerBegrunnelse = BEGRUNNELSE)
            else -> null
        }
    }

    private fun utledSærligeGrunner(særligeGrunner: List<SærligGrunn>): List<SærligGrunnDto> {
        return særligeGrunner.map {
            SærligGrunnDto(særligGrunn = it,
                           begrunnelse = if (it == SærligGrunn.ANNET) BEGRUNNELSE else null)
        }
    }

    fun build(): VilkårsvurderingDto {
        return VilkårsvurderingDto(vilkårsvurderingsperioder = vilkårsvurderingsperioder.toList())
    }
}
