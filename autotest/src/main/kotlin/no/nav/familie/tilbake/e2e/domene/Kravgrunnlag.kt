package no.nav.familie.tilbake.e2e.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import java.math.BigDecimal
import java.time.LocalDate

data class Kravgrunnlag(
    val detaljertKravgrunnlagMelding: DetaljertKravgrunnlagMelding
)

data class DetaljertKravgrunnlagMelding(
    val detaljertKravgrunnlag: DetaljertKravgrunnlag
)

data class DetaljertKravgrunnlag(
    val kravgrunnlagId: Int,
    val vedtakId: Int,
    val kodeStatusKrav: KodeStatusKrav,
    val kodeFagomraade: Fagsystem,
    val fagsystemId: String,
    val vedtakIdOmgjort: Int,
    val vedtakGjelderId: String,
    val typeGjelderId: String,
    val utbetalesTilId: String,
    val typeUtbetId: String,
    val enhetAnsvarlig: Int,
    val enhetBosted: Int,
    val enhetBehandl: Int,
    val kontrollfelt: String,
    val saksbehId: String,
    val referanse: String,
    val tilbakekrevingsPeriode: Set<TilbakekrevingsPeriode>
)

data class TilbakekrevingsPeriode(
    val periode: Periode,
    val belopSkattMnd: BigDecimal,
    val tilbakekrevingsBelop: Set<TilbakekrevingsBelop>
)

data class Periode(
    @JsonFormat(pattern = "yyyy-MM-dd")
    var fom: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    var tom: String
)

data class TilbakekrevingsBelop(
    val kodeKlasse: KodeKlasse,
    val typeKlasse: TypeKlasse,
    val belopOpprUtbet: BigDecimal,
    val belopNy: BigDecimal,
    val belopTilbakekreves: BigDecimal,
    val belopUinnkrevd: BigDecimal,
    val skattProsent: BigDecimal
)

enum class KodeStatusKrav {
    NY,
    ENDR,
    SPER,
    AVSL
}

enum class KodeKlasse {
    KL_KODE_FEIL_BA,
    KL_KODE_FEIL_EFOG,
    KL_KODE_FEIL_PEN,
    BATR,
    BATRSMA,
    EFOG,
    EFBT,
    EFSP
}

enum class TypeKlasse {
    FEIL,
    JUST,
    SKAT,
    TREK,
    YTEL
}
