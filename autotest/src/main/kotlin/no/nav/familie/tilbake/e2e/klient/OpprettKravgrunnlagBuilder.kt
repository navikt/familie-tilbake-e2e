package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import javax.validation.constraints.Max
import kotlin.random.Random

class OpprettKravgrunnlagBuilder {

    fun opprettKravgrunnlag(
        status: KodeStatusKrav,
        fagområde: Fagsystem,
        ytelsestype: Ytelsestype,
        eksternFagsakId: String,
        eksternBehandlingId: String,
        kravgrunnlagId: Int? = null,
        vedtakId: Int? = null,
        @Max(6)
        antallPerioder: Int,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean
    ): Kravgrunnlag {
        val finalKravgrunnlagId = kravgrunnlagId ?: Random.nextInt(100000, 999999)
        val finalVedtakId = vedtakId ?: Random.nextInt(100000, 999999)
        val finalKontrollfelt = LocalDate.now().toString()+"-12-00-00-000000"
        // Kommer til å trenge å kunne sette kontrollfelt tilbake i tid for at den plukkes av auto-opprett batch (ikke laget enda)

        return Kravgrunnlag(
            detaljertKravgrunnlagMelding = DetaljertKravgrunnlagMelding(
                detaljertKravgrunnlag = DetaljertKravgrunnlag(
                    kravgrunnlagId = finalKravgrunnlagId,
                    vedtakId = finalVedtakId,
                    kodeStatusKrav = status,
                    kodeFagomraade = fagområde,
                    fagsystemId = eksternFagsakId,
                    vedtakIdOmgjort = 0,
                    vedtakGjelderId = "12345678901",
                    typeGjelderId = "PERSON",
                    utbetalesTilId = "12345678901",
                    typeUtbetId = "PERSON",
                    enhetAnsvarlig = 8020,
                    enhetBosted = 8020,
                    enhetBehandl = 8020,
                    kontrollfelt = finalKontrollfelt,
                    saksbehId = "K231B433",
                    referanse = eksternBehandlingId,
                    tilbakekrevingsPeriode = tilbakekrevingsPerioder(
                        antallPerioder,
                        under4rettsgebyr,
                        muligforeldelse,
                        ytelsestype
                    )
                )
            )
        )
    }

    private fun tilbakekrevingsPerioder(
        antallPerioder: Int,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean,
        ytelsestype: Ytelsestype
    ): Set<TilbakekrevingsPeriode> {
        /*Lager alltid periodene 3 måneder lange med 1 måned mellom, så derfor gange med 4.
        Første periode starter for 3 år siden når det skal være muligforeldelse eller for 4 måneder siden gange med antall perioder*/
        val startDato = if (muligforeldelse) {
            LocalDate.now().minusYears(3).withDayOfMonth(1)
        } else {
            LocalDate.now().minusMonths(BigDecimal(4).multiply(BigDecimal(antallPerioder)).toLong())
        }

        /*Setter feilutbetalt beløp til 20000 kr, med mindre det skal være under4rettsgebyr, da skal det være under 4796 kr pr jan.2021*/
        val beløpprmåned = if (under4rettsgebyr) {
            BigDecimal(4500).divide(BigDecimal(antallPerioder).multiply(BigDecimal(3)), 0, RoundingMode.HALF_DOWN)
        } else {
            BigDecimal(20000).divide(BigDecimal(antallPerioder).multiply(BigDecimal(3)),0, RoundingMode.HALF_DOWN)
        }

        val tilbakekrevingsperioder: MutableSet<TilbakekrevingsPeriode> = mutableSetOf()
        for (i in 1..antallPerioder) { //TODO Disse for-loopene fungerer ikke :(
            for (j in 1..3) {
                tilbakekrevingsperioder.add(
                    TilbakekrevingsPeriode(
                        periode = Periode(
                            fom = startDato.toString(),
                            tom = startDato.withDayOfMonth(startDato.lengthOfMonth()).toString()
                        ),
                        belopSkattMnd = BigDecimal.ZERO,
                        tilbakekrevingsBelop = tilbakekrevingsBelopGenerator(beløpprmåned, ytelsestype)
                    )
                )
                startDato.plusMonths(1)
            }
            startDato.plusMonths(1)
        }
        return tilbakekrevingsperioder
    }

    private fun tilbakekrevingsBelopGenerator(
        beløpprmåned: BigDecimal,
        ytelsestype: Ytelsestype
    ): Set<TilbakekrevingsBelop> {
        val ytelKodeKlasse: KodeKlasse
        val feilKodeKlasse: KodeKlasse
        when (ytelsestype) {
            Ytelsestype.BARNETRYGD -> {
                ytelKodeKlasse = KodeKlasse.BATR
                feilKodeKlasse = KodeKlasse.KL_KODE_FEIL_BA
            }
            Ytelsestype.KONTANTSTØTTE -> {
                throw Exception("Kontantstøtte ikke implementert enda")
            }
            Ytelsestype.OVERGANGSSTØNAD -> {
                ytelKodeKlasse = KodeKlasse.EFOG
                feilKodeKlasse = KodeKlasse.KL_KODE_FEIL_EFOG
            }
            Ytelsestype.SKOLEPENGER -> {
                ytelKodeKlasse = KodeKlasse.EFSP
                throw Exception("Skolepenger er ikke implementert enda")
            }
            Ytelsestype.BARNETILSYN -> {
                ytelKodeKlasse = KodeKlasse.EFBT
                throw Exception("Barnetilsyn er ikke implementert enda")
            }
        }
        val tilbakekrevingsBelopSet: MutableSet<TilbakekrevingsBelop> = mutableSetOf()
        tilbakekrevingsBelopSet.add(
            TilbakekrevingsBelop(
                kodeKlasse = ytelKodeKlasse,
                typeKlasse = TypeKlasse.YTEL,
                belopOpprUtbet = beløpprmåned,
                belopNy = BigDecimal.ZERO,
                belopTilbakekreves = beløpprmåned,
                belopUinnkrevd = BigDecimal.ZERO,
                skattProsent = BigDecimal.ZERO
            )
        )
        tilbakekrevingsBelopSet.add(
            TilbakekrevingsBelop(
                kodeKlasse = feilKodeKlasse,
                typeKlasse = TypeKlasse.FEIL,
                belopOpprUtbet = BigDecimal.ZERO,
                belopNy = beløpprmåned,
                belopTilbakekreves = BigDecimal.ZERO,
                belopUinnkrevd = BigDecimal.ZERO,
                skattProsent = BigDecimal.ZERO
            )
        )
        return tilbakekrevingsBelopSet
    }
}
