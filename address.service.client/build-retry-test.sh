#!/bin/bash

echo "Executing Maven Build"
echo "- deactivating profile: ribbon-standard"
echo "- activating profile:   ribbon-retry-test"

# disables the ribbon-standard profile and enables the ribbon-retry-test profile
# essentially, this switches the main class used inside the generated .jar file.
mvn clean package -P -ribbon-standard,ribbon-retry-test