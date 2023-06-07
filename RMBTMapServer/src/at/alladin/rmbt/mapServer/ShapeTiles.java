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

import at.alladin.rmbt.mapServer.MapServerOptions.MapOption;
import at.alladin.rmbt.mapServer.MapServerOptions.SQLFilter;
import at.alladin.rmbt.mapServer.parameters.ShapeTileParameters;
import at.alladin.rmbt.mapServer.parameters.TileParameters.Path;
import at.alladin.rmbt.shared.SQLHelper;
import com.google.common.base.Strings;
import org.postgis.*;
import org.postgis.Point;
import org.postgis.Polygon;
import org.restlet.data.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ShapeTiles extends TileRestlet<ShapeTileParameters> {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ShapeTiles.class);

    private static final String SQL_SELECT_WHITE_SPACE = "WITH box AS"
            + " (SELECT ST_SetSRID(ST_MakeBox2D(ST_Point(?,?), ST_Point(?,?)), 900913) AS box)"
            + " SELECT ST_SnapToGrid(ST_intersection(s.geom, box.box), ?,?,?,?) AS geom,"
            + " count(\"%1$s\") count, percentile_cont(?) within group (order by \"%1$s\" asc) val FROM box, si_whitespaces s"
            + " LEFT JOIN v_test t ON s.geom && t.location AND ST_Contains(s.geom, t.location) AND"
            + " %2$s" + " WHERE s.geom && box.box" + " AND ST_intersects(s.geom, box.box)"
            + " GROUP BY s.geom, box.box";

    private static final String SQL_SELECT_BOX = "WITH box AS"
            + " (SELECT ST_SetSRID(ST_MakeBox2D(ST_Point(?,?),"
            + " ST_Point(?,?)), 900913) AS box)"
            + " SELECT"
            + " ST_SnapToGrid(ST_intersection(s.geom, box.box), ?,?,?,?) AS geom,"
            + " count(\"%1$s\") count,"
            + " percentile_cont(?) within group (order by \"%1$s\" asc) val"
            + " FROM box, \"%3$s\" s"
            + " JOIN v_test t ON s.geom && t.location AND ST_Contains(s.geom, t.location)"
            + " AND" + " %2$s"
            + " WHERE s.geom && box.box"
            + " AND ST_intersects(s.geom, box.box)"
            + " GROUP BY s.geom, box.box";

    private static class GeometryColor {
        final Geometry geometry;
        final Color color;

        public GeometryColor(final Geometry geometry, final Color color) {
            this.geometry = geometry;
            this.color = color;
        }
    }

    @Override
    protected ShapeTileParameters getTileParameters(Path path, Form params) {
        return new ShapeTileParameters(path, params);
    }

    @Override
    protected byte[] generateTile(final ShapeTileParameters params, final int tileSizeIdx, final int zoom, final DBox box,
                                  final MapOption mo, final List<SQLFilter> filters, final float quantile) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        double _transparency = params.getTransparency();

        final String shapeType = params.getShapeType();

        try {
            con = DbConnection.getConnection();

            final StringBuilder whereSQL = new StringBuilder(mo.sqlFilter);
            for (final SQLFilter sf : filters)
                whereSQL.append(" AND ").append(sf.where);

            final String sql;
            final boolean drawBorder;

            if (!Strings.isNullOrEmpty(shapeType) && shapeType.equals("whitespots")) {
                drawBorder = true;
                sql = String.format(SQL_SELECT_WHITE_SPACE, mo.valueColumnLog, whereSQL);
            } else {
                drawBorder = false;
                final String table;
                switch (shapeType) {
                    case "municipality":
                        table = "si_municipality";
                        break;

                    case "settlements":
                        table = "si_settlements";
                        break;

                    case "regions":
                    default:
                        table = "si_regions";
                }
                sql = String.format(
                        SQL_SELECT_BOX, mo.valueColumnLog, whereSQL, table);
            }

            ps = con.prepareStatement(sql);

            int idx = 1;

            /* makeBox2D */
            final double margin = box.res * 1;
            ps.setDouble(idx++, box.x1 - margin);
            ps.setDouble(idx++, box.y1 - margin);
            ps.setDouble(idx++, box.x2 + margin);
            ps.setDouble(idx++, box.y2 + margin);

            /* snapToGrid */
            ps.setDouble(idx++, box.x1);
            ps.setDouble(idx++, box.y1);
            ps.setDouble(idx++, box.res);
            ps.setDouble(idx++, box.res);

            ps.setFloat(idx++, quantile);

            for (final SQLFilter sf : filters)
                idx = sf.fillParams(idx, ps);

            rs = ps.executeQuery();

            logger.debug(ps.toString());

            if (rs == null)
                throw new IllegalArgumentException();

            final List<GeometryColor> geoms = new ArrayList<>();
            while (rs.next()) {
                final String geomStr = rs.getString("geom");
                if (!Strings.isNullOrEmpty(geomStr)) {
                    final Geometry geom = PGgeometry.geomFromString(geomStr);

                    final long count = rs.getLong("count");
                    final double val = rs.getDouble("val");
                    final int colorInt = valueToColor(mo.colorsSorted, mo.intervalsSorted, val);
                    double transparency = ((double) count / 20d) * _transparency;
                    if (transparency > _transparency)
                        transparency = _transparency;
                    final int alpha = (int) Math.round(transparency * 255) << 24;
                    final Color color = new Color(colorInt | alpha, true);

                    geoms.add(new GeometryColor(geom, color));
                }
            }

            if (geoms.isEmpty())
                return null;

            final Image img = images[tileSizeIdx].get();
            final Graphics2D g = img.g;

            g.setBackground(new Color(0, 0, 0, 0));
            g.clearRect(0, 0, img.width, img.height);
//                    g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//            final Stroke stroke = new BasicStroke(?);
//            g.setStroke(stroke);

            final Path2D.Double path = new Path2D.Double();

            for (final GeometryColor geomColor : geoms) {
                final Geometry geom = geomColor.geometry;

                final Polygon[] polys;
                if (geom instanceof MultiPolygon)
                    polys = ((MultiPolygon) geom).getPolygons();
                else if (geom instanceof Polygon)
                    polys = new Polygon[]{(Polygon) geom};
                else
                    polys = new Polygon[]{};

                for (final Polygon poly : polys)
                    for (int i = 0; i < poly.numRings(); i++) {
                        final Point[] points = poly.getRing(i).getPoints();

                        path.reset();
                        boolean initial = true;
                        for (final Point point : points) {
                            final double relX = (point.x - box.x1) / box.res;
                            final double relY = TILE_SIZES[tileSizeIdx] - (point.y - box.y1) / box.res;
                            if (initial) {
                                initial = false;
                                path.moveTo(relX, relY);
                            }
                            path.lineTo(relX, relY);
                        }
                        g.setPaint(geomColor.color);
                        g.fill(path);

                        if (drawBorder) {
                            g.setPaint(Color.GRAY);
                            g.draw(path);
                        }
                    }
            }
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(img.bi, "png", baos);
                return baos.toByteArray();
            }
        } catch (final Exception e) {
            logger.error(e.getMessage());
            throw new IllegalStateException(e);
        } finally {

            // close result set
            SQLHelper.closeResultSet(rs);

            // close prepared statement
            SQLHelper.closePreparedStatement(ps);

            // close connection
            SQLHelper.closeConnection(con);
        }
    }

}