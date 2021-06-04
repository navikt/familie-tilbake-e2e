package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.TotrinnskontrollDto
import no.nav.familie.tilbake.e2e.domene.dto.TotrinnsvurderingDto
import no.nav.familie.tilbake.e2e.domene.dto.VurdertTotrinnDto

class BehandleVedtakBuilder(hentTotrinnsvurderingerResponse: TotrinnsvurderingDto,
                            godkjent: Boolean) {

    private val request = TotrinnskontrollDto(totrinnsvurderinger = hentTotrinnsvurderingerResponse.totrinnsstegsinfo.map {
        VurdertTotrinnDto(behandlingssteg = it.behandlingssteg,
                          godkjent = godkjent,
                          begrunnelse = "Begrunnelse fra Autotest")
    })

    fun build(): TotrinnskontrollDto {
        return request
    }
}
