To build this bundle use:
mvn clean install -Pdevelopment [-DbrowserPath=/usr/bin/firefox]

To run the integration tests for this bundle use:
mvn clean verify site -Pdevelopment,integration-tests

To test this bundle, just open up:
http://localhost:8080/content/channel/{$channelName}.vodManager.html?sling:authRequestLogin=true
