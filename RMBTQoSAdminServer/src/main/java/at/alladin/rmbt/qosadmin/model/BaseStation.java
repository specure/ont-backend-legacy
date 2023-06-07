package at.alladin.rmbt.qosadmin.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "base_stations")
public class BaseStation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long uid;

    @Column
    String technology;

    @Column(name = "location_name")
    String locationName;

    @Column
    Double longitude;

    @Column
    Double latitude;

    @Column
    Integer mnc;

    @Column
    Integer ci;

    @Column
    Integer lac;

    @Column
    Integer enb;

    @Column(name = "physical_cell_id")
    Integer physicalCellId;

    @Column(name = "eci")
    Integer eci;

    @Column
    Integer tac;

    @Column(name = "rf_band")
    String rfBand;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getMnc() {
        return mnc;
    }

    public void setMnc(Integer mnc) {
        this.mnc = mnc;
    }

    public Integer getCi() {
        return ci;
    }

    public void setCi(Integer ci) {
        this.ci = ci;
    }

    public Integer getLac() {
        return lac;
    }

    public void setLac(Integer lac) {
        this.lac = lac;
    }

    public Integer getEnb() {
        return enb;
    }

    public void setEnb(Integer enb) {
        this.enb = enb;
    }

    public Integer getPhysicalCellId() {
        return physicalCellId;
    }

    public void setPhysicalCellId(Integer physicalCellId) {
        this.physicalCellId = physicalCellId;
    }

    public Integer getEci() {
        return eci;
    }

    public void setEci(Integer eci) {
        this.eci = eci;
    }

    public Integer getTac() {
        return tac;
    }

    public void setTac(Integer tac) {
        this.tac = tac;
    }

    public String getRfBand() {
        return rfBand;
    }

    public void setRfBand(String rfBand) {
        this.rfBand = rfBand;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eci == null) ? 0 : eci.hashCode());
        result = prime * result + ((ci == null) ? 0 : ci.hashCode());
        result = prime * result + ((enb == null) ? 0 : enb.hashCode());
        result = prime * result + ((lac == null) ? 0 : lac.hashCode());
        result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
        result = prime * result + ((locationName == null) ? 0 : locationName.hashCode());
        result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
        result = prime * result + ((mnc == null) ? 0 : mnc.hashCode());
        result = prime * result + ((physicalCellId == null) ? 0 : physicalCellId.hashCode());
        result = prime * result + ((rfBand == null) ? 0 : rfBand.hashCode());
        result = prime * result + ((tac == null) ? 0 : tac.hashCode());
        result = prime * result + ((technology == null) ? 0 : technology.hashCode());
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
        BaseStation other = (BaseStation) obj;
        if (eci == null) {
            if (other.eci != null)
                return false;
        } else if (!eci.equals(other.eci))
            return false;
        if (ci == null) {
            if (other.ci != null)
                return false;
        } else if (!ci.equals(other.ci))
            return false;
        if (enb == null) {
            if (other.enb != null)
                return false;
        } else if (!enb.equals(other.enb))
            return false;
        if (lac == null) {
            if (other.lac != null)
                return false;
        } else if (!lac.equals(other.lac))
            return false;
        if (latitude == null) {
            if (other.latitude != null)
                return false;
        } else if (!latitude.equals(other.latitude))
            return false;
        if (locationName == null) {
            if (other.locationName != null)
                return false;
        } else if (!locationName.equals(other.locationName))
            return false;
        if (longitude == null) {
            if (other.longitude != null)
                return false;
        } else if (!longitude.equals(other.longitude))
            return false;
        if (mnc == null) {
            if (other.mnc != null)
                return false;
        } else if (!mnc.equals(other.mnc))
            return false;
        if (physicalCellId == null) {
            if (other.physicalCellId != null)
                return false;
        } else if (!physicalCellId.equals(other.physicalCellId))
            return false;
        if (rfBand == null) {
            if (other.rfBand != null)
                return false;
        } else if (!rfBand.equals(other.rfBand))
            return false;
        if (tac == null) {
            if (other.tac != null)
                return false;
        } else if (!tac.equals(other.tac))
            return false;
        if (technology == null) {
            if (other.technology != null)
                return false;
        } else if (!technology.equals(other.technology))
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
        StringBuilder builder = new StringBuilder();
        builder.append("BaseStation [uid=").append(uid).append(", technology=").append(technology)
                .append(", locationName=").append(locationName).append(", longitude=").append(longitude)
                .append(", latitude=").append(latitude).append(", mnc=").append(mnc).append(", ci=").append(ci)
                .append(", lac=").append(lac).append(", enb=").append(enb).append(", physicalCellId=")
                .append(physicalCellId).append(", eci=").append(eci).append(", tac=").append(tac)
                .append(", rfBand=").append(rfBand).append("]");
        return builder.toString();
    }
}
