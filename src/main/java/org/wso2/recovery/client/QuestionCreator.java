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
import org.wso2.carbon.identity.recovery.stub.model.ChallengeQuestion;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;

/**
 * This class provides functionality to create the challenge questions.
 */
public class QuestionCreator {
    private static final Logger log = Logger.getLogger(QuestionCreator.class);
    private ChallengeQuestionManagementAdminServiceStub stub;
    private ChallengeQuestion[] challengeQuestions;

    /**
     * Create stub and populate data to invoke the stub.
     *
     * @param cookie
     * @param configs
     * @param questions
     * @throws AxisFault
     */
    public QuestionCreator(String cookie, Properties configs, Properties questions) throws AxisFault, MalformedURLException {
        URL baseUrl = new URL(configs.getProperty(Constants.BACK_END_URL));
        String serviceUrl = new URL(baseUrl, Constants.CHALLENGE_QUESTION_MGT_SERVICE_PATH).toString();
        log.info("Creating challenge questions mgt stub for the service URL : " + serviceUrl);
        stub = new ChallengeQuestionManagementAdminServiceStub(serviceUrl);

        // Configure stub
        ServiceClient serviceClient = stub._getServiceClient();
        Options options = serviceClient.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        // Read challenge questions
        log.info("Populating challenge questions array.");
        challengeQuestions = new ChallengeQuestion[questions.size()];
        int i = 0;
        for (Map.Entry<Object, Object> entry : questions.entrySet()) {
            log.debug("Challenge question ID : " + entry.getKey().toString());
            log.debug("Challenge question : " + entry.getValue().toString());
            challengeQuestions[i] = new ChallengeQuestion();
            challengeQuestions[i].setLocale(configs.getProperty(Constants.QUESTIONS_LOCALE));
            challengeQuestions[i].setQuestionSetId(configs.getProperty(Constants.QUESTIONS_SET_ID));
            // Challenge question id is only alpha numeric.
            // Take only the string after last '.' as the question id. Eg : form.list.common.ChallengeQuestion.Q9  -> Q9
            challengeQuestions[i].setQuestionId(entry.getKey().toString().substring(entry.getKey().toString().lastIndexOf('.') + 1));
            challengeQuestions[i].setQuestion(entry.getValue().toString());
            i++;
        }
    }

    /**
     * This method creates challenge questions by invoking the stub.
     *
     * @param tenantDomain
     * @throws RemoteException
     * @throws ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException
     */
    public void createQuestions(String tenantDomain) throws RemoteException, ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException {
        log.info("Started creating challenge questions for the tenant : " + tenantDomain);
        stub.setChallengeQuestionsOfTenant(challengeQuestions, tenantDomain);
        log.info("Challenge question creation successful.");
    }

}
