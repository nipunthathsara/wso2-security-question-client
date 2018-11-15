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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.recovery.client.Constants;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * This class provides functionality to authenticate the tenant admin to use the stub.
 */
public class AuthenticationServiceClient {
    private final static Logger log = Logger.getLogger(AuthenticationServiceClient.class);
    private static AuthenticationAdminStub authenticationAdminStub;

    public AuthenticationServiceClient(String serviceUrl) throws AxisFault {
        authenticationAdminStub = new AuthenticationAdminStub(serviceUrl);
    }

    public String authenticate(String userName, String password, String tenant) throws RemoteException, LoginAuthenticationExceptionException {
        //TODO what is localhost here
        if (authenticationAdminStub.login(userName, password, "localhost")) {
            log.info("Successfully authenticated for tenant : " + tenant);

            ServiceContext serviceContext = authenticationAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        } else {
            log.error("Authentication failure for tenant : " + tenant);
            return null;
        }
    }

    public void logOut() throws RemoteException, LogoutAuthenticationExceptionException {
        authenticationAdminStub.logout();
    }
}
