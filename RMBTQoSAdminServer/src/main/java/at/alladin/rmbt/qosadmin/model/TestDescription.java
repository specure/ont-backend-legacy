package at.alladin.rmbt.qosadmin.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Entity
@Table(name = "qos_test_desc")
public class TestDescription implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public final static Pattern KEY_PATTERN = Pattern.compile("^([a-zA-Z0-9]*)\\.");

	/*
	 *  uid serial NOT NULL
	  	desc_key text
	  	value text
	  	lang text
	 */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long uid;

    @Column(name = "desc_key")
    String key = "";

    @Column(name = "value")
    String value = "";

    @Column(name = "lang")
    String languageCode;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result
                + ((languageCode == null) ? 0 : languageCode.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        TestDescription other = (TestDescription) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (languageCode == null) {
            if (other.languageCode != null)
                return false;
        } else if (!languageCode.equals(other.languageCode))
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "QoSTestDescription [uid=" + uid + ", key=" + key + ", value="
                + value + ", languageCode=" + languageCode + "]";
    }

    ///////////////////////////////////
    // JSP methods:
    ///////////////////////////////////
    public String getHeadKey() {
        final Matcher matcher = KEY_PATTERN.matcher(key);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return key;
    }
}
