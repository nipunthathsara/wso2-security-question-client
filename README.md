# wso2-security-question-client
Client to automate the security question creation flow.

1. cloe the project.
2. Build using `mvn clean install` command.
3. Copy and extract the build artifact `org.wso2.recovery.client-1.0-SNAPSHOT-bundle.zip` from the target directory.
4. Place the correct `client-truststore.jks` in the directory.
5. Update the below information as needed in the `configuration.properties` file.
* admin.username = tenant admin username with the tenant domain
* admin.password = tenant admin password
* backend.url = server url and port
* questions.setid = question set id in which the questions should be created.
* questions.locale = Locale of the questions
* truststore.path = trust store name
* truststore.password = trust store password

Execute with the below command.
``java -jar ./org.wso2.recovery.client-1.0-SNAPSHOT-jar-with-dependencies.jar `` 
