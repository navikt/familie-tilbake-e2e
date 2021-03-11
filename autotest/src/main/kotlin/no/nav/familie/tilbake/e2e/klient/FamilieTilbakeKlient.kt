package no.nav.familie.tilbake.e2e.klient

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.tilbakekreving.Behandlingstype
import no.nav.familie.kontrakter.felles.tilbakekreving.Fagsystem
import no.nav.familie.kontrakter.felles.tilbakekreving.Faktainfo
import no.nav.familie.kontrakter.felles.tilbakekreving.OpprettTilbakekrevingRequest
import no.nav.familie.kontrakter.felles.tilbakekreving.Språkkode
import no.nav.familie.kontrakter.felles.tilbakekreving.Varsel
import no.nav.familie.kontrakter.felles.tilbakekreving.Ytelsestype
import no.nav.familie.tilbake.e2e.domene.Behandling
import no.nav.familie.tilbake.e2e.domene.Fagsak
import no.nav.familie.tilbake.e2e.domene.VersjonInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI
import java.time.LocalDate

@Service
class FamilieTilbakeKlient(@Value("\${FAMILIE_TILBAKE_API_URL}") private val familieTilbakeApiUrl: String,
                           private val restOperations: RestOperations) : AbstractRestClient(restOperations,
                                                                                            "familie-tilbake") {

    fun hentVersjonInfo(): Ressurs<VersjonInfo> {
        val uri = URI.create("$familieTilbakeApiUrl/api/info")

        return getForEntity(uri)
    }

    fun opprettTilbakekreving(
            fagsystem: Fagsystem,
            ytelsestype: Ytelsestype,
            eksternFagsakId: String,
            personIdent: String,
            eksternId: String,
            behandlingstype: Behandlingstype? = Behandlingstype.TILBAKEKREVING,
            språkkode: Språkkode = Språkkode.NB,
            varsel: Varsel,
            faktainfo: Faktainfo,
            revurderingsvedtaksdato: LocalDate,
    ): Ressurs<String> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/v1")

        val request = OpprettTilbakekrevingRequest(fagsystem = fagsystem,
                                                   ytelsestype = ytelsestype,
                                                   eksternFagsakId = eksternFagsakId,
                                                   personIdent = personIdent,
                                                   eksternId = eksternId,
                                                   behandlingstype = behandlingstype,
                                                   manueltOpprettet = false,
                                                   språkkode = språkkode,
                                                   enhetId = "",
                                                   enhetsnavn = "",
                                                   varsel = varsel,
                                                   revurderingsvedtaksdato = revurderingsvedtaksdato,
                                                   faktainfo = faktainfo)
        return postForEntity(uri, request)!!
    }

    fun hentFagsak(fagsystem: Fagsystem, eksternFagsakId: String): Ressurs<Fagsak> {
        val uri = URI.create("$familieTilbakeApiUrl/api/fagsak/v1?fagsystem=$fagsystem&fagsak=$eksternFagsakId")

        return getForEntity(uri)
    }

    fun hentBehandling(behandlingId: String): Ressurs<Behandling> {
        val uri = URI.create("$familieTilbakeApiUrl/api/behandling/v1/$behandlingId")

        return getForEntity(uri)
    }
}
