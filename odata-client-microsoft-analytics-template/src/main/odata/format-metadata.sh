#!/bin/bash
set -e
xmllint --format microsoft-analytics-v3-metadata.xml >/tmp/metadata.xml
cp /tmp/metadata.xml microsoft-analytics-v3-metadata.xml
