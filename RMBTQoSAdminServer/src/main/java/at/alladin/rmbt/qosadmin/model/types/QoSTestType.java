package at.alladin.rmbt.qosadmin.model.types;

import java.util.ArrayList;
import java.util.List;

/**
 * needed for cast
 *
 * @author alladin-IT (lb@alladin.at)
 */
public enum QoSTestType {
    WEBSITE("website"),
    HTTP_PROXY("http_proxy"),
    NON_TRANSPARENT_PROXY("non_transparent_proxy"),
    DNS("dns"),
    TCP("tcp"),
    UDP("udp"),
    VOIP("voip"),
    TRACEROUTE("traceroute");

    public static List<String> VALUE_LIST;
    public static List<String> NAME_LIST;

    static {
        VALUE_LIST = new ArrayList<>();
        for (QoSTestType t : QoSTestType.values()) {
            VALUE_LIST.add(t.getValue());
        }
        NAME_LIST = new ArrayList<>();
        for (QoSTestType t : QoSTestType.values()) {
            NAME_LIST.add(t.toString());
        }
    }

    protected String value;

    private QoSTestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
