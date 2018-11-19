# wso2-challenge-question-migration-client
Client to automate the challenge question metadta/userdata migration.

This project contains two modules.
1. org.wso2.challenge.question.metadata.migration.client - This module migrates the challenge question metadata.
2. org.wso2.user.challenge.question.migration.client - This module migrates the user challenge question/answer data.

##Migrating the metadata
1. clone the project.
2. Build the parent pom using `mvn clean install` command.
3. Copy and extract the build artifact from the first module. `org.wso2.challenge.question.metadata.migration.client-1.0-SNAPSHOT-bundle.zip` from the target directory.
4. Place the correct `client-truststore.jks` in the directory.
5. Place the questions metadata into `questions.properties` file.
(Note that `form.list.common.ChallengeQuestion.Q4` will be reformatted to `Q4` in the Identity Server question ID.)
6. Update the below information as needed in the `configuration.properties` file.
    1. admin.username = tenant admin username with the tenant domain
    2. admin.password = tenant admin password
    3. backend.url = server url and port
    4. questions.setid = question set id in which the questions should be created.
    5. questions.locale = Locale of the questions
    6. truststore.path = trust store name
    7. truststore.password = trust store password
7. Execute with the below command `java -jar ./org.wso2.challenge.question.metadata.migration.client-1.0-SNAPSHOT-jar-with-dependencies.jar`
8. Observe the challenge questions being created in the Identity Server.

##Migrating the users' challenge questions/answers data
1. clone the project.
2. Build the parent pom using `mvn clean install` command.
3. Copy and extract the build artifact from the second module. `org.wso2.user.challenge.question.migration.client-1.0-SNAPSHOT-bundle.zip` from the target directory.
4. Place the correct `client-truststore.jks` in the directory.
5. Place the user challenge questions answers data in the results.csv file.
(Please remove any starting and ending SQL statements from this data if any.)
6. Update the below information as needed in the `configuration.properties` file.
    1. admin.username = tenant admin username with the tenant domain
    2. admin.password = tenant admin password
    3. backend.url = server url and port
    4. input.file.name = file name of the user challenge answers data file.
    5. user.store.domain = user store domain of the users listed in the file.
    6. user.tenant.domain = tenant domain of the users listed in the file.
    7. user.questions.locale = locale of the questions listed in the file.
    8. user.questions.setid = group to which the questions in the results.csv file fall in to.
7. Execute the jar with the command `java -jar ./org.wso2.user.challenge.question.migration.client-1.0-SNAPSHOT-jar-with-dependencies.jar`
8. Observe the user challenge questions being populated.
