package no.nav.familie.tilbake.e2e.autotest

import no.nav.familie.tilbake.e2e.klienter.FamilieTilbakeKlient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SjekkVersionTest {
    @Autowired
    lateinit var familieTilbakeKlient: FamilieTilbakeKlient

    @Test
    fun `hentVersjonInfo skal hente versjon og dermed teste om familie-tilbake er oppe`() {
        familieTilbakeKlient.hentVersjonInfo()
    }
}
