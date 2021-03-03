package no.nav.familie.tilbake.e2e.domene

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

enum class Behandlingsstatus {
    AVSLUTTET,
    FATTER_VEDTAK,
    IVERKSETTER_VEDTAK,
    OPPRETTET,
    UTREDES
}

enum class Behandlingstype {
    TILBAKEKREVING,
    REVURDERING_TILBAKEKREVING
}

enum class Behandlingsresultatstype(val navn: String) {
    IKKE_FASTSATT("Ikke fastsatt"),
    HENLAGT_FEILOPPRETTET("Henlagt, søknaden er feilopprettet"),
    HENLAGT_FEILOPPRETTET_MED_BREV("Feilaktig opprettet - med henleggelsesbrev"),
    HENLAGT_FEILOPPRETTET_UTEN_BREV("Feilaktig opprettet - uten henleggelsesbrev"),
    HENLAGT_KRAVGRUNNLAG_NULLSTILT("Kravgrunnlaget er nullstilt"),
    HENLAGT_TEKNISK_VEDLIKEHOLD("Teknisk vedlikehold"),
    HENLAGT("Henlagt"),  // kun brukes i frontend
    INGEN_TILBAKEBETALING("Ingen tilbakebetaling"),
    DELVIS_TILBAKEBETALING("Delvis tilbakebetaling"),
    FULL_TILBAKEBETALING("Tilbakebetaling");
}

class Behandling(val eksternBrukId: UUID,
                 val behandlingId: UUID,
                 val erBehandlingHenlagt: Boolean,
                 val type: Behandlingstype,
                 val status: Behandlingsstatus,
                 val opprettetDato: LocalDate,
                 val avsluttetDato: LocalDate? = null,
                 val endretTidspunkt: LocalDateTime,
                 val vedtaksdato: LocalDate? = null,
                 val enhetskode: String,
                 val enhetsnavn: String,
                 val resultatstype: Behandlingsresultatstype? = null,
                 val ansvarligSaksbehandler: String,
                 val ansvarligBeslutter: String? = null,
                 val erBehandlingPåVent: Boolean,
                 val kanHenleggeBehandling: Boolean,
                 val harVerge: Boolean) {
}
