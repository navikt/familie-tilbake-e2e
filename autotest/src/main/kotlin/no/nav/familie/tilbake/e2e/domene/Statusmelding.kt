package no.nav.familie.tilbake.e2e.domene

data class Statusmelding(val endringKravOgVedtakstatus: EndringKravOgVedtakstatus) {
    data class EndringKravOgVedtakstatus(val kravOgVedtakstatus: KravOgVedtakstatus) {
        data class KravOgVedtakstatus(
            val vedtakId: Int,
            val kodeStatusKrav: KodeStatusKrav,
            val kodeFagomraade: Fagsaksstatus,
            val fagsystemId: Int,
            val vedtakGjelderId: Int,
            val typeGjelderId: String,
            val referanse: Int
        )
    }
}
