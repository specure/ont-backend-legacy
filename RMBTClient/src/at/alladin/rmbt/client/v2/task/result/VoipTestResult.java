package at.alladin.rmbt.client.v2.task.result;

public class VoipTestResult {


    public static final String JSON_OBJECT_IDENTIFIER = "jpl";

    /*****************************/
    /**         IN              **/
    /*****************************/
    private Integer voip_result_in_num_packets = null;
    private Integer voip_result_in_long_seq = null;
    private Integer voip_result_in_short_seq = null;
    private Long voip_result_in_mean_jitter = null;
    private Long voip_result_in_max_jitter = null;
    private Integer voip_result_in_sequence_error = null;
    private Long voip_result_in_skew = null;
    private Long voip_result_in_max_delta = null;


    /*****************************/
    /**         OUT             **/
    /*****************************/
    private Long voip_result_out_skew = null;
    private Long voip_result_out_max_delta = null;
    private Long voip_result_out_sequence_error = null;
    private Long voip_result_out_long_seq = null;
    private Long voip_result_out_short_seq = null;
    private Long voip_result_out_mean_jitter = null;
    private Long voip_result_out_max_jitter = null;
    private Long voip_result_out_num_packets = null;


    /*****************************/
    /**        OBJECTIVES       **/
    /*****************************/
    private Integer voip_objective_bits_per_sample = 8;
    private Integer voip_objective_in_port = null;
    private Integer voip_objective_out_port = null;
    private Long voip_objective_delay = 20000000L;
    private Long voip_objective_timeout = 3000000000L;
    private Integer voip_objective_payload = 0;
    private Long voip_objective_call_duration = 1000000000L;
    private Integer voip_objective_sample_rate = 8000;


    /*****************************/
    /**         GENERAL         **/
    /*****************************/
    private Long duration_ns = null;
    private Long start_time_ns = null;
    private String voip_result_status = "ERROR";
    private Integer classification_packet_loss = -1;
    private Integer classification_jitter = -1;


    public VoipTestResult() {
    }

    public VoipTestResult(Integer classificationPacketLoss, Integer classificationJitter, Integer resultInNumPackets,
                          Integer resultInLongestSeqPackets, Integer resultInShortestSeqPackets, Long resultInMeanJitter,
                          Long resultInMaxJitter, Integer resultInSeqError, Long resultInSkew, Long resultInMaxDelta,
                          Long resultOutSkew, Long resultOutMaxDelta, Long resultOutSeqError, Long resultOutLongestSeqPackets,
                          Long resultOutShortestSeqPackets, Long resultOutMeanJitter, Long resultOutMaxJitter, Long resultOutNumPackets,
                          Integer objectiveBitsPerSample, Integer objectivePortIn, Integer objectivePortOut, Long objectiveDelay,
                          Long objectiveTimeoutNS, Integer objectivePayload, Long objectiveCallDuration, Integer objectiveSampleRate,
                          Long testDurationInNS, Long startTimeInNS, String testResultStatus) {
        this.voip_result_in_num_packets = resultInNumPackets;
        this.classification_packet_loss = classificationPacketLoss;
        this.classification_jitter = classificationJitter;
        this.voip_result_in_long_seq = resultInLongestSeqPackets;
        this.voip_result_in_short_seq = resultInShortestSeqPackets;
        this.voip_result_in_mean_jitter = resultInMeanJitter;
        this.voip_result_in_max_jitter = resultInMaxJitter;
        this.voip_result_in_sequence_error = resultInSeqError;
        this.voip_result_in_skew = resultInSkew;
        this.voip_result_in_max_delta = resultInMaxDelta;
        this.voip_result_out_skew = resultOutSkew;
        this.voip_result_out_max_delta = resultOutMaxDelta;
        this.voip_result_out_sequence_error = resultOutSeqError;
        this.voip_result_out_long_seq = resultOutLongestSeqPackets;
        this.voip_result_out_short_seq = resultOutShortestSeqPackets;
        this.voip_result_out_mean_jitter = resultOutMeanJitter;
        this.voip_result_out_max_jitter = resultOutMaxJitter;
        this.voip_result_out_num_packets = resultOutNumPackets;
        this.voip_objective_bits_per_sample = objectiveBitsPerSample;
        this.voip_objective_in_port = objectivePortIn;
        this.voip_objective_out_port = objectivePortOut;
        this.voip_objective_delay = objectiveDelay;
        this.voip_objective_timeout = objectiveTimeoutNS;
        this.voip_objective_payload = objectivePayload;
        this.voip_objective_call_duration = objectiveCallDuration;
        this.voip_objective_sample_rate = objectiveSampleRate;
        this.duration_ns = testDurationInNS;
        this.start_time_ns = startTimeInNS;
        this.voip_result_status = testResultStatus;
    }

    public Integer getVoip_result_in_num_packets() {
        return voip_result_in_num_packets;
    }

    public void setVoip_result_in_num_packets(Integer voip_result_in_num_packets) {
        this.voip_result_in_num_packets = voip_result_in_num_packets;
    }

    public Integer getVoip_result_in_long_seq() {
        return voip_result_in_long_seq;
    }

    public void setVoip_result_in_long_seq(Integer voip_result_in_long_seq) {
        this.voip_result_in_long_seq = voip_result_in_long_seq;
    }

    public Integer getVoip_result_in_short_seq() {
        return voip_result_in_short_seq;
    }

    public void setVoip_result_in_short_seq(Integer voip_result_in_short_seq) {
        this.voip_result_in_short_seq = voip_result_in_short_seq;
    }

    public Long getVoip_result_in_mean_jitter() {
        return voip_result_in_mean_jitter;
    }

    public void setVoip_result_in_mean_jitter(Long voip_result_in_mean_jitter) {
        this.voip_result_in_mean_jitter = voip_result_in_mean_jitter;
    }

    public Long getVoip_result_in_max_jitter() {
        return voip_result_in_max_jitter;
    }

    public void setVoip_result_in_max_jitter(Long voip_result_in_max_jitter) {
        this.voip_result_in_max_jitter = voip_result_in_max_jitter;
    }

    public Integer getVoip_result_in_sequence_error() {
        return voip_result_in_sequence_error;
    }

    public void setVoip_result_in_sequence_error(Integer voip_result_in_sequence_error) {
        this.voip_result_in_sequence_error = voip_result_in_sequence_error;
    }

    public Long getVoip_result_in_skew() {
        return voip_result_in_skew;
    }

    public void setVoip_result_in_skew(Long voip_result_in_skew) {
        this.voip_result_in_skew = voip_result_in_skew;
    }

    public Long getVoip_result_in_max_delta() {
        return voip_result_in_max_delta;
    }

    public void setVoip_result_in_max_delta(Long voip_result_in_max_delta) {
        this.voip_result_in_max_delta = voip_result_in_max_delta;
    }

    public Long getVoip_result_out_skew() {
        return voip_result_out_skew;
    }

    public void setVoip_result_out_skew(Long voip_result_out_skew) {
        this.voip_result_out_skew = voip_result_out_skew;
    }

    public Long getVoip_result_out_max_delta() {
        return voip_result_out_max_delta;
    }

    public void setVoip_result_out_max_delta(Long voip_result_out_max_delta) {
        this.voip_result_out_max_delta = voip_result_out_max_delta;
    }

    public Long getVoip_result_out_sequence_error() {
        return voip_result_out_sequence_error;
    }

    public void setVoip_result_out_sequence_error(Long voip_result_out_sequence_error) {
        this.voip_result_out_sequence_error = voip_result_out_sequence_error;
    }

    public Long getVoip_result_out_long_seq() {
        return voip_result_out_long_seq;
    }

    public void setVoip_result_out_long_seq(Long voip_result_out_long_seq) {
        this.voip_result_out_long_seq = voip_result_out_long_seq;
    }

    public Long getVoip_result_out_short_seq() {
        return voip_result_out_short_seq;
    }

    public void setVoip_result_out_short_seq(Long voip_result_out_short_seq) {
        this.voip_result_out_short_seq = voip_result_out_short_seq;
    }

    public Long getVoip_result_out_mean_jitter() {
        return voip_result_out_mean_jitter;
    }

    public void setVoip_result_out_mean_jitter(Long voip_result_out_mean_jitter) {
        this.voip_result_out_mean_jitter = voip_result_out_mean_jitter;
    }

    public Long getVoip_result_out_max_jitter() {
        return voip_result_out_max_jitter;
    }

    public void setVoip_result_out_max_jitter(Long voip_result_out_max_jitter) {
        this.voip_result_out_max_jitter = voip_result_out_max_jitter;
    }

    public Long getVoip_result_out_num_packets() {
        return voip_result_out_num_packets;
    }

    public void setVoip_result_out_num_packets(Long voip_result_out_num_packets) {
        this.voip_result_out_num_packets = voip_result_out_num_packets;
    }

    public Integer getVoip_objective_bits_per_sample() {
        return voip_objective_bits_per_sample;
    }

    public void setVoip_objective_bits_per_sample(Integer voip_objective_bits_per_sample) {
        this.voip_objective_bits_per_sample = voip_objective_bits_per_sample;
    }

    public Integer getVoip_objective_in_port() {
        return voip_objective_in_port;
    }

    public void setVoip_objective_in_port(Integer voip_objective_in_port) {
        this.voip_objective_in_port = voip_objective_in_port;
    }

    public Integer getVoip_objective_out_port() {
        return voip_objective_out_port;
    }

    public void setVoip_objective_out_port(Integer voip_objective_out_port) {
        this.voip_objective_out_port = voip_objective_out_port;
    }

    public Long getVoip_objective_delay() {
        return voip_objective_delay;
    }

    public void setVoip_objective_delay(Long voip_objective_delay) {
        this.voip_objective_delay = voip_objective_delay;
    }

    public Long getVoip_objective_timeout() {
        return voip_objective_timeout;
    }

    public void setVoip_objective_timeout(Long voip_objective_timeout) {
        this.voip_objective_timeout = voip_objective_timeout;
    }

    public Integer getVoip_objective_payload() {
        return voip_objective_payload;
    }

    public void setVoip_objective_payload(Integer voip_objective_payload) {
        this.voip_objective_payload = voip_objective_payload;
    }

    public Long getVoip_objective_call_duration() {
        return voip_objective_call_duration;
    }

    public void setVoip_objective_call_duration(Long voip_objective_call_duration) {
        this.voip_objective_call_duration = voip_objective_call_duration;
    }

    public Integer getVoip_objective_sample_rate() {
        return voip_objective_sample_rate;
    }

    public void setVoip_objective_sample_rate(Integer voip_objective_sample_rate) {
        this.voip_objective_sample_rate = voip_objective_sample_rate;
    }

    public Long getDuration_ns() {
        return duration_ns;
    }

    public void setDuration_ns(Long duration_ns) {
        this.duration_ns = duration_ns;
    }

    public Long getStart_time_ns() {
        return start_time_ns;
    }

    public void setStart_time_ns(Long start_time_ns) {
        this.start_time_ns = start_time_ns;
    }

    public String getVoip_result_status() {
        return voip_result_status;
    }

    public void setVoip_result_status(String voip_result_status) {
        this.voip_result_status = voip_result_status;
    }

    public Integer getClassification_packet_loss() {
        return classification_packet_loss;
    }

    public void setClassification_packet_loss(Integer classification_packet_loss) {
        this.classification_packet_loss = classification_packet_loss;
    }

    public Integer getClassification_jitter() {
        return classification_jitter;
    }

    public void setClassification_jitter(Integer classification_jitter) {
        this.classification_jitter = classification_jitter;
    }

    public VoipTestResult createVoipTestResult() {
        return new VoipTestResult(classification_packet_loss, classification_jitter, voip_result_in_num_packets, voip_result_in_long_seq,
                voip_result_in_short_seq, voip_result_in_mean_jitter, voip_result_in_max_jitter, voip_result_in_sequence_error, voip_result_in_skew, voip_result_in_max_delta,
                voip_result_out_skew, voip_result_out_max_delta, voip_result_out_sequence_error, voip_result_out_long_seq, voip_result_out_short_seq,
                voip_result_out_mean_jitter, voip_result_out_max_jitter, voip_result_out_num_packets, voip_objective_bits_per_sample, voip_objective_in_port, voip_objective_out_port,
                voip_objective_delay, voip_objective_timeout, voip_objective_payload, voip_objective_call_duration, voip_objective_sample_rate, duration_ns,
                start_time_ns, voip_result_status);
    }
}
