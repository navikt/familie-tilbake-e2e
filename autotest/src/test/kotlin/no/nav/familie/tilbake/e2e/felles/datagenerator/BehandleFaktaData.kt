package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.FaktaDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsestype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Hendelsesundertype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.HentFaktaDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.VurdertFaktaFeilutbetaltPeriodeDto

class BehandleFaktaData(val hentFaktaResponse: HentFaktaDto,
                        val hendelsestype: Hendelsestype,
                        val hendelsesundertype: Hendelsesundertype
) {

    fun lag(): FaktaDto {
        return FaktaDto(begrunnelse = "Automatisk begrunnelse fra Autotest",
                        feilutbetaltePerioder = hentFaktaResponse.feilutbetaltePerioder.map {
                            VurdertFaktaFeilutbetaltPeriodeDto(periode = it.periode,
                                                               hendelsestype = hendelsestype,
                                                               hendelsesundertype = hendelsesundertype)
                        })
    }
}
