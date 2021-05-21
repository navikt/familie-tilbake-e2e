package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import java.math.BigInteger

data class GjeldendeBehandling(val fagsystem: Fagsystem? = null,
                               val ytelsestype: Ytelsestype? = null,
                               val eksternFagsakId: String? = null,
                               val eksternBehandlingId: String? = null,
                               var eksternBrukId: String? = null,
                               val behandlingId: String? = null,
                               var vedtakId: BigInteger? = null,
                               var kravgrunnlagId: BigInteger? = null)
