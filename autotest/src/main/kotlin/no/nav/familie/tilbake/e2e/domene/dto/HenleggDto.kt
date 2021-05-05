package no.nav.familie.tilbake.e2e.domene.dto

data class HenleggDto(val behandlingsresultatstype: Behandlingsresultatstype,
                      val begrunnelse: String,
                      var fritekst: String? = null)
