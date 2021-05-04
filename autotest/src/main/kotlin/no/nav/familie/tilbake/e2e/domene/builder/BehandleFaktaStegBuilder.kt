package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.FaktaDto
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.domene.dto.HentFaktaDto
import no.nav.familie.tilbake.e2e.domene.dto.VurdertFaktaFeilutbetaltPeriodeDto

class BehandleFaktaStegBuilder(hentFaktaResponse: HentFaktaDto,
                               hendelsestype: Hendelsestype,
                               hendelsesundertype: Hendelsesundertype) {

    private val feilutbetaltePerioder: MutableList<VurdertFaktaFeilutbetaltPeriodeDto> = mutableListOf()

    init {
        hentFaktaResponse.feilutbetaltePerioder.forEach {
            feilutbetaltePerioder.add(
                    VurdertFaktaFeilutbetaltPeriodeDto(periode = it.periode,
                                                       hendelsestype = hendelsestype,
                                                       hendelsesundertype = hendelsesundertype))
        }
    }

    fun build(): FaktaDto {
        return FaktaDto(begrunnelse = "Dette er en automatisk begrunnelse fra Autotest",
                        feilutbetaltePerioder = feilutbetaltePerioder.toList())
    }
}
