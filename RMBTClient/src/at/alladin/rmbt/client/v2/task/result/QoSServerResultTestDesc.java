/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
 ******************************************************************************/
package at.alladin.rmbt.client.v2.task.result;

import java.io.Serializable;

/**
 * @author lb
 */
public class QoSServerResultTestDesc implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private QoSTestResultEnum resultType;
    private String description;
    private String name;

    public QoSServerResultTestDesc(QoSTestResultEnum type, String description, String name) {
        this.resultType = type;
        this.description = description;
        this.name = name;
    }

    public QoSTestResultEnum getResultType() {
        return resultType;
    }

    public void setResultType(QoSTestResultEnum resultType) {
        this.resultType = resultType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "QoSServerResultTestDesc [resultType=" + resultType
                + ", description=" + description + ", name=" + name + "]";
    }

}