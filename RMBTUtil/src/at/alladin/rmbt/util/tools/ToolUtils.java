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

import java.io.*;

public class ToolUtils {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ToolUtils.class);

    /**
     * @param file
     * @return
     */
    public static String readFromProc(String file) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        File f = new File(file);
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String currInputLine = null;

            while ((currInputLine = br.readLine()) != null) {
                sb.append(currInputLine);
                sb.append("\n");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        return sb.toString();
    }

}
