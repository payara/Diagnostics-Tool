#!/bin/bash

buildName="payara-diagnostics-tool.jar"
installDir="./payara5/glassfish/lib/asadmin"
buildDir="./target"

if [[ ! -f ${buildDir}/${buildName} ]]; then
    echo "No Build to install"
    exit -1
fi

if [[ ! -d ${installDir} ]]; then
    echo "Installation directory does not exit"
    exit -1
fi

cp ${buildDir}/${buildName} ${installDir}

echo "Copied ${buildName} to ${installDir}"