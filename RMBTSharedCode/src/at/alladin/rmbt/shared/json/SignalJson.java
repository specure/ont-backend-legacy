package at.alladin.rmbt.shared.json;

public final class SignalJson {

    private Long time;
    private Integer network_type_id;
    private Integer lte_rsrp;           // signal strength value as RSRP, used in LTE
    private Integer lte_rsrq;           // signal quality RSRQ, used in LTE
    private Integer lte_rssnr;
    private Integer lte_cqi;
    private Long time_ns;            // relative ts in ns
    private Integer signal_strength;
    private Integer gsm_bit_error_rate;
    private Integer ss_rsrp;


    public SignalJson() {
        super();
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getNetwork_type_id() {
        return network_type_id;
    }

    public void setNetwork_type_id(Integer network_type_id) {
        this.network_type_id = network_type_id;
    }

    public Integer getLte_rsrp() {
        return lte_rsrp;
    }

    public void setLte_rsrp(Integer lte_rsrp) {
        this.lte_rsrp = lte_rsrp;
    }

    public Integer getLte_rsrq() {
        return lte_rsrq;
    }

    public void setLte_rsrq(Integer lte_rsrq) {
        this.lte_rsrq = lte_rsrq;
    }

    public Integer getLte_rssnr() {
        return lte_rssnr;
    }

    public void setLte_rssnr(Integer lte_rssnr) {
        this.lte_rssnr = lte_rssnr;
    }

    public Integer getLte_cqi() {
        return lte_cqi;
    }

    public void setLte_cqi(Integer lte_cqi) {
        this.lte_cqi = lte_cqi;
    }

    public Long getTime_ns() {
        return time_ns;
    }

    public void setTime_ns(Long time_ns) {
        this.time_ns = time_ns;
    }

    public Integer getSignal_strength() {
        return signal_strength;
    }

    public void setSignal_strength(Integer signal_strength) {
        this.signal_strength = signal_strength;
    }

    public Integer getGsm_bit_error_rate() {
        return gsm_bit_error_rate;
    }

    public void setGsm_bit_error_rate(Integer gsm_bit_error_rate) {
        this.gsm_bit_error_rate = gsm_bit_error_rate;
    }

    public Integer getSs_rsrp() {
        return ss_rsrp;
    }

    public void setSs_rsrp(Integer ss_rsrp) {
        this.ss_rsrp = ss_rsrp;
    }
}
