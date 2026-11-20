#!/usr/bin/env sh
# Copies the nightly database backups from the server to this computer, so a copy lives off the server.
# Run it from Git Bash (or any shell with ssh/scp):  sh scripts/pull-backups.sh
# Backups land in ~/bazaarready-backups; the newest 30 are kept there.
set -eu

SERVER="${BACKUP_SERVER:-deploy@148.66.159.79}"
KEY="${BACKUP_KEY:-$HOME/.ssh/id_ed25519}"
DEST="${BACKUP_DEST:-$HOME/bazaarready-backups}"

mkdir -p "$DEST"
latest=$(ssh -o BatchMode=yes -i "$KEY" "$SERVER" 'ls -1t ~/bazaarready/backups/bazaarready-*.dump 2>/dev/null | head -n 1')
if [ -z "$latest" ]; then
  echo "No backups on the server yet." >&2
  exit 1
fi
scp -q -o BatchMode=yes -i "$KEY" "$SERVER:$latest" "$DEST/"
echo "Copied $(basename "$latest") to $DEST"
ls -1t "$DEST"/bazaarready-*.dump | tail -n +31 | xargs -r rm -f
