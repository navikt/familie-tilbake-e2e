package no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving

data class HenleggDto(val behandlingsresultatstype: Behandlingsresultatstype,
                      val begrunnelse: String,
                      var fritekst: String? = null)
