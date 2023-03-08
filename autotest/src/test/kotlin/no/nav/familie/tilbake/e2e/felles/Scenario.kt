package no.nav.familie.tilbake.e2e.felles

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Regelverk
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype

data class Scenario(
    val eksternFagsakId: String,
    val eksternBehandlingId: String,
    val fagsystem: Fagsystem,
    val ytelsestype: Ytelsestype,
    val personIdent: String,
    val enhetId: String,
    val enhetsnavn: String,
    val regelverk: Regelverk = Regelverk.NASJONAL
)
