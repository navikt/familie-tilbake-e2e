package no.nav.familie.tilbake.e2e.felles

import no.nav.familie.tilbake.e2e.domene.FamilieTilbakeKlient
import no.nav.familie.tilbake.e2e.domene.builder.BehandleVedtakBuilder

class Beslutter(private val familieTilbakeKlient: FamilieTilbakeKlient) {

    fun behandleFatteVedtak(godkjenn: Boolean, behandlingId: String) {
        val hentTotrinnsvurderingerResponse =
            requireNotNull(familieTilbakeKlient.hentTotrinnsvurderinger(behandlingId).data)
            { "Kunne ikke hente data for behandling av fatte vedtak" }
        val request = BehandleVedtakBuilder(hentTotrinnsvurderingerResponse = hentTotrinnsvurderingerResponse,
                                            godkjenn = godkjenn).build()

        familieTilbakeKlient.behandleSteg(stegdata = request, behandlingId = behandlingId)
    }
}