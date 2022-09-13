package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.tilbake.e2e.klienter.dto.felles.PeriodeDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.AvsnittDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Avsnittstype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.ForeslåVedtakDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.FritekstavsnittDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.PeriodeMedTekstDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.UnderavsnittDto
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.Underavsnittstype

class BehandleForeslåVedtakData(val hentVedtakbrevtekstResponse: List<AvsnittDto>) {

    fun lag(): ForeslåVedtakDto {
        return ForeslåVedtakDto(
            fritekstavsnitt = FritekstavsnittDto(
                oppsummeringstekst = "Automatisk oppsummeringstekst fra Autotest.",
                perioderMedTekst = hentVedtakbrevtekstResponse
                    .filter { it.avsnittstype == Avsnittstype.PERIODE && it.fom != null && it.tom != null }
                    .map {
                        PeriodeMedTekstDto(
                            periode = PeriodeDto(
                                fom = requireNotNull(it.fom),
                                tom = requireNotNull(it.tom)
                            ),
                            faktaAvsnitt = utledAvsnitt(
                                it.underavsnittsliste,
                                Underavsnittstype.FAKTA
                            ),
                            foreldelseAvsnitt = utledAvsnitt(
                                it.underavsnittsliste,
                                Underavsnittstype.FORELDELSE
                            ),
                            vilkårAvsnitt = utledAvsnitt(
                                it.underavsnittsliste,
                                Underavsnittstype.VILKÅR
                            ),
                            særligeGrunnerAvsnitt = utledAvsnitt(
                                it.underavsnittsliste,
                                Underavsnittstype.SÆRLIGEGRUNNER
                            ),
                            særligeGrunnerAnnetAvsnitt = utledAvsnitt(
                                it.underavsnittsliste,
                                Underavsnittstype.SÆRLIGEGRUNNER_ANNET
                            )
                        )
                    }
            )
        )
    }

    private fun utledAvsnitt(
        underavsnittsliste: List<UnderavsnittDto>,
        underavsnittstype: Underavsnittstype
    ): String? {
        return if (underavsnittsliste.any { it.fritekstTillatt && it.underavsnittstype == underavsnittstype }) {
            "Automatisk vurdering fra Autotest for underavsnitt $underavsnittstype."
        } else {
            null
        }
    }
}
