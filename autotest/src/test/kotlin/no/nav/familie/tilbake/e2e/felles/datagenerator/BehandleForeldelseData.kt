package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.ForeldelseDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.HentForeldelseDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.VurdertForeldelsesperiodeDto
import java.time.LocalDate

class BehandleForeldelseData(
    val hentForeldelseResponse: HentForeldelseDto,
    val beslutning: Foreldelsesvurderingstype
) {

    fun lag(): ForeldelseDto {
        return ForeldelseDto(
            foreldetPerioder = hentForeldelseResponse.foreldetPerioder.map {
                VurdertForeldelsesperiodeDto(
                    periode = it.periode,
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
                    }
                )
            }
        )
    }
}
