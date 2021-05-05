package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.tilbake.e2e.domene.dto.AvsnittDto
import no.nav.familie.tilbake.e2e.domene.dto.Avsnittstype
import no.nav.familie.tilbake.e2e.domene.dto.ForeslåVedtakDto
import no.nav.familie.tilbake.e2e.domene.dto.FritekstavsnittDto
import no.nav.familie.tilbake.e2e.domene.dto.PeriodeMedTekstDto
import no.nav.familie.tilbake.e2e.domene.dto.UnderavsnittDto
import no.nav.familie.tilbake.e2e.domene.dto.Underavsnittstype
import no.nav.familie.tilbake.e2e.domene.dto.felles.PeriodeDto

class BehandleForeslåVedtakBuilder(hentVedtakbrevtekstResponse: List<AvsnittDto>,
                                   genererValgfriTekst: Boolean? = false) {

    private val perioderMedTekst: MutableList<PeriodeMedTekstDto> = mutableListOf()
    private val VURDERINGSTEKST: String = "Dette er en automatisk vurdering fra Autotest for avsnitt"

    init {

        hentVedtakbrevtekstResponse.filter { it.avsnittstype == Avsnittstype.PERIODE }.forEach {
            perioderMedTekst.add(
                    PeriodeMedTekstDto(
                            periode = PeriodeDto(fom = it.fom!!,
                                                 tom = it.tom!!),
                            faktaAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.FAKTA, genererValgfriTekst),
                            foreldelseAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.FORELDELSE, genererValgfriTekst),
                            vilkårAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.VILKÅR, genererValgfriTekst),
                            særligeGrunnerAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.SÆRLIGEGRUNNER, genererValgfriTekst),
                            særligeGrunnerAnnetAvsnitt = utledAvsnitt(it.underavsnittsliste, Underavsnittstype.SÆRLIGEGRUNNER_ANNET, genererValgfriTekst)))
        }
    }

    private fun harAvsnittUnderavsnittstype(underavsnittsliste: List<UnderavsnittDto>,
                                            underavsnittstype: Underavsnittstype): Boolean {
        return underavsnittsliste.any { it.underavsnittstype == underavsnittstype }
    }

    private fun utledAvsnitt(underavsnittsliste: List<UnderavsnittDto>,
                             underavsnittstype: Underavsnittstype,
                             genererValgfriTekst: Boolean?): String? {
        return if (underavsnittsliste.filter{
                    it.fritekstTillatt &&
                    it.fritekstPåkrevet != genererValgfriTekst }
                        .any { it.underavsnittstype == underavsnittstype }) {
            "$VURDERINGSTEKST $underavsnittstype"
        } else null
    }

    fun build(): ForeslåVedtakDto {
        return ForeslåVedtakDto(fritekstavsnitt = FritekstavsnittDto(
                oppsummeringstekst = "Oppsummeringstekst fra Autotest",
                perioderMedTekst = perioderMedTekst.toList()))
    }
}