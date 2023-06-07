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
package at.alladin.rmbt.mapServer;

import com.specure.rmbt.shared.res.customer.CustomerResource;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class MapServer extends Application {
    @Override
    public Restlet createInboundRoot() {
        final Router router = new Router(getContext());

        router.attach("/version", VersionResource.class);
        router.attach("/V2/version", VersionResource.class);
        router.attach("/V3/version", VersionResource.class);

        // check drawing points on map
        if (CustomerResource.getInstance().showPointsOnMap()) {
            // allow drawing of points on map

            final PointTiles pointTiles = new PointTiles();
            router.attach("/tiles/points/{zoom}/{x}/{y}.png", pointTiles);
            router.attach("/tiles/points", pointTiles);
            router.attach("/V2/tiles/points/{zoom}/{x}/{y}.png", pointTiles);
            router.attach("/V2/tiles/points", pointTiles);
            router.attach("/V3/tiles/points/{zoom}/{x}/{y}.png", pointTiles);
            router.attach("/V3/tiles/points", pointTiles);

            final HeatmapTiles heatmapTiles = new HeatmapTiles();
            router.attach("/tiles/heatmap/{zoom}/{x}/{y}.png", heatmapTiles);
            router.attach("/tiles/heatmap", heatmapTiles);
            router.attach("/V2/tiles/heatmap/{zoom}/{x}/{y}.png", heatmapTiles);
            router.attach("/V2/tiles/heatmap", heatmapTiles);
            router.attach("/V3/tiles/heatmap/{zoom}/{x}/{y}.png", heatmapTiles);
            router.attach("/V3/tiles/heatmap", heatmapTiles);
        } else {
            // no drawing of points on map

            final HeatmapTiles heatmapTiles = new HeatmapTiles();
            router.attach("/tiles/heatmap/{zoom}/{x}/{y}.png", heatmapTiles);
            router.attach("/tiles/heatmap", heatmapTiles);
            router.attach("/V2/tiles/heatmap/{zoom}/{x}/{y}.png", heatmapTiles);
            router.attach("/V2/tiles/heatmap", heatmapTiles);
            router.attach("/V3/tiles/heatmap/{zoom}/{x}/{y}.png", heatmapTiles);
            router.attach("/V3/tiles/heatmap", heatmapTiles);

            router.attach("/tiles/points/{zoom}/{x}/{y}.png", heatmapTiles);
            router.attach("/tiles/points", heatmapTiles);
            router.attach("/V2/tiles/points/{zoom}/{x}/{y}.png", heatmapTiles);
            router.attach("/V2/tiles/points", heatmapTiles);
            router.attach("/V3/tiles/points/{zoom}/{x}/{y}.png", heatmapTiles);
            router.attach("/V3/tiles/points", heatmapTiles);
        }

        final ShapeTiles shapeTiles = new ShapeTiles();
        router.attach("/tiles/shapes/{zoom}/{x}/{y}.png", shapeTiles);
        router.attach("/tiles/shapes", shapeTiles);
        router.attach("/V2/tiles/shapes/{zoom}/{x}/{y}.png", shapeTiles);
        router.attach("/V2/tiles/shapes", shapeTiles);
        router.attach("/V3/tiles/shapes/{zoom}/{x}/{y}.png", shapeTiles);
        router.attach("/V3/tiles/shapes", shapeTiles);

        router.attach("/tiles/markers", MarkerResource.class);
        router.attach("/V2/tiles/markers", MarkerResourceV2.class);
        router.attach("/V3/tiles/markers", MarkerResourceV2.class);

        router.attach("/tiles/info", InfoResource.class);
        router.attach("/V2/tiles/info", InfoResourceV2.class);
        router.attach("/V3/tiles/info", InfoResourceV3.class);

        router.attach("/tiles/mapFilterOperators", MapFilterOperators.class);
        router.attach("/V2/tiles/mapFilterOperators", MapFilterOperatorsV2.class);
        router.attach("/V3/tiles/mapFilterOperators", MapFilterOperatorsV2.class);

        return router;
    }
}
