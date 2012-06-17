To build this bundle use:
mvn clean install [-DbrowserPath=/usr/bin/firefox]

To test this bundle, just open up:
http://localhost:8080/content/channel/{$channelName}.dashboard.html?sling:authRequestLogin=true

This bundle contains the shared libraries(JS)/CSS/IMG file across multiple bundles.

To use this bundle into another bundle add this plugin to the build plugins:
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>2.1</version>
    <executions>
        <execution>
            <id>copy-common_bundle-dependencies</id>
            <phase>process-resources</phase>
            <goals>
                <goal>unpack</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <outputDirectory>
                            ${project.build.directory}/commons
                        </outputDirectory>
                        <groupId>org.mediacenter</groupId>
                        <artifactId>commons</artifactId>
                        <version>${project.parent.version}</version>
                        <type>jar</type>
                    </artifactItem>
                </artifactItems>
                <includes>**/*.js</includes>
            </configuration>
        </execution>
    </executions>
</plugin>