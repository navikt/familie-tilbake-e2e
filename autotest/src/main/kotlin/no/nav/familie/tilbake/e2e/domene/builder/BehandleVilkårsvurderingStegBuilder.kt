package no.nav.familie.tilbake.e2e.domene.builder

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.familie.tilbake.e2e.domene.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.domene.dto.AktsomhetDto
import no.nav.familie.tilbake.e2e.domene.dto.BehandleVilkårsvurderingDto
import no.nav.familie.tilbake.e2e.domene.dto.GodTroDto
import no.nav.familie.tilbake.e2e.domene.dto.HentVilkårsvurderingDto
import no.nav.familie.tilbake.e2e.domene.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.domene.dto.SærligGrunnDto
import no.nav.familie.tilbake.e2e.domene.dto.VilkårsvurderingsperiodeDto
import no.nav.familie.tilbake.e2e.domene.dto.Vilkårsvurderingsresultat
import java.math.BigDecimal
import javax.validation.constraints.Max
import javax.validation.constraints.Min

class BehandleVilkårsvurderingStegBuilder(
    hentVilkårsvurderingResponse: HentVilkårsvurderingDto,
    vilkårvurderingsresultat: Vilkårsvurderingsresultat,
    aktsomhet: Aktsomhet?,
    særligeGrunner: List<SærligGrunn>,
    beløpErIBehold: Boolean,
    andelTilbakekreves: BigDecimal?,
    beløpTilbakekreves: BigDecimal?,
    tilbakekrevSmåbeløp: Boolean?,
    private val vilkårsvurderingsperioder: MutableList<VilkårsvurderingsperiodeDto> = mutableListOf()
) {
    init {
        hentVilkårsvurderingResponse.perioder.forEach {
            vilkårsvurderingsperioder.add(
                VilkårsvurderingsperiodeDto(
                    periode = it.periode,
                    vilkårsvurderingsresultat = vilkårvurderingsresultat,
                    begrunnelse = "Dette er en begrunnelse fra Autotest",
                    godTroDto = if(vilkårvurderingsresultat == Vilkårsvurderingsresultat.GOD_TRO) {
                        GodTroDto(
                            begrunnelse = "Dette er en begrunnelse fra Autotest",
                            beløpErIBehold = beløpErIBehold,
                            beløpTilbakekreves = if (beløpErIBehold) beløpTilbakekreves ?: it.feilutbetaltBeløp else BigDecimal.ZERO
                        )
                    } else null,
                    aktsomhetDto = if(vilkårvurderingsresultat != Vilkårsvurderingsresultat.GOD_TRO && aktsomhet != Aktsomhet.FORSETT) {
                        AktsomhetDto(
                            aktsomhet = aktsomhet!!,
                            andelTilbakekreves = andelTilbakekreves,
                            beløpTilbakekreves = if(andelTilbakekreves == null) beløpTilbakekreves ?: it.feilutbetaltBeløp else null,
                            ileggRenter = false,
                            begrunnelse = "Dette er en begrunnelse fra Autotest",
                            særligeGrunnerTilReduksjon = (andelTilbakekreves != BigDecimal(100.0)),
                            tilbakekrevSmåbeløp = tilbakekrevSmåbeløp,
                            særligeGrunnerBegrunnelse = "Særlige grunner begrunnelse fra Autotest",
                            særligeGrunner = særligeGrunner.map { særligGrunn ->
                                SærligGrunnDto(
                                    særligGrunn = særligGrunn,
                                    begrunnelse = if (særligGrunn == SærligGrunn.ANNET) "Særlig grunn annet begrunnelse fra autotest" else null
                                )
                            }
                        )
                    } else if (aktsomhet == Aktsomhet.FORSETT) {
                        AktsomhetDto(
                            aktsomhet = aktsomhet,
                            begrunnelse = "Dette er en begrunnelse fra Autotest"
                        )
                    } else null
                )
            )
        }
    }

    fun build(): BehandleVilkårsvurderingDto {
        return BehandleVilkårsvurderingDto(
            vilkårsvurderingsperioder = vilkårsvurderingsperioder
        )
    }
}