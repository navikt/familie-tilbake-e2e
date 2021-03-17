# familie-tilbake-e2e
Autotester for familie-tilbake.

Her finner man ende-til-endetester (/autotest) og mijøet man kjører de med (/e2e).

Testriggen kan kjøre på ekstern server og lokalt. Lokalt kan man erstatte ett eller flere docker-images manuelt (/e2e/docker-compose.yml), slik at man kan teste applikasjonen i sammenheng med de andre applikasjonene. Man kan også koble seg til en eller flere applikasjoner via IntelliJ, for å kjøre debugging med breakpoints (se egen seksjon nedenfor).

## Kjøre tester

1. Legg inn secrets i e2e/.env. Hentes fra vault prod-fss/familie/default/familie-tilbake-e2e-env.
2. I /e2e: Kjør opp miljøet med kommandoen: docker-compose up -d
3. I /autotest: Kjør ønskede tester

###Tips
For effektiv utvikling kan disse kommandoene være nyttige:

* For mer effektivt bygg: mvn clean install -Dmaven.test.skip=true
* For å hente informasjon om docker containerne som kjører: docker ps
* For logger fra de ulike appene: docker logs <docker-id> -f

## Oppsett lokalt utviklingsmiljø

Start familie-tilbake i IntelliJ som vanlig med CLIENT_ID og FAMILIE_TILBAKE_FRONTEND_CLIENT_ID for azuread familie-tilbake-lokal og familie-tilbake-frontend-lokal.

Kjør testene med spring-profil "-Dspring.profiles.active=local". Hent TILBAKE_CLIENT_SECRET frå vault prod-fss/familie/default/familie-tilbake-e2e-env og legg det inn i Environment-variables for testen.


