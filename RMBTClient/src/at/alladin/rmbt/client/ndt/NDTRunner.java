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
package at.alladin.rmbt.client.ndt;

import at.alladin.rmbt.client.helper.JSONParser;
import at.alladin.rmbt.client.helper.NdtStatus;
import net.measurementlab.ndt.NdtTests;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class NDTRunner {
    private final AtomicInteger ndtProgress = new AtomicInteger();
    private final AtomicReference<NdtStatus> ndtStatus = new AtomicReference<NdtStatus>(NdtStatus.NOT_STARTED);
    private final AtomicBoolean ndtFailure = new AtomicBoolean();
    private final AtomicBoolean ndtCancelled = new AtomicBoolean();
    private final String ndtHost;

    public NDTRunner(String ndtHost) {
        if (ndtHost == null)
            this.ndtHost = getNdtHost();
        else
            this.ndtHost = ndtHost;
    }

    public NDTRunner() {
        this(null);
    }

    public static String getNdtHost() {
        final JSONParser jParser = new JSONParser();
        final JSONObject obj = jParser.getURL(URI.create(at.alladin.rmbt.client.helper.Config.MLAB_NS));
        try {
            if (obj != null) {
//                System.out.println(obj.toString(4));
                return obj.getString("fqdn");
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            System.err.println(e.getMessage());
        }
        return at.alladin.rmbt.client.helper.Config.NDT_FALLBACK_HOST;
    }

    public NdtStatus getNdtStatus() {
        return ndtStatus.get();
    }

    public float getNdtProgress() {
        final int progress = ndtProgress.get();
        return (float) progress / UiServices.TEST_STEPS;
    }

    public void setNdtCacelled(boolean cancelled) {
        ndtCancelled.set(cancelled);
    }

    public void runNDT(String ndtNetworkType, UiServices usa) {
        ndtProgress.set(0);
        ndtStatus.set(NdtStatus.RUNNING);
        System.out.println("ndt status RUNNING");

        if (usa == null)
            usa = new UiServices() {
                @Override
                public void sendResults() {
                }
            };

        try {
            final NdtTests ndt = new NdtTests(ndtHost, usa, ndtNetworkType);
            ndt.run();
        } catch (final Throwable t) {
            //t.printStackTrace();
            System.err.println(t.getMessage());
        }

        if (ndtCancelled.get()) {
            ndtStatus.set(NdtStatus.ABORTED);
            System.out.println("ndt status ABORTED");
        } else if (ndtFailure.get()) {
            ndtStatus.set(NdtStatus.ERROR);
            System.out.println("ndt status ERROR");
        } else {
            ndtStatus.set(NdtStatus.RESULTS);
            System.out.println("ndt status RESULTS");
            usa.sendResults();
            ndtStatus.set(NdtStatus.FINISHED);
            System.out.println("ndt status FINISHED");
        }
    }

    public abstract class UiServices extends UiServicesAdapter {
        @Override
        public void incrementProgress() {
            ndtProgress.incrementAndGet();
        }

        @Override
        public boolean wantToStop() {
            return ndtCancelled.get();
        }

        public void cancel() {
            ndtCancelled.set(true);
        }

        @Override
        public void onFailure(final String errorMessage) {
            ndtFailure.set(true);
            System.out.println("NDT error:" + errorMessage);
        }

        public abstract void sendResults();
    }
}
