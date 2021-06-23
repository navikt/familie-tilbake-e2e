package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.domene.dto.FaktaDto
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.domene.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.domene.dto.HentFaktaDto
import no.nav.familie.tilbake.e2e.domene.dto.VurdertFaktaFeilutbetaltPeriodeDto

class BehandleFaktaData(val hentFaktaResponse: HentFaktaDto,
                        val hendelsestype: Hendelsestype,
                        val hendelsesundertype: Hendelsesundertype) {

    fun lag(): FaktaDto {
        return FaktaDto(begrunnelse = "Automatisk begrunnelse fra Autotest",
                        feilutbetaltePerioder = hentFaktaResponse.feilutbetaltePerioder.map {
                            VurdertFaktaFeilutbetaltPeriodeDto(periode = it.periode,
                                                               hendelsestype = hendelsestype,
                                                               hendelsesundertype = hendelsesundertype)
                        })
    }
}
