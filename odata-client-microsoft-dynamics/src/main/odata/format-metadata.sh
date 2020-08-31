#!/bin/bash
set -e
xmllint --format microsoft-dynamics-metadata.xml >/tmp/metadata.xml
cp /tmp/metadata.xml microsoft-dynamics-metadata.xml

