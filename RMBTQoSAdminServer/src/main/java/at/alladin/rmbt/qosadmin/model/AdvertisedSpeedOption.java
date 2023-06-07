package at.alladin.rmbt.qosadmin.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "advertised_speed_option")
public class AdvertisedSpeedOption implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long uid;

    @Column
    String name;

    @Column(name = "min_speed_down_kbps")
    Long minSpeedDownKbps;

    @Column(name = "max_speed_down_kbps")
    Long maxSpeedDownKbps;

    @Column(name = "min_speed_up_kbps")
    Long minSpeedUpKbps;

    @Column(name = "max_speed_up_kbps")
    Long maxSpeedUpKbps;

    @Column(name = "enabled")
    Boolean isEnabled;

    public AdvertisedSpeedOption() {

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((isEnabled == null) ? 0 : isEnabled.hashCode());
        result = prime * result + ((maxSpeedDownKbps == null) ? 0 : maxSpeedDownKbps.hashCode());
        result = prime * result + ((maxSpeedUpKbps == null) ? 0 : maxSpeedUpKbps.hashCode());
        result = prime * result + ((minSpeedDownKbps == null) ? 0 : minSpeedDownKbps.hashCode());
        result = prime * result + ((minSpeedUpKbps == null) ? 0 : minSpeedUpKbps.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
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
        AdvertisedSpeedOption other = (AdvertisedSpeedOption) obj;
        if (isEnabled == null) {
            if (other.isEnabled != null)
                return false;
        } else if (!isEnabled.equals(other.isEnabled))
            return false;
        if (maxSpeedDownKbps == null) {
            if (other.maxSpeedDownKbps != null)
                return false;
        } else if (!maxSpeedDownKbps.equals(other.maxSpeedDownKbps))
            return false;
        if (maxSpeedUpKbps == null) {
            if (other.maxSpeedUpKbps != null)
                return false;
        } else if (!maxSpeedUpKbps.equals(other.maxSpeedUpKbps))
            return false;
        if (minSpeedDownKbps == null) {
            if (other.minSpeedDownKbps != null)
                return false;
        } else if (!minSpeedDownKbps.equals(other.minSpeedDownKbps))
            return false;
        if (minSpeedUpKbps == null) {
            if (other.minSpeedUpKbps != null)
                return false;
        } else if (!minSpeedUpKbps.equals(other.minSpeedUpKbps))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AdvertisedSpeedOption [uid=" + uid + ", name=" + name + ", minSpeedDownKbps=" + minSpeedDownKbps
                + ", maxSpeedDownKbps=" + maxSpeedDownKbps + ", minSpeedUpKbps=" + minSpeedUpKbps + ", maxSpeedUpKbps="
                + maxSpeedUpKbps + ", isEnabled=" + isEnabled + "]";
    }
}
