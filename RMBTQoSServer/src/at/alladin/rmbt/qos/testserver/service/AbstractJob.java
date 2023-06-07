/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.qos.testserver.service;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lb
 */
public abstract class AbstractJob<R> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJob.class);

    protected final AtomicBoolean isRunning = new AtomicBoolean(true);
    protected final AtomicReference<JobState> state = new AtomicReference<AbstractJob.JobState>(JobState.INIT);
    protected final ConcurrentHashMap<JobState, JobCallback> callbackMap = new ConcurrentHashMap<>();
    protected final TestServerServiceEnum service;
    R result = null;

    public AbstractJob(TestServerServiceEnum service) {
        this.service = service;
    }

    public abstract R execute() throws Exception;

    public abstract AbstractJob<R> getNewInstance();

    public abstract String getId();

    public synchronized void stop() {
        logger.info("STOPPING SERVICE!");
        isRunning.set(false);
        dispatchEvent(JobState.STOP, result);
    }

    public void start() {
        isRunning.set(true);
        dispatchEvent(JobState.START, result);
    }

    public void interrupt() {
        Thread.currentThread().interrupt();
    }

    public synchronized R getResult() {
        while (isRunning.get()) {

        }

        return result;
    }

    public void setResult(R result) {
        this.result = result;
    }

    public TestServerServiceEnum getService() {
        return service;
    }

    public JobState getState() {
        return state.get();
    }

    @Override
    public String toString() {
        return "AbstractJob [isRunning=" + isRunning + ", state=" + state
                + ", callbackMap=" + callbackMap + ", service=" + service
                + ", result=" + result + "]";
    }

    public boolean getIsRunning() {
        return isRunning.get();
    }

    public void setCallback(JobState onState, JobCallback callback) {
        callbackMap.put(onState, callback);
    }

    public void dispatchEvent(JobState newState, R result) {
        state.set(newState);

        JobCallback callback = callbackMap.get(newState);
        if (callback != null) {
            callback.onEvent(this, newState, result);
        }
    }

    public static enum JobState {
        INIT,
        START,
        RUN,
        STOP,
        ERROR
    }
}
