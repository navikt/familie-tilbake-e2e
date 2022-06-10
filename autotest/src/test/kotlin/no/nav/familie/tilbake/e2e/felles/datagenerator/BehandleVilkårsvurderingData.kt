package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.klienter.dto.Aktsomhet
import no.nav.familie.tilbake.e2e.klienter.dto.AktsomhetDto
import no.nav.familie.tilbake.e2e.klienter.dto.GodTroDto
import no.nav.familie.tilbake.e2e.klienter.dto.HentVilkårsvurderingDto
import no.nav.familie.tilbake.e2e.klienter.dto.SærligGrunn
import no.nav.familie.tilbake.e2e.klienter.dto.SærligGrunnDto
import no.nav.familie.tilbake.e2e.klienter.dto.VilkårsvurderingDto
import no.nav.familie.tilbake.e2e.klienter.dto.VilkårsvurderingsperiodeDto
import no.nav.familie.tilbake.e2e.klienter.dto.Vilkårsvurderingsresultat
import java.math.BigDecimal

class BehandleVilkårsvurderingData(
    val hentVilkårsvurderingResponse: HentVilkårsvurderingDto,
    val vilkårvurderingsresultat: Vilkårsvurderingsresultat,
    val aktsomhet: Aktsomhet?,
    val særligeGrunner: List<SærligGrunn>,
    val beløpErIBehold: Boolean,
    val andelTilbakekreves: BigDecimal?,
    val beløpTilbakekreves: BigDecimal?,
    val ileggRenter: Boolean,
    val tilbakekrevSmåbeløp: Boolean?,
    val ytelsestype: Ytelsestype
) {

    private val BEGRUNNELSE = "Automatisk begrunnelse fra Autotest"

    init {
        require(!(andelTilbakekreves != null && beløpTilbakekreves != null)) { "Kan ikke sette både andelTilbakekreves og beløpTilbakekreves" }
    }

    fun lag(): VilkårsvurderingDto {
        return VilkårsvurderingDto(
            vilkårsvurderingsperioder = hentVilkårsvurderingResponse.perioder.map {
                VilkårsvurderingsperiodeDto(
                    periode = it.periode,
                    vilkårsvurderingsresultat = vilkårvurderingsresultat,
                    begrunnelse = BEGRUNNELSE,
                    godTroDto = when (vilkårvurderingsresultat) {
                        Vilkårsvurderingsresultat.GOD_TRO ->
                            godTroGenerator(
                                beløpErIBehold = beløpErIBehold,
                                beløpTilbakekreves = beløpTilbakekreves,
                                feilutbetaltBeløp = it.feilutbetaltBeløp
                            )
                        else -> null
                    },
                    aktsomhetDto = when (vilkårvurderingsresultat) {
                        Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                        Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT,
                        Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER ->
                            aktsomhetGenerator(
                                aktsomhet = requireNotNull(aktsomhet) {
                                    "Må vurdere grad av uaktsomhet dersom vilkår " +
                                        "vurderes til $vilkårvurderingsresultat"
                                },
                                andelTilbakekreves = andelTilbakekreves,
                                beløpTilbakekreves = beløpTilbakekreves,
                                tilbakekrevSmåbeløp = tilbakekrevSmåbeløp,
                                særligeGrunner = særligeGrunner,
                                ileggRenter = utledRenter(
                                    ileggRenter = ileggRenter,
                                    ytelsestype = ytelsestype,
                                    vilkårvurderingsresultat = vilkårvurderingsresultat,
                                    aktsomhet = aktsomhet
                                ),
                                feilutbetaltBeløp = it.feilutbetaltBeløp
                            )
                        else -> null
                    }
                )
            }
        )
    }

    private fun godTroGenerator(
        beløpErIBehold: Boolean,
        beløpTilbakekreves: BigDecimal?,
        feilutbetaltBeløp: BigDecimal
    ): GodTroDto {
        return GodTroDto(
            begrunnelse = BEGRUNNELSE,
            beløpErIBehold = beløpErIBehold,
            beløpTilbakekreves = if (beløpErIBehold) beløpTilbakekreves ?: feilutbetaltBeløp else BigDecimal.ZERO
        )
    }

    private fun aktsomhetGenerator(
        aktsomhet: Aktsomhet?,
        andelTilbakekreves: BigDecimal?,
        beløpTilbakekreves: BigDecimal?,
        tilbakekrevSmåbeløp: Boolean?,
        særligeGrunner: List<SærligGrunn>,
        ileggRenter: Boolean,
        feilutbetaltBeløp: BigDecimal
    ): AktsomhetDto? {
        return when (aktsomhet) {
            Aktsomhet.FORSETT -> AktsomhetDto(
                aktsomhet = aktsomhet,
                ileggRenter = ileggRenter,
                begrunnelse = BEGRUNNELSE
            )
            Aktsomhet.GROV_UAKTSOMHET,
            Aktsomhet.SIMPEL_UAKTSOMHET -> AktsomhetDto(
                aktsomhet = aktsomhet,
                andelTilbakekreves = andelTilbakekreves,
                beløpTilbakekreves = if (andelTilbakekreves == null) beløpTilbakekreves
                    ?: feilutbetaltBeløp else null,
                ileggRenter = ileggRenter,
                begrunnelse = BEGRUNNELSE,
                særligeGrunnerTilReduksjon = (andelTilbakekreves != BigDecimal(100)),
                tilbakekrevSmåbeløp = tilbakekrevSmåbeløp,
                særligeGrunner = utledSærligeGrunner(særligeGrunner),
                særligeGrunnerBegrunnelse = BEGRUNNELSE
            )
            else -> throw Exception("Vilkårsvurdering med aktsomhet $aktsomhet er ikke implementert")
        }
    }

    private fun utledSærligeGrunner(særligeGrunner: List<SærligGrunn>): List<SærligGrunnDto> {
        return særligeGrunner.map {
            SærligGrunnDto(
                særligGrunn = it,
                begrunnelse = if (it == SærligGrunn.ANNET) BEGRUNNELSE else null
            )
        }
    }

    private fun utledRenter(
        ileggRenter: Boolean,
        ytelsestype: Ytelsestype,
        vilkårvurderingsresultat: Vilkårsvurderingsresultat,
        aktsomhet: Aktsomhet
    ): Boolean {
        return if (ytelsestype in listOf(Ytelsestype.BARNETRYGD) || aktsomhet != Aktsomhet.FORSETT) {
            // Ilegges ikke renter dersom ytelsestype er BA eller dersom vilkår ikke vurderes til forsett
            false
        } else {
            when (vilkårvurderingsresultat) {
                Vilkårsvurderingsresultat.FEIL_OPPLYSNINGER_FRA_BRUKER,
                Vilkårsvurderingsresultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER -> true
                Vilkårsvurderingsresultat.FORSTO_BURDE_FORSTÅTT -> ileggRenter
                else -> false
            }
        }
    }
}
