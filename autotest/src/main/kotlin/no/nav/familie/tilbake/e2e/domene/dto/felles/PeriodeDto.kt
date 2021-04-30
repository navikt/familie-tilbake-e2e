package no.nav.familie.tilbake.e2e.domene.dto.felles

import com.fasterxml.jackson.annotation.JsonFormat

data class Periode(
    @JsonFormat(pattern = "yyyy-MM-dd")
    var fom: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    var tom: String
)
