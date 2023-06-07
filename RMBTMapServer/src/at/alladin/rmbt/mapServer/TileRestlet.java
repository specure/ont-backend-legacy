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
package at.alladin.rmbt.mapServer;

import at.alladin.rmbt.mapServer.MapServerOptions.MapFilter;
import at.alladin.rmbt.mapServer.MapServerOptions.MapOption;
import at.alladin.rmbt.mapServer.MapServerOptions.SQLFilter;
import at.alladin.rmbt.mapServer.parameters.TileParameters;
import at.alladin.rmbt.mapServer.parameters.TileParameters.Path;
import at.alladin.rmbt.shared.cache.CacheHelper;
import at.alladin.rmbt.shared.cache.CacheHelper.ObjectWithTimestamp;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class TileRestlet<Params extends TileParameters> extends Restlet {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(TileRestlet.class);

    protected final MapServerOptions mso = MapServerOptions.getInstance();

    protected static final int[] TILE_SIZES = new int[]{256, 512, 768};
    protected static final byte[][] EMPTY_IMAGES = new byte[TILE_SIZES.length][];
    protected static final byte[] EMPTY_MARKER = "EMPTY".getBytes();

    private static final int CACHE_STALE = 3600;
    private static final int CACHE_EXPIRE = 7200;
    private final CacheHelper cache = CacheHelper.getInstance();


    static {
        for (int i = 0; i < TILE_SIZES.length; i++) {
            final BufferedImage img = new BufferedImage(TILE_SIZES[i], TILE_SIZES[i], BufferedImage.TYPE_INT_ARGB);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(img, "png", baos);
                EMPTY_IMAGES[i] = baos.toByteArray();
            } catch (final IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    protected static class Image {
        protected BufferedImage bi;
        protected Graphics2D g;
        protected int width;
        protected int height;
    }

    static class DPoint {
        double x;
        double y;
    }

    static class DBox {
        double x1;
        double y1;
        double x2;
        double y2;
        double res;
    }

    @SuppressWarnings("unchecked")
    protected final ThreadLocal<Image>[] images = new ThreadLocal[TILE_SIZES.length];

    public TileRestlet() {
        for (int i = 0; i < TILE_SIZES.length; i++) {
            final int tileSize = TILE_SIZES[i];
            images[i] = new ThreadLocal<Image>() {
                @Override
                protected Image initialValue() {
                    final Image image = new Image();
                    image.bi = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                    image.g = image.bi.createGraphics();
                    image.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    image.g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
                    image.g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    image.g.setStroke(new BasicStroke(1f));
                    image.width = image.bi.getWidth();
                    image.height = image.bi.getHeight();
                    return image;
                }

                ;
            };
        }
    }

    protected static int valueToColor(final int[] colors, final double[] intervals, final double value) {
        int idx = -1;
        for (int i = 0; i < intervals.length; i++)
            if (value < intervals[i]) {
                idx = i;
                break;
            }
        if (idx == 0)
            return colors[0];
        if (idx == -1)
            return colors[colors.length - 1];

        final double factor = (value - intervals[idx - 1]) / (intervals[idx] - intervals[idx - 1]);

        final int c0 = colors[idx - 1];
        final int c1 = colors[idx];

        final int c0r = c0 >> 16;
        final int c0g = c0 >> 8 & 0xff;
        final int c0b = c0 & 0xff;

        final int r = (int) (c0r + ((c1 >> 16) - c0r) * factor);
        final int g = (int) (c0g + ((c1 >> 8 & 0xff) - c0g) * factor);
        final int b = (int) (c0b + ((c1 & 0xff) - c0b) * factor);
        return r << 16 | g << 8 | b;
    }

    @Override
    public void handle(final Request req, final Response res) {
        final String zoomStr = (String) req.getAttributes().get("zoom");
        final String xStr = (String) req.getAttributes().get("x");
        final String yStr = (String) req.getAttributes().get("y");

        final Path path = new Path(zoomStr, xStr, yStr, req.getResourceRef().getQueryAsForm().getFirstValue("path", true));
        final Params p = getTileParameters(path, req.getResourceRef().getQueryAsForm());

        boolean useCache = true;
        if (p.isNoCache())
            useCache = false;

        final String cacheKey;

        if (useCache) {
            cacheKey = CacheHelper.getHash((TileParameters) p);
            final ObjectWithTimestamp cacheObject = cache.getWithTimestamp(cacheKey, CACHE_STALE);
            if (cacheObject != null) {
                logger.debug("cache hit for: " + cacheKey + "; is stale: " + cacheObject.stale);
                byte[] data = (byte[]) cacheObject.o;
                if (Arrays.equals(EMPTY_MARKER, data))
                    data = EMPTY_IMAGES[getTileSizeIdx(p)];
                if (cacheObject.stale) {
                    final Runnable refreshCacheRunnable = new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("adding in background: " + cacheKey);
                            final byte[] newData = generateTile(path, p, getTileSizeIdx(p));
                            cache.set(cacheKey, CACHE_EXPIRE, newData != null ? newData : EMPTY_MARKER, true);
                        }
                    };
                    cache.getExecutor().execute(refreshCacheRunnable);
                }
                res.setEntity(new PngOutputRepresentation(data));
                return;
            }
        } else
            cacheKey = null;

        final int tileSizeIdx = getTileSizeIdx(p);
        byte[] data = generateTile(path, p, tileSizeIdx);
//        if (data == null)

        if (useCache) {
            logger.debug("adding to cache: " + cacheKey);
            cache.set(cacheKey, CACHE_EXPIRE, data != null ? data : EMPTY_MARKER, true);
        }

        if (data == null)
            data = EMPTY_IMAGES[tileSizeIdx];
        res.setEntity(new PngOutputRepresentation(data));
    }

    private int getTileSizeIdx(final Params p) {
        int tileSizeIdx = 0;
        final int size = p.getSize();
        for (int i = 0; i < TILE_SIZES.length; i++) {
            if (size == TILE_SIZES[i]) {
                tileSizeIdx = i;
                break;
            }
        }
        return tileSizeIdx;
    }

    private byte[] generateTile(final Path path, Params p, int tileSizeIdx) {
        final MapOption mo = mso.getMapOptionMap().get(p.getMapOption());
        if (mo == null)
            throw new IllegalArgumentException();

//        check if filter contains country and provider or mobile_provider_name or mobile_provider
        checkFilter(p);

        final List<SQLFilter> filters = new ArrayList<>(mso.getDefaultMapFilters());
        for (final Map.Entry<String, String> entry : p.getFilterMap().entrySet()) {
            final MapFilter mapFilter = mso.getMapFilterMap().get(entry.getKey());
            if (mapFilter != null) {
                final SQLFilter filter = mapFilter.getFilter(entry.getValue());
                if (filter != null)
                    filters.add(filter);
            }
        }

        final DBox box = GeoCalc.xyToMeters(TILE_SIZES[tileSizeIdx], path.getX(), path.getY(), path.getZoom());

        float quantile = p.getQuantile();
        if (mo.reverseScale)
            quantile = 1 - quantile;

        final byte[] data = generateTile(p, tileSizeIdx, path.getZoom(), box, mo, filters, quantile);
        return data;
    }

    private void checkFilter(Params p) {
        if (p.getFilterMap().containsKey("mobile_provider_name")) {
            if (!p.getFilterMap().get("mobile_provider_name").isEmpty()) {
                setProviderAndOperatorNull(p);
                setCountryNull(p);
            }
        } else if (p.getFilterMap().containsKey("provider")){
            if(!p.getFilterMap().get("provider").isEmpty()) {
                setCountryNull(p);
            }
        } else if(p.getFilterMap().containsKey("operator")){
            if(!p.getFilterMap().get("operator").isEmpty()) {
                setCountryNull(p);
            }
        }
    }

    private void setCountryNull(Params p){
        p.getFilterMap().put("country", null);
    }

    private void setProviderAndOperatorNull(Params p){
        p.getFilterMap().put("provider", null);
        p.getFilterMap().put("operators", null);
    }

    protected abstract Params getTileParameters(TileParameters.Path path, Form params);

    protected abstract byte[] generateTile(Params params, int tileSizeIdx, int zoom, DBox box, MapOption mo,
                                           List<SQLFilter> filters, float quantile);
}
