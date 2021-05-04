package no.nav.familie.tilbake.e2e.domene.dto

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import java.time.LocalDate
import java.util.UUID

data class FagsakDto(val eksternFagsakId: String,
                     val ytelsestype: Ytelsestype,
                     val fagsystem: Fagsystem,
                     val språkkode: Språkkode,
                     val bruker: BrukerDto,
                     val behandlinger: Set<BehandlingsoppsummeringDto>)

data class BrukerDto(val personIdent: String,
                     val navn: String,
                     val fødselsdato: LocalDate,
                     val kjønn: Kjønn)

data class BehandlingsoppsummeringDto(val behandlingId: UUID,
                                      val eksternBrukId: UUID,
                                      val type: Behandlingstype,
                                      val status: Behandlingsstatus)

enum class Fagsaksstatus {
    OPPRETTET,
    UNDER_BEHANDLING,
    AVSLUTTET;
}

enum class Kjønn {
    MANN,
    KVINNE,
    UKJENT
}
