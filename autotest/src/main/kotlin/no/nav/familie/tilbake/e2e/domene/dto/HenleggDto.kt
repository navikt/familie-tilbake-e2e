package no.nav.familie.tilbake.e2e.domene.dto

data class Henlegg(
    val behandlingsresultatstype: Behandlingsresultatstype,
    val begrunnelse: String,
    var fritekst: String? = null
)
