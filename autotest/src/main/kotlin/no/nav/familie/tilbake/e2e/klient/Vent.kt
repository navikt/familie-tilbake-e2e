package no.nav.familie.tilbake.e2e.klient

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.function.Supplier

object Vent {

    fun til(supplier: Supplier<Boolean?>, timeoutInSeconds: Int, failReason: String?) {
        til(supplier, timeoutInSeconds) { failReason }
    }

    private fun til(supplier: Supplier<Boolean?>, timeoutInSeconds: Int, errorMessageProducer: Supplier<String?>) {
        val start: LocalDateTime = LocalDateTime.now()
        val end: LocalDateTime = start.plusSeconds(timeoutInSeconds.toLong())
        while (!supplier.get()!!) {
            if (LocalDateTime.now().isAfter(end)) {
                throw RuntimeException("Async venting timet ut etter $timeoutInSeconds sekunder fordi: ${errorMessageProducer.get()}")
            }
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                throw RuntimeException("Async venting interrupted ut etter ${ChronoUnit.SECONDS.between(start, LocalDateTime.now())}" +
                                       " sekunder fordi: ${errorMessageProducer.get()}", e)
            }
        }
    }
}
