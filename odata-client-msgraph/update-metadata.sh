#!/bin/bash
set -e 
set -x
curl -o /tmp/graph.metadata.xml 'https://graph.microsoft.com/v1.0/$metadata?$format=xml'
curl -o /tmp/beta.metadata.xml 'https://graph.microsoft.com/beta/$metadata?$format=xml'
xmllint --format /tmp/graph.metadata.xml >/tmp/graph.metadata.2
xmllint --format /tmp/beta.metadata.xml >/tmp/beta.metadata.2
cp /tmp/graph.metadata.2 $WK/odata-client/odata-client-generator/src/main/odata/msgraph-metadata.xml
cp /tmp/beta.metadata.2 $WK/odata-client/odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml
