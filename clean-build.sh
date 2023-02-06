#!/bin/bash

#Install dependencies

depName="server-mgmt-5.36.0.jar"
depPath="./dependencies"

if [[ ! -f ${depPath}/${depName} ]]; then
    echo "${depName} is missing from ${depPath}"
    exit -1
fi

mvn install:install-file -Dfile=./dependencies/server-mgmt-5.36.0.jar -DgroupId=fish.payara.server.internal.admin -DartifactId=server-mgm -Dversion=5.36.0 -Dpackaging=jar -DgeneratePom=true

#Build Project

mvn clean package