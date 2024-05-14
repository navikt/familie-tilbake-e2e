package no.nav.familie.tilbake.e2e.felles

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.felles.utils.LogiskPeriode
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.InstitusjonDto
import java.math.BigInteger

data class GjeldendeBehandling(
    val eksternFagsakId: String,
    val eksternBrukId: String,
    val behandlingId: String,
    val eksternBehandlingId: String,
    val fagsystem: Fagsystem,
    val ytelsestype: Ytelsestype,
    val personIdent: String,
    val enhetId: String,
    val enhetsnavn: String,
    val harVerge: Boolean,
    var vedtakId: BigInteger? = null,
    var kravgrunnlagId: BigInteger? = null,
    var feilutbetaltePerioder: List<LogiskPeriode>? = null,
    var revurderingBehandlingId: String? = null,
    var revurderingEksternId: String? = null,
    var institusjon: InstitusjonDto? = null
)
