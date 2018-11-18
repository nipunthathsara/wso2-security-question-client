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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.recovery.stub.ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException;
import org.wso2.carbon.identity.recovery.stub.ChallengeQuestionManagementAdminServiceStub;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * This class provides functionality to migrate the user challenge answer data to Identity Server.
 */
public class DataMigrator {
    private static final Logger log = Logger.getLogger(DataMigrator.class);
    private Properties configs;
    private ChallengeQuestionManagementAdminServiceStub stub;
    private String userStoreDomain;

    /**
     * Configure stub for user challenge data migration process.
     *
     * @param cookie
     * @param configs
     * @throws MalformedURLException
     * @throws AxisFault
     */
    public DataMigrator(String cookie, Properties configs) throws MalformedURLException, AxisFault {
        // Set configurations and users' tenant domain.
        this.configs = configs;
        if (configs.getProperty(Constants.USER_STORE_DOMAIN) != null) {
            this.userStoreDomain = configs.getProperty(Constants.USER_STORE_DOMAIN);
        } else {
            log.error("Users' user store domain missing in the " + Constants.CONFIGURATION_PROPERTIES + " file. Aborting process.");
            System.exit(1);
        }

        // Create and configure stub.
        if (configs.getProperty(Constants.BACK_END_URL) != null) {
            URL baseUrl = new URL(configs.getProperty(Constants.BACK_END_URL));
            String serviceUrl = new URL(baseUrl, Constants.CHALLENGE_QUESTION_MGT_SERVICE_PATH).toString();
            log.info("Creating challenge questions mgt stub for the service URL : " + serviceUrl);
            stub = new ChallengeQuestionManagementAdminServiceStub(serviceUrl);

            // Configure stub
            ServiceClient serviceClient = stub._getServiceClient();
            Options options = serviceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } else {
            log.error("Backend server URL missing in the " + Constants.CONFIGURATION_PROPERTIES + " file. Aborting process.");
            System.exit(1);
        }
    }

    /**
     * Migrate user challenge answer data.
     *
     * @param userData
     */
    public void migrateUserChallengeAnswerData(ArrayList<DTO> userData) {
        log.info("User challenge answers migration started.");
        for (DTO entry : userData) {
            try {
                log.info("Migrating entry User : " + entry.getUser().getUserName() +
                        " QuestionId : " + entry.getUserChallengeAnswers()[0].getQuestion().getQuestionId() +
                        " Answer : " + entry.getUserChallengeAnswers()[0].getAnswer());
                stub.setUserChallengeAnswers(entry.getUser(), entry.getUserChallengeAnswers());
            } catch (RemoteException | ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException e) {
                log.error("Error migrating the entry", e);
            }
        }
        log.info("User challenge answers migration finished.");
    }
}
