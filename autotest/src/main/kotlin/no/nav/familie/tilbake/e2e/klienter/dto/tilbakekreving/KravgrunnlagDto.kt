package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

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
    EFSP,
    KL_KODE_JUST_BA,
    KL_KODE_JUST_EFOG,
    KL_KODE_JUST_PEN
}

enum class TypeKlasse {
    FEIL,
    JUST,
    SKAT,
    TREK,
    YTEL
}
