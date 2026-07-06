#!/bin/bash
#
# GCE startup script for the openrun VM (Container-Optimized OS).
#
# Replaces the deprecated container startup agent ("Konlet" /
# `gcloud compute instances create-with-container`). It runs on every boot of a
# VM created by .github/workflows/cicd.yml and makes the instance self-provision
# the application container without relying on the deprecated agent.
#
# It is intentionally SECRET-FREE and safe to commit:
#   - The app image bakes application.yml in at build time, so the app container
#     needs no runtime secrets — this script can start it directly.
#   - It authenticates to Artifact Registry using the VM service account token
#     from the metadata server (no key material in this file).
#
# DISASTER-RECOVERY CAVEAT: this script does NOT create a MySQL container from
# scratch, because that needs credentials that must not live in a committed file.
#   - Snapshot restore: the `mysql` container (and its data) come back with the
#     disk; this script just ensures it is started with a restart policy.
#   - Truly fresh VM: MySQL is provisioned by the workflow's "Run a MySQL
#     container in the GCE" SSH step (which holds the secrets), OR you restore a
#     boot-disk snapshot. A fresh VM alone stands up an EMPTY database.
#
set -euo pipefail

DOCKER=/usr/bin/docker
AR_HOST="asia-northeast3-docker.pkg.dev"
DEFAULT_IMAGE="asia-northeast3-docker.pkg.dev/openrun-452712/openrun-server/openrun:latest"
NETWORK="openur-network"

log() { echo "[gce-startup] $*"; }

# Read an optional custom instance-metadata attribute (empty string if absent).
metadata_attr() {
  curl -s -f -H "Metadata-Flavor: Google" \
    "http://metadata.google.internal/computeMetadata/v1/instance/attributes/$1" \
    2>/dev/null || true
}

# App image: overridable via the `container-image` metadata attribute.
IMAGE="$(metadata_attr container-image)"
IMAGE="${IMAGE:-$DEFAULT_IMAGE}"

# 1. Authenticate Docker to Artifact Registry using the VM service-account token.
log "Authenticating Docker to ${AR_HOST}"
ACCESS_TOKEN="$(curl -s -H "Metadata-Flavor: Google" \
  "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token" \
  | grep -o '"access_token":"[^"]*' | grep -o '[^"]*$')"
if [ -n "${ACCESS_TOKEN}" ]; then
  echo "${ACCESS_TOKEN}" | "${DOCKER}" login -u oauth2accesstoken --password-stdin "https://${AR_HOST}"
else
  log "WARNING: could not obtain metadata access token; docker pull may fail"
fi

# 2. Ensure the shared docker network exists.
if [ -z "$("${DOCKER}" network ls -q -f name="^${NETWORK}$")" ]; then
  log "Creating docker network ${NETWORK}"
  "${DOCKER}" network create "${NETWORK}"
fi

# 3. Ensure MySQL is running IF it already exists (snapshot-restore case).
#    Never create it from scratch here — see the DR caveat above.
if "${DOCKER}" ps -a -q -f name="^mysql$" | grep -q .; then
  log "Ensuring existing mysql container has restart policy and is running"
  "${DOCKER}" update --restart always mysql || true
  "${DOCKER}" start mysql || true
else
  log "No mysql container present — expecting the CI SSH step or a snapshot restore to provide the database"
fi

# 4. Pull and (re)run the application container with a restart policy.
log "Pulling ${IMAGE}"
"${DOCKER}" pull "${IMAGE}"
if "${DOCKER}" ps -a -q -f name="^openrun$" | grep -q .; then
  log "Removing existing openrun container"
  "${DOCKER}" rm -f openrun || true
fi
log "Starting openrun container"
"${DOCKER}" run -d \
  --name openrun \
  --network "${NETWORK}" \
  --restart always \
  -p 80:8080 \
  "${IMAGE}"

log "Startup script finished."
