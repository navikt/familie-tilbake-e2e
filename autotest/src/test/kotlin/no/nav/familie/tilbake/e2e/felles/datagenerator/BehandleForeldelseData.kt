package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.familie_tilbake.dto.ForeldelseDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.HentForeldelseDto
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.familie_tilbake.dto.VurdertForeldelsesperiodeDto
import java.time.LocalDate

class BehandleForeldelseData(val hentForeldelseResponse: HentForeldelseDto,
                             val beslutning: Foreldelsesvurderingstype) {

    fun lag(): ForeldelseDto {
        return ForeldelseDto(foreldetPerioder = hentForeldelseResponse.foreldetPerioder.map {
            VurdertForeldelsesperiodeDto(periode = it.periode,
                                         foreldelsesvurderingstype = beslutning,
                                         begrunnelse = "Automatisk begrunnelse fra Autotest",
                                         foreldelsesfrist = when (beslutning) {
                                             Foreldelsesvurderingstype.FORELDET,
                                             Foreldelsesvurderingstype.TILLEGGSFRIST -> LocalDate.now().minusMonths(31)
                                             else -> null
                                         },
                                         oppdagelsesdato = when (beslutning) {
                                             Foreldelsesvurderingstype.TILLEGGSFRIST -> LocalDate.now().minusMonths(15)
                                             else -> null
                                         })
        })
    }
}
