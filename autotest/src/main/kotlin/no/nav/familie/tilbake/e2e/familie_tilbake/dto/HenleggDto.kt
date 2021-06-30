package no.nav.familie.tilbake.e2e.familie_tilbake.dto

data class HenleggDto(val behandlingsresultatstype: Behandlingsresultatstype,
                      val begrunnelse: String,
                      var fritekst: String? = null)
