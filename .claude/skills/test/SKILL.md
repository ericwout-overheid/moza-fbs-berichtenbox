---
name: test
description: Run tests for one or all Gradle modules. Use when user says /test, "test", "testen", or wants to run tests.
---

# Test Skill

Run Gradle tests for FBS modules.

## Steps

1. **Detect JAVA_HOME**: Find JDK 21 in `~/.jdks/`. Run:
   ```bash
   export JAVA_HOME=$(ls -d ~/.jdks/ms-21* 2>/dev/null | sort -V | tail -1)
   ```
   If not found, try `~/.jdks/*21*`. If still not found, check if `java -version` reports 21+.

2. **Determine target**:
   - `/test` (no args) -> run all tests: `./gradlew check`
   - `/test <module>` -> run module tests: `./gradlew :<module>:check`
   - `/test <module> unit` -> unit tests only: `./gradlew :<module>:test`
   - `/test <module> integration` -> integration tests only: `./gradlew :<module>:quarkusIntTest` (if exists)

3. **Run tests**:
   ```bash
   JAVA_HOME=$JAVA_HOME ./gradlew <target>
   ```

4. **Report results**: After the test run completes:
   - Show pass/fail summary
   - On failure, find and read test report XML files in `build/test-results/` to surface specific failures:
     ```bash
     find services/<module>/build/test-results -name '*.xml' -newer services/<module>/build/test-results 2>/dev/null | head -5
     ```
   - Show relevant error output from failed tests

## Module Mapping

| Short name | Gradle path |
|-----------|-------------|
| berichtenmagazijn | `:services:berichtenmagazijn` |
| berichtenlijst | `:services:berichtenlijst` |
| notificatie | `:services:notificatie` |
| notificatieprofiel | `:services:notificatieprofiel` |
| digitale-bereikbaarheid | `:services:digitale-bereikbaarheid` |
| admin-dashboard | `:services:admin-dashboard` |
| fbs-common, common | `:libs:fbs-common` |
| fbs-client-sdk, client-sdk | `:libs:fbs-client-sdk` |
| fbs-authzen-client, authzen | `:libs:fbs-authzen-client` |
| fbs-ldv, ldv | `:libs:fbs-ldv` |
| fbs-cloudevents, cloudevents | `:libs:fbs-cloudevents` |

## Examples

- `/test` -> `./gradlew check`
- `/test berichtenmagazijn` -> `./gradlew :services:berichtenmagazijn:check`
- `/test common` -> `./gradlew :libs:fbs-common:check`
- `/test berichtenmagazijn unit` -> `./gradlew :services:berichtenmagazijn:test`
