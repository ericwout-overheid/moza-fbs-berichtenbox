---
name: new-migration
description: Create a new Flyway migration SQL file with correct version numbering. Use when user says /new-migration, "migratie aanmaken", or wants to add a database migration.
---

# New Migration Skill

Create a Flyway migration file with the correct version number for a service.

## Steps

1. **Determine service**: Ask which service needs the migration if not specified. Only these services use Flyway:
   - `berichtenmagazijn`
   - `notificatie`
   - `notificatieprofiel`
   - `digitale-bereikbaarheid`

2. **Get description**: Ask for a short description if not provided. Use snake_case (e.g., `create_berichten_table`).

3. **Find next version**: Scan `services/<service>/src/main/resources/db/migration/` for existing `V*__.sql` files. Extract the highest version number N. Next version = N+1. If no migrations exist yet, start at V1.

4. **Create migration file**: Write the file at:
   ```
   services/<service>/src/main/resources/db/migration/V<N>__<description>.sql
   ```

5. **Generate SQL scaffold**: Based on the description, generate appropriate SQL. Always include:
   ```sql
   -- Flyway migration: V<N>__<description>.sql
   -- Service: <service>
   ```

## Naming Convention

- Filename: `V{version}__{description}.sql` (double underscore between version and description)
- Description: snake_case, lowercase, no spaces
- Example: `V2__add_bijlagen_kolom.sql`

## Example

User: `/new-migration berichtenmagazijn add_status_kolom`

Result: Creates `services/berichtenmagazijn/src/main/resources/db/migration/V2__add_status_kolom.sql`
