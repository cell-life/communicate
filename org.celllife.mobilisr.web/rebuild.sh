#!/bin/bash

#cd ../org.celllife.mobilisr.domain; mvn clean install -DskipTests
cd ../org.celllife.mobilisr.service.core; mvn clean install -DskipTests
cd ../org.celllife.mobilisr.service.gwt; mvn clean install -DskipTests
cd ../org.celllife.mobilisr.web
mvn gwt:run

