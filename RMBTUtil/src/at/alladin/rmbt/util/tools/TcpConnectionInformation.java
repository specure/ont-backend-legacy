/*******************************************************************************
 * Copyright 2015 SPECURE GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package at.alladin.rmbt.util.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * holder for tcp connection information
 *
 * @author lb
 */
public class TcpConnectionInformation extends ConnectionInformation {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(TcpConnectionInformation.class);

    /**
     * parses a linux /proc/net/ address string and returns an object
     *
     * @param str
     * @param addrSize
     * @return
     */
    public static TcpConnectionInformation parseAndroid(String str, int addrSize) {
        TcpConnectionInformation conn = new TcpConnectionInformation();

        String[] token = str.trim().replaceAll(" +", " ").split(" ");
        if (token.length < 12)
            return null;

        try {
            logger.debug(str);
            logger.debug(token[1]);
            logger.debug(token[2]);
            conn.setLocalAddr(getAddress(addrSize, token[1]));
            conn.setRemoteAddr(getAddress(addrSize, token[2]));
            conn.setConnectionState(ConnectionState.values()[Integer.parseInt(token[3], 16)]);
            conn.setProtocolType(addrSize == 16 ? ProtocolType.TCP6 : ProtocolType.TCP);
        } catch (Exception e) {
            logger.error(e.getMessage());
            //return null in case of an exception
            return null;
        }

        return conn;
    }
}

