/*******************************************************************************
 * Copyright 2015 SPECURE GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.mapServer.parameters;

import com.google.common.base.Strings;
import com.google.common.hash.PrimitiveSink;
import org.restlet.data.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointTileParameters extends TileParameters {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(PointTileParameters.class);

    private static final long serialVersionUID = 9123279457072532747L;
    protected final double pointDiameter;
    protected final boolean noFill;
    protected final boolean noColor;
    protected final String highlight;

    public PointTileParameters(Path path, Form params) {
        super(path, params, 0.6);

        final String diameterString = params.getFirstValue("point_diameter");
        double _diameter = 12.0;
        if (diameterString != null)
            try {
                _diameter = Double.parseDouble(diameterString);
            } catch (final NumberFormatException e) {
                logger.error(e.getMessage());
            }
        pointDiameter = _diameter;

        final String noFillString = params.getFirstValue("no_fill");
        boolean _noFill = false;
        if (noFillString != null)
            _noFill = Boolean.parseBoolean(noFillString);
        noFill = _noFill;

        final String noColorString = params.getFirstValue("no_color");
        boolean _noColor = false;
        if (noColorString != null)
            _noColor = Boolean.parseBoolean(noColorString);
        noColor = _noColor;

        String _highlight = params.getFirstValue("highlight");
        if (Strings.isNullOrEmpty(_highlight) || "undefined".equals(_highlight))
            _highlight = null;
        highlight = _highlight;
    }

    public double getPointDiameter() {
        return pointDiameter;
    }

    public boolean isNoFill() {
        return noFill;
    }

    public boolean isNoColor() {
        return noColor;
    }

    public String getHighlight() {
        return highlight;
    }

    @Override
    public boolean isNoCache() {
        return !Strings.isNullOrEmpty(highlight);
    }

    @Override
    public void funnel(TileParameters o, PrimitiveSink into) {
        super.funnel(o, into);
        if (o instanceof PointTileParameters) {
            final PointTileParameters _o = (PointTileParameters) o;
            into
                    .putDouble(_o.pointDiameter)
                    .putBoolean(_o.noFill)
                    .putBoolean(_o.noColor)
                    .putUnencodedChars(Strings.nullToEmpty(_o.highlight));
        }
    }
}
