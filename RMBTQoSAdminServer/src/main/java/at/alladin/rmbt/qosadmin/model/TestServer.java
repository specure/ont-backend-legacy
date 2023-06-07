package at.alladin.rmbt.qosadmin.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Entity
@Table(name = "test_server")
public class TestServer implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long uid;

    @Column
    String name;

    @Column(name = "web_address")
    String webAddress;

    @Column
    Integer port;

    @Column(name = "port_ssl")
    Integer portSsl;

    @Column
    String city;

    @Column
    String country;

    @Column(name = "geo_lat")
    Double geoLat;

    @Column(name = "geo_long")
    Double geoLong;

    @Column
    @Type(type = "at.alladin.rmbt.qosadmin.model.types.GeometryType")
    String location;

    @Column(name = "web_address_ipv4")
    String webAddressIpv4;

    @Column(name = "web_address_ipv6")
    String webAddressIpv6;

    public TestServer() {
    }

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

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPortSsl() {
        return portSsl;
    }

    public void setPortSsl(Integer portSsl) {
        this.portSsl = portSsl;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(Double geoLat) {
        this.geoLat = geoLat;
    }

    public Double getGeoLong() {
        return geoLong;
    }

    public void setGeoLong(Double geoLong) {
        this.geoLong = geoLong;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebAddressIpv4() {
        return webAddressIpv4;
    }

    public void setWebAddressIpv4(String webAddressIpv4) {
        this.webAddressIpv4 = webAddressIpv4;
    }

    public String getWebAddressIpv6() {
        return webAddressIpv6;
    }

    public void setWebAddressIpv6(String webAddressIpv6) {
        this.webAddressIpv6 = webAddressIpv6;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result + ((geoLat == null) ? 0 : geoLat.hashCode());
        result = prime * result + ((geoLong == null) ? 0 : geoLong.hashCode());
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        result = prime * result + ((portSsl == null) ? 0 : portSsl.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        result = prime * result
                + ((webAddress == null) ? 0 : webAddress.hashCode());
        result = prime * result
                + ((webAddressIpv4 == null) ? 0 : webAddressIpv4.hashCode());
        result = prime * result
                + ((webAddressIpv6 == null) ? 0 : webAddressIpv6.hashCode());
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
        TestServer other = (TestServer) obj;
        if (city == null) {
            if (other.city != null)
                return false;
        } else if (!city.equals(other.city))
            return false;
        if (country == null) {
            if (other.country != null)
                return false;
        } else if (!country.equals(other.country))
            return false;
        if (geoLat == null) {
            if (other.geoLat != null)
                return false;
        } else if (!geoLat.equals(other.geoLat))
            return false;
        if (geoLong == null) {
            if (other.geoLong != null)
                return false;
        } else if (!geoLong.equals(other.geoLong))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (port == null) {
            if (other.port != null)
                return false;
        } else if (!port.equals(other.port))
            return false;
        if (portSsl == null) {
            if (other.portSsl != null)
                return false;
        } else if (!portSsl.equals(other.portSsl))
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        if (webAddress == null) {
            if (other.webAddress != null)
                return false;
        } else if (!webAddress.equals(other.webAddress))
            return false;
        if (webAddressIpv4 == null) {
            if (other.webAddressIpv4 != null)
                return false;
        } else if (!webAddressIpv4.equals(other.webAddressIpv4))
            return false;
        if (webAddressIpv6 == null) {
            if (other.webAddressIpv6 != null)
                return false;
        } else if (!webAddressIpv6.equals(other.webAddressIpv6))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TestServer [uid=" + uid + ", name=" + name + ", webAddress="
                + webAddress + ", port=" + port + ", portSsl=" + portSsl
                + ", city=" + city + ", country=" + country + ", geoLat="
                + geoLat + ", geoLong=" + geoLong + ", location=" + location
                + ", webAddressIpv4=" + webAddressIpv4 + ", webAddressIpv6="
                + webAddressIpv6 + "]";
    }
}
