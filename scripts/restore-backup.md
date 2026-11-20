# Restoring a database backup

The server makes a compressed `pg_dump` every night into `~/bazaarready/backups` (the last 14 are kept),
and `scripts/pull-backups.sh` copies the newest one to `~/bazaarready-backups` on your computer.

To restore one onto the server (this replaces the current data):

1. Copy the backup to the server, if it is not there already:
   `scp ~/bazaarready-backups/bazaarready-2026-11-20-0000.dump deploy@148.66.159.79:~/bazaarready/backups/`
2. Stop the app so nothing writes while you restore:
   `docker compose -f docker-compose.yml -f docker-compose.prod.yml stop app`
3. Restore it (drops and recreates every table from the backup):
   `docker compose -f docker-compose.yml -f docker-compose.prod.yml exec -T db pg_restore --clean --if-exists -U bazaarready -d bazaarready < backups/bazaarready-2026-11-20-0000.dump`
4. Start the app again:
   `docker compose -f docker-compose.yml -f docker-compose.prod.yml start app`

Test a restore on your laptop first if you can: run the same `pg_restore` against the local database
(`docker compose exec -T db pg_restore --clean --if-exists -U bazaarready -d bazaarready < file.dump`).
