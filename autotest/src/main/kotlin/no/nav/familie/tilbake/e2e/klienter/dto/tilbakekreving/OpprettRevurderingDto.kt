package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingsårsakstype
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import java.util.UUID

data class OpprettRevurderingDto(val ytelsestype: Ytelsestype,
                                 val originalBehandlingId: UUID,
                                 val årsakstype: Behandlingsårsakstype)
