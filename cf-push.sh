#!/bin/bash
echo "Pushing to Cloud Foundry"
echo "- using variables file: mainfest-variables.yml"
echo "- Note: This requires latest CF CLI. You can find it here: https://github.com/cloudfoundry/cli"
cf push --vars-file manifest-variables.yml

