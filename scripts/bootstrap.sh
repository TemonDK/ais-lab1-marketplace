#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
tools_dir="$root/.tools"
mkdir -p "$tools_dir"

download_checked() {
    local url="$1" target="$2" sha="$3"
    if [[ ! -f "$target" ]]; then
        curl --fail --location --silent --show-error --retry 3 "$url" -o "$target.tmp"
        printf '%s  %s\n' "$sha" "$target.tmp" | sha256sum -c --status
        mv "$target.tmp" "$target"
    else
        printf '%s  %s\n' "$sha" "$target" | sha256sum -c --status
    fi
}

download_checked \
    'https://repo.maven.apache.org/maven2/org/contextmapper/context-mapper-cli/6.12.0/context-mapper-cli-6.12.0.tar' \
    "$tools_dir/context-mapper-cli-6.12.0.tar" \
    '1a3e2a6a48b428c32831c8f43e6152f45e2538836bad8852cea7e0ca6c04f6b6'

if [[ ! -x "$tools_dir/context-mapper-cli-6.12.0/bin/cm" ]]; then
    tar -xf "$tools_dir/context-mapper-cli-6.12.0.tar" -C "$tools_dir"
fi

download_checked \
    'https://github.com/plantuml/plantuml/releases/download/v1.2026.9/plantuml.jar' \
    "$tools_dir/plantuml.jar" \
    'aa4a1d06f8911c4b842beff4828dd955be431692b631a312d04c0e9fab9581de'
