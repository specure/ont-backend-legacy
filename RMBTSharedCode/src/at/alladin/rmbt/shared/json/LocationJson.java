package at.alladin.rmbt.shared.json;

import org.json.JSONObject;

/**
 * @author tomas.hreben
 * @date 27.7.2017
 */
public class LocationJson {

    private Long tstamp;
    private Long time_ns;
    private Double geo_lat;
    private Double geo_long;
    private Double accuracy;
    private Double altitude;
    private Double bearing;
    private Double speed;
    private String provider;

    public LocationJson(Long tstamp, Long time_ns, Double geo_lat, Double geo_long, Double accuracy, Double altitude,
                        Double bearing, Double speed, String provider) {
        this.tstamp = tstamp;
        this.time_ns = time_ns;
        this.geo_lat = geo_lat;
        this.geo_long = geo_long;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.bearing = bearing;
        this.speed = speed;
        this.provider = provider;
    }

    public LocationJson(JSONObject request) {
        this.tstamp = request.optLong("tstamp");
        this.time_ns = request.optLong("time_ns");
        if (request.has("geo_lat")) {
            this.geo_lat = request.optDouble("geo_lat");
        } else {
            this.geo_lat = request.optDouble("lat");
        }
        if (request.has("geo_long")) {
            this.geo_long = request.optDouble("geo_long");
        } else {
            this.geo_long = request.optDouble("long");
        }
        this.accuracy = request.optDouble("accuracy");
        this.altitude = request.optDouble("altitude");
        this.bearing = request.optDouble("bearing");
        this.speed = request.optDouble("speed");
        this.provider = request.optString("provider");
    }


    public LocationJson() {
        this.tstamp = null;
        this.time_ns = null;
        this.geo_lat = Double.MAX_VALUE;
        this.geo_long = Double.MAX_VALUE;
        this.accuracy = null;
        this.altitude = null;
        this.bearing = null;
        this.speed = null;
        this.provider = null;
    }

    public Long getTstamp() {
        return tstamp;
    }

    public void setTstamp(Long tstamp) {
        this.tstamp = tstamp;
    }

    public Long getTime_ns() {
        return time_ns;
    }

    public void setTime_ns(Long time_ns) {
        this.time_ns = time_ns;
    }

    public Double getGeo_lat() {
        return geo_lat;
    }

    public void setGeo_lat(Double geo_lat) {
        this.geo_lat = geo_lat;
    }

    public Double getGeo_long() {
        return geo_long;
    }

    public void setGeo_long(Double geo_long) {
        this.geo_long = geo_long;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "LocationJson [tstamp=" + tstamp + ", time_ns=" + time_ns + ", geo_lat=" + geo_lat + ", geo_long=" + geo_long + ", accuracy=" + accuracy
                + ", altitude=" + altitude + ", bearing=" + bearing + ", speed=" + speed + ", provider="
                + provider + "]";
    }

}
