package no.nav.familie.tilbake.e2e.klienter.dto.historikkinnslag

import no.nav.familie.kontrakter.felles.Applikasjon
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.historikkinnslag.Aktør
import no.nav.familie.kontrakter.felles.historikkinnslag.Historikkinnslagstype
import java.time.LocalDateTime

data class HistorikkinnslagDto(
    val behandlingId: String,
    val eksternFagsakId: String,
    val fagsystem: Fagsystem,
    val applikasjon: Applikasjon,
    val type: Historikkinnslagstype,
    val aktør: Aktør,
    val aktørIdent: String,
    val tittel: String,
    val tekst: String? = null,
    val steg: String? = null,
    val journalpostId: String? = null,
    val dokumentId: String? = null,
    val opprettetTid: LocalDateTime
)
