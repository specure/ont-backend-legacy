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
package at.alladin.rmbt.statisticServer;

import com.google.common.base.Strings;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StatisticParameters implements Serializable, Funnel<StatisticParameters> {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(StatisticParameters.class);

    private static final long serialVersionUID = 1L;

    private final String lang;
    private final float quantile;
    private final int maxDevices;
    private final String type;
    private final String networkTypeGroup;
    private final double accuracy;
    private final String country;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public StatisticParameters(String defaultLang, String params) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String _lang = defaultLang;
        float _quantile = 0.5f; // median is default quantile
        int _maxDevices = 100;
        String _type = "mobile";
        String _networkTypeGroup = null;
        double _accuracy = -1;
        String _country = null;
        LocalDate _endDate = LocalDate.now();
        LocalDate _startDate = _endDate.minusDays(90);

        if (params != null && !params.isEmpty())
            // try parse the string to a JSON object
            try {
                logger.debug(params);
                final JSONObject request = new JSONObject(params);
                _lang = request.optString("language", _lang);

                final double __quantile = request.optDouble("metric", Double.NaN);
                if (__quantile >= 0 && __quantile <= 1 || BigDecimal.valueOf(-1.0).equals(BigDecimal.valueOf(__quantile)))
                    _quantile = (float) __quantile;

                final JSONObject __duration = request.getJSONObject("duration");

                final LocalDate __startDate = LocalDate.parse(__duration.optString("startDate"), formatter);
                if (__startDate != null)
                    _startDate = __startDate;

                final LocalDate __endDate = LocalDate.parse(__duration.optString("endDate"), formatter);
                if (__startDate != null)
                    _endDate = __endDate;

                final int __maxDevices = request.optInt("max_devices", 0);
                if (__maxDevices > 0)
                    _maxDevices = __maxDevices;

                final String __type = request.optString("type", null);
                if (__type != null)
                    _type = __type;

                final String __networkTypeGroup = request.optString("network_type_group", null);
                if (__networkTypeGroup != null && !__networkTypeGroup.equalsIgnoreCase("all"))
                    _networkTypeGroup = __networkTypeGroup;

                final double __accuracy = request.optDouble("location_accuracy", -1);
                if (__accuracy != -1)
                    _accuracy = __accuracy;

                final String __country = request.optString("country", null);
                if (__country != null && __country.length() == 2)
                    _country = __country;
            } catch (final JSONException e) {
            }
        lang = _lang;
        quantile = _quantile;
        maxDevices = _maxDevices;
        type = _type;
        networkTypeGroup = _networkTypeGroup;
        accuracy = _accuracy;
        country = _country;
        startDate = _startDate;
        endDate = _endDate;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getLang() {
        return lang;
    }

    public float getQuantile() {
        return quantile;
    }

    public int getMaxDevices() {
        return maxDevices;
    }

    public String getType() {
        return type;
    }

    public String getNetworkTypeGroup() {
        return networkTypeGroup;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public String getCountry() {
        return country;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public void funnel(StatisticParameters o, PrimitiveSink into) {
        into
                .putUnencodedChars(o.getClass().getCanonicalName())
                .putChar(':')
                .putUnencodedChars(Strings.nullToEmpty(o.lang))
                .putFloat(o.quantile)
                .putUnencodedChars(Strings.nullToEmpty(o.type))
                .putInt(o.maxDevices)
                .putUnencodedChars(Strings.nullToEmpty(o.networkTypeGroup))
                .putDouble(o.accuracy)
                .putUnencodedChars(Strings.nullToEmpty(o.country))
                .putLong(o.startDate.toEpochDay())
                .putLong(o.endDate.toEpochDay());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + maxDevices;
        result = prime * result + ((networkTypeGroup == null) ? 0 : networkTypeGroup.hashCode());
        result = prime * result + Float.floatToIntBits(quantile);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((accuracy == -1) ? 0 : (int) accuracy);
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatisticParameters other = (StatisticParameters) obj;
        if (lang == null) {
            if (other.lang != null)
                return false;
        } else if (!lang.equals(other.lang))
            return false;
        if (maxDevices != other.maxDevices)
            return false;
        if (networkTypeGroup == null) {
            if (other.networkTypeGroup != null)
                return false;
        } else if (!networkTypeGroup.equals(other.networkTypeGroup))
            return false;
        if (Float.floatToIntBits(quantile) != Float.floatToIntBits(other.quantile))
            return false;
        if (accuracy != other.accuracy) {
            return false;
        }
        if (country != null && other.country != null && !country.equals(other.country)) {
            return false;
        }
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (startDate == null) {
            if (other.startDate != null)
                return false;
        } else if (!startDate.equals(other.startDate))
            return false;
        if (endDate == null) {
            if (other.endDate != null)
                return false;
        } else if (!endDate.equals(other.endDate))
            return false;
        return true;
    }

}
