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

package org.wso2.recovery.client.util;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.application.common.model.xsd.User;
import org.wso2.carbon.identity.recovery.stub.ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException;
import org.wso2.carbon.identity.recovery.stub.ChallengeQuestionManagementAdminServiceStub;
import org.wso2.carbon.identity.recovery.stub.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.stub.model.UserChallengeAnswer;
import org.wso2.recovery.client.Constants;
import org.wso2.recovery.client.DTO;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class provides functionality to read userdata file and populate DTOs.
 * This class is the client specific data reader. This should be modified in order to cater
 * different format of input data.
 */
public class DTOPopulator {
    private static final Logger log = Logger.getLogger(DTOPopulator.class);
    private String filePath;
    private Properties configs;
    private ChallengeQuestion[] tenantQuestions;
    private ChallengeQuestionManagementAdminServiceStub stub;

    public DTOPopulator(String filePath, Properties configs, ChallengeQuestionManagementAdminServiceStub stub) {
        this.filePath = filePath;
        this.configs = configs;
        this.stub = stub;
    }

    /**
     * This method reads the user answers file line by line and populates an DTO array from that.
     *
     * @return
     * @throws IOException
     */
    public ArrayList<DTO> populateUserChallengeAnswerData() throws IOException, ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException {
        ArrayList<DTO> userData = new ArrayList();
        // Get all challenge questions available for the tenant domain.
        this.tenantQuestions = getChallengeQuestionsOfTenant(configs.getProperty(Constants.USER_TENANT_DOMAIN));

        log.info("Reading user challenge answer data from file : " + filePath);
        File file = new File(filePath);
        List<String> lines = FileUtils.readLines(file, "UTF-8");

        // Read line by line
        for (String line : lines) {
            log.info("Reading line : " + line);
            // Trim the line and split line by whitespaces
            line = line.trim();
            String[] splitted = line.split("\\s+");

            User user = new User();
            UserChallengeAnswer[] userChallengeAnswers = new UserChallengeAnswer[1];
            UserChallengeAnswer userChallengeAnswer = new UserChallengeAnswer();
            ChallengeQuestion challengeQuestion = new ChallengeQuestion();
            DTO dto = new DTO();

            // Minimum 4 categories should be there per entry as - ID, Username, QuestionId, Answer
            if (splitted.length >= 4) {
                // Populate User's attributes
                user.setTenantDomain(configs.getProperty(Constants.USER_TENANT_DOMAIN));
                user.setUserName(splitted[1]);
                user.setUserStoreDomain(configs.getProperty(Constants.USER_STORE_DOMAIN));
                dto.setUser(user);
                // Populate userChallengeAnswers' attributes
                challengeQuestion.setQuestionId(splitted[2]);
                String questionString = getQuestionFromId(challengeQuestion.getQuestionId());
                if(questionString != null){
                    challengeQuestion.setQuestion(questionString);
                } else {
                    log.info("Aborting entry. Unable to find a matching question for the question Id.");
                    continue;
                }
                challengeQuestion.setLocale(configs.getProperty(Constants.USER_QUESTION_LOCALE));
                challengeQuestion.setQuestionSetId(configs.getProperty(Constants.USER_QUESTION_SET_ID));
                userChallengeAnswer.setAnswer(createAnswerString(splitted));
                userChallengeAnswer.setQuestion(challengeQuestion);
                userChallengeAnswers[0] = userChallengeAnswer;
                dto.setUserChallengeAnswers(userChallengeAnswers);

                // Add entry to userData
                userData.add(dto);
            } else {
                // Otherwise ignore that line
                log.info("Ignoring line due to insufficient data : " + line);
            }
        }
        return userData;
    }

    /**
     * This method creates a single answer string from the columns in the provided data.
     *
     * @param line
     * @return
     */
    private String createAnswerString(String[] line) {
        String answer = new String();
        // Answer starts at third column and ends where the line ends.
        for (int i = 3; i < line.length; i++) {
            answer += line[i];
            // If user answer has more than one word separate them by a single space.
            if ((i + 1) < line.length){
                answer += " ";
            }
        }
        return answer;
    }

    /**
     * This method returns all available challenge questions for a given tenant.
     * @param tenantDomain
     * @throws RemoteException
     * @throws ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException
     */
    private ChallengeQuestion[] getChallengeQuestionsOfTenant(String tenantDomain) throws RemoteException, ChallengeQuestionManagementAdminServiceIdentityRecoveryExceptionException {
        log.info("Retrieving all challenge questions for the tenant : " + tenantDomain);
        return stub.getChallengeQuestionsOfTenant(tenantDomain);
    }

    /**
     * This method search for a given question ID in the tenant challenge question and returns question string.
     * @param questionId
     * @return
     */
    private String getQuestionFromId(String questionId) {
        log.info("Searching question of question Id : " + questionId);
        if (tenantQuestions != null && tenantQuestions.length > 0) {
            for (ChallengeQuestion question : this.tenantQuestions) {
                if(question != null && questionId.equals(question.getQuestionId())){
                    log.info("Match found for question Id : " + questionId + " question : " + question.getQuestion());
                    return question.getQuestion();
                }
            }
        } else{
            log.error("Null or empty set of tenant questions received. Aborting process.");
            System.exit(1);
        }
        log.error("No matching question found for question Id : " + questionId);
        return null;
    }
}
