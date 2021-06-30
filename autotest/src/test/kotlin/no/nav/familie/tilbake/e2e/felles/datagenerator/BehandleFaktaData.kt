package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.familie_tilbake.dto.FaktaDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Hendelsestype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Hendelsesundertype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.HentFaktaDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.VurdertFaktaFeilutbetaltPeriodeDto

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
