#!/usr/bin/env bash

set -euo pipefail

BASE_URL="http://localhost:8080/api/v1"
API_KEY="bvpelt"
TOKEN_FILE="${PWD}/.timeseries_token"

# ─── HELPER ──────────────────────────────────────────────────────────────────

run() {
    echo "" >&2
    echo ">>> $*" >&2
    echo "" >&2
    "$@"
    echo "" >&2
}

save_token() {
    local token=$1
    echo "${token}" > "${TOKEN_FILE}"
    chmod 600 "${TOKEN_FILE}"           # readable only by current user
    echo "Token saved to ${TOKEN_FILE}" >&2
}

load_token() {
    if [ ! -f "${TOKEN_FILE}" ]; then
        echo "ERROR: no token file found — run login or authenticate first" >&2
        exit 1
    fi
    TOKEN=$(cat "${TOKEN_FILE}")
    if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
        echo "ERROR: token file is empty or invalid — run login again" >&2
        exit 1
    fi
}

clear_token() {
    rm -f "${TOKEN_FILE}"
    echo "Token cleared" >&2
}

# ─── REQUIRE TOKEN GUARD ─────────────────────────────────────────────────────

require_token() {
    load_token
}

# ─── AUTH ────────────────────────────────────────────────────────────────────

curl -X 'POST' \
  'http://localhost:8080/api/v1/login/login/user' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "user",
  "password": "kasjdkfkei",
  "email": "mymail@gmail.com",
  "phone": "+31645978573"
}'

login() {
    echo "=== LOGIN ==="
    run curl -s -X POST "${BASE_URL}/login/login/user" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "bvpelt",
            "password": "12345",
            "email": "bvpelt@gmail.com"
        }' | jq .

    TOKEN=$(curl -s -X POST "${BASE_URL}/login/login/user" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: ${API_KEY}" \
        -d '{
            "username": "bvpelt",
            "password": "12345",
            "email": "bvpelt@gmail.com"
        }' | jq -r '.token')

    if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
        echo "ERROR: login failed — no token in response" >&2
        exit 1
    fi

    save_token "$TOKEN"

    echo "Token saved: ${TOKEN}" >&2
}

authenticate() {
    echo "=== AUTHENTICATE ==="
    run curl -s -X POST "${BASE_URL}/login/authenticate" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: ${API_KEY}" \
        -d '{
            "username": "bvpelt",
            "password": "12345",
            "email": "bvpelt@gmail.com"
        }' | jq .
}

register() {
    echo "=== REGISTER ==="
    run curl -s -X POST "${BASE_URL}/login/register" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: ${API_KEY}" \
        -d '{
            "username": "newuser",
            "password": "secret",
            "email": "newuser@example.com"
        }' | jq .
}

# ─── ADDRESSES ───────────────────────────────────────────────────────────────

list_addresses() {
    echo "=== LIST ADDRESSES ==="
    run curl -s -X GET "${BASE_URL}/timeseries/addresses?page=0&size=20" \
        -H "accept: application/json" \
        -H "X-API-Key: ${API_KEY}" | jq .
}

create_address() {
    echo "=== CREATE ADDRESS ==="
    run curl -s -X POST "${BASE_URL}/timeseries/addresses" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: ${API_KEY}" \
        -d '{
            "street": "Hoofdstraat",
            "houseNumber": "1",
            "postalCode": "1234AB",
            "city": "Amsterdam"
        }' | jq .
}

# ─── PERSONS ─────────────────────────────────────────────────────────────────

list_persons() {
    echo "=== LIST PERSONS ==="
    run curl -s -X GET "${BASE_URL}/timeseries/persons?page=0&size=20" \
        -H "accept: application/json" \
        -H "X-API-Key: ${API_KEY}" | jq .
}

create_person() {
    echo "=== CREATE PERSON ==="
    run curl -s -X POST "${BASE_URL}/timeseries/persons" \
        -H "accept: application/json" \
        -H "Content-Type: application/json" \
        -H "X-API-Key: ${API_KEY}" \
        -d '{
            "firstName": "Bart",
            "lastName": "van Pelt",
            "dateOfBirth": "1970-01-01"
        }' | jq .
}

# ─── AUTH ───────────────────────────────────────────────────────────────

list-privileges() {
    echo "=== LIST PRIVILEGES ==="
    require_token
    run curl -s -X GET "${BASE_URL}/auth/privileges?page=1&size=20" \
        -H "accept: application/json" \
        -H "Authorization: Bearer ${TOKEN}"  | jq .
}

list-roles() {
    echo "=== LIST ROLES ==="
    require_token
    run curl -s -X GET "${BASE_URL}/auth/roles?page=1&size=20" \
        -H "accept: application/json" \
        -H "Authorization: Bearer ${TOKEN}"  | jq .
}

list-users() {
    echo "=== LIST USERS ==="
    require_token
    run curl -s -X GET "${BASE_URL}/auth/users?page=1&size=20" \
        -H "accept: application/json" \
        -H "Authorization: Bearer ${TOKEN}"  | jq .
}



# ─── USAGE ───────────────────────────────────────────────────────────────────

usage() {
    echo "Usage: $0 "
    echo " Login {login|authenticate|register}"
    echo " Timeseries {list-addresses|create-address|list-persons|create-person}"
    echo " Auth {list-privileges|list-roles}"
    exit 1
}

# ─── MAIN ────────────────────────────────────────────────────────────────────

ACTION=${1:-}

if [ -z "$ACTION" ]; then
    usage
    read -rp "Enter action: " ACTION
fi

case "${ACTION,,}" in
    login)            login ;;
    authenticate)     authenticate ;;
    register)         register ;;
    list-addresses)   list_addresses ;;
    create-address)   create_address ;;
    list-persons)     list_persons ;;
    create-person)    create_person ;;
    list-privileges)  list-privileges ;;
    list-roles)       list-roles ;;
    list-users)       list-users ;;
    *)                usage ;;
esac