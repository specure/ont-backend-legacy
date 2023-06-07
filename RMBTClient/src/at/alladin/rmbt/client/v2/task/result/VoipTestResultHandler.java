package at.alladin.rmbt.client.v2.task.result;


import java.util.HashMap;

public class VoipTestResultHandler {

    private static final String VOIP_TEST_RESULT_SHARED_PREF_KEY = "VOIP_TEST_RESULT_SHARED_PREF";

    /**
     * Converts results hash map to object used to send as JSON
     *
     * @param resultMap
     * @return
     */
    public VoipTestResult convertResultsToObject(HashMap<String, Object> resultMap) {

        VoipTestResult voipTestResult = new VoipTestResult();

        //IN
        if (resultMap.containsKey("voip_result_in_num_packets"))
            voipTestResult.setVoip_result_in_num_packets((Integer) resultMap.get("voip_result_in_num_packets"));

        if (resultMap.containsKey("voip_result_in_long_seq"))
            voipTestResult.setVoip_result_in_long_seq((Integer) resultMap.get("voip_result_in_long_seq"));

        if (resultMap.containsKey("voip_result_in_short_seq"))
            voipTestResult.setVoip_result_in_short_seq((Integer) resultMap.get("voip_result_in_short_seq"));

        if (resultMap.containsKey("voip_result_in_mean_jitter"))
            voipTestResult.setVoip_result_in_mean_jitter((Long) resultMap.get("voip_result_in_mean_jitter"));

        if (resultMap.containsKey("voip_result_in_max_jitter"))
            voipTestResult.setVoip_result_in_max_jitter((Long) resultMap.get("voip_result_in_max_jitter"));

        if (resultMap.containsKey("voip_result_in_sequence_error"))
            voipTestResult.setVoip_result_in_sequence_error((Integer) resultMap.get("voip_result_in_sequence_error"));

        if (resultMap.containsKey("voip_result_in_skew"))
            voipTestResult.setVoip_result_in_skew((Long) resultMap.get("voip_result_in_skew"));

        if (resultMap.containsKey("voip_result_in_max_delta"))
            voipTestResult.setVoip_result_in_max_delta((Long) resultMap.get("voip_result_in_max_delta"));


        //OUT

        if (resultMap.containsKey("voip_result_out_skew"))
            voipTestResult.setVoip_result_out_skew((Long) resultMap.get("voip_result_out_skew"));

        if (resultMap.containsKey("voip_result_out_max_delta"))
            voipTestResult.setVoip_result_out_max_delta((Long) resultMap.get("voip_result_out_max_delta"));

        if (resultMap.containsKey("voip_result_out_sequence_error"))
            voipTestResult.setVoip_result_out_sequence_error((Long) resultMap.get("voip_result_out_sequence_error"));

        if (resultMap.containsKey("voip_result_out_long_seq"))
            voipTestResult.setVoip_result_out_long_seq((Long) resultMap.get("voip_result_out_long_seq"));

        if (resultMap.containsKey("voip_result_out_short_seq"))
            voipTestResult.setVoip_result_out_short_seq((Long) resultMap.get("voip_result_out_short_seq"));

        if (resultMap.containsKey("voip_result_out_mean_jitter"))
            voipTestResult.setVoip_result_out_mean_jitter((Long) resultMap.get("voip_result_out_mean_jitter"));

        if (resultMap.containsKey("voip_result_out_max_jitter"))
            voipTestResult.setVoip_result_out_max_jitter((Long) resultMap.get("voip_result_out_max_jitter"));

        if (resultMap.containsKey("voip_result_out_num_packets"))
            voipTestResult.setVoip_result_out_num_packets((Long) resultMap.get("voip_result_out_num_packets"));


        //OBJECTIVES

        if (resultMap.containsKey("voip_objective_bits_per_sample"))
            voipTestResult.setVoip_objective_bits_per_sample((Integer) resultMap.get("voip_objective_bits_per_sample"));

        if (resultMap.containsKey("voip_objective_in_port"))
            voipTestResult.setVoip_objective_in_port((Integer) resultMap.get("voip_objective_in_port"));

        if (resultMap.containsKey("voip_objective_out_port"))
            voipTestResult.setVoip_objective_out_port((Integer) resultMap.get("voip_objective_out_port"));

        if (resultMap.containsKey("voip_objective_delay"))
            voipTestResult.setVoip_objective_delay((Long) resultMap.get("voip_objective_delay"));

        if (resultMap.containsKey("voip_objective_timeout"))
            voipTestResult.setVoip_objective_timeout((Long) resultMap.get("voip_objective_timeout"));

        if (resultMap.containsKey("voip_objective_payload"))
            voipTestResult.setVoip_objective_payload((Integer) resultMap.get("voip_objective_payload"));

        if (resultMap.containsKey("voip_objective_call_duration"))
            voipTestResult.setVoip_objective_call_duration((Long) resultMap.get("voip_objective_call_duration"));

        if (resultMap.containsKey("voip_objective_sample_rate"))
            voipTestResult.setVoip_objective_sample_rate((Integer) resultMap.get("voip_objective_sample_rate"));


        //GENERAL

        if (resultMap.containsKey("duration_ns"))
            voipTestResult.setDuration_ns((Long) resultMap.get("duration_ns"));

        if (resultMap.containsKey("start_time_ns"))
            voipTestResult.setStart_time_ns((Long) resultMap.get("start_time_ns"));

        if (resultMap.containsKey("voip_result_status"))
            voipTestResult.setVoip_result_status((String) resultMap.get("voip_result_status"));

        return voipTestResult;
    }

}
