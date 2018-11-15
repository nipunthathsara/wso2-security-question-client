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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.recovery.stub.ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException;
import org.wso2.recovery.client.util.AuthenticationServiceClient;
import org.wso2.recovery.client.util.PropertyReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
            log.error("Error while authentication", e);
            // Re throwing to stop the execution further.
            throw e;
        }

        // Create questions for the tenant
        if (cookie != null) {
            QuestionCreator questionCreator = new QuestionCreator(cookie, configs, questions);
            questionCreator.createQuestions(tenantDomain);
        }
    }

    /**
     * This method reads client configurations from configurations.properties file.
     */
    public static void initialize() throws IOException {
        // Set log4j configs
//        Properties log4j = new Properties();
//        log4j.load(new FileInputStream(Constants.LOG4J_PROPERTIES));
//        PropertyConfigurator.configure(log4j);
        // Read client configurations
        configs = PropertyReader.loadProperties(Constants.CONFIGURATION_PROPERTIES);
        // Read questions metadata
        questions = PropertyReader.loadProperties(Constants.QUESTIONS_PROPERTIES);

        // Set trust-store configurations to the JVM
        log.info("Initializing : setting trust store configurations to JVM.");
        System.setProperty("javax.net.ssl.trustStore", configs.getProperty(Constants.TRUST_STORE_PATH));
        System.setProperty("javax.net.ssl.trustStorePassword", configs.getProperty(Constants.TRUST_STORE_PASSWORD));
        System.setProperty("javax.net.ssl.trustStoreType", configs.getProperty(Constants.TRUST_STORE_TYPE));
    }

    /**
     * This method authenticates tenant admin user to use the stub.
     *
     * @return
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     */
    public static String login() throws RemoteException, LoginAuthenticationExceptionException, MalformedURLException {
        URL baseUrl = new URL (configs.getProperty(Constants.BACK_END_URL));
        String serviceUrl = new URL(baseUrl, Constants.AUTHENTICATOR_SERVICE_PATH).toString();

        AuthenticationServiceClient authenticator = new AuthenticationServiceClient(serviceUrl);

        // Construct username with tenant domain and password from properties
        String userName = configs.getProperty(Constants.ADMIN_USERNAME);
        String password = configs.getProperty(Constants.ADMIN_PASSWORD);
        tenantDomain = userName.substring(userName.lastIndexOf('@') + 1);
        log.info("Authenticating the tenant : " + tenantDomain);

        // Return session cookie
        return authenticator.authenticate(userName, password, tenantDomain);
    }
}
