package no.nav.familie.tilbake.e2e.domene

import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

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
enum class Venteårsak {
    VENT_PÅ_BRUKERTILBAKEMELDING,
    VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
    AVVENTER_DOKUMENTASJON,
    UTVIDET_TILSVAR_FRIST,
    ENDRE_TILKJENT_YTELSE,
    VENT_PÅ_MULIG_MOTREGNING
}

enum class Behandlingssteg {
    VARSEL,
    GRUNNLAG,
    VERGE,
    FAKTA,
    FORELDELSE,
    VILKÅRSVURDERING,
    FORESLÅ_VEDTAK,
    FATTE_VEDTAK,
    IVERKSETT_VEDTAK,
    AVSLUTTET;
}

enum class Behandlingsstegstatus {
    STARTET,
    VENTER,
    KLAR,
    UTFØRT,
    AUTOUTFØRT,
    TILBAKEFØRT,
    AVBRUTT;
}

data class Behandling(val eksternBrukId: UUID,
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
                 val harVerge: Boolean,
                 val behandlingsstegsinfo: Set<Behandlingsstegsinfo>)

data class Behandlingsstegsinfo(val behandlingssteg: Behandlingssteg,
                                val behandlingsstegstatus: Behandlingsstegstatus,
                                val venteårsak: Venteårsak?,
                                val tidsfrist: DateFormat
)
