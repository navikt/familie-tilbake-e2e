package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.KodeKlasse
import no.nav.familie.tilbake.e2e.klienter.dto.tilbakekreving.KodeStatusKrav
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

class KravgrunnlagData(
    val status: KodeStatusKrav,
    val ytelsestype: Ytelsestype,
    val eksternFagsakId: String,
    val eksternBehandlingId: String,
    val antallPerioder: Int,
    val under4rettsgebyr: Boolean,
    val muligforeldelse: Boolean,
    val periodeLengde: Int,
    val personIdent: String,
    val enhetId: String,
    val skattProsent: BigDecimal,
    val sumFeilutbetaling: BigDecimal,
    val medJustering: Boolean
) {

    // TODO: Vil trenge å kunne sette kontrollfelt tilbake i tid for at den plukkes av auto-opprett batch
    private val finalKontrollfelt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS"))
    private val FIRE_RETTSGEBYR = BigDecimal(4796)

    fun lag(): DetaljertKravgrunnlagMelding {
        return DetaljertKravgrunnlagMelding().also { detaljertKravgrunnlagMelding ->
            detaljertKravgrunnlagMelding.detaljertKravgrunnlag = DetaljertKravgrunnlagDto().apply {
                this.kravgrunnlagId = Random.nextInt(100000, 999999).toBigInteger()
                this.vedtakId = Random.nextInt(100000, 999999).toBigInteger()
                this.kodeStatusKrav = status.toString()
                this.kodeFagomraade = utledFagområdeKode(ytelsestype = ytelsestype)
                this.fagsystemId = eksternFagsakId
                this.vedtakIdOmgjort = BigInteger.ZERO
                this.vedtakGjelderId = personIdent
                this.typeGjelderId = TypeGjelderDto.PERSON
                this.utbetalesTilId = personIdent
                this.typeUtbetId = TypeGjelderDto.PERSON
                this.enhetAnsvarlig = enhetId
                this.enhetBosted = enhetId
                this.enhetBehandl = enhetId
                this.kontrollfelt = finalKontrollfelt
                this.saksbehId = "VL"
                this.referanse = eksternBehandlingId
                this.tilbakekrevingsPeriode.addAll(
                    utledTilbakekrevingsPerioder(
                        antallPerioder = antallPerioder,
                        under4rettsgebyr = under4rettsgebyr,
                        muligForeldelse = muligforeldelse,
                        ytelsestype = ytelsestype,
                        periodelengde = periodeLengde,
                        skattProsent = skattProsent,
                        sumFeilutbetaling = sumFeilutbetaling,
                        medJustering = medJustering
                    )
                )
            }
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

    private fun utledTilbakekrevingsPerioder(
        antallPerioder: Int,
        under4rettsgebyr: Boolean,
        muligForeldelse: Boolean,
        ytelsestype: Ytelsestype,
        periodelengde: Int,
        skattProsent: BigDecimal,
        sumFeilutbetaling: BigDecimal,
        medJustering: Boolean
    ): List<DetaljertKravgrunnlagPeriodeDto> {
        // Finner startdato for første periode, hvor periodene har 1 måned mellomrom
        val antallMånederTilbake = antallPerioder * periodelengde + antallPerioder - 1
        var startdato = LocalDate.now()
            .minusMonths(antallMånederTilbake.toLong())
            .withDayOfMonth(1)

        // Første periode må starte minst 3 år tilbake i tid dersom det skal være mulig foreldelse
        if (muligForeldelse) {
            startdato = minOf(startdato, LocalDate.now().minusYears(3L).withDayOfMonth(1))
        }

        // Setter feilutbetalt beløp til under 4.796 kr dersom det skal være under4rettsgebyr
        val feilutbetaltBeløp = if (under4rettsgebyr) {
            FIRE_RETTSGEBYR - BigDecimal(100)
        } else {
            sumFeilutbetaling
        }

        val beløpPrMåned = feilutbetaltBeløp
            .divide(BigDecimal(antallPerioder).multiply(BigDecimal(periodelengde)), RoundingMode.DOWN)

        // Finner skattbeløp
        val skattIProsent = when (ytelsestype) {
            Ytelsestype.BARNETRYGD -> BigDecimal.ZERO.setScale(2)
            else -> skattProsent
        }

        val tilbakekrevingsperiodeList: MutableList<DetaljertKravgrunnlagPeriodeDto> = mutableListOf()
        for (i in 1..antallPerioder) {
            for (j in 1..periodelengde) {
                val kravgrunnlagPeriode = DetaljertKravgrunnlagPeriodeDto().apply {
                    periode = PeriodeDto()
                    periode.fom = startdato
                    periode.tom = startdato.withDayOfMonth(startdato.lengthOfMonth())
                    belopSkattMnd = beløpPrMåned.multiply(skattIProsent)
                        .divide(BigDecimal(100)).setScale(0, RoundingMode.DOWN)
                    tilbakekrevingsBelop.addAll(utledTilbakekrevingsbelop(beløpPrMåned, ytelsestype, skattIProsent, medJustering))
                }

                tilbakekrevingsperiodeList.add(kravgrunnlagPeriode)

                startdato = startdato.plusMonths(1)
            }
            startdato = startdato.plusMonths(1)
        }
        return tilbakekrevingsperiodeList
    }

    private fun utledTilbakekrevingsbelop(
        beløpPrMnd: BigDecimal,
        ytelsestype: Ytelsestype,
        skattIProsent: BigDecimal,
        medJustering: Boolean = false
    ): Collection<DetaljertKravgrunnlagBelopDto> {
        val ytelKodeKlasse: KodeKlasse
        val feilKodeKlasse: KodeKlasse
        val justKodeKlasse: KodeKlasse
        when (ytelsestype) {
            Ytelsestype.BARNETRYGD -> {
                ytelKodeKlasse = KodeKlasse.BATR
                feilKodeKlasse = KodeKlasse.KL_KODE_FEIL_BA
                justKodeKlasse = KodeKlasse.KL_KODE_JUST_BA
            }
            Ytelsestype.KONTANTSTØTTE -> {
                throw Exception("Kontantstøtte ikke implementert enda")
            }
            Ytelsestype.OVERGANGSSTØNAD -> {
                ytelKodeKlasse = KodeKlasse.EFOG
                feilKodeKlasse = KodeKlasse.KL_KODE_FEIL_EFOG
                justKodeKlasse = KodeKlasse.KL_KODE_JUST_EFOG
            }
            Ytelsestype.SKOLEPENGER -> {
                ytelKodeKlasse = KodeKlasse.EFSP
                feilKodeKlasse = KodeKlasse.KL_KODE_FEIL_PEN
                justKodeKlasse = KodeKlasse.KL_KODE_JUST_PEN
            }
            Ytelsestype.BARNETILSYN -> {
                ytelKodeKlasse = KodeKlasse.EFBT
                feilKodeKlasse = KodeKlasse.KL_KODE_FEIL_PEN
                justKodeKlasse = KodeKlasse.KL_KODE_JUST_PEN
            }
        }
        val tilbakekrevingsBelopList: MutableList<DetaljertKravgrunnlagBelopDto> = mutableListOf()
        val tilbakekrevingsbelopYtel = DetaljertKravgrunnlagBelopDto().apply {
            kodeKlasse = ytelKodeKlasse.name
            typeKlasse = TypeKlasseDto.YTEL
            belopOpprUtbet = beløpPrMnd.setScale(2)
            belopNy = BigDecimal.ZERO.setScale(2)
            belopTilbakekreves = beløpPrMnd.setScale(2)
            belopUinnkrevd = BigDecimal.ZERO.setScale(2)
            skattProsent = skattIProsent.setScale(2)
        }
        tilbakekrevingsBelopList.add(tilbakekrevingsbelopYtel)

        val tilbakekrevingsbelopFeil = DetaljertKravgrunnlagBelopDto().apply {
            kodeKlasse = feilKodeKlasse.name
            typeKlasse = TypeKlasseDto.FEIL
            belopOpprUtbet = BigDecimal.ZERO.setScale(2)
            belopNy = beløpPrMnd.setScale(2)
            belopTilbakekreves = BigDecimal.ZERO.setScale(2)
            belopUinnkrevd = BigDecimal.ZERO.setScale(2)
            skattProsent = skattIProsent.setScale(2)
        }
        tilbakekrevingsBelopList.add(tilbakekrevingsbelopFeil)
        if (medJustering) {
            val tilbakekrevingsbelopJust = DetaljertKravgrunnlagBelopDto().apply {
                kodeKlasse = justKodeKlasse.name
                typeKlasse = TypeKlasseDto.JUST
                belopOpprUtbet = BigDecimal.ZERO.setScale(2)
                belopNy = BigDecimal(100).setScale(2)
                belopTilbakekreves = BigDecimal.ZERO.setScale(2)
                belopUinnkrevd = BigDecimal.ZERO.setScale(2)
                skattProsent = BigDecimal.ZERO.setScale(2)
            }
            tilbakekrevingsBelopList.add(tilbakekrevingsbelopJust)
        }

        return tilbakekrevingsBelopList
    }
}
