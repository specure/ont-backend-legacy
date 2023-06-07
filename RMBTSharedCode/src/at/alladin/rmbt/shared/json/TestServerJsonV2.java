package at.alladin.rmbt.shared.json;

import at.alladin.rmbt.shared.json.TestServerJson;

public class TestServerJsonV2 extends TestServerJson{

    private Integer distance;
    private String state;
    private String sponsor;
    private String city;

    public TestServerJsonV2(int id, String name, int port, String address, Float distance, String state, String city) {
        super(id, name, port, address);
        this.distance = Math.round(distance);
        this.state = state;
        this.sponsor = name;
        this.city = city;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = Math.round(distance);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "TestServerJson [id=" + getId() + ", name=" + getName() + ", port=" + getPort() + ", address=" + getAddress()
                +", disance=" + distance +", state=" + state +", sponsor=" + sponsor +", city=" + city +"]";
    }
}
