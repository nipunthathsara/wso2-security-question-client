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

import org.wso2.carbon.identity.application.common.model.xsd.User;
import org.wso2.carbon.identity.recovery.stub.model.UserChallengeAnswer;

public class DTO {
    private User user;
    private UserChallengeAnswer[] userChallengeAnswers;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserChallengeAnswer[] getUserChallengeAnswers() {
        return userChallengeAnswers;
    }

    public void setUserChallengeAnswers(UserChallengeAnswer[] userChallengeAnswers) {
        this.userChallengeAnswers = userChallengeAnswers;
    }
}
