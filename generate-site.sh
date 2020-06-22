#!/bin/bash
set -e
PROJECT=odata-client
mvn site
cd ../davidmoten.github.io
git pull
mkdir -p $PROJECT
cp -r ../$PROJECT/target/site/* $PROJECT/
git add .
git commit -am "update $PROJECT site reports"
git push
