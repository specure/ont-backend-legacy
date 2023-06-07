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
package at.alladin.rmbt.client.helper;

public class IntermediateResult {
    public final static double LOG10_MAX = Math.log10(250d);
    public final static double GAUGE_PARTS = 4.25d;
    public long initNano;
    public long pingNano;
    public long downBitPerSec;
    public long upBitPerSec;
    public TestStatus status;
    public float progress;
    public double downBitPerSecLog;
    public double upBitPerSecLog;
    public long remainingWait;

    public static double toLog(final long value) {
        if (value < 10000)
            return 0;
        return ((GAUGE_PARTS - LOG10_MAX) + Math.log10(value / 1e6)) / GAUGE_PARTS;
        // value in bps
        // < 0.01 -> 0
        // 0.01 Mbps -> 0
        // 250 Mbps -> 1
        // > 250 Mbps -> >1
    }

    public void setLogValues() {
        downBitPerSecLog = toLog(downBitPerSec);
        upBitPerSecLog = toLog(upBitPerSec);
    }
}
