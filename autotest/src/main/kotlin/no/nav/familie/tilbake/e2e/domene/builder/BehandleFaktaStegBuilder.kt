package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.BehandleFaktaDto
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.domene.dto.HentFaktaDto
import no.nav.familie.tilbake.e2e.domene.dto.VurdertFaktaFeilutbetaltPeriode

class BehandleFaktaStegBuilder(
    hentFaktaResponse: HentFaktaDto,
    hendelsestype: Hendelsestype,
    hendelsesundertype: Hendelsesundertype,
    private val feilutbetaltePerioder: MutableList<VurdertFaktaFeilutbetaltPeriode> = mutableListOf()
) {

    init {
        hentFaktaResponse.feilutbetaltePerioder.forEach {
            feilutbetaltePerioder.add(
                VurdertFaktaFeilutbetaltPeriode(
                    periode = it.periode,
                    hendelsestype = hendelsestype,
                    hendelsesundertype = hendelsesundertype
                )
            )
        }
    }

    fun build(): BehandleFaktaDto {
        return BehandleFaktaDto(
            begrunnelse = "Dette er en automatisk begrunnelse fra Autotest",
            feilutbetaltePerioder = feilutbetaltePerioder
        )
    }
}
