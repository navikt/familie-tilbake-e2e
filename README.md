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

## Opprett testdata for lokal utvikling
- Kjør opp `familie-tilbake`, `familie-tilbake-frontend` og `familie-historikk`
- Sett nødvendig miljøvariabel (Krever at du er pålogget naisdevice og gcloud) `export TILBAKE_CLIENT_SECRET=$(kubectl -n teamfamilie get secret azuread-familie-tilbake-frontend-lokal -o json | jq '.data | map_values(@base64d)' | jq -r '.AZURE_APP_CLIENT_SECRET')`
- Kjør tester `mvn -e -Dspring.profiles.active=local test`. 
  - Evt så kan du slenge på hvilken test du vil kjøre:
  - Eksempel BA: `mvn -e -Dspring.profiles.active=local test -Dtest='OpprettTilbakekrevingBATest#Oppretter uavsluttet delvis tilbakekreving for bruk ved utvikling'`
  - Eksempel EF: `mvn -e -Dspring.profiles.active=local test -Dtest='OpprettTilbakekrevingOsTest.Opprett dummy-test-data lokalt'`
- Saken er nå tilgjengelig på `http://localhost:8000/fagsystem/<system>/fagsak/<eksternFagsakId>/behandling/<eksternBrukId>/vilkaarsvurdering`
- eksternFagsakid og eksternBrukId finner du i loggen for testkjøringa.