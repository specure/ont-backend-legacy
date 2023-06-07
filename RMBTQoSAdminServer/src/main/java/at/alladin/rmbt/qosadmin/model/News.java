package at.alladin.rmbt.qosadmin.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Entity
@Table(name = "news")
public class News implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

	/*
	  uid serial NOT NULL,
	  "time" timestamp with time zone NOT NULL,
	  title_en text,
	  title_de text,
	  text_en text,
	  text_de text,
	  active boolean NOT NULL DEFAULT false,
	  force boolean NOT NULL DEFAULT false,
	  plattform text,
	  max_software_version_code integer,
	  min_software_version_code integer,
	  uuid uuid,
	*/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long uid;

    @Column
    @Type(type = "at.alladin.rmbt.qosadmin.model.types.TimestampType")
    String time;

    @Column(name = "title_en")
    String titleEn;

    @Column(name = "title_de")
    String titleDe;

    @Column(name = "text_en")
    String textEn;

    @Column(name = "text_de")
    String textDe;

    @Column
    Boolean active;

    @Column
    Boolean force;

    @Column
    String plattform;

    @Column(name = "max_software_version_code")
    Long maxSoftwareVersionCode;

    @Column(name = "min_software_version_code")
    Long minSoftwareVersionCode;

    @Column
    @Type(type = "at.alladin.rmbt.qosadmin.model.types.UuidType")
    String uuid;

    public News() {
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getTitleDe() {
        return titleDe;
    }

    public void setTitleDe(String titleDe) {
        this.titleDe = titleDe;
    }

    public String getTextEn() {
        return textEn;
    }

    public void setTextEn(String textEn) {
        this.textEn = textEn;
    }

    public String getTextDe() {
        return textDe;
    }

    public void setTextDe(String textDe) {
        this.textDe = textDe;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public String getPlattform() {
        return plattform;
    }

    public void setPlattform(String plattform) {
        this.plattform = plattform;
    }

    public Long getMaxSoftwareVersionCode() {
        return maxSoftwareVersionCode;
    }

    public void setMaxSoftwareVersionCode(Long maxSoftwareVersionCode) {
        this.maxSoftwareVersionCode = maxSoftwareVersionCode;
    }

    public Long getMinSoftwareVersionCode() {
        return minSoftwareVersionCode;
    }

    public void setMinSoftwareVersionCode(Long minSoftwareVersionCode) {
        this.minSoftwareVersionCode = minSoftwareVersionCode;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((active == null) ? 0 : active.hashCode());
        result = prime * result + ((force == null) ? 0 : force.hashCode());
        result = prime
                * result
                + ((maxSoftwareVersionCode == null) ? 0
                : maxSoftwareVersionCode.hashCode());
        result = prime
                * result
                + ((minSoftwareVersionCode == null) ? 0
                : minSoftwareVersionCode.hashCode());
        result = prime * result
                + ((plattform == null) ? 0 : plattform.hashCode());
        result = prime * result + ((textDe == null) ? 0 : textDe.hashCode());
        result = prime * result + ((textEn == null) ? 0 : textEn.hashCode());
        result = prime * result
                + ((time == null) ? 0 : time.hashCode());
        result = prime * result + ((titleDe == null) ? 0 : titleDe.hashCode());
        result = prime * result + ((titleEn == null) ? 0 : titleEn.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
        News other = (News) obj;
        if (active == null) {
            if (other.active != null)
                return false;
        } else if (!active.equals(other.active))
            return false;
        if (force == null) {
            if (other.force != null)
                return false;
        } else if (!force.equals(other.force))
            return false;
        if (maxSoftwareVersionCode == null) {
            if (other.maxSoftwareVersionCode != null)
                return false;
        } else if (!maxSoftwareVersionCode.equals(other.maxSoftwareVersionCode))
            return false;
        if (minSoftwareVersionCode == null) {
            if (other.minSoftwareVersionCode != null)
                return false;
        } else if (!minSoftwareVersionCode.equals(other.minSoftwareVersionCode))
            return false;
        if (plattform == null) {
            if (other.plattform != null)
                return false;
        } else if (!plattform.equals(other.plattform))
            return false;
        if (textDe == null) {
            if (other.textDe != null)
                return false;
        } else if (!textDe.equals(other.textDe))
            return false;
        if (textEn == null) {
            if (other.textEn != null)
                return false;
        } else if (!textEn.equals(other.textEn))
            return false;
        if (time == null) {
            if (other.time != null)
                return false;
        } else if (!time.equals(other.time))
            return false;
        if (titleDe == null) {
            if (other.titleDe != null)
                return false;
        } else if (!titleDe.equals(other.titleDe))
            return false;
        if (titleEn == null) {
            if (other.titleEn != null)
                return false;
        } else if (!titleEn.equals(other.titleEn))
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "News [uid=" + uid + ", timestamp=" + time + ", titleEn="
                + titleEn + ", titleDe=" + titleDe + ", textEn=" + textEn
                + ", textDe=" + textDe + ", active=" + active + ", force="
                + force + ", plattform=" + plattform
                + ", maxSoftwareVersionCode=" + maxSoftwareVersionCode
                + ", minSoftwareVersionCode=" + minSoftwareVersionCode
                + ", uuid=" + uuid + "]";
    }
}
