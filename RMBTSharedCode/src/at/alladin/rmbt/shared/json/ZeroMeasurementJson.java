package at.alladin.rmbt.shared.json;

import java.util.List;

public final class ZeroMeasurementJson {

    private String client_uuid;
    private String client_name;
    private String client_version;
    private String client_language;
    private Long time;
    private String timezone;
    //private String test_token;//will be generated on server side
    private String uuid;
    private String plattform;
    private String product;
    private String api_level;
    private String telephony_network_operator;
    private String client_software_version;
    private String telephony_network_is_roaming;
    private String os_version;
    private String telephony_network_country;
    private String network_type;
    private String telephony_network_operator_name;
    private String telephony_network_sim_operator_name;
    private String model;
    private String telephony_network_sim_operator;
    private String device;
    private String telephony_phone_type;
    private String telephony_data_state;
    private String telephony_network_sim_country;
    private List<LocationJson> geoLocations;
    private List<CellLocationJson> cellLocations;
    private List<SignalJson> signals;

    public ZeroMeasurementJson() {
        super();
    }

    public String getClient_uuid() {
        return client_uuid;
    }

    public void setClient_uuid(String client_uuid) {
        this.client_uuid = client_uuid;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getClient_version() {
        return client_version;
    }

    public void setClient_version(String client_version) {
        this.client_version = client_version;
    }

    public String getClient_language() {
        return client_language;
    }

    public void setClient_language(String client_language) {
        this.client_language = client_language;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    //    public String getTest_token() {
//        return test_token;
//    }
//
//    public void setTest_token(String test_token) {
//        this.test_token = test_token;
//    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPlattform() {
        return plattform;
    }

    public void setPlattform(String plattform) {
        this.plattform = plattform;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getApi_level() {
        return api_level;
    }

    public void setApi_level(String api_level) {
        this.api_level = api_level;
    }

    public String getTelephony_network_operator() {
        return telephony_network_operator;
    }

    public void setTelephony_network_operator(String telephony_network_operator) {
        this.telephony_network_operator = telephony_network_operator;
    }

    public String getClient_software_version() {
        return client_software_version;
    }

    public void setClient_software_version(String client_software_version) {
        this.client_software_version = client_software_version;
    }

    public String getTelephony_network_is_roaming() {
        return telephony_network_is_roaming;
    }

    public void setTelephony_network_is_roaming(String telephony_network_is_roaming) {
        this.telephony_network_is_roaming = telephony_network_is_roaming;
    }

    public String getOs_version() {
        return os_version;
    }

    public void setOs_version(String os_version) {
        this.os_version = os_version;
    }

    public String getTelephony_network_country() {
        return telephony_network_country;
    }

    public void setTelephony_network_country(String telephony_network_country) {
        this.telephony_network_country = telephony_network_country;
    }

    public String getNetwork_type() {
        return network_type;
    }

    public void setNetwork_type(String network_type) {
        this.network_type = network_type;
    }

    public String getTelephony_network_operator_name() {
        return telephony_network_operator_name;
    }

    public void setTelephony_network_operator_name(String telephony_network_operator_name) {
        this.telephony_network_operator_name = telephony_network_operator_name;
    }

    public String getTelephony_network_sim_operator_name() {
        return telephony_network_sim_operator_name;
    }

    public void setTelephony_network_sim_operator_name(String telephony_network_sim_operator_name) {
        this.telephony_network_sim_operator_name = telephony_network_sim_operator_name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTelephony_network_sim_operator() {
        return telephony_network_sim_operator;
    }

    public void setTelephony_network_sim_operator(String telephony_network_sim_operator) {
        this.telephony_network_sim_operator = telephony_network_sim_operator;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getTelephony_phone_type() {
        return telephony_phone_type;
    }

    public void setTelephony_phone_type(String telephony_phone_type) {
        this.telephony_phone_type = telephony_phone_type;
    }

    public String getTelephony_data_state() {
        return telephony_data_state;
    }

    public void setTelephony_data_state(String telephony_data_state) {
        this.telephony_data_state = telephony_data_state;
    }

    public String getTelephony_network_sim_country() {
        return telephony_network_sim_country;
    }

    public void setTelephony_network_sim_country(String telephony_network_sim_country) {
        this.telephony_network_sim_country = telephony_network_sim_country;
    }

    public List<LocationJson> getGeoLocations() {
        return geoLocations;
    }

    public void setGeoLocations(List<LocationJson> geoLocations) {
        this.geoLocations = geoLocations;
    }

    public List<CellLocationJson> getCellLocations() {
        return cellLocations;
    }

    public void setCellLocations(List<CellLocationJson> cellLocations) {
        this.cellLocations = cellLocations;
    }

    public List<SignalJson> getSignals() {
        return signals;
    }

    public void setSignals(List<SignalJson> signals) {
        this.signals = signals;
    }
}
