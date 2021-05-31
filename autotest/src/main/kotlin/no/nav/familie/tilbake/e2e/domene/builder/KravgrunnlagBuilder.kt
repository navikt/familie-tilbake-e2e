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
import kotlin.random.Random

class KravgrunnlagBuilder(status: KodeStatusKrav,
                          ytelsestype: Ytelsestype,
                          eksternFagsakId: String,
                          eksternBehandlingId: String,
                          kravgrunnlagId: BigInteger? = null,
                          vedtakId: BigInteger? = null,
                          antallPerioder: Int,
                          under4rettsgebyr: Boolean,
                          muligforeldelse: Boolean,
                          periodeLengde: Int) {

    // TODO: Vil trenge å kunne sette kontrollfelt tilbake i tid for at den plukkes av auto-opprett batch (ikke laget enda)
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
            this.tilbakekrevingsPeriode.addAll(utledTilbakekrevingsPerioder(antallPerioder = antallPerioder,
                                                                            under4rettsgebyr = under4rettsgebyr,
                                                                            muligForeldelse = muligforeldelse,
                                                                            ytelsestype = ytelsestype,
                                                                            periodelengde = periodeLengde))
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

    private fun utledTilbakekrevingsPerioder(antallPerioder: Int,
                                             under4rettsgebyr: Boolean,
                                             muligForeldelse: Boolean,
                                             ytelsestype: Ytelsestype,
                                             periodelengde: Int): List<DetaljertKravgrunnlagPeriodeDto> {
        // Finner startdato for første periode, hvor periodene har 1 måned mellomrom
        val antallMånederTilbake = antallPerioder * periodelengde + antallPerioder - 1
        var startdato = LocalDate.now()
            .minusMonths(antallMånederTilbake.toLong())
            .withDayOfMonth(1)

        // Første periode må starte 3 år tilbake i tid dersom det skal være mulig foreldelse
        if (muligForeldelse) {
            startdato = minOf(startdato, LocalDate.now().minusYears(3L).withDayOfMonth(1))
        }

        // Setter feilutbetalt beløp til under 4.796 kr dersom det skal være under4rettsgebyr
        val beløpprmåned = if (under4rettsgebyr) {
            BigDecimal(4500).divide(BigDecimal(antallPerioder).multiply(BigDecimal(3)), 2, RoundingMode.HALF_DOWN)
        } else {
            BigDecimal(20000).divide(BigDecimal(antallPerioder).multiply(BigDecimal(3)), 2, RoundingMode.HALF_DOWN)
        }

        val tilbakekrevingsperiodeList: MutableList<DetaljertKravgrunnlagPeriodeDto> = mutableListOf()
        for (i in 1..antallPerioder) {
            for (j in 1..periodelengde) {
                val kravgrunnlagPeriode = DetaljertKravgrunnlagPeriodeDto().apply {
                    periode = PeriodeDto()
                    periode.fom = startdato
                    periode.tom = startdato.withDayOfMonth(startdato.lengthOfMonth())
                    belopSkattMnd = BigDecimal.ZERO.setScale(2)
                    tilbakekrevingsBelop.addAll(utledTilbakekrevingsbelop(beløpprmåned, ytelsestype))
                }

                tilbakekrevingsperiodeList.add(kravgrunnlagPeriode)

                startdato = startdato.plusMonths(1)
            }
            startdato = startdato.plusMonths(1)
        }
        return tilbakekrevingsperiodeList
    }

    private fun utledTilbakekrevingsbelop(beløpprmåned: BigDecimal,
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
            belopNy =  BigDecimal.ZERO.setScale(2)
            belopTilbakekreves = beløpprmåned
            belopUinnkrevd =  BigDecimal.ZERO.setScale(2)
            skattProsent =  BigDecimal.ZERO.setScale(2)
        }
        tilbakekrevingsBelopList.add(tilbakekrevingsbelopYtel)

        val tilbakekrevingsbelopFeil = DetaljertKravgrunnlagBelopDto().apply {
            kodeKlasse = feilKodeKlasse.name
            typeKlasse = TypeKlasseDto.FEIL
            belopOpprUtbet =  BigDecimal.ZERO.setScale(2)
            belopNy = beløpprmåned
            belopTilbakekreves =  BigDecimal.ZERO.setScale(2)
            belopUinnkrevd =  BigDecimal.ZERO.setScale(2)
            skattProsent =  BigDecimal.ZERO.setScale(2)
        }
        tilbakekrevingsBelopList.add(tilbakekrevingsbelopFeil)

        return tilbakekrevingsBelopList
    }

    fun build(): DetaljertKravgrunnlagMelding {
        return request
    }
}