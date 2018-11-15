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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class provides functionality to read from a given property files.
 */
public class PropertyReader {
    private static final Logger log = Logger.getLogger(PropertyReader.class);

    public static Properties loadProperties (String fileName) throws IOException {
        log.info("Loading properties from : " + fileName);
        InputStream inputStream = PropertyReader.class.getClassLoader().getResourceAsStream(fileName);
        Properties properties = new Properties();
        if (inputStream != null) {
            properties.load(inputStream);
            return properties;
        } else {
            log.error("Null stream returned for file : " + fileName);
            System.exit(1);
        }
        return null;
    }
}
