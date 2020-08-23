#!/bin/bash
set -e
xmllint --format microsoft-analytics-v1-metadata.xml >/tmp/metadata.xml
cp /tmp/metadata.xml microsoft-analytics-v1-metadata.xml
xmllint --format microsoft-analytics-v2-metadata.xml >/tmp/metadata.xml
cp /tmp/metadata.xml microsoft-analytics-v2-metadata.xml
xmllint --format microsoft-analytics-v3-metadata.xml >/tmp/metadata.xml
cp /tmp/metadata.xml microsoft-analytics-v3-metadata.xml
xmllint --format microsoft-analytics-v4-metadata.xml >/tmp/metadata.xml
cp /tmp/metadata.xml microsoft-analytics-v4-metadata.xml

