#!/bin/bash
namespace=$1
release=$2
EXISTS=`helm list -a --namespace $namespace | awk -v rls=$release '($1==rls && $3=="1" && ($8=="pending-install" || $8=="failed"))' | awk '{print "true"}'`
echo $EXISTS   
if [ "$EXISTS" == "true" ]; then
helm delete $release --namespace $namespace
fi
