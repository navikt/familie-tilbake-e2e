package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.ForeldelseStegDto
import no.nav.familie.tilbake.e2e.domene.dto.HentForeldelseDto
import no.nav.familie.tilbake.e2e.domene.dto.Foreldelsesvurderingstype
import no.nav.familie.tilbake.e2e.domene.dto.VurdertForeldelsesperiode
import java.time.LocalDate

class BehandleForeldelseStegBuilder(
    hentForeldelseResponse: HentForeldelseDto,
    beslutning: Foreldelsesvurderingstype,
    private var foreldetPerioder: MutableList<VurdertForeldelsesperiode> = mutableListOf()
) {
    init {
        hentForeldelseResponse.foreldetPerioder.forEach {
            foreldetPerioder.add(
                VurdertForeldelsesperiode(
                    periode = it.periode,
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
                    }
                ))
        }
    }

    fun build() : ForeldelseStegDto {
        return ForeldelseStegDto(
            foreldetPerioder = foreldetPerioder
        )
    }
}
