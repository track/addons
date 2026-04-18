#!/bin/bash
set -e

VERSION=$(grep '^version=' gradle.properties | cut -d'=' -f2)
if [ -z "$VERSION" ]; then
  echo "Could not read version from gradle.properties"
  exit 1
fi

echo "Building analyse-addons v${VERSION}..."
./gradlew build --no-daemon -q

OUTPUT="analyse-addons-${VERSION}.zip"

# Collect every built addon jar (portable — works on macOS bash 3.2)
FOUND=()
while IFS= read -r jar; do
  FOUND+=("$jar")
done < <(find modules -path "*/build/libs/analyse-addon-*.jar" -type f | sort)

if [ ${#FOUND[@]} -eq 0 ]; then
  echo "No addon jars found, aborting"
  exit 1
fi

# Zip the jars (flat, no directory structure)
rm -f "$OUTPUT"
zip -j "$OUTPUT" "${FOUND[@]}"

echo ""
echo "Created ${OUTPUT} with ${#FOUND[@]} addon(s):"
for jar in "${FOUND[@]}"; do
  echo "  - $(basename "$jar")"
done
