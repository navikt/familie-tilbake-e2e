package no.nav.familie.tilbake.e2e.domene

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.text.SimpleDateFormat
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
    val kodeFagomraade: KodeFagomraade,
    val fagsystemId: Int,
    val vedtakIdOmgjort: Int,
    val vedtakGjelderId: Int,
    val typeGjelderId: String,
    val utbetalesTilId: Int,
    val typeUtbetId: String,
    val enhetAnsvarlig: Int,
    val enhetBosted: Int,
    val enhetBehandl: Int,
    @JsonFormat(pattern = "yyyy-MM-dd-hh-mm-ss-SSSSSS")
    val kontrollfelt: SimpleDateFormat,
    val saksbehId: String,
    val referanse: Int,
    val tilbakekrevingsPeriode: Set<TilbakekrevingsPeriode>
)

data class TilbakekrevingsPeriode(
    val periode: Periode,
    val belopSkattMnd: BigDecimal,
    val tilbakekrevingsBelop: Set<TilbakekrevingsBelop>
)

data class Periode(
    @JsonFormat(pattern = "yyyy-MM-dd")
    var fom: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    var tom: LocalDate
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

enum class KodeFagomraade {
    BA,
    EF,
    KS
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
