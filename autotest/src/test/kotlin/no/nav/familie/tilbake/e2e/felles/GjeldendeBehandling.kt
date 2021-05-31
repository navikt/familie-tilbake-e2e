package no.nav.familie.tilbake.e2e.felles

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto
import java.math.BigInteger

data class GjeldendeBehandling(val fagsystem: Fagsystem,
                               val ytelsestype: Ytelsestype,
                               val eksternFagsakId: String,
                               val eksternBehandlingId: String,
                               var eksternBrukId: String? = null,
                               val behandlingId: String? = null,
                               var vedtakId: BigInteger? = null,
                               var kravgrunnlagId: BigInteger? = null,
                               var perioder: List<PeriodeDto>? = null)
