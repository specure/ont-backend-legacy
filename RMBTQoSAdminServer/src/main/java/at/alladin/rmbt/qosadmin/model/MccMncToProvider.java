package at.alladin.rmbt.qosadmin.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "mccmnc2provider")
public class MccMncToProvider implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long uid;

    @Column(name = "mcc_mnc_sim")
    String mccMncSim;

    @Column(name = "provider_id")
    Long providerId;

    @Column(name = "mcc_mnc_network")
    String mccMncNetwork;

    @Column(name = "valid_from")
    Date validFrom;

    @Column(name = "valid_to")
    Date validTo;

    public MccMncToProvider() {

    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getMccMncSim() {
        return mccMncSim;
    }

    public void setMccMncSim(String mccMncSim) {
        this.mccMncSim = mccMncSim;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getMccMncNetwork() {
        return mccMncNetwork;
    }

    public void setMccMncNetwork(String mccMncNetwork) {
        this.mccMncNetwork = mccMncNetwork;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mccMncNetwork == null) ? 0 : mccMncNetwork.hashCode());
        result = prime * result
                + ((mccMncSim == null) ? 0 : mccMncSim.hashCode());
        result = prime * result
                + ((providerId == null) ? 0 : providerId.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        result = prime * result
                + ((validFrom == null) ? 0 : validFrom.hashCode());
        result = prime * result + ((validTo == null) ? 0 : validTo.hashCode());
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
        MccMncToProvider other = (MccMncToProvider) obj;
        if (mccMncNetwork == null) {
            if (other.mccMncNetwork != null)
                return false;
        } else if (!mccMncNetwork.equals(other.mccMncNetwork))
            return false;
        if (mccMncSim == null) {
            if (other.mccMncSim != null)
                return false;
        } else if (!mccMncSim.equals(other.mccMncSim))
            return false;
        if (providerId == null) {
            if (other.providerId != null)
                return false;
        } else if (!providerId.equals(other.providerId))
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        if (validFrom == null) {
            if (other.validFrom != null)
                return false;
        } else if (!validFrom.equals(other.validFrom))
            return false;
        if (validTo == null) {
            if (other.validTo != null)
                return false;
        } else if (!validTo.equals(other.validTo))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MccMncToProvider [uid=" + uid + ", mccMncSim=" + mccMncSim
                + ", providerId=" + providerId + ", mccMncNetwork="
                + mccMncNetwork + ", validFrom=" + validFrom + ", validTo="
                + validTo + "]";
    }
}
