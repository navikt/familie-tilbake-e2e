package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.AvsnittDto
import no.nav.familie.tilbake.e2e.domene.dto.Avsnittstype
import no.nav.familie.tilbake.e2e.domene.dto.ForeslåVedtakDto
import no.nav.familie.tilbake.e2e.domene.dto.FritekstavsnittDto
import no.nav.familie.tilbake.e2e.domene.dto.PeriodeMedTekstDto
import no.nav.familie.tilbake.e2e.domene.dto.UnderavsnittDto
import no.nav.familie.tilbake.e2e.domene.dto.Underavsnittstype
import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto

class BehandleForeslåVedtakBuilder(hentVedtakbrevtekstResponse: List<AvsnittDto>) {

    private val perioderMedTekst: MutableList<PeriodeMedTekstDto> = mutableListOf()

    init {
        hentVedtakbrevtekstResponse.filter { it.avsnittstype == Avsnittstype.PERIODE }.forEach {
            perioderMedTekst.add(
                    PeriodeMedTekstDto(
                            periode = PeriodeDto(fom = it.fom!!,
                                                 tom = it.tom!!),
                            faktaAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.FAKTA),
                            foreldelseAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.FORELDELSE),
                            vilkårAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.VILKÅR),
                            særligeGrunnerAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.SÆRLIGEGRUNNER),
                            særligeGrunnerAnnetAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.SÆRLIGEGRUNNER_ANNET)))
        }
    }

    private fun utledAvsnitt(underavsnittsliste: List<UnderavsnittDto>,
                             underavsnittstype: Underavsnittstype): String? {
        return if (underavsnittsliste
                        .filter { it.fritekstTillatt }
                        .any { it.underavsnittstype == underavsnittstype }) {
                            "Dette er en automatisk vurdering fra Autotest for avsnitt $underavsnittstype"
        } else null
    }

    fun build(): ForeslåVedtakDto {
        return ForeslåVedtakDto(fritekstavsnitt = FritekstavsnittDto(
                oppsummeringstekst = "Oppsummeringstekst fra Autotest",
                perioderMedTekst = perioderMedTekst.toList()))
    }
}
