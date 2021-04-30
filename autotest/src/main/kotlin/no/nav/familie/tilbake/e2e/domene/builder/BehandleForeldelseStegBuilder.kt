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
        hentForeldelseResponse.foreldetPerioder?.forEach {
            foreldetPerioder.add(VurdertForeldelsesperiode(periode = it.periode))
        }
        this.foreldetPerioder.forEach {
            it.foreldelsesvurderingstype = beslutning
            it.begrunnelse = "Dette er en automatisk begrunnelse fra Autotest"
            when (beslutning){
                Foreldelsesvurderingstype.FORELDET -> {
                    it.foreldelsesfrist = LocalDate.now().minusMonths(31)
                }
                Foreldelsesvurderingstype.TILLEGGSFRIST -> {
                    it.foreldelsesfrist = LocalDate.now().minusMonths(31)
                    it.oppdagelsesdato = LocalDate.now().minusMonths(15)
                }
            }
        }
    }

    fun build() : ForeldelseStegDto {
        return ForeldelseStegDto(
            foreldetPerioder = foreldetPerioder
        )
    }
}
