#!/bin/bash
set -e 
set -x
curl -o /tmp/graph.metadata.xml 'https://graph.microsoft.com/v1.0/$metadata?$format=xml'
curl -o /tmp/beta.metadata.xml 'https://graph.microsoft.com/beta/$metadata?$format=xml'
xmllint --format /tmp/graph.metadata.xml >/tmp/graph.metadata.2
xmllint --format /tmp/beta.metadata.xml >/tmp/beta.metadata.2
cp /tmp/graph.metadata.2 ../odata-client-generator/src/main/odata/msgraph-metadata.xml
cp /tmp/beta.metadata.2 ../odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml
sed -i 's/<EntityType Name="itemAttachment" BaseType="graph.attachment">/<EntityType Name="itemAttachment" BaseType="graph.attachment" HasStream="true">/g' ../odata-client-generator/src/main/odata/msgraph-metadata.xml
sed -i 's/<EntityType Name="message" BaseType="graph.outlookItem" OpenType="true">/<EntityType Name="message" BaseType="graph.outlookItem" OpenType="true" HasStream="true">/g' ../odata-client-generator/src/main/odata/msgraph-metadata.xml
sed -i 's/<EntityType Name="chatMembersNotificationAudience"\/>/<!-- commented out due no keys: <EntityType Name="chatMembersNotificationAudience"\/> -->/g' ../odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml
sed -i 's/<EntityType Name="auditLogRoot">/<EntityType Name="auditLogRoot" BaseType="graph.entity">/g' ../odata-client-generator/src/main/odata/msgraph-metadata.xml
sed -i 's/<EntityType Name="identityContainer">/<EntityType Name="identityContainer" BaseType="graph.entity">/g' ../odata-client-generator/src/main/odata/msgraph-metadata.xml
sed -i 's/<EntityType Name="directory">/<EntityType Name="directory" BaseType="graph.entity">/g' ../odata-client-generator/src/main/odata/msgraph-metadata.xml
sed -i 's/<ReturnType Type="graph.report"/<ReturnType Type="Edm.Stream"/g' ../odata-client-generator/src/main/odata/msgraph-metadata.xml

## not required, Microsoft removed this item from metadata
#sed -i 's/<Singleton Name="settings" Type="microsoft.graph.entitlementManagementSettings"\/>/<Singleton Name="entitlementManagementSettings" Type="microsoft.graph.entitlementManagementSettings"\/>/g' ../odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml
sed -i 's/<NavigationPropertyBinding Path="sites\/contentTypes\/columns\/term\/parentTerm" Target="sites\/termStore\/sets\/terms"\/>/<!-- remove dodgy NavigationPropertyBinding with non-existent target -->/g' ../odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml
sed -i 's/<NavigationPropertyBinding Path="sites\/contentTypes\/columns\/term\/termSet" Target="sites\/termStore\/sets"\/>/<!-- remove dodgy NavigationPropertyBinding with non-existent target -->/g' ../odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml

## remove bad targets 
sed -i 's/^.*NavigationPropertyBinding.*sites\/contentTypes\/.*$/<!-- remove dodgy NavigationPropertyBinding with non-existent target -->/g' ../odata-client-generator/src/main/odata/msgraph-metadata.xml
sed -i 's/^.*NavigationPropertyBinding.*Target="policies\/appManagementPolicies".*$/<!-- remove dodgy NavigationPropertyBinding with non-existent target -->/g' ../odata-client-generator/src/main/odata/msgraph-metadata.xml
sed -i 's/^.*NavigationPropertyBinding.*Target="employeeExperience\/learningCourseActivities".*$/<!-- remove dodgy NavigationPropertyBinding with non-existent target -->/g' ../odata-client-generator/src/main/odata/msgraph-metadata.xml
