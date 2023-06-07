package at.alladin.rmbt.client;

import at.alladin.rmbt.client.v2.task.service.TestSettings;

public class JitterTest extends VoipTest {

    public JitterTest(RMBTClient client, TestSettings nnTestSettings, boolean onlyVoipTest) {
        super(client, nnTestSettings, onlyVoipTest);
    }

    public JitterTest(RMBTClient client, TestSettings nnTestSettings) {
        super(client, nnTestSettings);
    }

    @Override
    protected String getTestId() {
        return RMBTClient.TASK_JITTER;
    }
}