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
package at.alladin.rmbt.shared.db.model;

import at.alladin.rmbt.shared.db.annotation.DatabasePrimaryKey;
import com.google.gson.annotations.SerializedName;

public class AdvertisedSpeedOption {

    @DatabasePrimaryKey
    @SerializedName("uid")
    Long uid;

    @SerializedName("name")
    String name;

    @SerializedName("min_speed_down_kbps")
    Long minSpeedDownKbps;

    @SerializedName("max_speed_down_kbps")
    Long maxSpeedDownKbps;

    @SerializedName("min_speed_up_kbps")
    Long minSpeedUpKbps;

    @SerializedName("max_speed_up_kbps")
    Long maxSpeedUpKbps;

    @SerializedName("enabled")
    Boolean isEnabled;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMinSpeedDownKbps() {
        return minSpeedDownKbps;
    }

    public void setMinSpeedDownKbps(Long minSpeedDownKbps) {
        this.minSpeedDownKbps = minSpeedDownKbps;
    }

    public Long getMaxSpeedDownKbps() {
        return maxSpeedDownKbps;
    }

    public void setMaxSpeedDownKbps(Long maxSpeedDownKbps) {
        this.maxSpeedDownKbps = maxSpeedDownKbps;
    }

    public Long getMinSpeedUpKbps() {
        return minSpeedUpKbps;
    }

    public void setMinSpeedUpKbps(Long minSpeedUpKbps) {
        this.minSpeedUpKbps = minSpeedUpKbps;
    }

    public Long getMaxSpeedUpKbps() {
        return maxSpeedUpKbps;
    }

    public void setMaxSpeedUpKbps(Long maxSpeedUpKbps) {
        this.maxSpeedUpKbps = maxSpeedUpKbps;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public String toString() {
        return "AdvertisedSpeedOption [uid=" + uid + ", name=" + name + ", minSpeedDownKbps=" + minSpeedDownKbps
                + ", maxSpeedDownKbps=" + maxSpeedDownKbps + ", minSpeedUpKbps=" + minSpeedUpKbps + ", maxSpeedUpKbps="
                + maxSpeedUpKbps + ", isEnabled=" + isEnabled + "]";
    }
}
