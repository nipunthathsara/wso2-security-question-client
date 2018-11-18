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
import org.wso2.carbon.identity.recovery.stub.ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException;
import org.wso2.recovery.client.util.AuthenticationServiceClient;
import org.wso2.recovery.client.util.PropertyReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Properties;

public class Invoker {
    private static final Logger log = Logger.getLogger(Invoker.class);
    public static Properties configs, questions;
    public static String tenantDomain;

    public static void main(String[] args) throws RemoteException, LoginAuthenticationExceptionException, ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException, MalformedURLException {
        try {
            initialize();
        } catch (IOException e) {
            log.error("Error while initializing the client.", e);
        }

        // Authenticate tenant
        String cookie;
        try {
            cookie = login();
        } catch (RemoteException | LoginAuthenticationExceptionException | MalformedURLException e) {
            log.error("Error while authentication. Aborting the process.", e);
            throw e;
        }

        // Create questions for the tenant
        if (cookie != null) {
            QuestionCreator questionCreator = new QuestionCreator(cookie, configs, questions);
            questionCreator.createQuestions(tenantDomain);
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
        PropertyConfigurator.configure(Paths.get(".", Constants.LOG4J_PROPERTIES).toString());
        // Read client configurations
        configs = PropertyReader.loadProperties(Paths.get(".", Constants.CONFIGURATION_PROPERTIES).toString());
        // Read questions metadata
        questions = PropertyReader.loadProperties(Paths.get(".", Constants.QUESTIONS_PROPERTIES).toString());

        // Set trust-store configurations to the JVM
        log.info("Setting trust store configurations to the JVM.");
        if (configs.getProperty(Constants.TRUST_STORE_PASSWORD) != null && configs.getProperty(Constants.TRUST_STORE_TYPE) != null
                && configs.getProperty(Constants.TRUST_STORE_PATH) != null) {
            System.setProperty("javax.net.ssl.trustStore", Paths.get(".", configs.getProperty(Constants.TRUST_STORE_PATH)).toString());
            System.setProperty("javax.net.ssl.trustStorePassword", configs.getProperty(Constants.TRUST_STORE_PASSWORD));
            System.setProperty("javax.net.ssl.trustStoreType", configs.getProperty(Constants.TRUST_STORE_TYPE));
        } else {
            log.error("Trust store configurations missing in the " + Constants.CONFIGURATION_PROPERTIES + " file. Aborting process.");
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
        AuthenticationServiceClient authenticator = null;
        if(configs.getProperty(Constants.BACK_END_URL) != null){
            URL baseUrl = new URL (configs.getProperty(Constants.BACK_END_URL));
            String serviceUrl = new URL(baseUrl, Constants.AUTHENTICATOR_SERVICE_PATH).toString();
            authenticator = new AuthenticationServiceClient(serviceUrl);
        } else {
            log.error("Server backend URL not defined in the " + Constants.CONFIGURATION_PROPERTIES + "file. Aborting process.");
            System.exit(1);
        }

        // Construct username with tenant domain and password from properties
        String userName = configs.getProperty(Constants.ADMIN_USERNAME);
        String password = configs.getProperty(Constants.ADMIN_PASSWORD);
        tenantDomain = userName.substring(userName.lastIndexOf('@') + 1);
        log.info("Authenticating the tenant : " + tenantDomain);

        // Return session cookie
        return authenticator.authenticate(userName, password, tenantDomain);
    }
}
