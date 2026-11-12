#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
bash "$root/scripts/bootstrap.sh"
command -v dot >/dev/null || { echo 'Graphviz (dot) is required' >&2; exit 1; }

cm="$root/.tools/context-mapper-cli-6.12.0/bin/cm"
jar="$root/.tools/plantuml.jar"
cml="$root/docs/architecture/marketplace.cml"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

"$cm" validate -i "$cml"
"$cm" generate -i "$cml" -g plantuml -o "$work"
cp "$work/marketplace_ContextMap.puml" "$root/docs/architecture/context-map.puml"

mkdir -p "$root/evidence"
java -jar "$jar" -failfast2 -tsvg -charset UTF-8 \
    -o "$root/evidence" \
    "$root/docs/architecture/system-context.puml" \
    "$root/docs/architecture/containers.puml" \
    "$root/docs/architecture/context-map.puml"

for name in system-context containers context-map; do
    test -s "$root/evidence/$name.svg"
done
echo 'Generated three SVG diagrams in evidence/'
