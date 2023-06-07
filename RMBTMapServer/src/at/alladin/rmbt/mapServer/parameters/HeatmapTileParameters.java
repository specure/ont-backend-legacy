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

import org.restlet.data.Form;

public class HeatmapTileParameters extends TileParameters {
    private static final long serialVersionUID = 3632506449635480626L;

    public HeatmapTileParameters(Path path, Form params) {
        super(path, params, 0.75);
    }

    @Override
    public boolean isNoCache() {
        return false;
    }
}
