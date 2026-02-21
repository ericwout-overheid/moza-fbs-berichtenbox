#!/bin/bash
# Hook script: warns if JAVA_HOME is not set when running gradlew commands.
# Called from .claude/settings.json PreToolUse hook for Bash tool.

# Check if command contains gradlew
if ! echo "$CLAUDE_TOOL_INPUT" | grep -q 'gradlew'; then
  exit 0
fi

# Check if JAVA_HOME is already set and valid
if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ]; then
  exit 0
fi

# Try to find JDK 21
FOUND_JDK=$(ls -d ~/.jdks/ms-21* 2>/dev/null | sort -V | tail -1)

if [ -n "$FOUND_JDK" ]; then
  echo "JAVA_HOME not set. Use: export JAVA_HOME=$FOUND_JDK"
else
  echo "WARNING: JDK 21 not found in ~/.jdks/. Gradle commands may fail."
fi

exit 0
