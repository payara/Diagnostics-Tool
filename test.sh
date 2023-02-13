#!/bin/bash

asadminDir="./payara5/bin/"
testCommands=("collect")

if [[ ! -d ${asadminDir} ]]; then
    echo "Bin direcotry ${asadminDir} does not exist"
    exit -1
fi

${asadminDir}/asadmin start-domain

for i in "${testCommands[@]}"; do
    ${asadminDir}/asadmin $i -s
done

${asadminDir}/asadmin stop-domain

echo "Tests Done"
