package no.nav.familie.tilbake.e2e.felles

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto
import no.nav.familie.tilbake.e2e.felles.utils.LogiskPeriode
import java.math.BigDecimal
import java.math.BigInteger

data class GjeldendeBehandling(val eksternFagsakId: String,
                               var eksternBrukId: String? = null,
                               var behandlingId: String? = null,
                               val eksternBehandlingId: String,
                               val fagsystem: Fagsystem,
                               val ytelsestype: Ytelsestype,
                               var vedtakId: BigInteger? = null,
                               var kravgrunnlagId: BigInteger? = null,
                               var feilutbetaltePerioder: List<LogiskPeriode>? = null,
                               val personIdent: String,
                               val enhetId: String,
                               val enhetsnavn: String)
