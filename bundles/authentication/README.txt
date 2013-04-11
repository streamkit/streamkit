To build this bundle use:
mvn clean install -Pdevelopment [-DbrowserPath=/usr/bin/firefox]

To run the integration tests for this bundle use:
mvn clean verify site -Pdevelopment,integration-tests

To test this bundle, just open up:
http://localhost:8080/content/channel/{$channelName}.vodManager.html?sling:authRequestLogin=true

Executing JS unit tests with IntelliJ

1. Make sure you have JSTestDriver plugin installed
2. Open src/test/resources/jsTestDriver.conf and make sure basepath points to the right folder on your local machine
   basepath: "/Projects/MediaCenter/media-center-git/bundles/content_management"
3. Run src/test/resources/jsTestDriver.conf ( right-click on it and select Run )
4. When making changes to the CoffeeScript files, run the following maven command to recompile the JS files
   mvn clean process-test-resources
5. To run a single test, edit the jsTestDriver.conf file and specify which test file to run
    I.e.: test:
           - target/test-classes/js/VodModelTest.js
    This config runs just the VodModelTest file
6. When finished, revert the jsTestDriver.conf file so that it doesn't get committed into Git
