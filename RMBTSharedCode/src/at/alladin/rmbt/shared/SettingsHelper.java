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
package at.alladin.rmbt.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsHelper {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(SettingsHelper.class);

    private static final String SQL_SETTINGS =
            "SELECT value FROM settings WHERE key=? AND (lang IS NULL OR lang = '' OR lang = ?)"
                    + " ORDER BY lang DESC NULLS LAST LIMIT 1";

    public static String getSetting(Connection conn, String key) {
        return getSetting(conn, key, null);
    }

    // TODO: add caching!
    public static String getSetting(Connection conn, String key, String lang) {
        if (conn == null)
            return null;


        try (final PreparedStatement st = conn.prepareStatement(
                SQL_SETTINGS);) {

            st.setString(1, key);
            st.setString(2, lang);

            try (final ResultSet rs = st.executeQuery();) {

                if (rs != null && rs.next())
                    return rs.getString("value");
            }
            return null;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
