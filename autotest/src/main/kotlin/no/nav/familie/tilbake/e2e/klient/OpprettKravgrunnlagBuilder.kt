package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.KodeKlasse
import no.nav.familie.tilbake.e2e.domene.KodeStatusKrav
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagBelopDto
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagMelding
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagPeriodeDto
import no.nav.tilbakekreving.status.v1.EndringKravOgVedtakstatus
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsbelopDto
import no.nav.tilbakekreving.typer.v1.PeriodeDto
import no.nav.tilbakekreving.typer.v1.TypeGjelderDto
import no.nav.tilbakekreving.typer.v1.TypeKlasseDto
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
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
        kravgrunnlagId: BigInteger? = null,
        vedtakId: BigInteger? = null,
        @Max(6)
        antallPerioder: Int,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean,
    ): DetaljertKravgrunnlagMelding {
        val finalKravgrunnlagId = kravgrunnlagId ?: Random.nextInt(100000, 999999).toBigInteger()
        val finalVedtakId = vedtakId ?: Random.nextInt(100000, 999999).toBigInteger()
        val finalKontrollfelt = LocalDate.now().toString() + "-12-00-00-000000"
        // Kommer til å trenge å kunne sette kontrollfelt tilbake i tid for at den plukkes av auto-opprett batch (ikke laget enda)

        val response = DetaljertKravgrunnlagMelding()
        response.detaljertKravgrunnlag = DetaljertKravgrunnlagDto()
        response.detaljertKravgrunnlag.kravgrunnlagId = finalKravgrunnlagId
        response.detaljertKravgrunnlag.vedtakId = finalVedtakId
        response.detaljertKravgrunnlag.kodeStatusKrav = status.toString()
        response.detaljertKravgrunnlag.kodeFagomraade = fagområde.toString()
        response.detaljertKravgrunnlag.fagsystemId = eksternFagsakId
        response.detaljertKravgrunnlag.vedtakIdOmgjort = BigInteger.ZERO
        response.detaljertKravgrunnlag.vedtakGjelderId = "12345678901"
        response.detaljertKravgrunnlag.typeGjelderId = TypeGjelderDto.PERSON
        response.detaljertKravgrunnlag.utbetalesTilId = "12345678901"
        response.detaljertKravgrunnlag.typeUtbetId = TypeGjelderDto.PERSON
        response.detaljertKravgrunnlag.enhetAnsvarlig = "8020"
        response.detaljertKravgrunnlag.enhetBosted = "8020"
        response.detaljertKravgrunnlag.enhetBehandl = "8020"
        response.detaljertKravgrunnlag.kontrollfelt = finalKontrollfelt
        response.detaljertKravgrunnlag.saksbehId = "K231B433"
        response.detaljertKravgrunnlag.referanse = eksternBehandlingId
        response.detaljertKravgrunnlag.tilbakekrevingsPeriode.addAll(tilbakekrevingsPerioder(
            antallPerioder,
            under4rettsgebyr,
            muligforeldelse,
            ytelsestype
        )
        )
        return response
    }

    fun opprettStatusmelding(
        vedtakId: BigInteger,
        kodeStatusKrav: KodeStatusKrav,
        fagområde: Fagsystem,
        eksternFagsakId: String,
        eksternBehandlingId: String
    ): EndringKravOgVedtakstatus {
        val response = EndringKravOgVedtakstatus()
        response.kravOgVedtakstatus = KravOgVedtakstatus()
        response.kravOgVedtakstatus.vedtakId = vedtakId
        response.kravOgVedtakstatus.kodeStatusKrav = kodeStatusKrav.toString()
        response.kravOgVedtakstatus.kodeFagomraade = fagområde.toString()
        response.kravOgVedtakstatus.fagsystemId = eksternFagsakId
        response.kravOgVedtakstatus.vedtakGjelderId = "12345678901"
        response.kravOgVedtakstatus.typeGjelderId = TypeGjelderDto.PERSON
        response.kravOgVedtakstatus.referanse = eksternBehandlingId
        return response
    }

    private fun tilbakekrevingsPerioder(
        antallPerioder: Int,
        under4rettsgebyr: Boolean,
        muligforeldelse: Boolean,
        ytelsestype: Ytelsestype,
    ): List<DetaljertKravgrunnlagPeriodeDto> {
        /*Lager alltid periodene 3 måneder lange med 1 måned mellom, så derfor gange med 4.
        Første periode starter for 3 år siden når det skal være muligforeldelse eller for 4 måneder siden gange med antall perioder*/
        var startDato = if (muligforeldelse) {
            LocalDate.now().minusYears(3).withDayOfMonth(1)
        } else {
            LocalDate.now().minusMonths(BigDecimal(4).multiply(BigDecimal(antallPerioder)).toLong()).withDayOfMonth(1)
        }

        /*Setter feilutbetalt beløp til 20000 kr, med mindre det skal være under4rettsgebyr, da skal det være under 4796 kr pr jan.2021*/
        val beløpprmåned = if (under4rettsgebyr) {
            BigDecimal(4500).divide(BigDecimal(antallPerioder).multiply(BigDecimal(3)), 2, RoundingMode.HALF_DOWN)
        } else {
            BigDecimal(20000).divide(BigDecimal(antallPerioder).multiply(BigDecimal(3)), 2, RoundingMode.HALF_DOWN)
        }

        val tilbakekrevingsperiodeList: MutableList<DetaljertKravgrunnlagPeriodeDto> = mutableListOf()
        for (i in 1..antallPerioder) {
            for (j in 1..3) {
                val kravgrunnlagPeriode = DetaljertKravgrunnlagPeriodeDto()
                kravgrunnlagPeriode.periode = PeriodeDto()
                kravgrunnlagPeriode.periode.fom = startDato
                kravgrunnlagPeriode.periode.tom = startDato.withDayOfMonth(startDato.lengthOfMonth())
                kravgrunnlagPeriode.belopSkattMnd = BigDecimal(BigInteger.ZERO, 2)
                kravgrunnlagPeriode.tilbakekrevingsBelop.addAll(tilbakekrevingsBelopGenerator(beløpprmåned, ytelsestype))
                tilbakekrevingsperiodeList.add(kravgrunnlagPeriode)

                startDato = startDato.plusMonths(1)
            }
            startDato = startDato.plusMonths(1)
        }
        return tilbakekrevingsperiodeList
    }

    private fun tilbakekrevingsBelopGenerator(
        beløpprmåned: BigDecimal,
        ytelsestype: Ytelsestype,
    ): Collection<DetaljertKravgrunnlagBelopDto> {
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
        val tilbakekrevingsBelopList: MutableList<DetaljertKravgrunnlagBelopDto> = mutableListOf()
        val tilbakekrevingsbelopYtel = DetaljertKravgrunnlagBelopDto()
        tilbakekrevingsbelopYtel.kodeKlasse = ytelKodeKlasse.toString()
        tilbakekrevingsbelopYtel.typeKlasse = TypeKlasseDto.YTEL
        tilbakekrevingsbelopYtel.belopOpprUtbet = beløpprmåned
        tilbakekrevingsbelopYtel.belopNy = BigDecimal(BigInteger.ZERO, 2)
        tilbakekrevingsbelopYtel.belopTilbakekreves = beløpprmåned
        tilbakekrevingsbelopYtel.belopUinnkrevd = BigDecimal(BigInteger.ZERO, 2)
        tilbakekrevingsbelopYtel.skattProsent = BigDecimal(BigInteger.ZERO, 2)
        tilbakekrevingsBelopList.add(tilbakekrevingsbelopYtel)

        val tilbakekrevingsbelopFeil = DetaljertKravgrunnlagBelopDto()
        tilbakekrevingsbelopFeil.kodeKlasse = feilKodeKlasse.toString()
        tilbakekrevingsbelopFeil.typeKlasse = TypeKlasseDto.FEIL
        tilbakekrevingsbelopFeil.belopOpprUtbet = BigDecimal(BigInteger.ZERO, 2)
        tilbakekrevingsbelopFeil.belopNy = beløpprmåned
        tilbakekrevingsbelopFeil.belopTilbakekreves = BigDecimal(BigInteger.ZERO, 2)
        tilbakekrevingsbelopFeil.belopUinnkrevd = BigDecimal(BigInteger.ZERO, 2)
        tilbakekrevingsbelopFeil.skattProsent = BigDecimal(BigInteger.ZERO, 2)
        tilbakekrevingsBelopList.add(tilbakekrevingsbelopFeil)

        return tilbakekrevingsBelopList
    }
}
