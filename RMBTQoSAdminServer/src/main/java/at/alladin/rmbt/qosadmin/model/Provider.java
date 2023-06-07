package at.alladin.rmbt.qosadmin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "provider")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Provider implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long uid;

    @Column
    String name;

    @Column(name = "mcc_mnc")
    String mccMnc;

    @Column(name = "shortname")
    String shortName;

    @Column(name = "map_filter")
    Boolean mapFilter = false;

    public Provider() {

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

    public String getMccMnc() {
        return mccMnc;
    }

    public void setMccMnc(String mccMnc) {
        this.mccMnc = mccMnc;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Boolean getMapFilter() {
        return mapFilter;
    }

    public void setMapFilter(Boolean mapFilter) {
        this.mapFilter = mapFilter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mapFilter == null) ? 0 : mapFilter.hashCode());
        result = prime * result + ((mccMnc == null) ? 0 : mccMnc.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((shortName == null) ? 0 : shortName.hashCode());
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
        Provider other = (Provider) obj;
        if (mapFilter == null) {
            if (other.mapFilter != null)
                return false;
        } else if (!mapFilter.equals(other.mapFilter))
            return false;
        if (mccMnc == null) {
            if (other.mccMnc != null)
                return false;
        } else if (!mccMnc.equals(other.mccMnc))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (shortName == null) {
            if (other.shortName != null)
                return false;
        } else if (!shortName.equals(other.shortName))
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
        return "Provider [uid=" + uid + ", name=" + name + ", mccMnc=" + mccMnc
                + ", shortName=" + shortName + ", mapFilter=" + mapFilter + "]";
    }
}
