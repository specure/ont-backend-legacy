package at.alladin.rmbt.shared.json;

public final class CellLocationJson {

    private Long time;
    private Long time_ns;
    private Integer location_id;
    private Integer area_code;
    private Integer primary_scrambling_code;

    public CellLocationJson() {
        super();
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getTime_ns() {
        return time_ns;
    }

    public void setTime_ns(Long time_ns) {
        this.time_ns = time_ns;
    }

    public Integer getLocation_id() {
        return location_id;
    }

    public void setLocation_id(Integer location_id) {
        this.location_id = location_id;
    }

    public Integer getArea_code() {
        return area_code;
    }

    public void setArea_code(Integer area_code) {
        this.area_code = area_code;
    }

    public Integer getPrimary_scrambling_code() {
        return primary_scrambling_code;
    }

    public void setPrimary_scrambling_code(Integer primary_scrambling_code) {
        this.primary_scrambling_code = primary_scrambling_code;
    }
}
