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

public class Constants {
    public static final String CONFIGURATION_PROPERTIES = "configurations.properties";
    public static final String LOG4J_PROPERTIES = "log4j.properties";
    public static final String INPUT_FILE_NAME = "input.file.name";

    public static final String BACK_END_URL = "backend.url";
    public static final String AUTHENTICATOR_SERVICE_PATH = "services/AuthenticationAdmin";
    public static final String CHALLENGE_QUESTION_MGT_SERVICE_PATH = "services/ChallengeQuestionManagementAdminService";

    public static final String ADMIN_USERNAME = "admin.username";
    public static final String ADMIN_PASSWORD = "admin.password";

    public static final String TRUST_STORE = "truststore";
    public static final String TRUST_STORE_PASSWORD = "truststore.password";
    public static final String TRUST_STORE_TYPE = "truststore.type";

    // User data constants
    public static final String USER_STORE_DOMAIN = "user.store.domain";
    public static final String USER_TENANT_DOMAIN = "user.tenant.domain";
    public static final String USER_QUESTION_LOCALE = "user.questions.locale";
    public static final String USER_QUESTION_SET_ID = "user.questions.setid";
}
