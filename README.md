# familie-tilbake-e2e
Autotester for familie-tilbake.

Her finner man ende-til-endetester (/autotest) og mijøet man kjører de med (/e2e).

Testriggen kan kjøre på ekstern server og lokalt. Lokalt kan man erstatte ett eller flere docker-images manuelt (/e2e/docker-compose.yml), slik at man kan teste applikasjonen i sammenheng med de andre applikasjonene. Man kan også koble seg til en eller flere applikasjoner via IntelliJ, for å kjøre debugging med breakpoints (se egen seksjon nedenfor).

## Kjøre tester

### Tips
For effektiv utvikling kan disse kommandoene være nyttige:

* For mer effektivt bygg: mvn verify -DskipTests
* For å hente informasjon om docker containerne som kjører: docker ps
* For logger fra de ulike appene: docker logs <docker-id> -f

## Oppsett lokalt utviklingsmiljø

Start familie-tilbake i IntelliJ som vanlig med CLIENT_ID og FAMILIE_TILBAKE_FRONTEND_CLIENT_ID for azuread familie-tilbake-lokal og familie-tilbake-frontend-lokal.

Kjør testene med spring-profil "-Dspring.profiles.active=local". Hent TILBAKE_CLIENT_SECRET frå .env for familie-tilbake-frontend og legg det inn i Environment-variables for testen.

Løsninga er nå tilgjengelig på http://localhost:8000/fagsystem/<system>/fagsak/<eksternFagsakId>/behandling/<eksternBrukId>/vilkaarsvurdering

eksternFagsakid og eksternBrukId finner du i loggen for testkjøringa.

## Opprett testdata for lokal utvikling
- Kjør opp `familie-tilbake`, `familie-tilbake-frontend` og `familie-historikk`
- Kjør testen `OpprettTilbakekrevingOsTest.Opprett dummy-test-data lokalt`
- Den kommer til å feile fordi du må ha følgende satt:
  - `-Dspring.profiles.active=local` i run-config 
  - miljøvariabelen `TILBAKE_CLIENT_SECRET` satt (dette er secret som brukes av `familie-tilbake-frontend`, ikke backend-applikasjonen for å late som at testene kjører i kontekst av frontend)

Eksempel: 

![dummy-data](https://user-images.githubusercontent.com/402915/204290705-75996d85-6967-427f-80f7-e3d6532527c7.png)
