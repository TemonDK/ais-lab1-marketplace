#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
bash "$root/scripts/bootstrap.sh"
command -v dot >/dev/null || { echo 'Graphviz (dot) is required' >&2; exit 1; }
command -v python3 >/dev/null || { echo 'Python 3 is required for SVG XML validation' >&2; exit 1; }

cm="$root/.tools/context-mapper-cli-6.12.0/bin/cm"
jar="$root/.tools/plantuml.jar"
cml="$root/docs/architecture/marketplace.cml"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

"$cm" validate -i "$cml"
"$cm" generate -i "$cml" -g plantuml -o "$work"
cmp "$work/marketplace_ContextMap.puml" "$root/docs/architecture/context-map.puml" \
    || { echo 'Context Map source is outdated; run make build-docs' >&2; exit 1; }

java -jar "$jar" -failfast2 -checkonly -charset UTF-8 \
    "$root/docs/architecture/system-context.puml" \
    "$root/docs/architecture/containers.puml" \
    "$root/docs/architecture/context-map.puml"

java -jar "$jar" -failfast2 -tsvg -charset UTF-8 -o "$work" \
    "$root/docs/architecture/system-context.puml" \
    "$root/docs/architecture/containers.puml" \
    "$root/docs/architecture/context-map.puml"

# SVG element IDs may vary between Graphviz/PlantUML versions. Check that both
# the newly rendered diagrams and the committed evidence are well-formed SVG.
python3 - "$work" "$root/evidence" <<'PY'
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

svg_tag = "{http://www.w3.org/2000/svg}svg"
for directory in map(Path, sys.argv[1:]):
    for name in ("system-context", "containers", "context-map"):
        path = directory / f"{name}.svg"
        try:
            root = ET.parse(path).getroot()
            if root.tag != svg_tag:
                raise ValueError(f"expected SVG root element, got {root.tag}")
        except (OSError, ET.ParseError, ValueError) as error:
            raise SystemExit(f"Invalid SVG {path}: {error}") from error
print("CML, PlantUML and SVG XML are valid; Context Map source is in sync.")
PY
