/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class IntervalJob<R> extends AbstractJob<R> {

    private static final Logger logger = LoggerFactory.getLogger(IntervalJob.class);

    public final static int DEFAULT_JOB_INTERVAL = 60000;

    private final AtomicInteger interval = new AtomicInteger(0);
    private final AtomicLong executionCounter = new AtomicLong(0);
    private final AtomicLong executionDuration = new AtomicLong(0);

    public IntervalJob(TestServerServiceEnum service) {
        super(service);
        setJobInterval(DEFAULT_JOB_INTERVAL);
    }

    public abstract boolean restartOnError();

    public int getJobInterval() {
        return interval.get();
    }

    public void setJobInterval(int intervalMs) {
        interval.set(intervalMs);
    }

    public long getExecutionCounter() {
        return executionCounter.get();
    }

    public void run() {
        try {
            logger.info("STARTING SERVICE '" + service.getName() + "'");

            final DecimalFormat df = new DecimalFormat("##0.000");

            while (isRunning.get()) {
                Thread.sleep(interval.get());

                final long tsStart = System.nanoTime();
                result = execute();
                final long timePassed = (System.nanoTime() - tsStart);

                executionCounter.addAndGet(1);
                executionDuration.addAndGet(timePassed);

                logger.info(result.toString());
                logger.info("times executed: " + executionCounter.get() + ",  this time it took: " + df.format(((double) (System.nanoTime() - tsStart) / 1000000d)) + "ms"
                        + ", total time since start: " + df.format(((double) executionDuration.get() / 1000000000d)) + "s");

                dispatchEvent(JobState.RUN, result);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            state.set(JobState.ERROR);
        } finally {
            isRunning.set(false);
        }

        dispatchEvent(getState(), result);

        logger.info("STOPPED SERVICE '" + service.getName() + "'");
    }
}
