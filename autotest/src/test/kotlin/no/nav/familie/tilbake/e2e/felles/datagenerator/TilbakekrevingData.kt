package no.nav.familie.tilbake.e2e.felles.datagenerator

import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingstype
import no.nav.familie.kontrakter.felles.tilbakekreving.Faktainfo
import no.nav.familie.kontrakter.felles.tilbakekreving.Institusjon
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import no.nav.familie.kontrakter.felles.tilbakekreving.Varsel
import no.nav.familie.kontrakter.felles.tilbakekreving.Verge
import no.nav.familie.kontrakter.felles.tilbakekreving.Vergetype
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

class TilbakekrevingData(
    val eksternFagsakId: String,
    val eksternBehandlingId: String? = null,
    val fagsystem: Fagsystem,
    val ytelsestype: Ytelsestype,
    val personIdent: String,
    val enhetId: String,
    val enhetsnavn: String,
    val varsel: Boolean,
    val verge: Boolean,
    val sumFeilutbetaling: BigDecimal? = null,
    val saksbehandlerIdent: String,
    val institusjon: Boolean
) {

    fun lag(): OpprettTilbakekrevingRequest {
        return OpprettTilbakekrevingRequest(
            fagsystem = fagsystem,
            ytelsestype = ytelsestype,
            eksternFagsakId = eksternFagsakId,
            eksternId = eksternBehandlingId ?: Random.nextInt(1000000, 9999999).toString(),
            personIdent = personIdent,
            saksbehandlerIdent = saksbehandlerIdent,
            behandlingstype = Behandlingstype.TILBAKEKREVING,
            språkkode = Språkkode.NB,
            enhetId = enhetId,
            enhetsnavn = enhetsnavn,
            revurderingsvedtaksdato = LocalDate.now().minusDays(35),
            faktainfo = Faktainfo(
                revurderingsårsak = "Nye opplysninger",
                revurderingsresultat = "Endring i ytelsen",
                tilbakekrevingsvalg = utledTilbakekrevingsvalg(varsel),
                konsekvensForYtelser = setOf(
                    "Reduksjon av ytelsen",
                    "Feilutbetaling"
                )
            ),
            varsel = utledVarsel(varsel, sumFeilutbetaling),
            manueltOpprettet = false,
            verge = utledVerge(verge),
            institusjon = utledInstitusjon(institusjon)
        )
    }

    private fun utledTilbakekrevingsvalg(varsel: Boolean): Tilbakekrevingsvalg {
        return if (varsel) {
            Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_MED_VARSEL
        } else {
            Tilbakekrevingsvalg.OPPRETT_TILBAKEKREVING_UTEN_VARSEL
        }
    }

    private fun utledVarsel(varsel: Boolean, sumFeilutbetaling: BigDecimal?): Varsel? {
        return if (varsel) {
            Varsel(
                varseltekst = "Automatisk varseltekst fra Autotest",
                sumFeilutbetaling = sumFeilutbetaling ?: BigDecimal(8124),
                perioder = arrayListOf(
                    Periode(
                        fom = LocalDate.now().minusMonths(5).withDayOfMonth(1),
                        tom = LocalDate.now()
                            .minusMonths(5)
                            .withDayOfMonth(LocalDate.now().minusMonths(5).lengthOfMonth())
                    ),
                    Periode(
                        fom = LocalDate.now().minusMonths(3).withDayOfMonth(1),
                        tom = LocalDate.now()
                            .minusMonths(3)
                            .withDayOfMonth(LocalDate.now().minusMonths(3).lengthOfMonth())
                    )
                )
            )
        } else {
            null
        }
    }

    private fun utledVerge(verge: Boolean): Verge? {
        return if (verge) {
            Verge(vergetype = Vergetype.ADVOKAT, navn = "Jens Pettersen", organisasjonsnummer = "987654321")
        } else {
            null
        }
    }

    private fun utledInstitusjon(institusjon: Boolean): Institusjon? {
        return if (institusjon) {
            Institusjon(organisasjonsnummer = "987654321", navn = "Testinstitusjon")
        } else {
            null
        }
    }
}
