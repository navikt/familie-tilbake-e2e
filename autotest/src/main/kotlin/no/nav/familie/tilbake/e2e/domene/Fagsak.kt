package no.nav.familie.tilbake.e2e.domene

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import java.time.LocalDate
import java.util.UUID

enum class Fagsaksstatus {
    OPPRETTET,
    UNDER_BEHANDLING,
    AVSLUTTET;
}

enum class Kjønn {
    MANN, KVINNE, UKJENT
}

data class Fagsak(val eksternFagsakId: String,
                     val status: Fagsaksstatus,
                     val ytelsestype: Ytelsestype,
                     val fagsystem: Fagsystem,
                     val språkkode: Språkkode,
                     val bruker: Bruker,
                     val behandlinger: Set<Behandlingsoppsummering>)

data class Bruker(val personIdent: String,
                     val navn: String,
                     val fødselsdato: LocalDate,
                     val kjønn: Kjønn)

data class Behandlingsoppsummering(val behandlingId: UUID,
                                      val eksternBrukId: UUID,
                                      val type: Behandlingstype,
                                      val status: Behandlingsstatus)
