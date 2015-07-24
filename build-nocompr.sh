#!/bin/sh

# Requires maven to be on the classpath
# Skips test phase

mvn clean install -DskipTests=true -DuseWarCompression=false
mvn clean install -DskipTests=true -DuseWarCompression=false -f dhis-web/pom.xml
