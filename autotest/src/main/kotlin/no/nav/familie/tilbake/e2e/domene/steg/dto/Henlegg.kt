package no.nav.familie.tilbake.e2e.domene.steg.dto

import no.nav.familie.tilbake.e2e.domene.Behandlingsresultatstype

data class Henlegg(val behandlingsresultatstype: Behandlingsresultatstype,
                   val begrunnelse: String,
                   var fritekst: String? = null)
