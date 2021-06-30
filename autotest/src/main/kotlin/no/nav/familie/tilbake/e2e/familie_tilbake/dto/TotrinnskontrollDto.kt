package no.nav.familie.tilbake.e2e.familie_tilbake.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * DTO-er relatert til behandle steg
 */
data class TotrinnskontrollDto(@JsonProperty("@type")
                               val type: String = "FATTE_VEDTAK",
                               val totrinnsvurderinger: List<VurdertTotrinnDto>,)

data class VurdertTotrinnDto(val behandlingssteg: Behandlingssteg,
                             val godkjent: Boolean,
                             val begrunnelse: String? = null)

/**
 * DTO-er relatert til hentTotrinnsvurdering
 */
data class TotrinnsvurderingDto(val totrinnsstegsinfo: List<Totrinnsstegsinfo>)

data class Totrinnsstegsinfo(val behandlingssteg: Behandlingssteg,
                             val godkjent: Boolean? = null,
                             val begrunnelse: String? = null)
