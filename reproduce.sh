#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mvn clean package
java -Xmx2g -jar target/graphs-1.0.jar
python3 scripts/make_report.py
