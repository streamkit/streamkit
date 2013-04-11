Streamkit

## Building project

The build depends on [Apache Maven](http://maven.apache.org/guides/getting-started/index.html).
For those familiar with `maven` it's pretty straight forward:

    mvn clean install
    
### NOTE: on the first build you need to execute the following command    
    mvn clean install -gs=./initial-settings.xml
    
Build also integrates javascript unit tests; in case the build fails b/c it cannot open a browser add `browserPath` parameter:

    mvn clean install -DbrowserPath=/path/to/my/browser

If you want to skip the tests, you can execute:

    mvn clean install -Dmaven.test.skip=true

## Links and resources

* [Maven Getting Started Guide](http://maven.apache.org/guides/getting-started/index.html)
