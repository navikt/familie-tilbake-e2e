#!/bin/bash
TILBAKE_CLIENT_SECRET=$(kubectl -n teamfamilie get secret azuread-familie-tilbake-frontend-lokal -o json | jq '.data | map_values(@base64d)' | jq -r '.AZURE_APP_CLIENT_SECRET')
export TILBAKE_CLIENT_SECRET