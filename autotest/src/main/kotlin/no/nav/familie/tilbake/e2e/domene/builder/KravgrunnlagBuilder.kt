package no.nav.familie.tilbake.e2e.domene.builder

import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.dto.KodeKlasse
import no.nav.familie.tilbake.e2e.domene.dto.KodeStatusKrav
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelopDto
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagMelding
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriodeDto
import no.nav.tilbakekreving.typer.v1.PeriodeDto
import no.nav.tilbakekreving.typer.v1.TypeGjelderDto
import no.nav.tilbakekreving.typer.v1.TypeKlasseDto
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.validation.constraints.Max
import kotlin.random.Random

class KravgrunnlagBuilder(status: KodeStatusKrav,
                          ytelsestype: Ytelsestype,
                          eksternFagsakId: String,
                          eksternBehandlingId: String,
                          kravgrunnlagId: BigInteger? = null,
                          vedtakId: BigInteger? = null,
                          @Max(6)
                                 antallPerioder: Int,
                          under4rettsgebyr: Boolean,
                          muligforeldelse: Boolean,
                          periodeLengde: Int) {

    // Kommer til å trenge å kunne sette kontrollfelt tilbake i tid for at den plukkes av auto-opprett batch (ikke laget enda)
    private val finalKontrollfelt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS"))

    private val request = DetaljertKravgrunnlagMelding().also { detaljertKravgrunnlagMelding ->
        detaljertKravgrunnlagMelding.detaljertKravgrunnlag = DetaljertKravgrunnlagDto().apply {
            this.kravgrunnlagId = kravgrunnlagId ?: Random.nextInt(100000, 999999).toBigInteger()
            this.vedtakId = vedtakId ?: Random.nextInt(100000, 999999).toBigInteger()
            this.kodeStatusKrav = status.toString()
            this.kodeFagomraade = utledFagområdeKode(ytelsestype = ytelsestype)
            this.fagsystemId = eksternFagsakId
            this.vedtakIdOmgjort = BigInteger.ZERO
            this.vedtakGjelderId = "12345678901"
            this.typeGjelderId = TypeGjelderDto.PERSON
            this.utbetalesTilId = "12345678901"
            this.typeUtbetId = TypeGjelderDto.PERSON
            this.enhetAnsvarlig = "8020"
            this.enhetBosted = "8020"
            this.enhetBehandl = "8020"
            this.kontrollfelt = finalKontrollfelt
            this.saksbehId = "K231B433"
            this.referanse = eksternBehandlingId
            this.tilbakekrevingsPeriode.addAll(tilbakekrevingsPerioder(antallPerioder = antallPerioder,
                                                                       under4rettsgebyr = under4rettsgebyr,
                                                                       muligforeldelse = muligforeldelse,
                                                                       ytelsestype = ytelsestype,
                                                                       periodeLengde = periodeLengde))
        }
    }

    private fun utledFagområdeKode(ytelsestype: Ytelsestype): String {
        return when (ytelsestype) {
            Ytelsestype.BARNETRYGD -> "BA"
            Ytelsestype.OVERGANGSSTØNAD -> "EFOG"
            Ytelsestype.BARNETILSYN -> "EFBT"
            Ytelsestype.SKOLEPENGER -> "EFSP"
            Ytelsestype.KONTANTSTØTTE -> "KS"
        }
    }

    private fun tilbakekrevingsPerioder(antallPerioder: Int,
                                        under4rettsgebyr: Boolean,
                                        muligforeldelse: Boolean,
                                        ytelsestype: Ytelsestype,
                                        periodeLengde: Int, ): List<DetaljertKravgrunnlagPeriodeDto> {
        /*Lager alltid periodene 3 måneder lange med 1 måned mellom, så derfor gange med 4.
        Første periode starter for 3 år siden når det skal være muligforeldelse eller for 4 måneder siden gange med antall perioder*/
        var startDato = if (muligforeldelse) {
            val antallAarTilbakeITid = if (periodeLengde > 3) 4L else 3L
            LocalDate.now().minusYears(antallAarTilbakeITid).withDayOfMonth(1)
        } else {
            val multiplierPgaPeriodeLengde = if (periodeLengde > 3) 2 else 1
            LocalDate.now()
                    .minusMonths(BigDecimal(4).multiply(BigDecimal((antallPerioder * multiplierPgaPeriodeLengde))).toLong())
                    .withDayOfMonth(1)
        }

        /*Setter feilutbetalt beløp til 20000 kr, med mindre det skal være under4rettsgebyr, da skal det være under 4796 kr pr jan.2021*/
        val beløpprmåned = if (under4rettsgebyr) {
            BigDecimal(4500).divide(BigDecimal(antallPerioder).multiply(BigDecimal(3)), 2, RoundingMode.HALF_DOWN)
        } else {
            BigDecimal(20000).divide(BigDecimal(antallPerioder).multiply(BigDecimal(3)), 2, RoundingMode.HALF_DOWN)
        }

        val tilbakekrevingsperiodeList: MutableList<DetaljertKravgrunnlagPeriodeDto> = mutableListOf()
        for (i in 1..antallPerioder) {
            for (j in 1..periodeLengde) {
                val kravgrunnlagPeriode = DetaljertKravgrunnlagPeriodeDto().apply {
                    periode = PeriodeDto()
                    periode.fom = startDato
                    periode.tom = startDato.withDayOfMonth(startDato.lengthOfMonth())
                    belopSkattMnd = BigDecimal(BigInteger.ZERO, 2)
                    tilbakekrevingsBelop.addAll(tilbakekrevingsbelopGenerator(beløpprmåned, ytelsestype))
                }

                tilbakekrevingsperiodeList.add(kravgrunnlagPeriode)

                startDato = startDato.plusMonths(1)
            }
            startDato = startDato.plusMonths(1)
        }
        return tilbakekrevingsperiodeList
    }

    private fun tilbakekrevingsbelopGenerator(beløpprmåned: BigDecimal,
                                              ytelsestype: Ytelsestype): Collection<DetaljertKravgrunnlagBelopDto> {
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
                feilKodeKlasse = KodeKlasse.KL_KODE_FEIL_PEN
            }
            Ytelsestype.BARNETILSYN -> {
                ytelKodeKlasse = KodeKlasse.EFBT
                feilKodeKlasse = KodeKlasse.KL_KODE_FEIL_PEN
            }
        }
        val tilbakekrevingsBelopList: MutableList<DetaljertKravgrunnlagBelopDto> = mutableListOf()
        val tilbakekrevingsbelopYtel = DetaljertKravgrunnlagBelopDto().apply {
            kodeKlasse = ytelKodeKlasse.name
            typeKlasse = TypeKlasseDto.YTEL
            belopOpprUtbet = beløpprmåned
            belopNy = BigDecimal(BigInteger.ZERO, 2)
            belopTilbakekreves = beløpprmåned
            belopUinnkrevd = BigDecimal(BigInteger.ZERO, 2)
            skattProsent = BigDecimal(BigInteger.ZERO, 2)
        }
        tilbakekrevingsBelopList.add(tilbakekrevingsbelopYtel)

        val tilbakekrevingsbelopFeil = DetaljertKravgrunnlagBelopDto().apply {
            kodeKlasse = feilKodeKlasse.name
            typeKlasse = TypeKlasseDto.FEIL
            belopOpprUtbet = BigDecimal(BigInteger.ZERO, 2)
            belopNy = beløpprmåned
            belopTilbakekreves = BigDecimal(BigInteger.ZERO, 2)
            belopUinnkrevd = BigDecimal(BigInteger.ZERO, 2)
            skattProsent = BigDecimal(BigInteger.ZERO, 2)
        }
        tilbakekrevingsBelopList.add(tilbakekrevingsbelopFeil)

        return tilbakekrevingsBelopList
    }

    fun build(): DetaljertKravgrunnlagMelding {
        return request
    }
}
