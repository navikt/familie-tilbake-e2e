package no.nav.familie.tilbake.e2e.domene

import com.fasterxml.jackson.annotation.JsonFormat

data class Periode(
    @JsonFormat(pattern = "yyyy-MM-dd")
    var fom: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    var tom: String
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
