#!/bin/sh

mvn clean install -f plugin/pom.xml && mvn clean install -f integration/pom.xml