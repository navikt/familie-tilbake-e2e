package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.TotrinnskontrollDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.TotrinnsvurderingDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.VurdertTotrinnDto

class BehandleFatteVedtakData(
    val hentTotrinnsvurderingerResponse: TotrinnsvurderingDto,
    val godkjent: Boolean
) {

    fun lag(): TotrinnskontrollDto {
        return TotrinnskontrollDto(
            totrinnsvurderinger = hentTotrinnsvurderingerResponse.totrinnsstegsinfo.map {
                VurdertTotrinnDto(
                    behandlingssteg = it.behandlingssteg,
                    godkjent = godkjent,
                    begrunnelse = "Begrunnelse fra Autotest"
                )
            }
        )
    }
}
