/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.recovery.client;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.recovery.client.util.AuthenticationServiceClient;
import org.wso2.recovery.client.util.DTOPopulator;
import org.wso2.recovery.client.util.PropertyReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;

public class Invoker {
    private static final Logger log = Logger.getLogger(Invoker.class);
    public static Properties configs;
    public static String tenantDomain;

    public static void main(String[] args) throws IOException, LoginAuthenticationExceptionException {
        // Initialize
        try {
            initialize();
        } catch (IOException e) {
            log.error("Error while initializing the client. Aborting process.", e);
            throw e;
        }

        // Authenticate tenant
        String cookie;
        try {
            cookie = login();
        } catch (RemoteException | LoginAuthenticationExceptionException | MalformedURLException e) {
            log.error("Error while authentication. Aborting the process", e);
            throw e;
        }

        // Create data for the tenant
        if (cookie != null) {
            // Read data and populate DTO
            DTOPopulator DTOPopulator = new DTOPopulator("/home/nipun/data/vodafone/wso2-security-question-client/org.wso2.user.challenge.question.migration.client/src/main/resources/result.csv", configs);
            ArrayList<DTO> userData = DTOPopulator.populateUserChallengeAnswerData();
            // Migrate challenge answer data to Identity Server.
            DataMigrator dataMigrator = new DataMigrator(cookie, configs);
            dataMigrator.migrateUserChallengeAnswerData(userData);
        } else {
            log.error("Session cookie null. Authentication failure. Aborting process. ");
            System.exit(1);
        }
    }

    /**
     * This method reads client configurations from configurations.properties file.
     */
    public static void initialize() throws IOException {
        log.info("Initialization started.");
        // Set log4j configs
        PropertyConfigurator.configure(Paths.get("/home/nipun/data/vodafone/wso2-security-question-client/org.wso2.user.challenge.question.migration.client/src/main/resources", Constants.LOG4J_PROPERTIES).toString());
        // Read client configurations
        configs = PropertyReader.loadProperties(Paths.get("/home/nipun/data/vodafone/wso2-security-question-client/org.wso2.user.challenge.question.migration.client/src/main/resources", Constants.CONFIGURATION_PROPERTIES).toString());

        // Set trust-store configurations to the JVM
        log.info("Setting trust store configurations to JVM.");
        if (configs.getProperty(Constants.TRUST_STORE_PASSWORD) != null && configs.getProperty(Constants.TRUST_STORE_TYPE) != null
                && configs.getProperty(Constants.TRUST_STORE_PATH) != null) {
            System.setProperty("javax.net.ssl.trustStore", Paths.get("/home/nipun/data/vodafone/wso2-security-question-client/org.wso2.user.challenge.question.migration.client/src/main/resources", configs.getProperty(Constants.TRUST_STORE_PATH)).toString());
            System.setProperty("javax.net.ssl.trustStorePassword", configs.getProperty(Constants.TRUST_STORE_PASSWORD));
            System.setProperty("javax.net.ssl.trustStoreType", configs.getProperty(Constants.TRUST_STORE_TYPE));
        } else {
            log.error("Trust store configurations missing in the configurations.properties file. Aborting process.");
            System.exit(1);
        }
        log.info("Initialization finished.");
    }

    /**
     * This method authenticates tenant admin user to use the stub.
     *
     * @return
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     */
    public static String login() throws RemoteException, LoginAuthenticationExceptionException, MalformedURLException {
        log.info("Authentication started");
        URL baseUrl = new URL (configs.getProperty(Constants.BACK_END_URL));
        String serviceUrl = new URL(baseUrl, Constants.AUTHENTICATOR_SERVICE_PATH).toString();

        log.info("Creating authentication service client on URL : " + serviceUrl);
        AuthenticationServiceClient authenticator = new AuthenticationServiceClient(serviceUrl);

        // Construct username with tenant domain and password from properties
        String userName = configs.getProperty(Constants.ADMIN_USERNAME);
        String password = configs.getProperty(Constants.ADMIN_PASSWORD);
        tenantDomain = userName.substring(userName.lastIndexOf('@') + 1);

        // Return session cookie
        log.info("Authenticating user : " + userName + " password : " + password + " tenant domain : " + tenantDomain);
        return authenticator.authenticate(userName, password, tenantDomain);
    }
}
