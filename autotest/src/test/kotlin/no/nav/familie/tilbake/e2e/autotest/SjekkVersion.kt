package no.nav.familie.tilbake.e2e.autotest

import net.bytebuddy.implementation.bytecode.Throw
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.tilbake.e2e.klient.FamilieTilbakeKlient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [ApplicationConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SjekkVersion(@Autowired private val familieTilbakeKlient: FamilieTilbakeKlient) {

    @Test
    fun `Skal hente versjon og dermed teste om familie-tilbake er oppe`() {
        val respons = familieTilbakeKlient.hentVersjonInfo()
        assertTrue(respons.status == Ressurs.Status.SUKSESS, "Versjoninfo skulle ha blitt hentet!")
    }
}
