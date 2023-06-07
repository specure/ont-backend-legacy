package at.alladin.rmbt.qosadmin.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "as2provider")
public class AsToProvider implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long uid;

    @Column
    Long asn;

    @Column(name = "dns_part")
    String dnsPart;

    @Column(name = "provider_id")
    Long providerId;

    public AsToProvider() {

    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getAsn() {
        return asn;
    }

    public void setAsn(Long asn) {
        this.asn = asn;
    }

    public String getDnsPart() {
        return dnsPart;
    }

    public void setDnsPart(String dnsPart) {
        this.dnsPart = dnsPart;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((asn == null) ? 0 : asn.hashCode());
        result = prime * result + ((dnsPart == null) ? 0 : dnsPart.hashCode());
        result = prime * result
                + ((providerId == null) ? 0 : providerId.hashCode());
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
        AsToProvider other = (AsToProvider) obj;
        if (asn == null) {
            if (other.asn != null)
                return false;
        } else if (!asn.equals(other.asn))
            return false;
        if (dnsPart == null) {
            if (other.dnsPart != null)
                return false;
        } else if (!dnsPart.equals(other.dnsPart))
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
        return true;
    }

    @Override
    public String toString() {
        return "AsToPrivoder [uid=" + uid + ", asn=" + asn + ", dnsPart="
                + dnsPart + ", providerId=" + providerId + "]";
    }
}
