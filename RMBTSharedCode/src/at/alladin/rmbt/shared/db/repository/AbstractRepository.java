/*******************************************************************************
 * Copyright 2016 SPECURE GmbH
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
package at.alladin.rmbt.shared.db.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;

public class AbstractRepository {

    protected final Connection connection;

    protected final Gson gson;

    public AbstractRepository(Connection conn) {
        GsonBuilder gb = new GsonBuilder().
                serializeNulls();

        this.gson = gb.create();
        this.connection = conn;
    }

    public Connection getConnection() {
        return connection;
    }

    public Gson getGson() {
        return gson;
    }
}
