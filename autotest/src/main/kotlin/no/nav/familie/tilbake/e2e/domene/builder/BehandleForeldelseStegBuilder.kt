package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.ForeldelseDto
import no.nav.familie.tilbake.e2e.domene.dto.HentForeldelseDto
import no.nav.familie.tilbake.e2e.domene.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.domene.dto.VurdertForeldelsesperiodeDto
import java.time.LocalDate

class BehandleForeldelseStegBuilder(hentForeldelseResponse: HentForeldelseDto,
                                    beslutning: Foreldelsesvurderingstype) {

    private val foreldetPerioder: MutableList<VurdertForeldelsesperiodeDto> = mutableListOf()

    init {
        hentForeldelseResponse.foreldetPerioder.forEach {
            foreldetPerioder.add(
                    VurdertForeldelsesperiodeDto(periode = it.periode,
                                                 foreldelsesvurderingstype = beslutning,
                                                 begrunnelse = "Dette er en automatisk begrunnelse fra Autotest",
                                                 foreldelsesfrist = when (beslutning) {
                                                  Foreldelsesvurderingstype.FORELDET,
                                                  Foreldelsesvurderingstype.TILLEGGSFRIST -> LocalDate.now().minusMonths(31)
                                                  else -> null
                                              },
                                                 oppdagelsesdato = when (beslutning) {
                                                  Foreldelsesvurderingstype.TILLEGGSFRIST -> LocalDate.now().minusMonths(15)
                                                  else -> null
                                              }))
        }
    }

    fun build(): ForeldelseDto {
        return ForeldelseDto(foreldetPerioder = foreldetPerioder.toList())
    }
}
