package no.nav.familie.tilbake.e2e.klient

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.function.Supplier

object Vent {

    fun til(supplier: Supplier<Boolean?>, timeoutInSeconds: Int, failReason: String?) {
        til(supplier, timeoutInSeconds, { failReason })
    }

    private fun til(supplier: Supplier<Boolean?>, timeoutInSeconds: Int, errorMessageProducer: Supplier<String?>) {
        val start: LocalDateTime = LocalDateTime.now()
        val end: LocalDateTime = start.plusSeconds(timeoutInSeconds.toLong())
        while (!supplier.get()!!) {
            if (LocalDateTime.now().isAfter(end)) {
                throw RuntimeException(
                    String.format(
                        "Async venting timet ut etter %s sekunder fordi: %s",
                        timeoutInSeconds, errorMessageProducer.get()
                    )
                )
            }
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                throw RuntimeException(
                    String.format(
                        "Async venting interrupted ut etter %s sekunder fordi: %s",
                        ChronoUnit.SECONDS.between(start, LocalDateTime.now()), errorMessageProducer.get()
                    ),
                    e
                )
            }
        }
    }
}
