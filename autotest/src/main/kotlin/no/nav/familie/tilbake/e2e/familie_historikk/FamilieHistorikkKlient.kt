package no.nav.familie.tilbake.e2e.familie_historikk

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.tilbake.e2e.familie_historikk.dto.HistorikkinnslagDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class FamilieHistorikkKlient(@Value("\${FAMILIE_HISTORIKK_API_URL}") private val familieHistorikkApiUrl: String,
                             @Qualifier("azure") private val restOperations: RestOperations)
    : AbstractRestClient(restOperations, "familie-historikk") {

        fun hentHistorikkinnslag(applikasjon: String, behandlingId: String): Ressurs<List<HistorikkinnslagDto>> {
            val uri = URI.create("$familieHistorikkApiUrl/api/historikk/applikasjon/$applikasjon/behandling/$behandlingId")

            return getForEntity(uri)
        }
}
