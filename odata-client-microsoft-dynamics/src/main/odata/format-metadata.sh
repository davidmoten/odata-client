#!/bin/bash
set -e
xmllint --format microsoft-dynamics-crm4-v9.1-metadata.xml >/tmp/metadata.xml
cp /tmp/metadata.xml microsoft-dynamics-crm4-v9.1-metadata.xml 
xmllint --format microsoft-dynamics-crm6-v9.1-metadata.xml >/tmp/metadata.xml
cp /tmp/metadata.xml microsoft-dynamics-crm6-v9.1-metadata.xml 


