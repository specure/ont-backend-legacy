--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: hstore; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public;


--
-- Name: EXTENSION hstore; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION hstore IS 'data type for storing sets of (key, value) pairs';


--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


--
-- Name: quantile; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS quantile WITH SCHEMA public;


--
-- Name: EXTENSION quantile; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION quantile IS 'Provides quantile aggregate function.';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET search_path = public, pg_catalog;

--
-- Name: mobiletech; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE mobiletech AS ENUM (
    'unknown',
    '2G',
    '3G',
    '4G',
    'mixed'
);


ALTER TYPE mobiletech OWNER TO postgres;

--
-- Name: qostest; Type: TYPE; Schema: public; Owner: rmbt
--

CREATE TYPE qostest AS ENUM (
    'website',
    'http_proxy',
    'non_transparent_proxy',
    'dns',
    'tcp',
    'udp',
    'traceroute',
    'voip'
);


ALTER TYPE qostest OWNER TO rmbt;

--
-- Name: _final_median(anyarray); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION _final_median(anyarray) RETURNS double precision
    LANGUAGE sql IMMUTABLE
    AS $_$ 
  WITH q AS
  (
     SELECT val
     FROM unnest($1) val
     WHERE VAL IS NOT NULL
     ORDER BY 1
  ),
  cnt AS
  (
    SELECT COUNT(*) AS c FROM q
  )
  SELECT AVG(val)::float8
  FROM 
  (
    SELECT val FROM q
    LIMIT  2 - MOD((SELECT c FROM cnt), 2)
    OFFSET GREATEST(CEIL((SELECT c FROM cnt) / 2.0) - 1,0)  
  ) q2;
$_$;


ALTER FUNCTION public._final_median(anyarray) OWNER TO postgres;

--
-- Name: addbbox(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION addbbox(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_addBBOX';


ALTER FUNCTION public.addbbox(geometry) OWNER TO postgres;

--
-- Name: addpoint(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION addpoint(geometry, geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_addpoint';


ALTER FUNCTION public.addpoint(geometry, geometry) OWNER TO postgres;

--
-- Name: addpoint(geometry, geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION addpoint(geometry, geometry, integer) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_addpoint';


ALTER FUNCTION public.addpoint(geometry, geometry, integer) OWNER TO postgres;

--
-- Name: affine(geometry, double precision, double precision, double precision, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION affine(geometry, double precision, double precision, double precision, double precision, double precision, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  $2, $3, 0,  $4, $5, 0,  0, 0, 1,  $6, $7, 0)$_$;


ALTER FUNCTION public.affine(geometry, double precision, double precision, double precision, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: affine(geometry, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION affine(geometry, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_affine';


ALTER FUNCTION public.affine(geometry, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: akos_generate_random_point(integer, geometry); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION akos_generate_random_point(_radius integer, _point geometry) RETURNS geometry
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $$
	DECLARE
		_rand_point geometry;
	BEGIN
		SELECT ST_Transform(ST_Project(ST_Transform(_point,4326), random() * _radius, radians(360 * random()))::geometry, 900913) INTO _rand_point;
		RETURN _rand_point;
	END;
$$;


ALTER FUNCTION public.akos_generate_random_point(_radius integer, _point geometry) OWNER TO rmbt;

--
-- Name: area(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION area(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_area_polygon';


ALTER FUNCTION public.area(geometry) OWNER TO postgres;

--
-- Name: area2d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION area2d(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_area_polygon';


ALTER FUNCTION public.area2d(geometry) OWNER TO postgres;

--
-- Name: asbinary(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION asbinary(geometry) RETURNS bytea
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_asBinary';


ALTER FUNCTION public.asbinary(geometry) OWNER TO postgres;

--
-- Name: asbinary(geometry, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION asbinary(geometry, text) RETURNS bytea
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_asBinary';


ALTER FUNCTION public.asbinary(geometry, text) OWNER TO postgres;

--
-- Name: asewkb(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION asewkb(geometry) RETURNS bytea
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'WKBFromLWGEOM';


ALTER FUNCTION public.asewkb(geometry) OWNER TO postgres;

--
-- Name: asewkb(geometry, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION asewkb(geometry, text) RETURNS bytea
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'WKBFromLWGEOM';


ALTER FUNCTION public.asewkb(geometry, text) OWNER TO postgres;

--
-- Name: asewkt(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION asewkt(geometry) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_asEWKT';


ALTER FUNCTION public.asewkt(geometry) OWNER TO postgres;

--
-- Name: asgml(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION asgml(geometry) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsGML(2, $1, 15, 0, null, null)$_$;


ALTER FUNCTION public.asgml(geometry) OWNER TO postgres;

--
-- Name: asgml(geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION asgml(geometry, integer) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsGML(2, $1, $2, 0, null, null)$_$;


ALTER FUNCTION public.asgml(geometry, integer) OWNER TO postgres;

--
-- Name: ashexewkb(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ashexewkb(geometry) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_asHEXEWKB';


ALTER FUNCTION public.ashexewkb(geometry) OWNER TO postgres;

--
-- Name: ashexewkb(geometry, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ashexewkb(geometry, text) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_asHEXEWKB';


ALTER FUNCTION public.ashexewkb(geometry, text) OWNER TO postgres;

--
-- Name: askml(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION askml(geometry) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsKML(2, ST_Transform($1,4326), 15, null)$_$;


ALTER FUNCTION public.askml(geometry) OWNER TO postgres;

--
-- Name: askml(geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION askml(geometry, integer) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsKML(2, ST_transform($1,4326), $2, null)$_$;


ALTER FUNCTION public.askml(geometry, integer) OWNER TO postgres;

--
-- Name: askml(integer, geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION askml(integer, geometry, integer) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsKML($1, ST_Transform($2,4326), $3, null)$_$;


ALTER FUNCTION public.askml(integer, geometry, integer) OWNER TO postgres;

--
-- Name: assvg(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION assvg(geometry) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_asSVG';


ALTER FUNCTION public.assvg(geometry) OWNER TO postgres;

--
-- Name: assvg(geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION assvg(geometry, integer) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_asSVG';


ALTER FUNCTION public.assvg(geometry, integer) OWNER TO postgres;

--
-- Name: assvg(geometry, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION assvg(geometry, integer, integer) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_asSVG';


ALTER FUNCTION public.assvg(geometry, integer, integer) OWNER TO postgres;

--
-- Name: astext(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION astext(geometry) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_asText';


ALTER FUNCTION public.astext(geometry) OWNER TO postgres;

--
-- Name: azimuth(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION azimuth(geometry, geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_azimuth';


ALTER FUNCTION public.azimuth(geometry, geometry) OWNER TO postgres;

--
-- Name: bdmpolyfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bdmpolyfromtext(text, integer) RETURNS geometry
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$
DECLARE
	geomtext alias for $1;
	srid alias for $2;
	mline geometry;
	geom geometry;
BEGIN
	mline := ST_MultiLineStringFromText(geomtext, srid);

	IF mline IS NULL
	THEN
		RAISE EXCEPTION 'Input is not a MultiLinestring';
	END IF;

	geom := ST_Multi(ST_BuildArea(mline));

	RETURN geom;
END;
$_$;


ALTER FUNCTION public.bdmpolyfromtext(text, integer) OWNER TO postgres;

--
-- Name: bdpolyfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bdpolyfromtext(text, integer) RETURNS geometry
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$
DECLARE
	geomtext alias for $1;
	srid alias for $2;
	mline geometry;
	geom geometry;
BEGIN
	mline := ST_MultiLineStringFromText(geomtext, srid);

	IF mline IS NULL
	THEN
		RAISE EXCEPTION 'Input is not a MultiLinestring';
	END IF;

	geom := ST_BuildArea(mline);

	IF GeometryType(geom) != 'POLYGON'
	THEN
		RAISE EXCEPTION 'Input returns more then a single polygon, try using BdMPolyFromText instead';
	END IF;

	RETURN geom;
END;
$_$;


ALTER FUNCTION public.bdpolyfromtext(text, integer) OWNER TO postgres;

--
-- Name: boundary(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION boundary(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'boundary';


ALTER FUNCTION public.boundary(geometry) OWNER TO postgres;

--
-- Name: buffer(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION buffer(geometry, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'buffer';


ALTER FUNCTION public.buffer(geometry, double precision) OWNER TO postgres;

--
-- Name: buffer(geometry, double precision, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION buffer(geometry, double precision, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_Buffer($1, $2, $3)$_$;


ALTER FUNCTION public.buffer(geometry, double precision, integer) OWNER TO postgres;

--
-- Name: buildarea(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION buildarea(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'ST_BuildArea';


ALTER FUNCTION public.buildarea(geometry) OWNER TO postgres;

--
-- Name: centroid(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION centroid(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'centroid';


ALTER FUNCTION public.centroid(geometry) OWNER TO postgres;

--
-- Name: collect(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION collect(geometry, geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE
    AS '$libdir/postgis-2.1', 'LWGEOM_collect';


ALTER FUNCTION public.collect(geometry, geometry) OWNER TO postgres;

--
-- Name: combine_bbox(box2d, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION combine_bbox(box2d, geometry) RETURNS box2d
    LANGUAGE c IMMUTABLE
    AS '$libdir/postgis-2.1', 'BOX2D_combine';


ALTER FUNCTION public.combine_bbox(box2d, geometry) OWNER TO postgres;

--
-- Name: combine_bbox(box3d, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION combine_bbox(box3d, geometry) RETURNS box3d
    LANGUAGE c IMMUTABLE
    AS '$libdir/postgis-2.1', 'BOX3D_combine';


ALTER FUNCTION public.combine_bbox(box3d, geometry) OWNER TO postgres;

--
-- Name: contains(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION contains(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'contains';


ALTER FUNCTION public.contains(geometry, geometry) OWNER TO postgres;

--
-- Name: convexhull(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION convexhull(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'convexhull';


ALTER FUNCTION public.convexhull(geometry) OWNER TO postgres;

--
-- Name: crosses(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION crosses(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'crosses';


ALTER FUNCTION public.crosses(geometry, geometry) OWNER TO postgres;

--
-- Name: difference(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION difference(geometry, geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'difference';


ALTER FUNCTION public.difference(geometry, geometry) OWNER TO postgres;

--
-- Name: dimension(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dimension(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_dimension';


ALTER FUNCTION public.dimension(geometry) OWNER TO postgres;

--
-- Name: disjoint(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION disjoint(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'disjoint';


ALTER FUNCTION public.disjoint(geometry, geometry) OWNER TO postgres;

--
-- Name: distance(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION distance(geometry, geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'LWGEOM_mindistance2d';


ALTER FUNCTION public.distance(geometry, geometry) OWNER TO postgres;

--
-- Name: distance_sphere(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION distance_sphere(geometry, geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'LWGEOM_distance_sphere';


ALTER FUNCTION public.distance_sphere(geometry, geometry) OWNER TO postgres;

--
-- Name: distance_spheroid(geometry, geometry, spheroid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION distance_spheroid(geometry, geometry, spheroid) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'LWGEOM_distance_ellipsoid';


ALTER FUNCTION public.distance_spheroid(geometry, geometry, spheroid) OWNER TO postgres;

--
-- Name: dropbbox(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dropbbox(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_dropBBOX';


ALTER FUNCTION public.dropbbox(geometry) OWNER TO postgres;

--
-- Name: dump(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dump(geometry) RETURNS SETOF geometry_dump
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_dump';


ALTER FUNCTION public.dump(geometry) OWNER TO postgres;

--
-- Name: dumprings(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION dumprings(geometry) RETURNS SETOF geometry_dump
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_dump_rings';


ALTER FUNCTION public.dumprings(geometry) OWNER TO postgres;

--
-- Name: endpoint(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION endpoint(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_endpoint_linestring';


ALTER FUNCTION public.endpoint(geometry) OWNER TO postgres;

--
-- Name: envelope(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION envelope(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_envelope';


ALTER FUNCTION public.envelope(geometry) OWNER TO postgres;

--
-- Name: estimated_extent(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION estimated_extent(text, text) RETURNS box2d
    LANGUAGE c IMMUTABLE STRICT SECURITY DEFINER
    AS '$libdir/postgis-2.1', 'geometry_estimated_extent';


ALTER FUNCTION public.estimated_extent(text, text) OWNER TO postgres;

--
-- Name: estimated_extent(text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION estimated_extent(text, text, text) RETURNS box2d
    LANGUAGE c IMMUTABLE STRICT SECURITY DEFINER
    AS '$libdir/postgis-2.1', 'geometry_estimated_extent';


ALTER FUNCTION public.estimated_extent(text, text, text) OWNER TO postgres;

--
-- Name: expand(box2d, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION expand(box2d, double precision) RETURNS box2d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX2D_expand';


ALTER FUNCTION public.expand(box2d, double precision) OWNER TO postgres;

--
-- Name: expand(box3d, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION expand(box3d, double precision) RETURNS box3d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_expand';


ALTER FUNCTION public.expand(box3d, double precision) OWNER TO postgres;

--
-- Name: expand(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION expand(geometry, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_expand';


ALTER FUNCTION public.expand(geometry, double precision) OWNER TO postgres;

--
-- Name: exteriorring(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION exteriorring(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_exteriorring_polygon';


ALTER FUNCTION public.exteriorring(geometry) OWNER TO postgres;

--
-- Name: find_extent(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION find_extent(text, text) RETURNS box2d
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$
DECLARE
	tablename alias for $1;
	columnname alias for $2;
	myrec RECORD;

BEGIN
	FOR myrec IN EXECUTE 'SELECT ST_Extent("' || columnname || '") As extent FROM "' || tablename || '"' LOOP
		return myrec.extent;
	END LOOP;
END;
$_$;


ALTER FUNCTION public.find_extent(text, text) OWNER TO postgres;

--
-- Name: find_extent(text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION find_extent(text, text, text) RETURNS box2d
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$
DECLARE
	schemaname alias for $1;
	tablename alias for $2;
	columnname alias for $3;
	myrec RECORD;

BEGIN
	FOR myrec IN EXECUTE 'SELECT ST_Extent("' || columnname || '") FROM "' || schemaname || '"."' || tablename || '" As extent ' LOOP
		return myrec.extent;
	END LOOP;
END;
$_$;


ALTER FUNCTION public.find_extent(text, text, text) OWNER TO postgres;

--
-- Name: fix_geometry_columns(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION fix_geometry_columns() RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
	mislinked record;
	result text;
	linked integer;
	deleted integer;
	foundschema integer;
BEGIN

	-- Since 7.3 schema support has been added.
	-- Previous postgis versions used to put the database name in
	-- the schema column. This needs to be fixed, so we try to
	-- set the correct schema for each geometry_colums record
	-- looking at table, column, type and srid.
	
	return 'This function is obsolete now that geometry_columns is a view';

END;
$$;


ALTER FUNCTION public.fix_geometry_columns() OWNER TO postgres;

--
-- Name: force_2d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION force_2d(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_force_2d';


ALTER FUNCTION public.force_2d(geometry) OWNER TO postgres;

--
-- Name: force_3d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION force_3d(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_force_3dz';


ALTER FUNCTION public.force_3d(geometry) OWNER TO postgres;

--
-- Name: force_3dm(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION force_3dm(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_force_3dm';


ALTER FUNCTION public.force_3dm(geometry) OWNER TO postgres;

--
-- Name: force_3dz(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION force_3dz(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_force_3dz';


ALTER FUNCTION public.force_3dz(geometry) OWNER TO postgres;

--
-- Name: force_4d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION force_4d(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_force_4d';


ALTER FUNCTION public.force_4d(geometry) OWNER TO postgres;

--
-- Name: force_collection(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION force_collection(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_force_collection';


ALTER FUNCTION public.force_collection(geometry) OWNER TO postgres;

--
-- Name: forcerhr(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION forcerhr(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_force_clockwise_poly';


ALTER FUNCTION public.forcerhr(geometry) OWNER TO postgres;

--
-- Name: geomcollfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geomcollfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromText($1)) = 'GEOMETRYCOLLECTION'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.geomcollfromtext(text) OWNER TO postgres;

--
-- Name: geomcollfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geomcollfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromText($1, $2)) = 'GEOMETRYCOLLECTION'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.geomcollfromtext(text, integer) OWNER TO postgres;

--
-- Name: geomcollfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geomcollfromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromWKB($1)) = 'GEOMETRYCOLLECTION'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.geomcollfromwkb(bytea) OWNER TO postgres;

--
-- Name: geomcollfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geomcollfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromWKB($1, $2)) = 'GEOMETRYCOLLECTION'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.geomcollfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: geometryfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geometryfromtext(text) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_from_text';


ALTER FUNCTION public.geometryfromtext(text) OWNER TO postgres;

--
-- Name: geometryfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geometryfromtext(text, integer) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_from_text';


ALTER FUNCTION public.geometryfromtext(text, integer) OWNER TO postgres;

--
-- Name: geometryn(geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geometryn(geometry, integer) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_geometryn_collection';


ALTER FUNCTION public.geometryn(geometry, integer) OWNER TO postgres;

--
-- Name: geomfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geomfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_GeomFromText($1)$_$;


ALTER FUNCTION public.geomfromtext(text) OWNER TO postgres;

--
-- Name: geomfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geomfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_GeomFromText($1, $2)$_$;


ALTER FUNCTION public.geomfromtext(text, integer) OWNER TO postgres;

--
-- Name: geomfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geomfromwkb(bytea) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_from_WKB';


ALTER FUNCTION public.geomfromwkb(bytea) OWNER TO postgres;

--
-- Name: geomfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geomfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_SetSRID(ST_GeomFromWKB($1), $2)$_$;


ALTER FUNCTION public.geomfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: geomunion(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION geomunion(geometry, geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'geomunion';


ALTER FUNCTION public.geomunion(geometry, geometry) OWNER TO postgres;

--
-- Name: get_sync_code(uuid); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION get_sync_code(client_uuid uuid) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE 
	return_code VARCHAR;
	count integer;
	
BEGIN
count := 0;
SELECT sync_code INTO return_code FROM client WHERE client.uuid = CAST(client_uuid AS UUID);

if (return_code ISNULL OR char_length(return_code) < 1) then
	LOOP
		return_code := random_sync_code(7);
		BEGIN
			UPDATE client
			SET sync_code = return_code
			WHERE client.uuid = CAST(client_uuid AS UUID);
			return return_code;
		EXCEPTION WHEN unique_violation THEN
			-- return NULL when tried 10 times;
			if (count > 10) then
				return NULL;
			end if;
			count := count + 1;
		END;
	END LOOP;
else 
	return return_code;
end if;
END;
$$;


ALTER FUNCTION public.get_sync_code(client_uuid uuid) OWNER TO rmbt;

--
-- Name: getbbox(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION getbbox(geometry) RETURNS box2d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_to_BOX2D';


ALTER FUNCTION public.getbbox(geometry) OWNER TO postgres;

--
-- Name: getsrid(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION getsrid(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_get_srid';


ALTER FUNCTION public.getsrid(geometry) OWNER TO postgres;

--
-- Name: hasbbox(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION hasbbox(geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_hasBBOX';


ALTER FUNCTION public.hasbbox(geometry) OWNER TO postgres;

--
-- Name: hstore2json(hstore); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION hstore2json(hs hstore) RETURNS text
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
rv text;
r record;
BEGIN
rv:='';
for r in (select key, val from each(hs) as h(key, val)) loop
if rv<>'' then
rv:=rv||',';
end if;
rv:=rv || '"' || r.key || '":';
r.val := REPLACE(r.val, E'\\', E'\\\\');
r.val := REPLACE(r.val, '"', E'\\"');
r.val := REPLACE(r.val, E'\n', E'\\n');
r.val := REPLACE(r.val, E'\r', E'\\r');
rv:=rv || CASE WHEN r.val IS NULL THEN 'null' ELSE '"' || r.val || '"' END;
end loop;
return '{'||rv||'}';
END;
$$;


ALTER FUNCTION public.hstore2json(hs hstore) OWNER TO postgres;

--
-- Name: interiorringn(geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION interiorringn(geometry, integer) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_interiorringn_polygon';


ALTER FUNCTION public.interiorringn(geometry, integer) OWNER TO postgres;

--
-- Name: intersection(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION intersection(geometry, geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'intersection';


ALTER FUNCTION public.intersection(geometry, geometry) OWNER TO postgres;

--
-- Name: intersects(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION intersects(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'intersects';


ALTER FUNCTION public.intersects(geometry, geometry) OWNER TO postgres;

--
-- Name: isclosed(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION isclosed(geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_isclosed';


ALTER FUNCTION public.isclosed(geometry) OWNER TO postgres;

--
-- Name: isempty(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION isempty(geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_isempty';


ALTER FUNCTION public.isempty(geometry) OWNER TO postgres;

--
-- Name: isring(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION isring(geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'isring';


ALTER FUNCTION public.isring(geometry) OWNER TO postgres;

--
-- Name: issimple(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION issimple(geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'issimple';


ALTER FUNCTION public.issimple(geometry) OWNER TO postgres;

--
-- Name: isvalid(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION isvalid(geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'isvalid';


ALTER FUNCTION public.isvalid(geometry) OWNER TO postgres;

--
-- Name: json_object_set_key(json, text, anyelement); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION json_object_set_key(json_in json, key_to_set text, value_to_set anyelement) RETURNS json
    LANGUAGE sql IMMUTABLE
    AS $$
SELECT concat('{', string_agg(to_json("key") || ':' || "value", ','), '}')::json
  FROM (SELECT *
          FROM json_each(coalesce("json_in", '{}'::json))
         WHERE "key" <> "key_to_set"
         UNION ALL
        SELECT "key_to_set", to_json("value_to_set")) AS "fields"
$$;


ALTER FUNCTION public.json_object_set_key(json_in json, key_to_set text, value_to_set anyelement) OWNER TO rmbt;

--
-- Name: length(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION length(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_length_linestring';


ALTER FUNCTION public.length(geometry) OWNER TO postgres;

--
-- Name: length2d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION length2d(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_length2d_linestring';


ALTER FUNCTION public.length2d(geometry) OWNER TO postgres;

--
-- Name: length2d_spheroid(geometry, spheroid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION length2d_spheroid(geometry, spheroid) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'LWGEOM_length2d_ellipsoid';


ALTER FUNCTION public.length2d_spheroid(geometry, spheroid) OWNER TO postgres;

--
-- Name: length3d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION length3d(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_length_linestring';


ALTER FUNCTION public.length3d(geometry) OWNER TO postgres;

--
-- Name: length3d_spheroid(geometry, spheroid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION length3d_spheroid(geometry, spheroid) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_length_ellipsoid_linestring';


ALTER FUNCTION public.length3d_spheroid(geometry, spheroid) OWNER TO postgres;

--
-- Name: length_spheroid(geometry, spheroid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION length_spheroid(geometry, spheroid) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'LWGEOM_length_ellipsoid_linestring';


ALTER FUNCTION public.length_spheroid(geometry, spheroid) OWNER TO postgres;

--
-- Name: line_interpolate_point(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION line_interpolate_point(geometry, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_line_interpolate_point';


ALTER FUNCTION public.line_interpolate_point(geometry, double precision) OWNER TO postgres;

--
-- Name: line_locate_point(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION line_locate_point(geometry, geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_line_locate_point';


ALTER FUNCTION public.line_locate_point(geometry, geometry) OWNER TO postgres;

--
-- Name: line_substring(geometry, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION line_substring(geometry, double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_line_substring';


ALTER FUNCTION public.line_substring(geometry, double precision, double precision) OWNER TO postgres;

--
-- Name: linefrommultipoint(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linefrommultipoint(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_line_from_mpoint';


ALTER FUNCTION public.linefrommultipoint(geometry) OWNER TO postgres;

--
-- Name: linefromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linefromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'LINESTRING'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linefromtext(text) OWNER TO postgres;

--
-- Name: linefromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linefromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1, $2)) = 'LINESTRING'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linefromtext(text, integer) OWNER TO postgres;

--
-- Name: linefromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linefromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'LINESTRING'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linefromwkb(bytea) OWNER TO postgres;

--
-- Name: linefromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linefromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'LINESTRING'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linefromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: linemerge(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linemerge(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'linemerge';


ALTER FUNCTION public.linemerge(geometry) OWNER TO postgres;

--
-- Name: linestringfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linestringfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT LineFromText($1)$_$;


ALTER FUNCTION public.linestringfromtext(text) OWNER TO postgres;

--
-- Name: linestringfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linestringfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT LineFromText($1, $2)$_$;


ALTER FUNCTION public.linestringfromtext(text, integer) OWNER TO postgres;

--
-- Name: linestringfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linestringfromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'LINESTRING'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linestringfromwkb(bytea) OWNER TO postgres;

--
-- Name: linestringfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION linestringfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'LINESTRING'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linestringfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: locate_along_measure(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION locate_along_measure(geometry, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$ SELECT ST_locate_between_measures($1, $2, $2) $_$;


ALTER FUNCTION public.locate_along_measure(geometry, double precision) OWNER TO postgres;

--
-- Name: locate_between_measures(geometry, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION locate_between_measures(geometry, double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_locate_between_m';


ALTER FUNCTION public.locate_between_measures(geometry, double precision, double precision) OWNER TO postgres;

--
-- Name: m(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION m(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_m_point';


ALTER FUNCTION public.m(geometry) OWNER TO postgres;

--
-- Name: makebox2d(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makebox2d(geometry, geometry) RETURNS box2d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX2D_construct';


ALTER FUNCTION public.makebox2d(geometry, geometry) OWNER TO postgres;

--
-- Name: makebox3d(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makebox3d(geometry, geometry) RETURNS box3d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_construct';


ALTER FUNCTION public.makebox3d(geometry, geometry) OWNER TO postgres;

--
-- Name: makeline(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makeline(geometry, geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_makeline';


ALTER FUNCTION public.makeline(geometry, geometry) OWNER TO postgres;

--
-- Name: makeline_garray(geometry[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makeline_garray(geometry[]) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_makeline_garray';


ALTER FUNCTION public.makeline_garray(geometry[]) OWNER TO postgres;

--
-- Name: makepoint(double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makepoint(double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_makepoint';


ALTER FUNCTION public.makepoint(double precision, double precision) OWNER TO postgres;

--
-- Name: makepoint(double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makepoint(double precision, double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_makepoint';


ALTER FUNCTION public.makepoint(double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: makepoint(double precision, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makepoint(double precision, double precision, double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_makepoint';


ALTER FUNCTION public.makepoint(double precision, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: makepointm(double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makepointm(double precision, double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_makepoint3dm';


ALTER FUNCTION public.makepointm(double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: makepolygon(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makepolygon(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_makepoly';


ALTER FUNCTION public.makepolygon(geometry) OWNER TO postgres;

--
-- Name: makepolygon(geometry, geometry[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION makepolygon(geometry, geometry[]) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_makepoly';


ALTER FUNCTION public.makepolygon(geometry, geometry[]) OWNER TO postgres;

--
-- Name: max_distance(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION max_distance(geometry, geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_maxdistance2d_linestring';


ALTER FUNCTION public.max_distance(geometry, geometry) OWNER TO postgres;

--
-- Name: mem_size(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mem_size(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_mem_size';


ALTER FUNCTION public.mem_size(geometry) OWNER TO postgres;

--
-- Name: mlinefromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mlinefromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'MULTILINESTRING'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mlinefromtext(text) OWNER TO postgres;

--
-- Name: mlinefromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mlinefromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromText($1, $2)) = 'MULTILINESTRING'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mlinefromtext(text, integer) OWNER TO postgres;

--
-- Name: mlinefromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mlinefromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTILINESTRING'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mlinefromwkb(bytea) OWNER TO postgres;

--
-- Name: mlinefromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mlinefromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'MULTILINESTRING'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mlinefromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: mpointfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mpointfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'MULTIPOINT'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpointfromtext(text) OWNER TO postgres;

--
-- Name: mpointfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mpointfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1,$2)) = 'MULTIPOINT'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpointfromtext(text, integer) OWNER TO postgres;

--
-- Name: mpointfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mpointfromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTIPOINT'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpointfromwkb(bytea) OWNER TO postgres;

--
-- Name: mpointfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mpointfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1,$2)) = 'MULTIPOINT'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpointfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: mpolyfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mpolyfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'MULTIPOLYGON'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpolyfromtext(text) OWNER TO postgres;

--
-- Name: mpolyfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mpolyfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1, $2)) = 'MULTIPOLYGON'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpolyfromtext(text, integer) OWNER TO postgres;

--
-- Name: mpolyfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mpolyfromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTIPOLYGON'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpolyfromwkb(bytea) OWNER TO postgres;

--
-- Name: mpolyfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION mpolyfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'MULTIPOLYGON'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpolyfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: multi(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multi(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_force_multi';


ALTER FUNCTION public.multi(geometry) OWNER TO postgres;

--
-- Name: multilinefromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multilinefromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTILINESTRING'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multilinefromwkb(bytea) OWNER TO postgres;

--
-- Name: multilinefromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multilinefromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'MULTILINESTRING'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multilinefromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: multilinestringfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multilinestringfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_MLineFromText($1)$_$;


ALTER FUNCTION public.multilinestringfromtext(text) OWNER TO postgres;

--
-- Name: multilinestringfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multilinestringfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MLineFromText($1, $2)$_$;


ALTER FUNCTION public.multilinestringfromtext(text, integer) OWNER TO postgres;

--
-- Name: multipointfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multipointfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MPointFromText($1)$_$;


ALTER FUNCTION public.multipointfromtext(text) OWNER TO postgres;

--
-- Name: multipointfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multipointfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MPointFromText($1, $2)$_$;


ALTER FUNCTION public.multipointfromtext(text, integer) OWNER TO postgres;

--
-- Name: multipointfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multipointfromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTIPOINT'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multipointfromwkb(bytea) OWNER TO postgres;

--
-- Name: multipointfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multipointfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1,$2)) = 'MULTIPOINT'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multipointfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: multipolyfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multipolyfromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTIPOLYGON'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multipolyfromwkb(bytea) OWNER TO postgres;

--
-- Name: multipolyfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multipolyfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'MULTIPOLYGON'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multipolyfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: multipolygonfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multipolygonfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MPolyFromText($1)$_$;


ALTER FUNCTION public.multipolygonfromtext(text) OWNER TO postgres;

--
-- Name: multipolygonfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION multipolygonfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MPolyFromText($1, $2)$_$;


ALTER FUNCTION public.multipolygonfromtext(text, integer) OWNER TO postgres;

--
-- Name: ndims(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ndims(geometry) RETURNS smallint
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_ndims';


ALTER FUNCTION public.ndims(geometry) OWNER TO postgres;

--
-- Name: noop(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION noop(geometry) RETURNS geometry
    LANGUAGE c STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_noop';


ALTER FUNCTION public.noop(geometry) OWNER TO postgres;

--
-- Name: npoints(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION npoints(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_npoints';


ALTER FUNCTION public.npoints(geometry) OWNER TO postgres;

--
-- Name: nrings(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION nrings(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_nrings';


ALTER FUNCTION public.nrings(geometry) OWNER TO postgres;

--
-- Name: numgeometries(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION numgeometries(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_numgeometries_collection';


ALTER FUNCTION public.numgeometries(geometry) OWNER TO postgres;

--
-- Name: numinteriorring(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION numinteriorring(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_numinteriorrings_polygon';


ALTER FUNCTION public.numinteriorring(geometry) OWNER TO postgres;

--
-- Name: numinteriorrings(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION numinteriorrings(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_numinteriorrings_polygon';


ALTER FUNCTION public.numinteriorrings(geometry) OWNER TO postgres;

--
-- Name: numpoints(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION numpoints(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_numpoints_linestring';


ALTER FUNCTION public.numpoints(geometry) OWNER TO postgres;

--
-- Name: overlaps(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION "overlaps"(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'overlaps';


ALTER FUNCTION public."overlaps"(geometry, geometry) OWNER TO postgres;

--
-- Name: perimeter2d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION perimeter2d(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_perimeter2d_poly';


ALTER FUNCTION public.perimeter2d(geometry) OWNER TO postgres;

--
-- Name: perimeter3d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION perimeter3d(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_perimeter_poly';


ALTER FUNCTION public.perimeter3d(geometry) OWNER TO postgres;

--
-- Name: point_inside_circle(geometry, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION point_inside_circle(geometry, double precision, double precision, double precision) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_inside_circle_point';


ALTER FUNCTION public.point_inside_circle(geometry, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: pointfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pointfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'POINT'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.pointfromtext(text) OWNER TO postgres;

--
-- Name: pointfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pointfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1, $2)) = 'POINT'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.pointfromtext(text, integer) OWNER TO postgres;

--
-- Name: pointfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pointfromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'POINT'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.pointfromwkb(bytea) OWNER TO postgres;

--
-- Name: pointfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pointfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(ST_GeomFromWKB($1, $2)) = 'POINT'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.pointfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: pointn(geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pointn(geometry, integer) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_pointn_linestring';


ALTER FUNCTION public.pointn(geometry, integer) OWNER TO postgres;

--
-- Name: pointonsurface(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION pointonsurface(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'pointonsurface';


ALTER FUNCTION public.pointonsurface(geometry) OWNER TO postgres;

--
-- Name: polyfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION polyfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'POLYGON'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polyfromtext(text) OWNER TO postgres;

--
-- Name: polyfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION polyfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1, $2)) = 'POLYGON'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polyfromtext(text, integer) OWNER TO postgres;

--
-- Name: polyfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION polyfromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'POLYGON'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polyfromwkb(bytea) OWNER TO postgres;

--
-- Name: polyfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION polyfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'POLYGON'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polyfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: polygonfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION polygonfromtext(text) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT PolyFromText($1)$_$;


ALTER FUNCTION public.polygonfromtext(text) OWNER TO postgres;

--
-- Name: polygonfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION polygonfromtext(text, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT PolyFromText($1, $2)$_$;


ALTER FUNCTION public.polygonfromtext(text, integer) OWNER TO postgres;

--
-- Name: polygonfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION polygonfromwkb(bytea) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'POLYGON'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polygonfromwkb(bytea) OWNER TO postgres;

--
-- Name: polygonfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION polygonfromwkb(bytea, integer) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1,$2)) = 'POLYGON'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polygonfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: polygonize_garray(geometry[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION polygonize_garray(geometry[]) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'polygonize_garray';


ALTER FUNCTION public.polygonize_garray(geometry[]) OWNER TO postgres;

--
-- Name: probe_geometry_columns(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION probe_geometry_columns() RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
	inserted integer;
	oldcount integer;
	probed integer;
	stale integer;
BEGIN


	RETURN 'This function is obsolete now that geometry_columns is a view';
END

$$;


ALTER FUNCTION public.probe_geometry_columns() OWNER TO postgres;

--
-- Name: qos_import_json(text, text, boolean); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION qos_import_json(_filename text, _lang text, _insert boolean) RETURNS TABLE(key text, value text, lang text)
    LANGUAGE plpgsql STRICT
    AS $$
DECLARE
	_data text;
	_row record;
	_copy text;
BEGIN
   CREATE TEMP TABLE import(data text) ON COMMIT DROP;
   _copy = 'COPY import(data) FROM ' || quote_literal(_filename);
   EXECUTE _copy;
   SELECT data FROM import INTO _data;
   FOR _row IN SELECT * FROM json_each_text(_data::json)
   LOOP
	key := _row.key;
	value := _row.value;
	lang := _lang;
	IF (_insert) THEN
		INSERT INTO qos_test_desc(desc_key, value, lang) VALUES(key, value, lang);
	END IF;
	RETURN NEXT;
   END LOOP;
END;
$$;


ALTER FUNCTION public.qos_import_json(_filename text, _lang text, _insert boolean) OWNER TO rmbt;

--
-- Name: random_sync_code(integer); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION random_sync_code(integer) RETURNS text
    LANGUAGE sql
    AS $_$

    select upper(
        substring(
            (
                SELECT string_agg(md5(random()::TEXT), '')
                FROM generate_series(1, CEIL($1 / 32.)::integer)
                ),
        (33-$1))
    );

$_$;


ALTER FUNCTION public.random_sync_code(integer) OWNER TO rmbt;

--
-- Name: relate(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION relate(geometry, geometry) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'relate_full';


ALTER FUNCTION public.relate(geometry, geometry) OWNER TO postgres;

--
-- Name: relate(geometry, geometry, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION relate(geometry, geometry, text) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'relate_pattern';


ALTER FUNCTION public.relate(geometry, geometry, text) OWNER TO postgres;

--
-- Name: removepoint(geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION removepoint(geometry, integer) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_removepoint';


ALTER FUNCTION public.removepoint(geometry, integer) OWNER TO postgres;

--
-- Name: rename_geometry_table_constraints(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION rename_geometry_table_constraints() RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $$
SELECT 'rename_geometry_table_constraint() is obsoleted'::text
$$;


ALTER FUNCTION public.rename_geometry_table_constraints() OWNER TO postgres;

--
-- Name: reverse(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION reverse(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_reverse';


ALTER FUNCTION public.reverse(geometry) OWNER TO postgres;

--
-- Name: rmbt_fill_open_uuid(); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION rmbt_fill_open_uuid() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
 _t RECORD;
 _uuid uuid;
BEGIN

FOR _t IN SELECT uid,client_id,time FROM test WHERE open_uuid IS NULL ORDER BY uid LOOP
    SELECT INTO _uuid open_uuid FROM test WHERE client_id=_t.client_id AND (_t.time - INTERVAL '4 hours' < time) AND uid<_t.uid ORDER BY uid DESC LIMIT 1;
    IF (_uuid IS NULL) THEN
        _uuid = uuid_generate_v4();
    END IF;
    UPDATE test SET open_uuid=_uuid WHERE uid=_t.uid;
END LOOP;

END;$$;


ALTER FUNCTION public.rmbt_fill_open_uuid() OWNER TO rmbt;

--
-- Name: rmbt_get_next_test_slot(bigint); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION rmbt_get_next_test_slot(_test_id bigint) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
  _slot integer;
  _count integer;
  _server_id integer;
BEGIN
SELECT server_id FROM test WHERE uid = _test_id INTO _server_id;
_slot := EXTRACT(EPOCH FROM NOW())::int - 2;
_count := 100;
WHILE _count >= 5 LOOP
  _slot := _slot + 1;
  SELECT COUNT(uid) FROM test WHERE test_slot = _slot AND server_id=_server_id INTO _count;
END LOOP;
  UPDATE test SET test_slot = _slot WHERE uid = _test_id;
RETURN _slot;
END;
$$;


ALTER FUNCTION public.rmbt_get_next_test_slot(_test_id bigint) OWNER TO rmbt;

--
-- Name: rmbt_get_sync_code(uuid); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION rmbt_get_sync_code(client_uuid uuid) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE 
	return_code VARCHAR;
	count integer;
	
BEGIN
count := 0;
SELECT sync_code INTO return_code FROM client WHERE client.uuid = CAST(client_uuid AS UUID) AND sync_code_timestamp + INTERVAL '1 month' > NOW();

if (return_code ISNULL OR char_length(return_code) < 1) then
	LOOP
		return_code := random_sync_code(12);
		BEGIN
			UPDATE client
			SET sync_code = return_code,
			sync_code_timestamp = NOW()
			WHERE client.uuid = CAST(client_uuid AS UUID);
			return return_code;
		EXCEPTION WHEN unique_violation THEN
			-- return NULL when tried 10 times;
			if (count > 10) then
				return NULL;
			end if;
			count := count + 1;
		END;
	END LOOP;
else 
	return return_code;
end if;
END;
$$;


ALTER FUNCTION public.rmbt_get_sync_code(client_uuid uuid) OWNER TO rmbt;

--
-- Name: rmbt_random_sync_code(integer); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION rmbt_random_sync_code(integer) RETURNS text
    LANGUAGE sql
    AS $_$

    select upper(
        substring(
            (
                SELECT string_agg(md5(random()::TEXT), '')
                FROM generate_series(1, CEIL($1 / 32.)::integer)
                ),
        (33-$1))
    );

$_$;


ALTER FUNCTION public.rmbt_random_sync_code(integer) OWNER TO rmbt;

--
-- Name: rmbt_set_provider_from_as(bigint); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION rmbt_set_provider_from_as(_test_id bigint) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
  _asn bigint;
  _rdns character varying;
  _provider_id integer;
  _provider_name character varying;
BEGIN

SELECT
  ap.provider_id,
  p.shortname
  FROM test t
  JOIN as2provider ap
  ON t.public_ip_asn=ap.asn 
  AND (ap.dns_part IS NULL OR t.public_ip_rdns ILIKE ap.dns_part /*Case insensitive regexp, DJ per #235:*/ OR t.public_ip_rdns ~* ap.dns_part)
  JOIN provider p
  ON p.uid = ap.provider_id
  WHERE t.uid = _test_id
  ORDER BY dns_part IS NOT NULL DESC
  LIMIT 1
  INTO _provider_id, _provider_name;

IF _provider_id IS NOT NULL THEN
  UPDATE test SET provider_id = _provider_id WHERE uid = _test_id;
  RETURN _provider_name;
ELSE
  RETURN NULL;
END IF;

END;
$$;


ALTER FUNCTION public.rmbt_set_provider_from_as(_test_id bigint) OWNER TO rmbt;

--
-- Name: rmbt_tmp_fill_cid(); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION rmbt_tmp_fill_cid() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
 _t RECORD;
 _l RECORD;
 _first boolean;
BEGIN

FOR _t IN SELECT uid,data FROM test WHERE status='FINISHED' ORDER BY uid LOOP

    _first := true;
    FOR _l IN SELECT lid.location_id,b.location_name FROM
	(SELECT location_id from cell_location where test_id=_t.uid group by location_id order by min(uid)) lid
	LEFT JOIN base_stations b ON (lid.location_id=b.ci or lid.location_id=b.eci) LOOP

        if (_first) THEN
            _t.data = json_object_set_key(_t.data, 'cell_id', _l.location_id);
	    _t.data = json_object_set_key(_t.data, 'cell_name', _l.location_name);
	    _first := false;
        ELSE
	    _t.data = json_object_set_key(_t.data, 'cell_id_multiple', true);
        END IF;

    END LOOP;	

    -- RAISE NOTICE 'data: %', _t.data;
    UPDATE test SET data = _t.data WHERE uid=_t.uid;
END LOOP;

END;$$;


ALTER FUNCTION public.rmbt_tmp_fill_cid() OWNER TO rmbt;

--
-- Name: rotate(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION rotate(geometry, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_rotateZ($1, $2)$_$;


ALTER FUNCTION public.rotate(geometry, double precision) OWNER TO postgres;

--
-- Name: rotatex(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION rotatex(geometry, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1, 1, 0, 0, 0, cos($2), -sin($2), 0, sin($2), cos($2), 0, 0, 0)$_$;


ALTER FUNCTION public.rotatex(geometry, double precision) OWNER TO postgres;

--
-- Name: rotatey(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION rotatey(geometry, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  cos($2), 0, sin($2),  0, 1, 0,  -sin($2), 0, cos($2), 0,  0, 0)$_$;


ALTER FUNCTION public.rotatey(geometry, double precision) OWNER TO postgres;

--
-- Name: rotatez(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION rotatez(geometry, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  cos($2), -sin($2), 0,  sin($2), cos($2), 0,  0, 0, 1,  0, 0, 0)$_$;


ALTER FUNCTION public.rotatez(geometry, double precision) OWNER TO postgres;

--
-- Name: scale(geometry, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION scale(geometry, double precision, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_scale($1, $2, $3, 1)$_$;


ALTER FUNCTION public.scale(geometry, double precision, double precision) OWNER TO postgres;

--
-- Name: scale(geometry, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION scale(geometry, double precision, double precision, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  $2, 0, 0,  0, $3, 0,  0, 0, $4,  0, 0, 0)$_$;


ALTER FUNCTION public.scale(geometry, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: se_envelopesintersect(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION se_envelopesintersect(geometry, geometry) RETURNS boolean
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$ 
	SELECT $1 && $2
	$_$;


ALTER FUNCTION public.se_envelopesintersect(geometry, geometry) OWNER TO postgres;

--
-- Name: se_is3d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION se_is3d(geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_hasz';


ALTER FUNCTION public.se_is3d(geometry) OWNER TO postgres;

--
-- Name: se_ismeasured(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION se_ismeasured(geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_hasm';


ALTER FUNCTION public.se_ismeasured(geometry) OWNER TO postgres;

--
-- Name: se_locatealong(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION se_locatealong(geometry, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$ SELECT SE_LocateBetween($1, $2, $2) $_$;


ALTER FUNCTION public.se_locatealong(geometry, double precision) OWNER TO postgres;

--
-- Name: se_locatebetween(geometry, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION se_locatebetween(geometry, double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_locate_between_m';


ALTER FUNCTION public.se_locatebetween(geometry, double precision, double precision) OWNER TO postgres;

--
-- Name: se_m(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION se_m(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_m_point';


ALTER FUNCTION public.se_m(geometry) OWNER TO postgres;

--
-- Name: se_z(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION se_z(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_z_point';


ALTER FUNCTION public.se_z(geometry) OWNER TO postgres;

--
-- Name: segmentize(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION segmentize(geometry, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_segmentize2d';


ALTER FUNCTION public.segmentize(geometry, double precision) OWNER TO postgres;

--
-- Name: setpoint(geometry, integer, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION setpoint(geometry, integer, geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_setpoint_linestring';


ALTER FUNCTION public.setpoint(geometry, integer, geometry) OWNER TO postgres;

--
-- Name: setsrid(geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION setsrid(geometry, integer) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_set_srid';


ALTER FUNCTION public.setsrid(geometry, integer) OWNER TO postgres;

--
-- Name: shift_longitude(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION shift_longitude(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_longitude_shift';


ALTER FUNCTION public.shift_longitude(geometry) OWNER TO postgres;

--
-- Name: simplify(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION simplify(geometry, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_simplify2d';


ALTER FUNCTION public.simplify(geometry, double precision) OWNER TO postgres;

--
-- Name: snaptogrid(geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION snaptogrid(geometry, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_SnapToGrid($1, 0, 0, $2, $2)$_$;


ALTER FUNCTION public.snaptogrid(geometry, double precision) OWNER TO postgres;

--
-- Name: snaptogrid(geometry, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION snaptogrid(geometry, double precision, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_SnapToGrid($1, 0, 0, $2, $3)$_$;


ALTER FUNCTION public.snaptogrid(geometry, double precision, double precision) OWNER TO postgres;

--
-- Name: snaptogrid(geometry, double precision, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION snaptogrid(geometry, double precision, double precision, double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_snaptogrid';


ALTER FUNCTION public.snaptogrid(geometry, double precision, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: snaptogrid(geometry, geometry, double precision, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION snaptogrid(geometry, geometry, double precision, double precision, double precision, double precision) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_snaptogrid_pointoff';


ALTER FUNCTION public.snaptogrid(geometry, geometry, double precision, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: srid(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION srid(geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_get_srid';


ALTER FUNCTION public.srid(geometry) OWNER TO postgres;

--
-- Name: st_asbinary(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_asbinary(text) RETURNS bytea
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$ SELECT ST_AsBinary($1::geometry);$_$;


ALTER FUNCTION public.st_asbinary(text) OWNER TO postgres;

--
-- Name: st_astext(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_astext(bytea) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$ SELECT ST_AsText($1::geometry);$_$;


ALTER FUNCTION public.st_astext(bytea) OWNER TO postgres;

--
-- Name: st_box(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_box(box3d) RETURNS box
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_to_BOX';


ALTER FUNCTION public.st_box(box3d) OWNER TO postgres;

--
-- Name: st_box(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_box(geometry) RETURNS box
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_to_BOX';


ALTER FUNCTION public.st_box(geometry) OWNER TO postgres;

--
-- Name: st_box2d(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_box2d(box3d) RETURNS box2d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_to_BOX2D';


ALTER FUNCTION public.st_box2d(box3d) OWNER TO postgres;

--
-- Name: st_box2d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_box2d(geometry) RETURNS box2d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_to_BOX2D';


ALTER FUNCTION public.st_box2d(geometry) OWNER TO postgres;

--
-- Name: st_box3d(box2d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_box3d(box2d) RETURNS box3d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX2D_to_BOX3D';


ALTER FUNCTION public.st_box3d(box2d) OWNER TO postgres;

--
-- Name: st_box3d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_box3d(geometry) RETURNS box3d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_to_BOX3D';


ALTER FUNCTION public.st_box3d(geometry) OWNER TO postgres;

--
-- Name: st_box3d_in(cstring); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_box3d_in(cstring) RETURNS box3d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_in';


ALTER FUNCTION public.st_box3d_in(cstring) OWNER TO postgres;

--
-- Name: st_box3d_out(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_box3d_out(box3d) RETURNS cstring
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_out';


ALTER FUNCTION public.st_box3d_out(box3d) OWNER TO postgres;

--
-- Name: st_bytea(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_bytea(geometry) RETURNS bytea
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_to_bytea';


ALTER FUNCTION public.st_bytea(geometry) OWNER TO postgres;

--
-- Name: st_geometry(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry(bytea) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_from_bytea';


ALTER FUNCTION public.st_geometry(bytea) OWNER TO postgres;

--
-- Name: st_geometry(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry(text) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'parse_WKT_lwgeom';


ALTER FUNCTION public.st_geometry(text) OWNER TO postgres;

--
-- Name: st_geometry(box2d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry(box2d) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX2D_to_LWGEOM';


ALTER FUNCTION public.st_geometry(box2d) OWNER TO postgres;

--
-- Name: st_geometry(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry(box3d) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_to_LWGEOM';


ALTER FUNCTION public.st_geometry(box3d) OWNER TO postgres;

--
-- Name: st_geometry_cmp(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry_cmp(geometry, geometry) RETURNS integer
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'lwgeom_cmp';


ALTER FUNCTION public.st_geometry_cmp(geometry, geometry) OWNER TO postgres;

--
-- Name: st_geometry_eq(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry_eq(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'lwgeom_eq';


ALTER FUNCTION public.st_geometry_eq(geometry, geometry) OWNER TO postgres;

--
-- Name: st_geometry_ge(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry_ge(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'lwgeom_ge';


ALTER FUNCTION public.st_geometry_ge(geometry, geometry) OWNER TO postgres;

--
-- Name: st_geometry_gt(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry_gt(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'lwgeom_gt';


ALTER FUNCTION public.st_geometry_gt(geometry, geometry) OWNER TO postgres;

--
-- Name: st_geometry_le(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry_le(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'lwgeom_le';


ALTER FUNCTION public.st_geometry_le(geometry, geometry) OWNER TO postgres;

--
-- Name: st_geometry_lt(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_geometry_lt(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'lwgeom_lt';


ALTER FUNCTION public.st_geometry_lt(geometry, geometry) OWNER TO postgres;

--
-- Name: st_length3d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_length3d(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_length_linestring';


ALTER FUNCTION public.st_length3d(geometry) OWNER TO postgres;

--
-- Name: st_length_spheroid3d(geometry, spheroid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_length_spheroid3d(geometry, spheroid) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'LWGEOM_length_ellipsoid_linestring';


ALTER FUNCTION public.st_length_spheroid3d(geometry, spheroid) OWNER TO postgres;

--
-- Name: st_makebox3d(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_makebox3d(geometry, geometry) RETURNS box3d
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_construct';


ALTER FUNCTION public.st_makebox3d(geometry, geometry) OWNER TO postgres;

--
-- Name: st_makeline_garray(geometry[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_makeline_garray(geometry[]) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_makeline_garray';


ALTER FUNCTION public.st_makeline_garray(geometry[]) OWNER TO postgres;

--
-- Name: st_perimeter3d(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_perimeter3d(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_perimeter_poly';


ALTER FUNCTION public.st_perimeter3d(geometry) OWNER TO postgres;

--
-- Name: st_polygonize_garray(geometry[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_polygonize_garray(geometry[]) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT COST 100
    AS '$libdir/postgis-2.1', 'polygonize_garray';


ALTER FUNCTION public.st_polygonize_garray(geometry[]) OWNER TO postgres;

--
-- Name: st_text(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_text(geometry) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_to_text';


ALTER FUNCTION public.st_text(geometry) OWNER TO postgres;

--
-- Name: st_unite_garray(geometry[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION st_unite_garray(geometry[]) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'pgis_union_geometry_array';


ALTER FUNCTION public.st_unite_garray(geometry[]) OWNER TO postgres;

--
-- Name: startpoint(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION startpoint(geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_startpoint_linestring';


ALTER FUNCTION public.startpoint(geometry) OWNER TO postgres;

--
-- Name: summary(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION summary(geometry) RETURNS text
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_summary';


ALTER FUNCTION public.summary(geometry) OWNER TO postgres;

--
-- Name: symdifference(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION symdifference(geometry, geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'symdifference';


ALTER FUNCTION public.symdifference(geometry, geometry) OWNER TO postgres;

--
-- Name: symmetricdifference(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION symmetricdifference(geometry, geometry) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'symdifference';


ALTER FUNCTION public.symmetricdifference(geometry, geometry) OWNER TO postgres;

--
-- Name: touches(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION touches(geometry, geometry) RETURNS boolean
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'touches';


ALTER FUNCTION public.touches(geometry, geometry) OWNER TO postgres;

--
-- Name: transform(geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION transform(geometry, integer) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'transform';


ALTER FUNCTION public.transform(geometry, integer) OWNER TO postgres;

--
-- Name: translate(geometry, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION translate(geometry, double precision, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_translate($1, $2, $3, 0)$_$;


ALTER FUNCTION public.translate(geometry, double precision, double precision) OWNER TO postgres;

--
-- Name: translate(geometry, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION translate(geometry, double precision, double precision, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1, 1, 0, 0, 0, 1, 0, 0, 0, 1, $2, $3, $4)$_$;


ALTER FUNCTION public.translate(geometry, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: transscale(geometry, double precision, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION transscale(geometry, double precision, double precision, double precision, double precision) RETURNS geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  $4, 0, 0,  0, $5, 0,
		0, 0, 1,  $2 * $4, $3 * $5, 0)$_$;


ALTER FUNCTION public.transscale(geometry, double precision, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: trigger_test(); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION trigger_test() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    _tmp_uuid uuid;
    _tmp_uid integer;
    _tmp_time timestamp;
    _tmp_network_group_name VARCHAR;
    _mcc_sim VARCHAR;
    _mcc_net VARCHAR;
    _min_accuracy integer;

    -- 2G thresholds for test im plausibility
    _threshold_implausible_2G_download_kbps integer;
    _threshold_implausible_2G_upload_kbps integer;
    _threshold_implausible_2G_ping_ns bigint;

    -- 3G thresholds for test im plausibility
    _threshold_implausible_3G_download_kbps integer;
    _threshold_implausible_3G_upload_kbps integer;
    _threshold_implausible_3G_ping_ns bigint;

    -- 4G thresholds for test im plausibility
    _threshold_implausible_4G_download_kbps integer;
    _threshold_implausible_4G_upload_kbps integer;
    _threshold_implausible_4G_ping_ns bigint;

    v_old_data TEXT;
    v_new_data TEXT;
    _first boolean;
    _l RECORD;
BEGIN

    SELECT INTO _min_accuracy "value"::integer FROM settings WHERE key='rmbt_geo_distance_detail_limit';
    IF (_min_accuracy IS NULL) THEN
	_min_accuracy := 3000;
    END IF;

    IF ((TG_OP = 'INSERT' OR NEW.speed_download IS DISTINCT FROM OLD.speed_download) AND NEW.speed_download > 0) THEN
        NEW.speed_download_log=(log(NEW.speed_download::double precision/10))/4;
    END IF;
    IF ((TG_OP = 'INSERT' OR NEW.speed_upload IS DISTINCT FROM OLD.speed_upload) AND NEW.speed_upload > 0) THEN
        NEW.speed_upload_log=(log(NEW.speed_upload::double precision/10))/4;
    END IF;
    IF ((TG_OP = 'INSERT' OR NEW.ping_shortest IS DISTINCT FROM OLD.ping_shortest) AND NEW.ping_shortest > 0) THEN
        NEW.ping_shortest_log=(log(NEW.ping_shortest::double precision/1000000))/3;
        SELECT INTO NEW.ping_median floor(median(coalesce(value_server,value))) FROM ping WHERE NEW.uid = test_id;
        NEW.ping_median_log=(log(NEW.ping_median::double precision/1000000))/3;
        IF (NEW.ping_median IS NULL) THEN
             NEW.ping_median = NEW.ping_shortest;
        END IF;
    END IF;

    IF (TG_OP = 'INSERT' OR NEW.location IS DISTINCT FROM OLD.location) THEN
        IF (NEW.location IS NULL OR NEW.geo_accuracy > _min_accuracy) THEN
            NEW.zip_code_geo = NULL;
            NEW.country_location = NULL;
        ELSE
	    -- generate fake coordinates if publish_public_data is set to false:
	    IF (NOT NEW.publish_public_data) THEN
		NEW.real_location = NEW.location;
		NEW.real_geo_lat = NEW.geo_lat;
		NEW.real_geo_long = NEW.geo_long;
		NEW.location = ST_Transform(ST_Project(ST_Transform(NEW.location,4326), random() * 150, radians(360 * random()))::geometry, 900913);  -- akos_generate_random_point(150, NEW.location);
		NEW.geo_lat = ST_Y(ST_Transform(NEW.location, 4326));
		NEW.geo_long = ST_X(ST_Transform(NEW.location, 4326));
		NEW.geo_provider = 'restricted';
		NEW.geo_accuracy = 150;
	    END IF;        
            -- si gis
            -- si gis
            NEW.data = json_object_set_key(NEW.data, 'region', (SELECT ime FROM si_regions s WHERE ST_Contains(s.geom, NEW.location) LIMIT 1));
            NEW.data = json_object_set_key(NEW.data, 'municipality', (SELECT ob_uime FROM si_municipality s WHERE ST_Contains(s.geom, NEW.location) LIMIT 1));
            NEW.data = json_object_set_key(NEW.data, 'settlement', (SELECT na_uime FROM si_settlements s WHERE ST_Contains(s.geom, NEW.location) LIMIT 1));
            NEW.data = json_object_set_key(NEW.data, 'whitespace', (SELECT na_uime FROM si_whitespaces s WHERE ST_Contains(s.geom, NEW.location) LIMIT 1));
            
            IF (EXISTS (SELECT ime FROM si_regions s WHERE ST_Contains(s.geom, NEW.location))) THEN
                NEW.country_location = 'SI'; -- si_regions is more accurate for AT than ne_50m_admin_0_countries
            ELSE
                SELECT INTO NEW.country_location iso_a2
                FROM ne_50m_admin_0_countries
                WHERE NEW.location && the_geom AND Within(NEW.location, the_geom) AND char_length(iso_a2)=2
                LIMIT 1;
            END IF;
        END IF;
    END IF;

    IF (TG_OP = 'INSERT'
        OR NEW.network_sim_operator IS DISTINCT FROM OLD.network_sim_operator
        OR NEW.network_operator IS DISTINCT FROM OLD.network_operator
        OR NEW.time IS DISTINCT FROM OLD.time
        ) THEN

            IF (NEW.network_sim_operator IS NULL OR NEW.network_operator IS NULL) THEN
                NEW.roaming_type = NULL;
            ELSE
		IF (NEW.network_sim_operator = NEW.network_operator) THEN
			NEW.roaming_type = 0; -- no roaming
		ELSE
                    _mcc_sim := split_part(NEW.network_sim_operator, '-', 1);
                    _mcc_net := split_part(NEW.network_operator, '-', 1);
                    IF (_mcc_sim = _mcc_net) THEN
                        NEW.roaming_type = 1;  -- national roaming
                    ELSE
			NEW.roaming_type = 2;  -- international roaming
		    END IF;
                END IF;
            END IF;

            IF ((NEW.roaming_type IS NULL AND NEW.country_location != 'SI') OR NEW.roaming_type = 2) THEN -- not for foreign networks
                NEW.mobile_provider_id = NULL;
            ELSE
                SELECT INTO NEW.mobile_provider_id provider_id FROM mccmnc2provider
                    WHERE mcc_mnc_sim = NEW.network_sim_operator
                    AND (valid_from IS NULL OR valid_from <= NEW.time) AND (valid_to IS NULL OR valid_to >= NEW.time)
                    AND (mcc_mnc_network IS NULL OR mcc_mnc_network = NEW.network_operator)
                    ORDER BY mcc_mnc_network NULLS LAST
                    LIMIT 1;
            END IF;
    END IF;

     IF ((TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED') 
          AND (NEW.time > (now() - INTERVAL '5 minutes'))) THEN -- update only new entries, skip old entries
          IF (NEW.network_operator is not NULL) THEN
            SELECT INTO NEW.mobile_network_id COALESCE(n.mapped_uid,n.uid) 
                FROM mccmnc2name n
                WHERE NEW.network_operator=n.mccmnc 
                AND (n.valid_from is null OR n.valid_from <= NEW.time)
                AND (n.valid_to is null or n.valid_to  >= NEW.time)
                AND use_for_network=TRUE
                ORDER BY n.uid NULLS LAST
                LIMIT 1;
          END IF;
          
          IF (NEW.network_sim_operator is not NULL) THEN
          SELECT INTO NEW.mobile_sim_id COALESCE(n.mapped_uid,n.uid) 
                FROM mccmnc2name n
                WHERE NEW.network_sim_operator=n.mccmnc 
                AND (n.valid_from is null OR n.valid_from <= NEW.time)
                AND (n.valid_to is null or n.valid_to  >= NEW.time)
                AND (NEW.network_sim_operator=n.mcc_mnc_network_mapping OR n.mcc_mnc_network_mapping is NULL)
                AND use_for_sim=TRUE
                ORDER BY n.uid NULLS LAST
                LIMIT 1;
          END IF;

          _first := true;
	  FOR _l IN SELECT lid.location_id,b.location_name FROM
	      (SELECT location_id from cell_location where test_id=NEW.uid group by location_id ORDER BY min(uid)) lid
	      LEFT JOIN base_stations b ON (lid.location_id=b.ci OR lid.location_id=b.eci) LOOP

              if (_first) THEN
	          NEW.data = json_object_set_key(NEW.data, 'cell_id', _l.location_id);
	          NEW.data = json_object_set_key(NEW.data, 'cell_name', _l.location_name);
	          _first := false;
	      ELSE
	          NEW.data = json_object_set_key(NEW.data, 'cell_id_multiple', true);
	      END IF;
	  END LOOP;
          
     END IF;


/* disable for java CLI functionality */
    IF ((TG_OP = 'UPDATE')  AND (NEW.time > (now() - INTERVAL '5 minutes')) AND NEW.network_type=97/*CLI*/ AND NEW.deleted=FALSE) THEN
        NEW.deleted=TRUE;
        NEW.comment='Exclude CLI per #211';
    END IF;    

-- test of plausibility
    IF ((TG_OP = 'UPDATE')  AND (NEW.time > (now() - INTERVAL '5 minutes')) AND NEW.deleted=FALSE) THEN

      -- 2G mobile technology
      IF (NEW.network_type IN (1,2,4,5,6,7,11,12,14)) THEN

        -- 2G download in kbps, default 0.5 Mbit/s
        SELECT INTO _threshold_implausible_2G_download_kbps "value"::integer FROM settings WHERE key='threshold_implausible_2G_download_kbps';
        IF (_threshold_implausible_2G_download_kbps IS NULL) THEN
        _threshold_implausible_2G_download_kbps := 500;
        END IF;

        -- 2G upload in kbps, default 0.3Mbit/s
        SELECT INTO _threshold_implausible_2G_upload_kbps "value"::integer FROM settings WHERE key='threshold_implausible_2G_upload_kbps';
        IF (_threshold_implausible_2G_upload_kbps IS NULL) THEN
        _threshold_implausible_2G_upload_kbps := 300;
        END IF;

        -- 2G ping in nanoseconds, default 50 miliseconds
        SELECT INTO _threshold_implausible_2G_ping_ns "value"::bigint FROM settings WHERE key='threshold_implausible_2G_ping_ns';
        IF (_threshold_implausible_2G_ping_ns IS NULL) THEN
        _threshold_implausible_2G_ping_ns := 50000000;
        END IF;

        -- check download speed
        IF (NEW.speed_download IS NOT NULL AND NEW.speed_download > _threshold_implausible_2G_download_kbps) THEN
          NEW.implausible = TRUE;
          NEW.comment = CONCAT( NEW.comment, 'Excluded because of implausible download speed. ');
        END IF;

        -- check upload speed
        IF (NEW.speed_upload IS NOT NULL AND NEW.speed_upload > _threshold_implausible_2G_upload_kbps) THEN
          NEW.implausible = TRUE;
          NEW.comment = CONCAT( NEW.comment, 'Excluded because of implausible upload speed. ');
        END IF;

        -- check ping
        IF (NEW.ping_shortest IS NOT NULL AND NEW.ping_shortest < _threshold_implausible_2G_ping_ns) THEN
          NEW.implausible = TRUE;
          NEW.comment = CONCAT( NEW.comment, 'Excluded because of implausible ping. ');
        END IF;

      END IF;--2G


      -- 3G mobile technology
      --IF (NEW.network_type=3 OR NEW.network_type=8 OR NEW.network_type=9 OR NEW.network_type=10 OR NEW.network_type=15) THEN
      IF (NEW.network_type IN (3,8,9,10,15)) THEN

        -- 3G download in kbps, default 50 Mbit/s
        SELECT INTO _threshold_implausible_3G_download_kbps "value"::integer FROM settings WHERE key='threshold_implausible_3G_download_kbps';
        IF (_threshold_implausible_3G_download_kbps IS NULL) THEN
        _threshold_implausible_3G_download_kbps := 50000;
        END IF;

        -- 3G upload in kbps, default 30 Mbit/s
        SELECT INTO _threshold_implausible_3G_upload_kbps "value"::integer FROM settings WHERE key='threshold_implausible_3G_upload_kbps';
        IF (_threshold_implausible_3G_upload_kbps IS NULL) THEN
        _threshold_implausible_3G_upload_kbps := 30000;
        END IF;

        -- 3G ping in nanoseconds, defalt 10 miliseconds
        SELECT INTO _threshold_implausible_3G_ping_ns "value"::bigint FROM settings WHERE key='threshold_implausible_3G_ping_ns';
        IF (_threshold_implausible_3G_ping_ns IS NULL) THEN
        _threshold_implausible_3G_ping_ns := 10000000;
        END IF;

        -- check download speed
        IF (NEW.speed_download IS NOT NULL AND NEW.speed_download > _threshold_implausible_3G_download_kbps) THEN
          NEW.implausible = TRUE;
          NEW.comment = CONCAT( NEW.comment, 'Excluded because of implausible download speed. ');
        END IF;

        -- check upload speed
        IF (NEW.speed_upload IS NOT NULL AND NEW.speed_upload > _threshold_implausible_3G_upload_kbps) THEN
          NEW.implausible = TRUE;
          NEW.comment = CONCAT( NEW.comment, 'Excluded because of implausible upload speed. ');
        END IF;

        -- check ping
        IF (NEW.ping_shortest IS NOT NULL AND NEW.ping_shortest < _threshold_implausible_3G_ping_ns) THEN
          NEW.implausible = TRUE;
          NEW.comment = CONCAT( NEW.comment, 'Excluded because of implausible ping. ');
        END IF;

      END IF;--3G


      -- 4G mobile technology
      IF (NEW.network_type = 13) THEN

        -- 4G download in kbps, default 0.6 Gbit/s
        SELECT INTO _threshold_implausible_4G_download_kbps "value"::integer FROM settings WHERE key='threshold_implausible_4G_download_kbps';
        IF (_threshold_implausible_4G_download_kbps IS NULL) THEN
        _threshold_implausible_4G_download_kbps := 600000;
        END IF;

        -- 4G upload in kbps, default 0.4 Gbit/s
        SELECT INTO _threshold_implausible_4G_upload_kbps "value"::integer FROM settings WHERE key='threshold_implausible_4G_upload_kbps';
        IF (_threshold_implausible_4G_upload_kbps IS NULL) THEN
        _threshold_implausible_4G_upload_kbps := 400000;
        END IF;

        -- 4G ping in nanoseconds, default 5 milisecond
        SELECT INTO _threshold_implausible_4G_ping_ns "value"::bigint FROM settings WHERE key='threshold_implausible_4G_ping_ns';
        IF (_threshold_implausible_4G_ping_ns IS NULL) THEN
        _threshold_implausible_4G_ping_ns := 5000000;
        END IF;

        -- check download speed
        IF (NEW.speed_download IS NOT NULL AND NEW.speed_download > _threshold_implausible_4G_download_kbps) THEN
          NEW.implausible = TRUE;
          NEW.comment = CONCAT( NEW.comment, 'Excluded because of implausible download speed. ');
        END IF;

        -- check upload speed
        IF (NEW.speed_upload IS NOT NULL AND NEW.speed_upload > _threshold_implausible_4G_upload_kbps) THEN
          NEW.implausible = TRUE;
          NEW.comment = CONCAT( NEW.comment, 'Excluded because of implausible upload speed. ');
        END IF;

        -- check ping
        IF (NEW.ping_shortest IS NOT NULL AND NEW.ping_shortest < _threshold_implausible_4G_ping_ns) THEN
          NEW.implausible = TRUE;
          NEW.comment = CONCAT( NEW.comment, 'Excluded because of implausible ping. ');
        END IF;

      END IF;--4G

    END IF;
-- end of test of plausibility

    IF ((TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED')
      AND (NEW.time > (now() - INTERVAL '5 minutes')) 
      AND NEW.model='SM-N9005' 
      AND NEW.geo_provider='network') THEN
         NEW.geo_accuracy = 99999;
    END IF;
 
    IF ((TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED' )  
      AND (NEW.time > (now() - INTERVAL '5 minutes')) 
      AND NEW.geo_accuracy is not null
      AND NEW.geo_accuracy <= 10000 ) THEN
      
     SELECT INTO _tmp_uid uid FROM test
        WHERE client_id=NEW.client_id
        AND (NEW.time - INTERVAL '24 hours' < time)
        AND geo_accuracy is not null 
        AND geo_accuracy <= 10000
        ORDER BY uid DESC LIMIT 1;

      IF _tmp_uid is not null THEN
        SELECT INTO NEW.dist_prev ST_Distance(t.location,NEW.location) 
        FROM test t WHERE uid=_tmp_uid;
        IF NEW.dist_prev is not null THEN
            SELECT INTO _tmp_time time FROM test t
            WHERE uid=_tmp_uid;
            NEW.speed_prev = NEW.dist_prev/EXTRACT(EPOCH FROM (NEW.time - _tmp_time));
        END IF;
      END IF;
    END IF;

    IF ((NEW.network_type > 0) AND (NEW.time > (now() - INTERVAL '5 minutes'))) THEN
       SELECT INTO NEW.network_group_name group_name FROM network_type 
          WHERE uid = NEW.network_type
          LIMIT 1;
       SELECT INTO NEW.network_group_type type FROM network_type 
          WHERE uid = NEW.network_type
          LIMIT 1;
    END IF;

    IF (TG_OP = 'INSERT' AND NEW.open_uuid IS NULL) THEN
        SELECT INTO _tmp_uuid open_uuid FROM test
        WHERE client_id=NEW.client_id
        AND (now() - INTERVAL '4 hours' < time)
        AND (now()::date = time::date)
        ORDER BY uid DESC LIMIT 1;
        IF (_tmp_uuid IS NULL) THEN
            _tmp_uuid = uuid_generate_v4();
        END IF;
        NEW.open_uuid = _tmp_uuid;
    END IF;

     IF (TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED') THEN
       SELECT INTO _tmp_network_group_name network_group_name FROM test
          WHERE OLD.open_uuid = open_uuid AND
          OLD.uid != uid AND
          status = 'FINISHED' 
          ORDER BY uid DESC LIMIT 1;
       IF (_tmp_network_group_name IS NOT NULL AND
          ((_tmp_network_group_name = 'WLAN' AND NEW.network_group_name != 'WLAN') OR  
          (_tmp_network_group_name != 'WLAN' AND NEW.network_group_name = 'WLAN'))) THEN
             NEW.open_uuid=uuid_generate_v4();
        END IF;
         
    END IF;

  


    IF (TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED') THEN
        NEW.timestamp = now();
        
        SELECT INTO NEW.location_max_distance
          round(|/((xmax(st_extent(location))-xmin(st_extent(location)))^2+(ymax(st_extent(location))-ymin(st_extent(location)))^2))
          FROM geo_location
          WHERE test_id=NEW.uid;
    END IF;

    IF ((NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
       AND ( 
           (NEW.network_operator ILIKE '293%') -- test with si mobile network operator
           )
       AND ST_Distance(
             ST_Transform (NEW.location, 4326), -- location of the test
             ST_Transform ((select the_geom from ne_50m_admin_0_countries where iso_a2 = 'SI'),4326)::geography -- Austria shape
           ) > 35000 -- location is more than 35 km outside of si
    ) -- if
    THEN NEW.status='UPDATE ERROR'; NEW.comment='Automatic update error due to invalid location';
    END IF;

    IF ((NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
       AND (NEW.model='unknown') -- model is 'unknown'
       ) 
    THEN NEW.status='UPDATE ERROR'; NEW.comment='Automatic update error due to unknown model';
    END IF;

    RETURN NEW;

END;$$;


ALTER FUNCTION public.trigger_test() OWNER TO rmbt;

--
-- Name: unite_garray(geometry[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION unite_garray(geometry[]) RETURNS geometry
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'pgis_union_geometry_array';


ALTER FUNCTION public.unite_garray(geometry[]) OWNER TO postgres;

--
-- Name: within(geometry, geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION within(geometry, geometry) RETURNS boolean
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_Within($1, $2)$_$;


ALTER FUNCTION public.within(geometry, geometry) OWNER TO postgres;

--
-- Name: x(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION x(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_x_point';


ALTER FUNCTION public.x(geometry) OWNER TO postgres;

--
-- Name: xmax(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xmax(box3d) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_xmax';


ALTER FUNCTION public.xmax(box3d) OWNER TO postgres;

--
-- Name: xmin(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION xmin(box3d) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_xmin';


ALTER FUNCTION public.xmin(box3d) OWNER TO postgres;

--
-- Name: y(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION y(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_y_point';


ALTER FUNCTION public.y(geometry) OWNER TO postgres;

--
-- Name: ymax(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ymax(box3d) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_ymax';


ALTER FUNCTION public.ymax(box3d) OWNER TO postgres;

--
-- Name: ymin(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ymin(box3d) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_ymin';


ALTER FUNCTION public.ymin(box3d) OWNER TO postgres;

--
-- Name: z(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION z(geometry) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_z_point';


ALTER FUNCTION public.z(geometry) OWNER TO postgres;

--
-- Name: zmax(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION zmax(box3d) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_zmax';


ALTER FUNCTION public.zmax(box3d) OWNER TO postgres;

--
-- Name: zmflag(geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION zmflag(geometry) RETURNS smallint
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'LWGEOM_zmflag';


ALTER FUNCTION public.zmflag(geometry) OWNER TO postgres;

--
-- Name: zmin(box3d); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION zmin(box3d) RETURNS double precision
    LANGUAGE c IMMUTABLE STRICT
    AS '$libdir/postgis-2.1', 'BOX3D_zmin';


ALTER FUNCTION public.zmin(box3d) OWNER TO postgres;

--
-- Name: accum(geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE accum(geometry) (
    SFUNC = pgis_geometry_accum_transfn,
    STYPE = pgis_abs,
    FINALFUNC = pgis_geometry_accum_finalfn
);


ALTER AGGREGATE public.accum(geometry) OWNER TO postgres;

--
-- Name: extent(geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE extent(geometry) (
    SFUNC = public.st_combine_bbox,
    STYPE = box3d,
    FINALFUNC = public.box2d
);


ALTER AGGREGATE public.extent(geometry) OWNER TO postgres;

--
-- Name: extent3d(geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE extent3d(geometry) (
    SFUNC = public.combine_bbox,
    STYPE = box3d
);


ALTER AGGREGATE public.extent3d(geometry) OWNER TO postgres;

--
-- Name: makeline(geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE makeline(geometry) (
    SFUNC = pgis_geometry_accum_transfn,
    STYPE = pgis_abs,
    FINALFUNC = pgis_geometry_makeline_finalfn
);


ALTER AGGREGATE public.makeline(geometry) OWNER TO postgres;

--
-- Name: median(anyelement); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE median(anyelement) (
    SFUNC = array_append,
    STYPE = anyarray,
    INITCOND = '{}',
    FINALFUNC = _final_median
);


ALTER AGGREGATE public.median(anyelement) OWNER TO postgres;

--
-- Name: memcollect(geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE memcollect(geometry) (
    SFUNC = public.st_collect,
    STYPE = geometry
);


ALTER AGGREGATE public.memcollect(geometry) OWNER TO postgres;

--
-- Name: memgeomunion(geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE memgeomunion(geometry) (
    SFUNC = geomunion,
    STYPE = geometry
);


ALTER AGGREGATE public.memgeomunion(geometry) OWNER TO postgres;

--
-- Name: st_extent3d(geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE st_extent3d(geometry) (
    SFUNC = public.st_combine_bbox,
    STYPE = box3d
);


ALTER AGGREGATE public.st_extent3d(geometry) OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: advertised_speed_option; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE advertised_speed_option (
    uid integer NOT NULL,
    name character varying(200) NOT NULL,
    min_speed_down_kbps integer,
    max_speed_down_kbps integer,
    min_speed_up_kbps integer,
    max_speed_up_kbps integer,
    enabled boolean DEFAULT true
);


ALTER TABLE advertised_speed_option OWNER TO rmbt;

--
-- Name: advertised_speed_option_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE advertised_speed_option_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE advertised_speed_option_uid_seq OWNER TO rmbt;

--
-- Name: advertised_speed_option_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE advertised_speed_option_uid_seq OWNED BY advertised_speed_option.uid;


--
-- Name: device_map; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE device_map (
    uid integer NOT NULL,
    codename character varying(200),
    fullname character varying(200),
    source character varying(200),
    "timestamp" timestamp with time zone
);


ALTER TABLE device_map OWNER TO rmbt;

--
-- Name: android_device_map_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE android_device_map_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE android_device_map_uid_seq OWNER TO rmbt;

--
-- Name: android_device_map_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE android_device_map_uid_seq OWNED BY device_map.uid;


--
-- Name: as2provider; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE as2provider (
    uid integer NOT NULL,
    asn bigint,
    dns_part character varying(200),
    provider_id integer
);


ALTER TABLE as2provider OWNER TO rmbt;

--
-- Name: as2provider_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE as2provider_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE as2provider_uid_seq OWNER TO rmbt;

--
-- Name: as2provider_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE as2provider_uid_seq OWNED BY as2provider.uid;


--
-- Name: asn2country; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE asn2country (
    uid integer NOT NULL,
    asn bigint NOT NULL,
    country character(2) NOT NULL
);


ALTER TABLE asn2country OWNER TO rmbt;

--
-- Name: asn2country_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE asn2country_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE asn2country_uid_seq OWNER TO rmbt;

--
-- Name: asn2country_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE asn2country_uid_seq OWNED BY asn2country.uid;


--
-- Name: base_stations; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE base_stations (
    uid integer NOT NULL,
    technology character varying,
    location_name character varying,
    longitude double precision,
    latitude double precision,
    mnc integer,
    ci integer,
    lac integer,
    enb integer,
    physical_cell_id integer,
    eci integer,
    tac integer,
    rf_band character varying
);


ALTER TABLE base_stations OWNER TO rmbt;

--
-- Name: base_stations_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE base_stations_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE base_stations_uid_seq OWNER TO rmbt;

--
-- Name: base_stations_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE base_stations_uid_seq OWNED BY base_stations.uid;


--
-- Name: cell_location; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE cell_location (
    uid bigint NOT NULL,
    test_id bigint,
    location_id integer,
    area_code integer,
    "time" timestamp with time zone,
    primary_scrambling_code integer,
    time_ns bigint
);


ALTER TABLE cell_location OWNER TO rmbt;

--
-- Name: cell; Type: VIEW; Schema: public; Owner: rmbt
--

CREATE VIEW cell AS
 SELECT DISTINCT cell_location.test_id,
    cell_location.location_id,
    cell_location.area_code
   FROM cell_location
  ORDER BY cell_location.test_id DESC;


ALTER TABLE cell OWNER TO rmbt;

--
-- Name: geo_location; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE geo_location (
    uid bigint NOT NULL,
    test_id bigint NOT NULL,
    "time" timestamp with time zone,
    accuracy double precision,
    altitude double precision,
    bearing double precision,
    speed double precision,
    provider character varying(200),
    geo_lat double precision,
    geo_long double precision,
    location geometry,
    time_ns bigint,
    CONSTRAINT enforce_dims_location CHECK ((st_ndims(location) = 2)),
    CONSTRAINT enforce_geotype_location CHECK (((geometrytype(location) = 'POINT'::text) OR (location IS NULL))),
    CONSTRAINT enforce_srid_location CHECK ((st_srid(location) = 900913))
);


ALTER TABLE geo_location OWNER TO rmbt;

--
-- Name: test; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE test (
    uid bigint NOT NULL,
    uuid uuid,
    client_id bigint,
    client_version character varying(10),
    client_name character varying,
    client_language character varying(10),
    client_local_ip character varying(100),
    token character varying(500),
    server_id integer,
    port integer,
    use_ssl boolean DEFAULT false NOT NULL,
    "time" timestamp with time zone,
    speed_upload integer,
    speed_download integer,
    ping_shortest bigint,
    encryption character varying(50),
    client_public_ip character varying(100),
    plattform character varying(200),
    os_version character varying(100),
    api_level character varying(10),
    device character varying(200),
    model character varying(200),
    product character varying(200),
    phone_type integer,
    data_state integer,
    network_country character varying(10),
    network_operator character varying(10),
    network_operator_name character varying(200),
    network_sim_country character varying(10),
    network_sim_operator character varying(10),
    network_sim_operator_name character varying(200),
    wifi_ssid character varying(200),
    wifi_bssid character varying(200),
    wifi_network_id character varying(200),
    duration integer,
    num_threads integer,
    status character varying(100),
    timezone character varying(200),
    bytes_download bigint,
    bytes_upload bigint,
    nsec_download bigint,
    nsec_upload bigint,
    server_ip character varying(100),
    client_software_version character varying(100),
    geo_lat double precision,
    geo_long double precision,
    network_type integer,
    location geometry,
    signal_strength integer,
    software_revision character varying(200),
    client_test_counter bigint,
    nat_type character varying(200),
    client_previous_test_status character varying(200),
    public_ip_asn bigint,
    speed_upload_log double precision,
    speed_download_log double precision,
    total_bytes_download bigint,
    total_bytes_upload bigint,
    wifi_link_speed integer,
    public_ip_rdns character varying(200),
    public_ip_as_name character varying(200),
    test_slot integer,
    provider_id integer,
    network_is_roaming boolean,
    ping_shortest_log double precision,
    run_ndt boolean,
    num_threads_requested integer,
    client_public_ip_anonymized character varying(100),
    zip_code integer,
    geo_provider character varying(200),
    geo_accuracy double precision,
    deleted boolean DEFAULT false NOT NULL,
    comment text,
    open_uuid uuid,
    client_time timestamp with time zone,
    zip_code_geo integer,
    mobile_provider_id integer,
    roaming_type integer,
    open_test_uuid uuid,
    country_asn character(2),
    country_location character(2),
    test_if_bytes_download bigint,
    test_if_bytes_upload bigint,
    implausible boolean DEFAULT false NOT NULL,
    testdl_if_bytes_download bigint,
    testdl_if_bytes_upload bigint,
    testul_if_bytes_download bigint,
    testul_if_bytes_upload bigint,
    country_geoip character(2),
    location_max_distance integer,
    location_max_distance_gps integer,
    network_group_name character varying(200),
    network_group_type character varying(200),
    time_dl_ns bigint,
    time_ul_ns bigint,
    num_threads_ul integer,
    "timestamp" timestamp without time zone DEFAULT now(),
    source_ip character varying(50),
    lte_rsrp integer,
    lte_rsrq integer,
    mobile_network_id integer,
    mobile_sim_id integer,
    dist_prev double precision,
    speed_prev double precision,
    tag character varying(512),
    client_ip_local character varying(50),
    client_ip_local_anonymized character varying(50),
    client_ip_local_type character varying(50),
    ping_median bigint,
    ping_median_log double precision,
    source_ip_anonymized character varying(50),
    hidden_code character varying(8),
    data json,
    real_geo_lat double precision,
    real_geo_long double precision,
    real_location geometry,
    publish_public_data boolean DEFAULT true NOT NULL,
    gkz integer,
    opendata_source character varying,
    adv_spd_option_id integer,
    adv_spd_up_kbit integer,
    adv_spd_down_kbit integer,
    adv_spd_option_name character varying(100),
    additional_report_fields json,
    ping_variance numeric,
    zero_measurement boolean DEFAULT false NOT NULL,
    CONSTRAINT enforce_dims_location CHECK ((st_ndims(location) = 2)),
    CONSTRAINT enforce_geotype_location CHECK (((geometrytype(location) = 'POINT'::text) OR (location IS NULL))),
    CONSTRAINT enforce_geotype_real_location CHECK (((geometrytype(real_location) = 'POINT'::text) OR (real_location IS NULL))),
    CONSTRAINT enforce_srid_location CHECK ((st_srid(location) = 900913)),
    CONSTRAINT enforce_srid_real_location CHECK ((st_srid(real_location) = 900913)),
    CONSTRAINT test_speed_download_noneg CHECK ((speed_download >= 0)),
    CONSTRAINT test_speed_upload_noneg CHECK ((speed_upload >= 0))
);


ALTER TABLE test OWNER TO rmbt;

--
-- Name: COLUMN test.server_id; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN test.server_id IS 'id of test server used';


--
-- Name: cell2earth; Type: VIEW; Schema: public; Owner: rmbt
--

CREATE VIEW cell2earth AS
 SELECT cell_location.location_id,
    cell_location.area_code,
    test.network_operator,
    geo_location.provider,
    test.network_group_name,
    count(*) AS count,
    round(avg(geo_location.accuracy)) AS avg_accuracy,
    round(stddev(geo_location.accuracy)) AS sd_accuracy,
    round(min(geo_location.accuracy)) AS min_accuracy,
    round(max(geo_location.accuracy)) AS max_accuracy,
    avg(geo_location.geo_lat) AS avg_geo_lat,
    stddev(geo_location.geo_lat) AS sd_geo_lat,
    min(geo_location.geo_lat) AS min_geo_lat,
    max(geo_location.geo_lat) AS max_geo_lat,
    avg(geo_location.geo_long) AS avg_geo_long,
    stddev(geo_location.geo_long) AS sd_geo_long,
    min(geo_location.geo_long) AS min_geo_long,
    max(geo_location.geo_long) AS max_geo_long
   FROM cell_location,
    geo_location,
    test
  WHERE ((((cell_location.test_id = geo_location.test_id) AND (cell_location.test_id = test.uid)) AND (geo_location.geo_lat IS NOT NULL)) AND (geo_location.geo_long IS NOT NULL))
  GROUP BY cell_location.location_id, cell_location.area_code, test.network_operator, geo_location.provider, test.network_group_name
  ORDER BY cell_location.location_id, cell_location.area_code, test.network_operator, geo_location.provider, test.network_group_name;


ALTER TABLE cell2earth OWNER TO rmbt;

--
-- Name: VIEW cell2earth; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON VIEW cell2earth IS 'Used in display of cells for Google Earth';


--
-- Name: geo; Type: VIEW; Schema: public; Owner: rmbt
--

CREATE VIEW geo AS
 SELECT DISTINCT geo_location.test_id,
    geo_location.provider,
    (round((geo_location.accuracy / (10.0)::double precision)) * (10)::double precision) AS accuracy_rd,
    round((geo_location.geo_lat)::numeric, 3) AS lat_rd,
    round((geo_location.geo_long)::numeric, 3) AS long_rd
   FROM geo_location
  ORDER BY geo_location.test_id DESC;


ALTER TABLE geo OWNER TO rmbt;

--
-- Name: cell_geo_test; Type: VIEW; Schema: public; Owner: rmbt
--

CREATE VIEW cell_geo_test AS
 SELECT cell.test_id,
    cell.location_id,
    cell.area_code,
    geo.test_id AS test_id2,
    geo.provider,
    geo.accuracy_rd,
    geo.lat_rd,
    geo.long_rd,
    test.uid,
    test.uuid,
    test.client_id,
    test.client_version,
    test.client_name,
    test.client_language,
    test.client_local_ip,
    test.token,
    test.server_id,
    test.port,
    test.use_ssl,
    test."time",
    test.speed_upload,
    test.speed_download,
    test.ping_shortest,
    test.encryption,
    test.client_public_ip,
    test.plattform,
    test.os_version,
    test.api_level,
    test.device,
    test.model,
    test.product,
    test.phone_type,
    test.data_state,
    test.network_country,
    test.network_operator,
    test.network_operator_name,
    test.network_sim_country,
    test.network_sim_operator,
    test.network_sim_operator_name,
    test.wifi_ssid,
    test.wifi_bssid,
    test.wifi_network_id,
    test.duration,
    test.num_threads,
    test.status,
    test.timezone,
    test.bytes_download,
    test.bytes_upload,
    test.nsec_download,
    test.nsec_upload,
    test.server_ip,
    test.client_software_version,
    test.geo_lat,
    test.geo_long,
    test.network_type,
    test.location,
    test.signal_strength,
    test.software_revision,
    test.client_test_counter,
    test.nat_type,
    test.client_previous_test_status,
    test.public_ip_asn,
    test.speed_upload_log,
    test.speed_download_log,
    test.total_bytes_download,
    test.total_bytes_upload,
    test.wifi_link_speed,
    test.public_ip_rdns,
    test.public_ip_as_name,
    test.test_slot,
    test.provider_id,
    test.network_is_roaming,
    test.ping_shortest_log,
    test.run_ndt,
    test.num_threads_requested,
    test.client_public_ip_anonymized,
    test.zip_code,
    test.geo_provider,
    test.geo_accuracy,
    test.deleted,
    test.comment,
    test.open_uuid,
    test.client_time,
    test.zip_code_geo,
    test.mobile_provider_id,
    test.roaming_type,
    test.open_test_uuid,
    test.country_asn,
    test.country_location,
    test.test_if_bytes_download,
    test.test_if_bytes_upload,
    test.implausible,
    test.testdl_if_bytes_download,
    test.testdl_if_bytes_upload,
    test.testul_if_bytes_download,
    test.testul_if_bytes_upload,
    test.country_geoip,
    test.location_max_distance,
    test.location_max_distance_gps,
    test.network_group_name,
    test.network_group_type,
    test.time_dl_ns,
    test.time_ul_ns,
    test.num_threads_ul,
    test."timestamp",
    test.source_ip,
    test.lte_rsrp,
    test.lte_rsrq,
    test.mobile_network_id,
    test.mobile_sim_id
   FROM cell,
    geo,
    test
  WHERE ((cell.test_id = geo.test_id) AND (cell.test_id = test.uid))
  ORDER BY cell.location_id, cell.area_code, cell.test_id DESC, geo.accuracy_rd, geo.lat_rd, geo.long_rd;


ALTER TABLE cell_geo_test OWNER TO rmbt;

--
-- Name: cell_location_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE cell_location_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE cell_location_uid_seq OWNER TO rmbt;

--
-- Name: cell_location_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE cell_location_uid_seq OWNED BY cell_location.uid;


--
-- Name: client; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE client (
    uid bigint NOT NULL,
    uuid uuid NOT NULL,
    client_type_id integer,
    "time" timestamp with time zone,
    sync_group_id integer,
    sync_code character varying(12),
    terms_and_conditions_accepted boolean DEFAULT false NOT NULL,
    sync_code_timestamp timestamp with time zone
);


ALTER TABLE client OWNER TO rmbt;

--
-- Name: client_type; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE client_type (
    uid integer NOT NULL,
    name character varying(200)
);


ALTER TABLE client_type OWNER TO rmbt;

--
-- Name: client_type_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE client_type_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE client_type_uid_seq OWNER TO rmbt;

--
-- Name: client_type_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE client_type_uid_seq OWNED BY client_type.uid;


--
-- Name: client_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE client_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE client_uid_seq OWNER TO rmbt;

--
-- Name: client_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE client_uid_seq OWNED BY client.uid;


--
-- Name: location_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE location_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE location_uid_seq OWNER TO rmbt;

--
-- Name: location_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE location_uid_seq OWNED BY geo_location.uid;


--
-- Name: logged_actions; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE logged_actions (
    schema_name text NOT NULL,
    table_name text NOT NULL,
    user_name text,
    action_tstamp timestamp with time zone DEFAULT now() NOT NULL,
    action text NOT NULL,
    original_data text,
    new_data text,
    query text,
    CONSTRAINT logged_actions_action_check CHECK ((action = ANY (ARRAY['I'::text, 'D'::text, 'U'::text])))
)
WITH (fillfactor='100');


ALTER TABLE logged_actions OWNER TO rmbt;

--
-- Name: mcc2country; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE mcc2country (
    mcc character varying(3) NOT NULL,
    country character varying(2) NOT NULL
);


ALTER TABLE mcc2country OWNER TO rmbt;

--
-- Name: mccmnc2name; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE mccmnc2name (
    uid integer NOT NULL,
    mccmnc character varying(7) NOT NULL,
    valid_from date DEFAULT '0001-01-01'::date,
    valid_to date DEFAULT '9999-12-31'::date,
    country character varying(2),
    name character varying(200) NOT NULL,
    shortname character varying(100),
    use_for_sim boolean DEFAULT true,
    use_for_network boolean DEFAULT true,
    mcc_mnc_network_mapping character varying(10),
    comment character varying(200),
    mapped_uid integer
);


ALTER TABLE mccmnc2name OWNER TO rmbt;

--
-- Name: mccmnc2name_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE mccmnc2name_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE mccmnc2name_uid_seq OWNER TO rmbt;

--
-- Name: mccmnc2name_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE mccmnc2name_uid_seq OWNED BY mccmnc2name.uid;


--
-- Name: mccmnc2provider; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE mccmnc2provider (
    uid integer NOT NULL,
    mcc_mnc_sim character varying(10),
    provider_id integer NOT NULL,
    mcc_mnc_network character varying(10),
    valid_from date,
    valid_to date
);


ALTER TABLE mccmnc2provider OWNER TO rmbt;

--
-- Name: mccmnc2provider_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE mccmnc2provider_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE mccmnc2provider_uid_seq OWNER TO rmbt;

--
-- Name: mccmnc2provider_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE mccmnc2provider_uid_seq OWNED BY mccmnc2provider.uid;


--
-- Name: ne_50m_admin_0_countries; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE ne_50m_admin_0_countries (
    gid integer NOT NULL,
    scalerank smallint,
    featurecla character varying(30),
    labelrank double precision,
    sovereignt character varying(254),
    sov_a3 character varying(254),
    adm0_dif double precision,
    level double precision,
    type character varying(254),
    admin character varying(254),
    adm0_a3 character varying(254),
    geou_dif double precision,
    geounit character varying(254),
    gu_a3 character varying(254),
    su_dif double precision,
    subunit character varying(254),
    su_a3 character varying(254),
    brk_diff double precision,
    name character varying(254),
    name_long character varying(254),
    brk_a3 character varying(254),
    brk_name character varying(254),
    brk_group character varying(254),
    abbrev character varying(254),
    postal character varying(254),
    formal_en character varying(254),
    formal_fr character varying(254),
    note_adm0 character varying(254),
    note_brk character varying(254),
    name_sort character varying(254),
    name_alt character varying(254),
    mapcolor7 double precision,
    mapcolor8 double precision,
    mapcolor9 double precision,
    mapcolor13 double precision,
    pop_est double precision,
    gdp_md_est double precision,
    pop_year double precision,
    lastcensus double precision,
    gdp_year double precision,
    economy character varying(254),
    income_grp character varying(254),
    wikipedia double precision,
    fips_10 character varying(254),
    iso_a2 character varying(254),
    iso_a3 character varying(254),
    iso_n3 character varying(254),
    un_a3 character varying(254),
    wb_a2 character varying(254),
    wb_a3 character varying(254),
    woe_id double precision,
    adm0_a3_is character varying(254),
    adm0_a3_us character varying(254),
    adm0_a3_un double precision,
    adm0_a3_wb double precision,
    continent character varying(254),
    region_un character varying(254),
    subregion character varying(254),
    region_wb character varying(254),
    name_len double precision,
    long_len double precision,
    abbrev_len double precision,
    tiny double precision,
    homepart double precision,
    the_geom geometry,
    CONSTRAINT enforce_dims_the_geom CHECK ((st_ndims(the_geom) = 2)),
    CONSTRAINT enforce_geotype_the_geom CHECK (((geometrytype(the_geom) = 'MULTIPOLYGON'::text) OR (the_geom IS NULL))),
    CONSTRAINT enforce_srid_the_geom CHECK ((st_srid(the_geom) = 900913))
);


ALTER TABLE ne_50m_admin_0_countries OWNER TO rmbt;

--
-- Name: TABLE ne_50m_admin_0_countries; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON TABLE ne_50m_admin_0_countries IS 'shp2pgsql -d -W LATIN1 -c -D -s 4326 -I ne_50m_admin_0_countries.shp  > ne_50m_admin_0_countries.sql
ALTER TABLE ne_50m_admin_0_countries DROP CONSTRAINT enforce_srid_the_geom;
update ne_50m_admin_0_countries set the_geom=(ST_TRANSFORM(the_geom, 900913));
alter table ne_50m_admin_0_countries add CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 900913);';


--
-- Name: ne_50m_admin_0_countries_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE ne_50m_admin_0_countries_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ne_50m_admin_0_countries_gid_seq OWNER TO rmbt;

--
-- Name: ne_50m_admin_0_countries_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE ne_50m_admin_0_countries_gid_seq OWNED BY ne_50m_admin_0_countries.gid;


--
-- Name: network_type; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE network_type (
    uid integer NOT NULL,
    name character varying(200) NOT NULL,
    group_name character varying NOT NULL,
    aggregate character varying[],
    type character varying NOT NULL,
    technology_order integer DEFAULT 0 NOT NULL
);


ALTER TABLE network_type OWNER TO rmbt;

--
-- Name: network_type_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE network_type_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE network_type_uid_seq OWNER TO rmbt;

--
-- Name: network_type_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE network_type_uid_seq OWNED BY network_type.uid;


--
-- Name: news; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE news (
    uid integer NOT NULL,
    "time" timestamp with time zone NOT NULL,
    title_en text,
    title_de text,
    text_en text,
    text_de text,
    active boolean DEFAULT false NOT NULL,
    force boolean DEFAULT false NOT NULL,
    plattform text,
    max_software_version_code integer,
    min_software_version_code integer,
    uuid uuid
);


ALTER TABLE news OWNER TO rmbt;

--
-- Name: news_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE news_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE news_uid_seq OWNER TO rmbt;

--
-- Name: news_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE news_uid_seq OWNED BY news.uid;


--
-- Name: ping; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE ping (
    uid bigint NOT NULL,
    test_id bigint,
    value bigint,
    value_server bigint,
    time_ns bigint
);


ALTER TABLE ping OWNER TO rmbt;

--
-- Name: ping_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE ping_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ping_uid_seq OWNER TO rmbt;

--
-- Name: ping_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE ping_uid_seq OWNED BY ping.uid;


--
-- Name: plz2001; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE plz2001 (
    gid integer NOT NULL,
    objectid integer,
    plz_4 integer,
    "fläche" numeric,
    plz_3 integer,
    shape_leng numeric,
    shape_area numeric,
    the_geom geometry,
    CONSTRAINT enforce_dims_the_geom CHECK ((st_ndims(the_geom) = 2)),
    CONSTRAINT enforce_geotype_the_geom CHECK (((geometrytype(the_geom) = 'MULTIPOLYGON'::text) OR (the_geom IS NULL))),
    CONSTRAINT enforce_srid_the_geom CHECK ((st_srid(the_geom) = 900913))
);


ALTER TABLE plz2001 OWNER TO rmbt;

--
-- Name: TABLE plz2001; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON TABLE plz2001 IS 'shp2pgsql -d -W LATIN1 -c -D -s 97064 -I PLZ2001.shp
update plz2001 set the_geom=(ST_TRANSFORM(the_geom, 900913));';


--
-- Name: plz2001_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE plz2001_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE plz2001_gid_seq OWNER TO rmbt;

--
-- Name: plz2001_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE plz2001_gid_seq OWNED BY plz2001.gid;


--
-- Name: provider; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE provider (
    uid integer NOT NULL,
    name character varying(200),
    mcc_mnc character varying(10),
    shortname character varying(100),
    map_filter boolean NOT NULL
);


ALTER TABLE provider OWNER TO rmbt;

--
-- Name: provider_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE provider_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE provider_uid_seq OWNER TO rmbt;

--
-- Name: provider_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE provider_uid_seq OWNED BY provider.uid;


--
-- Name: qos_test_desc; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE qos_test_desc (
    uid integer NOT NULL,
    desc_key text,
    value text,
    lang text
);


ALTER TABLE qos_test_desc OWNER TO rmbt;

--
-- Name: qos_test_desc_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE qos_test_desc_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qos_test_desc_uid_seq OWNER TO rmbt;

--
-- Name: qos_test_desc_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE qos_test_desc_uid_seq OWNED BY qos_test_desc.uid;


--
-- Name: qos_test_objective; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE qos_test_objective (
    uid integer NOT NULL,
    test qostest NOT NULL,
    test_class integer,
    test_server integer,
    concurrency_group integer DEFAULT 0 NOT NULL,
    test_desc text,
    test_summary text,
    param json DEFAULT '{}'::json NOT NULL,
    results json
);


ALTER TABLE qos_test_objective OWNER TO rmbt;

--
-- Name: qos_test_objective_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE qos_test_objective_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qos_test_objective_uid_seq OWNER TO rmbt;

--
-- Name: qos_test_objective_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE qos_test_objective_uid_seq OWNED BY qos_test_objective.uid;


--
-- Name: qos_test_result; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE qos_test_result (
    uid integer NOT NULL,
    test_uid bigint,
    qos_test_uid bigint,
    success_count integer DEFAULT 0 NOT NULL,
    failure_count integer DEFAULT 0 NOT NULL,
    implausible boolean DEFAULT false,
    deleted boolean DEFAULT false,
    result json DEFAULT '{}'::json NOT NULL
);


ALTER TABLE qos_test_result OWNER TO rmbt;

--
-- Name: qos_test_result_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE qos_test_result_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qos_test_result_uid_seq OWNER TO rmbt;

--
-- Name: qos_test_result_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE qos_test_result_uid_seq OWNED BY qos_test_result.uid;


--
-- Name: qos_test_type_desc; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE qos_test_type_desc (
    uid integer NOT NULL,
    test qostest,
    test_desc text,
    test_name text
);


ALTER TABLE qos_test_type_desc OWNER TO rmbt;

--
-- Name: qos_test_type_desc_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE qos_test_type_desc_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE qos_test_type_desc_uid_seq OWNER TO rmbt;

--
-- Name: qos_test_type_desc_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE qos_test_type_desc_uid_seq OWNED BY qos_test_type_desc.uid;


--
-- Name: settings; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE settings (
    uid integer NOT NULL,
    key character varying NOT NULL,
    lang character(2),
    value character varying NOT NULL
);


ALTER TABLE settings OWNER TO rmbt;

--
-- Name: settings_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE settings_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE settings_uid_seq OWNER TO rmbt;

--
-- Name: settings_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE settings_uid_seq OWNED BY settings.uid;


--
-- Name: si_municipality; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE si_municipality (
    gid integer NOT NULL,
    ob_mid double precision,
    ob_id integer,
    ob_ime character varying(30),
    ob_uime character varying(50),
    ob_dj character varying(50),
    ob_tip character varying(1),
    d_od date,
    povrsina double precision,
    y_c double precision,
    x_c double precision,
    geom geometry(MultiPolygon,900913)
);


ALTER TABLE si_municipality OWNER TO rmbt;

--
-- Name: si_municipality_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE si_municipality_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE si_municipality_gid_seq OWNER TO rmbt;

--
-- Name: si_municipality_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE si_municipality_gid_seq OWNED BY si_municipality.gid;


--
-- Name: si_regions; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE si_regions (
    gid integer NOT NULL,
    id character varying(2),
    ime character varying(21),
    preb_s numeric,
    preb_m numeric,
    preb_z numeric,
    s_14 numeric,
    m_14 numeric,
    z_14 numeric,
    s_15_64 numeric,
    m_15_64 numeric,
    z_15_64 numeric,
    s_65 numeric,
    m_65 numeric,
    z_65 numeric,
    del_s_14 numeric,
    del_m_14 numeric,
    del_z_14 numeric,
    del_s15_64 numeric,
    del_m15_64 numeric,
    del_z15_64 numeric,
    del_s_65 numeric,
    del_m_65 numeric,
    del_z_65 numeric,
    ind_fem numeric,
    star_s numeric,
    star_m numeric,
    star_z numeric,
    ind_star_s numeric,
    ind_star_m numeric,
    ind_star_z numeric,
    gost_preb numeric,
    del_tujci numeric,
    del_exyu numeric,
    del_eu numeric,
    del_ostalo numeric,
    s0_4 numeric,
    m_0_4 numeric,
    z_0_4 numeric,
    s5_9 numeric,
    m_5_9 numeric,
    z_5_9 numeric,
    s10_14 numeric,
    m_10_14 numeric,
    z_10_14 numeric,
    s15_19 numeric,
    m_15_19 numeric,
    z_15_19 numeric,
    s20_24 numeric,
    m_20_24 numeric,
    z_20_24 numeric,
    s25_29 numeric,
    m_25_29 numeric,
    z_25_29 numeric,
    s30_34 numeric,
    m_30_34 numeric,
    z_30_34 numeric,
    s35_39 numeric,
    m_35_39 numeric,
    z_35_39 numeric,
    s40_44 numeric,
    m_40_44 numeric,
    z_40_44 numeric,
    s45_49 numeric,
    m_45_49 numeric,
    z_45_49 numeric,
    s50_54 numeric,
    m_50_54 numeric,
    z_50_54 numeric,
    s55_59 numeric,
    m_55_59 numeric,
    z_55_59 numeric,
    s60_64 numeric,
    m_60_64 numeric,
    z_60_64 numeric,
    s65_69 numeric,
    m_65_69 numeric,
    z_65_69 numeric,
    s70_74 numeric,
    m_70_74 numeric,
    z_70_74 numeric,
    s75_79 numeric,
    m_75_79 numeric,
    z_75_79 numeric,
    s80_84 numeric,
    m_80_84 numeric,
    z_80_84 numeric,
    s85_89 numeric,
    m_85_89 numeric,
    z_85_89 numeric,
    s90_94 numeric,
    m_90_94 numeric,
    z_90_99 numeric,
    s95_99 numeric,
    m_95_99 numeric,
    z_95_99 numeric,
    s100 numeric,
    m_100 numeric,
    z_100 numeric,
    geom geometry(Polygon,900913)
);


ALTER TABLE si_regions OWNER TO rmbt;

--
-- Name: si_regions_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE si_regions_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE si_regions_gid_seq OWNER TO rmbt;

--
-- Name: si_regions_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE si_regions_gid_seq OWNED BY si_regions.gid;


--
-- Name: si_settlements; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE si_settlements (
    gid integer NOT NULL,
    ob_id integer,
    ob_ime character varying(30),
    ob_uime character varying(50),
    ob_dj character varying(50),
    na_mid double precision,
    na_id integer,
    na_ime character varying(30),
    na_uime character varying(50),
    na_dj character varying(50),
    d_od_g date,
    na_pov double precision,
    y_c double precision,
    x_c double precision,
    geom geometry(MultiPolygon,900913)
);


ALTER TABLE si_settlements OWNER TO rmbt;

--
-- Name: si_settlements_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE si_settlements_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE si_settlements_gid_seq OWNER TO rmbt;

--
-- Name: si_settlements_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE si_settlements_gid_seq OWNED BY si_settlements.gid;


--
-- Name: si_whitespaces; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE si_whitespaces (
    gid integer NOT NULL,
    ob_id integer,
    ob_ime character varying(30),
    ob_uime character varying(50),
    ob_dj character varying(50),
    na_mid double precision,
    na_id integer,
    na_ime character varying(30),
    na_uime character varying(50),
    na_dj character varying(50),
    d_od_g date,
    na_pov numeric,
    y_c double precision,
    x_c double precision,
    geom geometry(MultiPolygon,900913)
);


ALTER TABLE si_whitespaces OWNER TO rmbt;

--
-- Name: si_whitespaces_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE si_whitespaces_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE si_whitespaces_gid_seq OWNER TO rmbt;

--
-- Name: si_whitespaces_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE si_whitespaces_gid_seq OWNED BY si_whitespaces.gid;


--
-- Name: signal; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE signal (
    uid bigint NOT NULL,
    test_id bigint,
    "time" timestamp with time zone,
    signal_strength integer,
    network_type_id integer,
    wifi_link_speed integer,
    gsm_bit_error_rate integer,
    wifi_rssi integer,
    time_ns bigint,
    lte_rsrp integer,
    lte_rsrq integer,
    lte_rssnr integer,
    lte_cqi integer
);


ALTER TABLE signal OWNER TO rmbt;

--
-- Name: signal_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE signal_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE signal_uid_seq OWNER TO rmbt;

--
-- Name: signal_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE signal_uid_seq OWNED BY signal.uid;


--
-- Name: status; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE status (
    uid integer NOT NULL,
    client_uuid uuid NOT NULL,
    "time" timestamp with time zone,
    plattform character varying(50),
    model character varying(50),
    product character varying(50),
    device character varying(50),
    software_version_code character varying(50),
    api_level character varying(10),
    ip character varying(50),
    age bigint,
    lat double precision,
    long double precision,
    accuracy double precision,
    altitude double precision,
    speed double precision,
    provider character varying(50)
);


ALTER TABLE status OWNER TO rmbt;

--
-- Name: status_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE status_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE status_uid_seq OWNER TO rmbt;

--
-- Name: status_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE status_uid_seq OWNED BY status.uid;


--
-- Name: sync_group; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE sync_group (
    uid integer NOT NULL,
    tstamp timestamp with time zone NOT NULL
);


ALTER TABLE sync_group OWNER TO rmbt;

--
-- Name: sync_group_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE sync_group_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE sync_group_uid_seq OWNER TO rmbt;

--
-- Name: sync_group_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE sync_group_uid_seq OWNED BY sync_group.uid;


--
-- Name: test_ndt; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE test_ndt (
    uid integer NOT NULL,
    test_id bigint,
    s2cspd double precision,
    c2sspd double precision,
    avgrtt double precision,
    main text,
    stat text,
    diag text,
    time_ns bigint,
    time_end_ns bigint
);


ALTER TABLE test_ndt OWNER TO rmbt;

--
-- Name: test_ndt_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE test_ndt_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test_ndt_uid_seq OWNER TO rmbt;

--
-- Name: test_ndt_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE test_ndt_uid_seq OWNED BY test_ndt.uid;


--
-- Name: test_server; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE test_server (
    uid integer NOT NULL,
    name character varying(200),
    web_address character varying(500),
    port integer,
    port_ssl integer,
    city character varying,
    country character varying,
    geo_lat double precision,
    geo_long double precision,
    location geometry(Point,900913),
    web_address_ipv4 character varying(200),
    web_address_ipv6 character varying(200),
    server_type character varying(10),
    priority integer DEFAULT 0 NOT NULL,
    weight integer DEFAULT 1 NOT NULL,
    active boolean DEFAULT true NOT NULL,
    uuid uuid DEFAULT uuid_generate_v4() NOT NULL,
    secret_key varchar(100),
    CONSTRAINT enforce_dims_location CHECK ((st_ndims(location) = 2)),
    CONSTRAINT enforce_geotype_location CHECK (((geometrytype(location) = 'POINT'::text) OR (location IS NULL))),
    CONSTRAINT enforce_srid_location CHECK ((st_srid(location) = 900913))
);


ALTER TABLE test_server OWNER TO rmbt;

ALTER TABLE test_server ADD server_group integer;

--
-- Name: test_server_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE test_server_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test_server_uid_seq OWNER TO rmbt;

--
-- Name: test_server_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE test_server_uid_seq OWNED BY test_server.uid;


--
-- Name: test_speed; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE test_speed (
    uid bigint NOT NULL,
    test_id bigint NOT NULL,
    upload boolean NOT NULL,
    thread smallint NOT NULL,
    "time" bigint NOT NULL,
    bytes bigint NOT NULL
);


ALTER TABLE test_speed OWNER TO rmbt;

--
-- Name: COLUMN test_speed.upload; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN test_speed.upload IS 'f=down,t=up';


--
-- Name: test_speed_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE test_speed_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test_speed_uid_seq OWNER TO rmbt;

--
-- Name: test_speed_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE test_speed_uid_seq OWNED BY test_speed.uid;


--
-- Name: test_stat; Type: TABLE; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE TABLE test_stat (
    test_uid bigint NOT NULL,
    cpu_usage json,
    mem_usage json
);


ALTER TABLE test_stat OWNER TO rmbt;

--
-- Name: test_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE test_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE test_uid_seq OWNER TO rmbt;

--
-- Name: test_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE test_uid_seq OWNED BY test.uid;


CREATE SEQUENCE public.test_jpl_seq 
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.test_jpl(
    uid bigint NOT NULL,
    test_uid uuid,
    voip_objective_bits_per_sample bigint,
    voip_result_out_long_seq bigint,
    duration_ns bigint,
    voip_result_in_sequence_error bigint,
    voip_objective_out_port bigint,
    voip_objective_payload bigint,
    voip_objective_call_duration bigint,
    voip_result_out_short_seq bigint,
    voip_objective_sample_rate bigint,
    voip_result_out_mean_jitter bigint,
    voip_result_out_num_packets bigint,
    voip_result_status varchar,
    voip_result_in_skew bigint,
    voip_result_in_max_jitter bigint,
    voip_result_out_sequence_error bigint,
    voip_objective_timeout bigint,
    voip_result_in_num_packets bigint,
    voip_result_in_mean_jitter bigint,
    voip_objective_in_port bigint,
    voip_result_out_max_jitter bigint,
    voip_result_in_max_delta bigint,
    voip_result_in_long_seq bigint,
    voip_result_out_max_delta bigint,
    start_time_ns bigint,
    voip_objective_delay bigint,
    voip_result_in_short_seq bigint,
    voip_result_out_skew bigint,
    voip_result_jitter varchar(20),
    voip_result_packet_loss varchar(20),

    CONSTRAINT test_jpl_pkey PRIMARY KEY (uid)
);

ALTER TABLE ONLY test_jpl ADD CONSTRAINT test_uid_fkey 
FOREIGN KEY (test_uid) REFERENCES test(uuid);

ALTER TABLE ONLY test_jpl ALTER COLUMN uid SET DEFAULT nextval('test_jpl_seq'::regclass);

GRANT SELECT,INSERT,UPDATE ON TABLE test_jpl TO rmbt_control;

GRANT USAGE, SELECT ON SEQUENCE test_jpl_seq TO rmbt_control;


--
-- Name: v_test; Type: VIEW; Schema: public; Owner: rmbt
--

CREATE OR REPLACE VIEW v_test AS
 SELECT test.uid,
    test.uuid,
    test.client_id,
    test.client_version,
    test.client_name,
    test.client_language,
    test.client_local_ip,
    test.token,
    test.server_id,
    test.port,
    test.use_ssl,
    test."time",
    test.speed_upload,
    test.speed_download,
    test.ping_shortest,
    test.encryption,
    test.client_public_ip,
    test.plattform,
    test.os_version,
    test.api_level,
    test.device,
    test.model,
    test.product,
    test.phone_type,
    test.data_state,
    test.network_country,
    test.network_operator,
    test.network_operator_name,
    test.network_sim_country,
    test.network_sim_operator,
    test.network_sim_operator_name,
    test.wifi_ssid,
    test.wifi_bssid,
    test.wifi_network_id,
    test.duration,
    test.num_threads,
    test.status,
    test.timezone,
    test.bytes_download,
    test.bytes_upload,
    test.nsec_download,
    test.nsec_upload,
    test.server_ip,
    test.client_software_version,
    test.geo_lat,
    test.geo_long,
    test.network_type,
    test.location,
    test.signal_strength,
    test.software_revision,
    test.client_test_counter,
    test.nat_type,
    test.client_previous_test_status,
    test.public_ip_asn,
    test.speed_upload_log,
    test.speed_download_log,
    test.total_bytes_download,
    test.total_bytes_upload,
    test.wifi_link_speed,
    test.public_ip_rdns,
    test.public_ip_as_name,
    test.test_slot,
    test.provider_id,
    test.network_is_roaming,
    test.ping_shortest_log,
    test.run_ndt,
    test.num_threads_requested,
    test.client_public_ip_anonymized,
    test.zip_code,
    test.geo_provider,
    test.geo_accuracy,
    test.deleted,
    test.comment,
    test.open_uuid,
    test.client_time,
    test.zip_code_geo,
    test.mobile_provider_id,
    test.roaming_type,
    test.open_test_uuid,
    test.country_asn,
    test.country_location,
    test.test_if_bytes_download,
    test.test_if_bytes_upload,
    test.implausible,
    test.testdl_if_bytes_download,
    test.testdl_if_bytes_upload,
    test.testul_if_bytes_download,
    test.testul_if_bytes_upload,
    test.country_geoip,
    test.location_max_distance,
    test.location_max_distance_gps,
    test.network_group_name,
    test.network_group_type,
    test.time_dl_ns,
    test.time_ul_ns,
    test.num_threads_ul,
    test."timestamp",
    test.source_ip,
    test.lte_rsrp,
    test.lte_rsrq,
    test.mobile_network_id,
    test.mobile_sim_id,
    test.dist_prev,
    test.speed_prev,
    test.tag,
    test.client_ip_local,
    test.client_ip_local_anonymized,
    test.client_ip_local_type,
    test.ping_median,
    test.ping_median_log,
    test.source_ip_anonymized,
    test.hidden_code,
    test.data,
    test.real_geo_lat,
    test.real_geo_long,
    test.real_location,
    test.publish_public_data,
    test.gkz,
    test.opendata_source,
    test.adv_spd_option_id,
    test.adv_spd_up_kbit,
    test.adv_spd_down_kbit,
    test.adv_spd_option_name,
    COALESCE((test.lte_rsrp + 10), test.signal_strength) AS merged_signal,
    test.zero_measurement
   FROM test;


ALTER TABLE v_test OWNER TO rmbt;

--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY advertised_speed_option ALTER COLUMN uid SET DEFAULT nextval('advertised_speed_option_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY as2provider ALTER COLUMN uid SET DEFAULT nextval('as2provider_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY asn2country ALTER COLUMN uid SET DEFAULT nextval('asn2country_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY base_stations ALTER COLUMN uid SET DEFAULT nextval('base_stations_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY cell_location ALTER COLUMN uid SET DEFAULT nextval('cell_location_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY client ALTER COLUMN uid SET DEFAULT nextval('client_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY client_type ALTER COLUMN uid SET DEFAULT nextval('client_type_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY device_map ALTER COLUMN uid SET DEFAULT nextval('android_device_map_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY geo_location ALTER COLUMN uid SET DEFAULT nextval('location_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY mccmnc2name ALTER COLUMN uid SET DEFAULT nextval('mccmnc2name_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY mccmnc2provider ALTER COLUMN uid SET DEFAULT nextval('mccmnc2provider_uid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY ne_50m_admin_0_countries ALTER COLUMN gid SET DEFAULT nextval('ne_50m_admin_0_countries_gid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY network_type ALTER COLUMN uid SET DEFAULT nextval('network_type_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY news ALTER COLUMN uid SET DEFAULT nextval('news_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY ping ALTER COLUMN uid SET DEFAULT nextval('ping_uid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY plz2001 ALTER COLUMN gid SET DEFAULT nextval('plz2001_gid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY provider ALTER COLUMN uid SET DEFAULT nextval('provider_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY qos_test_desc ALTER COLUMN uid SET DEFAULT nextval('qos_test_desc_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY qos_test_objective ALTER COLUMN uid SET DEFAULT nextval('qos_test_objective_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY qos_test_result ALTER COLUMN uid SET DEFAULT nextval('qos_test_result_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY qos_test_type_desc ALTER COLUMN uid SET DEFAULT nextval('qos_test_type_desc_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY settings ALTER COLUMN uid SET DEFAULT nextval('settings_uid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY si_municipality ALTER COLUMN gid SET DEFAULT nextval('si_municipality_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY si_regions ALTER COLUMN gid SET DEFAULT nextval('si_regions_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY si_settlements ALTER COLUMN gid SET DEFAULT nextval('si_settlements_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY si_whitespaces ALTER COLUMN gid SET DEFAULT nextval('si_whitespaces_gid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY signal ALTER COLUMN uid SET DEFAULT nextval('signal_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY status ALTER COLUMN uid SET DEFAULT nextval('status_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY sync_group ALTER COLUMN uid SET DEFAULT nextval('sync_group_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test ALTER COLUMN uid SET DEFAULT nextval('test_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test_ndt ALTER COLUMN uid SET DEFAULT nextval('test_ndt_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test_server ALTER COLUMN uid SET DEFAULT nextval('test_server_uid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test_speed ALTER COLUMN uid SET DEFAULT nextval('test_speed_uid_seq'::regclass);


--
-- Name: advertised_speed_option_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY advertised_speed_option
    ADD CONSTRAINT advertised_speed_option_pkey PRIMARY KEY (uid);


--
-- Name: android_device_map_codename_key; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY device_map
    ADD CONSTRAINT android_device_map_codename_key UNIQUE (codename);


--
-- Name: android_device_map_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY device_map
    ADD CONSTRAINT android_device_map_pkey PRIMARY KEY (uid);


--
-- Name: as2provider_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY as2provider
    ADD CONSTRAINT as2provider_pkey PRIMARY KEY (uid);


--
-- Name: asn2country_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY asn2country
    ADD CONSTRAINT asn2country_pkey PRIMARY KEY (uid);


--
-- Name: base_stations_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY base_stations
    ADD CONSTRAINT base_stations_pkey PRIMARY KEY (uid);


--
-- Name: cell_location_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY cell_location
    ADD CONSTRAINT cell_location_pkey PRIMARY KEY (uid);


--
-- Name: client_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY client
    ADD CONSTRAINT client_pkey PRIMARY KEY (uid);


--
-- Name: client_sync_code; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY client
    ADD CONSTRAINT client_sync_code UNIQUE (sync_code);


--
-- Name: client_type_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY client_type
    ADD CONSTRAINT client_type_pkey PRIMARY KEY (uid);


--
-- Name: client_uuid_key; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY client
    ADD CONSTRAINT client_uuid_key UNIQUE (uuid);


--
-- Name: device_map_fullname_key; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY device_map
    ADD CONSTRAINT device_map_fullname_key UNIQUE (fullname);


--
-- Name: location_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY geo_location
    ADD CONSTRAINT location_pkey PRIMARY KEY (uid);


--
-- Name: mcc2country_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY mcc2country
    ADD CONSTRAINT mcc2country_pkey PRIMARY KEY (mcc);


--
-- Name: mccmnc2name_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY mccmnc2name
    ADD CONSTRAINT mccmnc2name_pkey PRIMARY KEY (uid);


--
-- Name: mccmnc2provider_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY mccmnc2provider
    ADD CONSTRAINT mccmnc2provider_pkey PRIMARY KEY (uid);


--
-- Name: ne_50m_admin_0_countries_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY ne_50m_admin_0_countries
    ADD CONSTRAINT ne_50m_admin_0_countries_pkey PRIMARY KEY (gid);


--
-- Name: network_type_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY network_type
    ADD CONSTRAINT network_type_pkey PRIMARY KEY (uid);


--
-- Name: ping_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY ping
    ADD CONSTRAINT ping_pkey PRIMARY KEY (uid);


--
-- Name: plz2001_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY plz2001
    ADD CONSTRAINT plz2001_pkey PRIMARY KEY (gid);


--
-- Name: provider_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY provider
    ADD CONSTRAINT provider_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_desc_desc_key_lang_key; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY qos_test_desc
    ADD CONSTRAINT qos_test_desc_desc_key_lang_key UNIQUE (desc_key, lang);


--
-- Name: qos_test_desc_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY qos_test_desc
    ADD CONSTRAINT qos_test_desc_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_objective_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY qos_test_objective
    ADD CONSTRAINT qos_test_objective_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_result_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY qos_test_result
    ADD CONSTRAINT qos_test_result_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_type_desc_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY qos_test_type_desc
    ADD CONSTRAINT qos_test_type_desc_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_type_desc_test_key; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY qos_test_type_desc
    ADD CONSTRAINT qos_test_type_desc_test_key UNIQUE (test);


--
-- Name: settings_key_lang_key; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY settings
    ADD CONSTRAINT settings_key_lang_key UNIQUE (key, lang);


--
-- Name: settings_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY settings
    ADD CONSTRAINT settings_pkey PRIMARY KEY (uid);


--
-- Name: si_municipality_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY si_municipality
    ADD CONSTRAINT si_municipality_pkey PRIMARY KEY (gid);


--
-- Name: si_regions_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY si_regions
    ADD CONSTRAINT si_regions_pkey PRIMARY KEY (gid);


--
-- Name: si_settlements_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY si_settlements
    ADD CONSTRAINT si_settlements_pkey PRIMARY KEY (gid);


--
-- Name: si_whitespaces_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY si_whitespaces
    ADD CONSTRAINT si_whitespaces_pkey PRIMARY KEY (gid);


--
-- Name: signal_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY signal
    ADD CONSTRAINT signal_pkey PRIMARY KEY (uid);


--
-- Name: status_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY status
    ADD CONSTRAINT status_pkey PRIMARY KEY (uid);


--
-- Name: sync_group_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY sync_group
    ADD CONSTRAINT sync_group_pkey PRIMARY KEY (uid);


--
-- Name: test_ndt_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY test_ndt
    ADD CONSTRAINT test_ndt_pkey PRIMARY KEY (uid);


--
-- Name: test_ndt_test_id_unique; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY test_ndt
    ADD CONSTRAINT test_ndt_test_id_unique UNIQUE (test_id);


--
-- Name: test_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY test
    ADD CONSTRAINT test_pkey PRIMARY KEY (uid);


--
-- Name: test_server_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY test_server
    ADD CONSTRAINT test_server_pkey PRIMARY KEY (uid);


--
-- Name: test_speed_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY test_speed
    ADD CONSTRAINT test_speed_pkey PRIMARY KEY (uid);


--
-- Name: test_stat_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY test_stat
    ADD CONSTRAINT test_stat_pkey PRIMARY KEY (test_uid);


--
-- Name: test_uuid_key; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY test
    ADD CONSTRAINT test_uuid_key UNIQUE (uuid);


--
-- Name: uid; Type: CONSTRAINT; Schema: public; Owner: rmbt; Tablespace: 
--

ALTER TABLE ONLY news
    ADD CONSTRAINT uid PRIMARY KEY (uid);


--
-- Name: as2provider_provider_id_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX as2provider_provider_id_idx ON as2provider USING btree (provider_id);


--
-- Name: asn2country_asn_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX asn2country_asn_idx ON asn2country USING btree (asn);


--
-- Name: cell_location_test_id_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX cell_location_test_id_idx ON cell_location USING btree (test_id);


--
-- Name: cell_location_test_id_time_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX cell_location_test_id_time_idx ON cell_location USING btree (test_id, "time");


--
-- Name: client_client_type_id_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX client_client_type_id_idx ON client USING btree (client_type_id);


--
-- Name: client_sync_group_id_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX client_sync_group_id_idx ON client USING btree (sync_group_id);


--
-- Name: download_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX download_idx ON test USING btree (bytes_download, network_type);


--
-- Name: fki_qos_test_result_qos_test_uid_fkey; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX fki_qos_test_result_qos_test_uid_fkey ON qos_test_result USING btree (qos_test_uid);


--
-- Name: fki_qos_test_result_test_uid; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX fki_qos_test_result_test_uid ON qos_test_result USING btree (test_uid);


--
-- Name: geo_location_location_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX geo_location_location_idx ON geo_location USING gist (location);


--
-- Name: geo_location_test_id_key; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX geo_location_test_id_key ON geo_location USING btree (test_id);


--
-- Name: geo_location_test_id_provider; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX geo_location_test_id_provider ON geo_location USING btree (test_id, provider);


--
-- Name: geo_location_test_id_provider_time_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX geo_location_test_id_provider_time_idx ON geo_location USING btree (test_id, provider, "time");


--
-- Name: geo_location_test_id_time_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX geo_location_test_id_time_idx ON geo_location USING btree (test_id, "time");


--
-- Name: location_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX location_idx ON test USING gist (location);


--
-- Name: logged_actions_action_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX logged_actions_action_idx ON logged_actions USING btree (action);


--
-- Name: logged_actions_action_tstamp_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX logged_actions_action_tstamp_idx ON logged_actions USING btree (action_tstamp);


--
-- Name: logged_actions_schema_table_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX logged_actions_schema_table_idx ON logged_actions USING btree ((((schema_name || '.'::text) || table_name)));


--
-- Name: mcc2country_mcc; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX mcc2country_mcc ON mcc2country USING btree (mcc);


--
-- Name: mccmnc2name_mccmnc; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX mccmnc2name_mccmnc ON mccmnc2name USING btree (mccmnc);


--
-- Name: mccmnc2provider_mcc_mnc_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX mccmnc2provider_mcc_mnc_idx ON mccmnc2provider USING btree (mcc_mnc_sim, mcc_mnc_network);


--
-- Name: mccmnc2provider_provider_id; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX mccmnc2provider_provider_id ON mccmnc2provider USING btree (provider_id);


--
-- Name: ne_50m_admin_0_countries_iso_a2_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX ne_50m_admin_0_countries_iso_a2_idx ON ne_50m_admin_0_countries USING btree (iso_a2);


--
-- Name: ne_50m_admin_0_countries_the_geom_gist; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX ne_50m_admin_0_countries_the_geom_gist ON ne_50m_admin_0_countries USING gist (the_geom);


--
-- Name: network_type_group_name_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX network_type_group_name_idx ON network_type USING btree (group_name);


--
-- Name: network_type_type_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX network_type_type_idx ON network_type USING btree (type);


--
-- Name: news_time_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX news_time_idx ON news USING btree ("time");


--
-- Name: ping_test_id_key; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX ping_test_id_key ON ping USING btree (test_id);


--
-- Name: plz2001_the_geom_gist; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX plz2001_the_geom_gist ON plz2001 USING gist (the_geom);


--
-- Name: provider_mcc_mnc_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX provider_mcc_mnc_idx ON provider USING btree (mcc_mnc);


--
-- Name: qos_test_desc_desc_key_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX qos_test_desc_desc_key_idx ON qos_test_desc USING btree (desc_key);


--
-- Name: settings_key_lang_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX settings_key_lang_idx ON settings USING btree (key, lang);


--
-- Name: si_municipality_geom_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX si_municipality_geom_idx ON si_municipality USING gist (geom);


--
-- Name: si_regions_geom_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX si_regions_geom_idx ON si_regions USING gist (geom);


--
-- Name: si_settlements_geom_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX si_settlements_geom_idx ON si_settlements USING gist (geom);


--
-- Name: si_whitespaces_geom_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX si_whitespaces_geom_idx ON si_whitespaces USING gist (geom);


--
-- Name: signal_test_id_key; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX signal_test_id_key ON signal USING btree (test_id);


--
-- Name: test_client_id_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_client_id_idx ON test USING btree (client_id);


--
-- Name: test_deleted_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_deleted_idx ON test USING btree (deleted);


--
-- Name: test_device_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_device_idx ON test USING btree (device);


--
-- Name: test_geo_accuracy_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_geo_accuracy_idx ON test USING btree (geo_accuracy);


--
-- Name: test_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_idx ON test USING btree (((network_type <> ALL (ARRAY[0, 99]))));


--
-- Name: test_mobile_network_id_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_mobile_network_id_idx ON test USING btree (mobile_network_id);


--
-- Name: test_mobile_provider_id_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_mobile_provider_id_idx ON test USING btree (mobile_provider_id);


--
-- Name: test_ndt_test_id_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_ndt_test_id_idx ON test_ndt USING btree (test_id);


--
-- Name: test_network_operator_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_network_operator_idx ON test USING btree (network_operator);


--
-- Name: test_network_type_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_network_type_idx ON test USING btree (network_type);


--
-- Name: test_open_test_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_open_test_uuid_idx ON test USING btree (open_test_uuid);


--
-- Name: test_open_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_open_uuid_idx ON test USING btree (open_uuid);


--
-- Name: test_opendata_source; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_opendata_source ON test USING btree (opendata_source);


--
-- Name: test_ping_median_log_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_ping_median_log_idx ON test USING btree (ping_median_log);


--
-- Name: test_ping_shortest_log_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_ping_shortest_log_idx ON test USING btree (ping_shortest_log);


--
-- Name: test_provider_id_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_provider_id_idx ON test USING btree (provider_id);


--
-- Name: test_speed_download_log_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_speed_download_log_idx ON test USING btree (speed_download_log);


--
-- Name: test_speed_test_id_key; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_speed_test_id_key ON test_speed USING btree (test_id);


--
-- Name: test_speed_upload_log_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_speed_upload_log_idx ON test USING btree (speed_upload_log);


--
-- Name: test_status_finished2_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_status_finished2_idx ON test USING btree (((((NOT deleted) AND (NOT implausible)) AND ((status)::text = 'FINISHED'::text))), network_type);


--
-- Name: test_status_finished_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_status_finished_idx ON test USING btree ((((deleted = false) AND ((status)::text = 'FINISHED'::text))), network_type);


--
-- Name: test_status_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_status_idx ON test USING btree (status);


--
-- Name: test_test_slot_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_test_slot_idx ON test USING btree (test_slot);


--
-- Name: test_time_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_time_idx ON test USING btree ("time");


--
-- Name: test_zip_code_idx; Type: INDEX; Schema: public; Owner: rmbt; Tablespace: 
--

CREATE INDEX test_zip_code_idx ON test USING btree (zip_code);


--
-- Name: trigger_test; Type: TRIGGER; Schema: public; Owner: rmbt
--

CREATE TRIGGER trigger_test BEFORE INSERT OR UPDATE ON test FOR EACH ROW EXECUTE PROCEDURE trigger_test();


--
-- Name: as2provider_provider_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY as2provider
    ADD CONSTRAINT as2provider_provider_id_fkey FOREIGN KEY (provider_id) REFERENCES provider(uid);


--
-- Name: cell_location_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY cell_location
    ADD CONSTRAINT cell_location_test_id_fkey FOREIGN KEY (test_id) REFERENCES test(uid) ON DELETE CASCADE;


--
-- Name: client_client_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY client
    ADD CONSTRAINT client_client_type_id_fkey FOREIGN KEY (client_type_id) REFERENCES client_type(uid);


--
-- Name: client_sync_group_id; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY client
    ADD CONSTRAINT client_sync_group_id FOREIGN KEY (sync_group_id) REFERENCES sync_group(uid);


--
-- Name: location_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY geo_location
    ADD CONSTRAINT location_test_id_fkey FOREIGN KEY (test_id) REFERENCES test(uid) ON DELETE CASCADE;


--
-- Name: mccmnc2provider_provider_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY mccmnc2provider
    ADD CONSTRAINT mccmnc2provider_provider_id_fkey FOREIGN KEY (provider_id) REFERENCES provider(uid);


--
-- Name: ping_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY ping
    ADD CONSTRAINT ping_test_id_fkey FOREIGN KEY (test_id) REFERENCES test(uid) ON DELETE CASCADE;


--
-- Name: qos_test_result_qos_test_uid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY qos_test_result
    ADD CONSTRAINT qos_test_result_qos_test_uid_fkey FOREIGN KEY (qos_test_uid) REFERENCES qos_test_objective(uid) ON DELETE CASCADE;


--
-- Name: qos_test_result_test_uid; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY qos_test_result
    ADD CONSTRAINT qos_test_result_test_uid FOREIGN KEY (test_uid) REFERENCES test(uid) ON DELETE CASCADE;


--
-- Name: signal_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY signal
    ADD CONSTRAINT signal_test_id_fkey FOREIGN KEY (test_id) REFERENCES test(uid) ON DELETE CASCADE;


--
-- Name: test_adv_spd_option_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test
    ADD CONSTRAINT test_adv_spd_option_id_fkey FOREIGN KEY (adv_spd_option_id) REFERENCES advertised_speed_option(uid);


--
-- Name: test_client_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test
    ADD CONSTRAINT test_client_id_fkey FOREIGN KEY (client_id) REFERENCES client(uid) ON DELETE CASCADE;


--
-- Name: test_mobile_provider_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test
    ADD CONSTRAINT test_mobile_provider_id_fkey FOREIGN KEY (mobile_provider_id) REFERENCES provider(uid) ON DELETE SET NULL;


--
-- Name: test_ndt_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test_ndt
    ADD CONSTRAINT test_ndt_test_id_fkey FOREIGN KEY (test_id) REFERENCES test(uid) ON DELETE CASCADE;


--
-- Name: test_provider_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test
    ADD CONSTRAINT test_provider_fkey FOREIGN KEY (provider_id) REFERENCES provider(uid) ON DELETE SET NULL;


--
-- Name: test_stat_test_uid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test_stat
    ADD CONSTRAINT test_stat_test_uid_fkey FOREIGN KEY (test_uid) REFERENCES test(uid);


--
-- Name: test_test_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY test
    ADD CONSTRAINT test_test_server_id_fkey FOREIGN KEY (server_id) REFERENCES test_server(uid);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: advertised_speed_option; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE advertised_speed_option FROM PUBLIC;
REVOKE ALL ON TABLE advertised_speed_option FROM rmbt;
GRANT ALL ON TABLE advertised_speed_option TO rmbt;
GRANT SELECT ON TABLE advertised_speed_option TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE advertised_speed_option TO rmbt_web_admin;


--
-- Name: device_map; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE device_map FROM PUBLIC;
REVOKE ALL ON TABLE device_map FROM rmbt;
GRANT ALL ON TABLE device_map TO rmbt;
GRANT SELECT ON TABLE device_map TO rmbt_group_read_only;


--
-- Name: as2provider; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE as2provider FROM PUBLIC;
REVOKE ALL ON TABLE as2provider FROM rmbt;
GRANT ALL ON TABLE as2provider TO rmbt;
GRANT SELECT ON TABLE as2provider TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE as2provider TO rmbt_web_admin;


--
-- Name: as2provider_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE as2provider_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE as2provider_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE as2provider_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE as2provider_uid_seq TO rmbt_web_admin;


--
-- Name: base_stations; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE base_stations FROM PUBLIC;
REVOKE ALL ON TABLE base_stations FROM rmbt;
GRANT ALL ON TABLE base_stations TO rmbt;
GRANT SELECT ON TABLE base_stations TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE base_stations TO rmbt_web_admin;


--
-- Name: base_stations_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE base_stations_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE base_stations_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE base_stations_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE base_stations_uid_seq TO rmbt_web_admin;


--
-- Name: cell_location; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE cell_location FROM PUBLIC;
REVOKE ALL ON TABLE cell_location FROM rmbt;
GRANT ALL ON TABLE cell_location TO rmbt;
GRANT SELECT ON TABLE cell_location TO rmbt_group_read_only;
GRANT INSERT ON TABLE cell_location TO rmbt_group_control;


--
-- Name: geo_location; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE geo_location FROM PUBLIC;
REVOKE ALL ON TABLE geo_location FROM rmbt;
GRANT ALL ON TABLE geo_location TO rmbt;
GRANT SELECT ON TABLE geo_location TO rmbt_group_read_only;
GRANT INSERT ON TABLE geo_location TO rmbt_group_control;


--
-- Name: test; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE test FROM PUBLIC;
REVOKE ALL ON TABLE test FROM rmbt;
GRANT ALL ON TABLE test TO rmbt;
GRANT SELECT ON TABLE test TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE test TO rmbt_group_control;


--
-- Name: cell_location_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE cell_location_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE cell_location_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE cell_location_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE cell_location_uid_seq TO rmbt_group_control;


--
-- Name: client; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE client FROM PUBLIC;
REVOKE ALL ON TABLE client FROM rmbt;
GRANT ALL ON TABLE client TO rmbt;
GRANT SELECT ON TABLE client TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE client TO rmbt_group_control;


--
-- Name: client_type; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE client_type FROM PUBLIC;
REVOKE ALL ON TABLE client_type FROM rmbt;
GRANT ALL ON TABLE client_type TO rmbt;
GRANT SELECT ON TABLE client_type TO rmbt_group_read_only;


--
-- Name: client_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE client_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE client_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE client_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE client_uid_seq TO rmbt_group_control;


--
-- Name: location_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE location_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE location_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE location_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE location_uid_seq TO rmbt_group_control;


--
-- Name: logged_actions; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE logged_actions FROM PUBLIC;
REVOKE ALL ON TABLE logged_actions FROM rmbt;
GRANT ALL ON TABLE logged_actions TO rmbt;
GRANT INSERT ON TABLE logged_actions TO rmbt_group_control;


--
-- Name: mcc2country; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE mcc2country FROM PUBLIC;
REVOKE ALL ON TABLE mcc2country FROM rmbt;
GRANT ALL ON TABLE mcc2country TO rmbt;
GRANT SELECT ON TABLE mcc2country TO rmbt_group_read_only;


--
-- Name: mccmnc2name; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE mccmnc2name FROM PUBLIC;
REVOKE ALL ON TABLE mccmnc2name FROM rmbt;
GRANT ALL ON TABLE mccmnc2name TO rmbt;
GRANT SELECT ON TABLE mccmnc2name TO rmbt_group_read_only;
GRANT SELECT ON TABLE mccmnc2name TO rmbt_group_control;


--
-- Name: mccmnc2provider; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE mccmnc2provider FROM PUBLIC;
REVOKE ALL ON TABLE mccmnc2provider FROM rmbt;
GRANT ALL ON TABLE mccmnc2provider TO rmbt;
GRANT SELECT ON TABLE mccmnc2provider TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE mccmnc2provider TO rmbt_web_admin;


--
-- Name: mccmnc2provider_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE mccmnc2provider_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE mccmnc2provider_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE mccmnc2provider_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE mccmnc2provider_uid_seq TO rmbt_web_admin;


--
-- Name: ne_50m_admin_0_countries; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE ne_50m_admin_0_countries FROM PUBLIC;
REVOKE ALL ON TABLE ne_50m_admin_0_countries FROM rmbt;
GRANT ALL ON TABLE ne_50m_admin_0_countries TO rmbt;
GRANT SELECT ON TABLE ne_50m_admin_0_countries TO rmbt_group_read_only;


--
-- Name: network_type; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE network_type FROM PUBLIC;
REVOKE ALL ON TABLE network_type FROM rmbt;
GRANT ALL ON TABLE network_type TO rmbt;
GRANT SELECT ON TABLE network_type TO rmbt_group_read_only;


--
-- Name: news; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE news FROM PUBLIC;
REVOKE ALL ON TABLE news FROM rmbt;
GRANT ALL ON TABLE news TO rmbt;
GRANT SELECT ON TABLE news TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE news TO rmbt_web_admin;


--
-- Name: news_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE news_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE news_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE news_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE news_uid_seq TO rmbt_web_admin;


--
-- Name: ping; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE ping FROM PUBLIC;
REVOKE ALL ON TABLE ping FROM rmbt;
GRANT ALL ON TABLE ping TO rmbt;
GRANT SELECT ON TABLE ping TO rmbt_group_read_only;
GRANT INSERT ON TABLE ping TO rmbt_group_control;


--
-- Name: ping_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE ping_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE ping_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE ping_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE ping_uid_seq TO rmbt_group_control;


--
-- Name: plz2001; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE plz2001 FROM PUBLIC;
REVOKE ALL ON TABLE plz2001 FROM rmbt;
GRANT ALL ON TABLE plz2001 TO rmbt;
GRANT SELECT ON TABLE plz2001 TO rmbt_group_read_only;


--
-- Name: provider; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE provider FROM PUBLIC;
REVOKE ALL ON TABLE provider FROM rmbt;
GRANT ALL ON TABLE provider TO rmbt;
GRANT SELECT ON TABLE provider TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE provider TO rmbt_web_admin;


--
-- Name: provider_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE provider_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE provider_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE provider_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE provider_uid_seq TO rmbt_web_admin;


--
-- Name: qos_test_desc; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE qos_test_desc FROM PUBLIC;
REVOKE ALL ON TABLE qos_test_desc FROM rmbt;
GRANT ALL ON TABLE qos_test_desc TO rmbt;
GRANT SELECT ON TABLE qos_test_desc TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE qos_test_desc TO rmbt_web_admin;


--
-- Name: qos_test_desc_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE qos_test_desc_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE qos_test_desc_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE qos_test_desc_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE qos_test_desc_uid_seq TO rmbt_web_admin;


--
-- Name: qos_test_objective; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE qos_test_objective FROM PUBLIC;
REVOKE ALL ON TABLE qos_test_objective FROM rmbt;
GRANT ALL ON TABLE qos_test_objective TO rmbt;
GRANT SELECT ON TABLE qos_test_objective TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE qos_test_objective TO rmbt_web_admin;


--
-- Name: qos_test_objective_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE qos_test_objective_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE qos_test_objective_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE qos_test_objective_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE qos_test_objective_uid_seq TO rmbt_web_admin;


--
-- Name: qos_test_result; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE qos_test_result FROM PUBLIC;
REVOKE ALL ON TABLE qos_test_result FROM rmbt;
GRANT ALL ON TABLE qos_test_result TO rmbt;
GRANT SELECT ON TABLE qos_test_result TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE qos_test_result TO rmbt_group_control;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE qos_test_result TO rmbt_web_admin;


--
-- Name: qos_test_result_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE qos_test_result_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE qos_test_result_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE qos_test_result_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE qos_test_result_uid_seq TO rmbt_group_control;
GRANT ALL ON SEQUENCE qos_test_result_uid_seq TO rmbt_web_admin;


--
-- Name: qos_test_type_desc; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE qos_test_type_desc FROM PUBLIC;
REVOKE ALL ON TABLE qos_test_type_desc FROM rmbt;
GRANT ALL ON TABLE qos_test_type_desc TO rmbt;
GRANT SELECT ON TABLE qos_test_type_desc TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE qos_test_type_desc TO rmbt_web_admin;


--
-- Name: qos_test_type_desc_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE qos_test_type_desc_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE qos_test_type_desc_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE qos_test_type_desc_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE qos_test_type_desc_uid_seq TO rmbt_web_admin;


--
-- Name: settings; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE settings FROM PUBLIC;
REVOKE ALL ON TABLE settings FROM rmbt;
GRANT ALL ON TABLE settings TO rmbt;
GRANT SELECT ON TABLE settings TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE settings TO rmbt_web_admin;


--
-- Name: settings_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE settings_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE settings_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE settings_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE settings_uid_seq TO rmbt_web_admin;


--
-- Name: si_municipality; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE si_municipality FROM PUBLIC;
REVOKE ALL ON TABLE si_municipality FROM rmbt;
GRANT ALL ON TABLE si_municipality TO rmbt;
GRANT SELECT ON TABLE si_municipality TO rmbt_group_read_only;


--
-- Name: si_regions; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE si_regions FROM PUBLIC;
REVOKE ALL ON TABLE si_regions FROM rmbt;
GRANT ALL ON TABLE si_regions TO rmbt;
GRANT SELECT ON TABLE si_regions TO rmbt_group_read_only;


--
-- Name: si_settlements; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE si_settlements FROM PUBLIC;
REVOKE ALL ON TABLE si_settlements FROM rmbt;
GRANT ALL ON TABLE si_settlements TO rmbt;
GRANT SELECT ON TABLE si_settlements TO rmbt_group_read_only;


--
-- Name: si_whitespaces; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE si_whitespaces FROM PUBLIC;
REVOKE ALL ON TABLE si_whitespaces FROM rmbt;
GRANT ALL ON TABLE si_whitespaces TO rmbt;
GRANT SELECT ON TABLE si_whitespaces TO rmbt_group_read_only;


--
-- Name: signal; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE signal FROM PUBLIC;
REVOKE ALL ON TABLE signal FROM rmbt;
GRANT ALL ON TABLE signal TO rmbt;
GRANT SELECT ON TABLE signal TO rmbt_group_read_only;
GRANT INSERT ON TABLE signal TO rmbt_group_control;


--
-- Name: signal_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE signal_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE signal_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE signal_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE signal_uid_seq TO rmbt_group_control;


--
-- Name: status; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE status FROM PUBLIC;
REVOKE ALL ON TABLE status FROM rmbt;
GRANT ALL ON TABLE status TO rmbt;
GRANT INSERT,UPDATE ON TABLE status TO rmbt_group_control;
GRANT SELECT ON TABLE status TO rmbt_group_read_only;


--
-- Name: status_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE status_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE status_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE status_uid_seq TO rmbt;
GRANT UPDATE ON SEQUENCE status_uid_seq TO rmbt_group_control;


--
-- Name: sync_group; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE sync_group FROM PUBLIC;
REVOKE ALL ON TABLE sync_group FROM rmbt;
GRANT ALL ON TABLE sync_group TO rmbt;
GRANT SELECT ON TABLE sync_group TO rmbt_group_read_only;
GRANT INSERT,DELETE ON TABLE sync_group TO rmbt_group_control;


--
-- Name: sync_group_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE sync_group_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE sync_group_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE sync_group_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE sync_group_uid_seq TO rmbt_group_control;


--
-- Name: test_ndt; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE test_ndt FROM PUBLIC;
REVOKE ALL ON TABLE test_ndt FROM rmbt;
GRANT ALL ON TABLE test_ndt TO rmbt;
GRANT SELECT ON TABLE test_ndt TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE test_ndt TO rmbt_group_control;


--
-- Name: test_ndt_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE test_ndt_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE test_ndt_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE test_ndt_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE test_ndt_uid_seq TO rmbt_group_control;


--
-- Name: test_server; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE test_server FROM PUBLIC;
REVOKE ALL ON TABLE test_server FROM rmbt;
GRANT ALL ON TABLE test_server TO rmbt;
GRANT SELECT ON TABLE test_server TO rmbt_group_read_only;
GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE test_server TO rmbt_web_admin;


--
-- Name: test_server_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE test_server_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE test_server_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE test_server_uid_seq TO rmbt;
GRANT ALL ON SEQUENCE test_server_uid_seq TO rmbt_web_admin;


--
-- Name: test_speed; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE test_speed FROM PUBLIC;
REVOKE ALL ON TABLE test_speed FROM rmbt;
GRANT ALL ON TABLE test_speed TO rmbt;
GRANT INSERT,UPDATE ON TABLE test_speed TO rmbt_group_control;
GRANT SELECT ON TABLE test_speed TO rmbt_group_read_only;


--
-- Name: test_speed_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE test_speed_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE test_speed_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE test_speed_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE test_speed_uid_seq TO rmbt_group_control;


--
-- Name: test_stat; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE test_stat FROM PUBLIC;
REVOKE ALL ON TABLE test_stat FROM rmbt;
GRANT ALL ON TABLE test_stat TO rmbt;
GRANT SELECT ON TABLE test_stat TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE test_stat TO rmbt_group_control;


--
-- Name: test_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON SEQUENCE test_uid_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE test_uid_seq FROM rmbt;
GRANT ALL ON SEQUENCE test_uid_seq TO rmbt;
GRANT USAGE ON SEQUENCE test_uid_seq TO rmbt_group_control;


--
-- Name: v_test; Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON TABLE v_test FROM PUBLIC;
REVOKE ALL ON TABLE v_test FROM rmbt;
GRANT ALL ON TABLE v_test TO rmbt;
GRANT SELECT ON TABLE v_test TO rmbt_group_read_only;


-- ----------------------Begin add user:tj 2017.03.21 ------------------------------------------------------------------------------------

-- ---------------add table: test_sum_speed--------------------------
-- Table: public.test_sum_speed
-- DROP TABLE public.test_sum_speed;

CREATE TABLE public.test_sum_speed
(
    uid bigint NOT NULL,
    test_id bigint NOT NULL,
    upload boolean NOT NULL,
    thread smallint NOT NULL,
    "time" bigint NOT NULL,
    bytes bigint NOT NULL,
    sumbytes bigint,
    speed double precision,
    CONSTRAINT test_sum_speed_pkey PRIMARY KEY (uid)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.test_sum_speed OWNER to rmbt;

GRANT INSERT, SELECT ON TABLE public.test_sum_speed TO rmbt_control;
GRANT ALL ON TABLE public.test_sum_speed TO rmbt;
GRANT SELECT ON TABLE public.test_sum_speed TO rmbt_group_read_only;
GRANT INSERT, UPDATE ON TABLE public.test_sum_speed TO rmbt_group_control;

-- Index: test_sum_speed_test_id_key
-- DROP INDEX public.test_sum_speed_test_id_key;

CREATE INDEX test_sum_speed_test_id_key
    ON public.test_sum_speed USING btree
    (test_id)
    TABLESPACE pg_default;


-- --------------add json_object_update_key---------------------------------------
-- FUNCTION: public.json_object_update_key(json, text, anyelement)
-- DROP FUNCTION public.json_object_update_key(json, text, anyelement);

CREATE OR REPLACE FUNCTION public.json_object_update_key(
	json json,
	key_to_set text,
	value_to_set anyelement)
RETURNS json
    LANGUAGE 'sql'
    COST 100.0
    IMMUTABLE NOT LEAKPROOF STRICT 
AS $function$

SELECT CASE
  WHEN ("json" -> "key_to_set") IS NULL THEN "json"
  ELSE (SELECT concat('{', string_agg(to_json("key") || ':' || "value", ','), '}')
          FROM (SELECT *
                  FROM json_each("json")
                 WHERE "key" <> "key_to_set"
                 UNION ALL
                SELECT "key_to_set", to_json("value_to_set")) AS "fields")::json
END

$function$;

ALTER FUNCTION public.json_object_update_key(json, text, anyelement) OWNER TO rmbt;

-- ------------add function: a_sum_speed, convert one test data by test_id,  from test_speed to test_sum_speed--- 
-- FUNCTION: public.a_sum_speed(bigint)
-- DROP FUNCTION public.a_sum_speed(bigint);

CREATE OR REPLACE FUNCTION public.a_sum_speed(
	p_id bigint DEFAULT 0)
RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100.0
    VOLATILE NOT LEAKPROOF 
AS $function$

DECLARE
  i bigint;  j bigint;
  cnt bigint;
  cc0 refcursor; cc1 refcursor; cc2 refcursor; 
  cc3 refcursor; cc4 refcursor; cc5 refcursor; cc6 refcursor;
  maxTh bigint;
  newB bigint[];
  oldB bigint[];
  deltaB bigint[];
  newMS bigint[];
  oldMS bigint[];
  deltaMS bigint[];
  newID bigint[];
  oldID bigint[];
  Total bigint;
  v_id bigint;
  v_b bigint;
  v_bytes bigint;
  v_ms bigint;
  v_up boolean;
  v_speed real;
  maxMs bigint;
  ix bigint;
  KB bigint;
  v_xx bigint;
  v_continue boolean;
  v_tt bigint;
  v_downloadSpeedKB bigint;
  v_uploadSpeedKB bigint;
  v_maxDownloadSpeedKB bigint;
  v_maxUploadSpeedKB bigint;
  v_nsecDownload bigint;
  v_nsecUpload bigint;
  v_downloadB bigint;
  v_uploadB bigint;
BEGIN
  cnt:=0;v_uploadB:=0; 
  v_downloadB:=0;v_downloadSpeedKB:=0;v_maxDownloadSpeedKB:=0; v_nsecDownload:=0; 
  v_uploadB:=0;  v_uploadSpeedKB:=0;v_maxUploadSpeedKB:=0; v_nsecUpload:=0; 
  maxTh:=(select Max(thread) th from test_speed where test_id=p_id);
  if (maxTh is null) then RETURN 0; end if; -- if is empty data 
  j:=0;
  while j<=1 LOOP
     Total:=0;
     v_up:=(j=1);
     v_tt:=(select Min(AA.ms) ms from (SELECT Max(time /1000000) ms from test_speed where test_id=p_id and upload=v_up group by thread) AA );
     if j=1 then 
          -- close all cursors 
          i:=0;
          WHILE i<=maxTh LOOP 
           if i=0 then close cc0; end if;
           if i=1 then close cc1; end if;
           if i=2 then close cc2; end if;
           if i=3 then close cc3; end if;
           if i=4 then close cc4; end if;
           if i=5 then close cc5; end if;
           if i=6 then close cc6; end if;    
           i:=i+1;
          END LOOP; 
      end if;
      j:=j+1;
    
     -- open cursors for all threads, and zero fill arrays 
      i:=0;
      WHILE (i<=maxTh) LOOP 
        if i=0 then open cc0 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=1 then open cc1 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=2 then open cc2 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=3 then open cc3 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=4 then open cc4 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=5 then open cc5 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=6 then open cc6 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        newB[i]=0; newMS[i]:=0; newID[i]:=0; deltaMS[i]:=0; deltaB[i]:=0; oldB[i]:=0; oldMS[i]:=0; oldID[i]:=0; 
        i:=i+1;
      END LOOP;
      
      -- load data from threads 
      i:=0;
      WHILE (i<=maxTh) LOOP 
        if i=0 then FETCH cc0 INTO v_id,v_b,v_ms;   end if;
        if i=1 then FETCH cc1 INTO v_id,v_b,v_ms;  end if;
        if i=2 then FETCH cc2 INTO v_id,v_b,v_ms;  end if;
        if i=3 then FETCH cc3 INTO v_id,v_b,v_ms;  end if;
        if i=4 then FETCH cc4 INTO v_id,v_b,v_ms;  end if;
        if i=5 then FETCH cc5 INTO v_id,v_b,v_ms;  end if;
        if i=6 then FETCH cc6 INTO v_id,v_b,v_ms;  end if;
        oldID[i]:=newID[i]; oldB[i]:=newB[i]; oldMS[i]:=newMS[i]; 
        if (v_id is not null) then
            newID[i]:=v_id; newB[i]:=v_b; newMS[i]:=v_ms; 
            deltaB[i]:=newB[i]-oldB[i]; deltaMS[i]:=newMS[i]-oldMS[i];
            if deltaB[i]<0 then deltaB[i]:=0; end if;
        end if;
        i:=i+1;
      END LOOP;

      v_continue:=true;
      WHILE v_continue LOOP
          -- find minimum ms 
          i:=0; ix:=-1; maxMs:=9999999999999999;
          WHILE (i<=maxTh) LOOP 
            if (newMS[i] is not null) and (newMS[i]>0) and (newMS[i]<maxMs) then maxMs:=newMS[i]; ix:=i; end if;
            i:=i+1;
          END LOOP;  
          if (ix>-1) then 
              -- claculate Total bytes and aliquot bytes form other threads (KB)
              KB:=0; i:=0; 
              WHILE (i<=maxTh) LOOP        
                if (newMS[i] is not null) then  
                    if (newMS[i]=newMS[ix]) then
                      if (i<>ix) then 
                        v_id:=newID[i]; v_ms:=newMS[i]; v_bytes:=newB[i];
                        insert into test_sum_speed(uid, test_id, upload, thread, time, bytes, sumbytes, speed) VALUES (v_id, p_id, v_up, i, v_ms, v_bytes, null, null);
                      end if;
                      Total:= Total + deltaB[i];
                    else 
                      if (newMS[ix]>oldMS[ix]) and (deltaMS[i]>0) then 
                        KB:=KB + round( deltaB[i]*(newMS[ix]-oldMS[i])/deltaMS[i] );
                      end if;
                    end if;
                 end if;
                 i:=i+1; 
               END LOOP;
               -- save data 
                v_id:=newID[ix]; v_ms:=newMS[ix]; v_bytes:=newB[ix]; v_b:=Total + KB; v_speed:=8 * (Total+ KB) /(newMS[ix]/1e3);
                if v_ms<=v_tt then 
                  if v_up=false then 
                    v_downloadSpeedKB:=round(v_speed/1e3); 
                    v_downloadB:=v_b;
                    if v_downloadSpeedKB>v_maxDownloadSpeedKB then  v_maxDownloadSpeedKB:=v_downloadSpeedKB; end if;
                    v_nsecDownload:=v_ms * 1000000;
                  else 
                    v_uploadSpeedKB:=round(v_speed/1e3);
                    v_uploadB:=v_b; 
                    if v_uploadSpeedKB>v_maxUploadSpeedKB then  v_maxUploadSpeedKB:=v_uploadSpeedKB; end if;
                    v_nsecUpload :=v_ms * 1000000;
                  end if;
                  insert into test_sum_speed(uid, test_id, upload, thread, time, bytes, sumbytes, speed) VALUES (v_id, p_id, v_up, ix, v_ms, v_bytes, v_b, v_speed);
                else
                  insert into test_sum_speed(uid, test_id, upload, thread, time, bytes, sumbytes, speed) VALUES (v_id, p_id, v_up, ix, v_ms, v_bytes, null, null);
                end if;
                cnt:=cnt+1;
               -- move to next row(s), and load data to arrays 
               v_xx:=newMS[ix];
               i:=0;
               WHILE (i<=maxTh) LOOP 
                 if (newMS[i] is not null) then
                   if (newMS[i]=v_xx) then 
                        if i=0 then FETCH cc0 INTO v_id,v_b,v_ms;  end if;
                        if i=1 then FETCH cc1 INTO v_id,v_b,v_ms;  end if;
                        if i=2 then FETCH cc2 INTO v_id,v_b,v_ms;  end if;
                        if i=3 then FETCH cc3 INTO v_id,v_b,v_ms;  end if;
                        if i=4 then FETCH cc4 INTO v_id,v_b,v_ms;  end if;
                        if i=5 then FETCH cc5 INTO v_id,v_b,v_ms;  end if;
                        if i=6 then FETCH cc6 INTO v_id,v_b,v_ms;  end if;
                        oldID[i]:=newID[i]; oldB[i]:=newB[i]; oldMS[i]:=newMS[i]; 
                        newID[i]:=v_id; newB[i]:=v_b; newMS[i]:=v_ms; 
                        if (newMS[i] is not null) then 
                          deltaB[i]:=newB[i]-oldB[i]; deltaMS[i]:=newMS[i]-oldMS[i];
                          if deltaB[i]<0 then deltaB[i]:=0; end if;
                        end if;
                   end if;
                 end if;
                 i:=i+1;
               END LOOP;
          else 
            v_continue:=false;
          end if;
      END LOOP;
  END LOOP;
  -- close all cursors 
  i:=0;
  WHILE (i<=maxTh) LOOP 
   if i=0 then close cc0; end if;
   if i=1 then close cc1; end if;
   if i=2 then close cc2; end if;
   if i=3 then close cc3; end if;
   if i=4 then close cc4; end if;
   if i=5 then close cc5; end if;
   if i=6 then close cc6; end if;    
   i:=i+1;
  END LOOP; 
  -- data to time v_tt (t*), SpecureNettest-Product-Description_v1.7.pdf
  update test set 
    additional_report_fields=json_object_update_key(json_object_update_key(additional_report_fields,'peak_up_kbit',v_maxUploadSpeedKB),'peak_down_kbit',v_maxDownloadSpeedKB),
    speed_download=v_downloadSpeedKB, 
    nsec_download=v_nsecDownload, 
    bytes_download=v_downloadB,
    speed_upload=v_uploadSpeedKB, 
    nsec_upload=v_nsecUpload,  
    bytes_upload=v_uploadB,
    speed_download_log=(log(v_downloadSpeedKB::double precision/10))/4,
    speed_upload_log=(log(v_uploadSpeedKB::double precision/10))/4
   
  where uid=p_id;

  RETURN cnt;
END;

$function$;

ALTER FUNCTION public.a_sum_speed(bigint) OWNER TO rmbt;

-- add function: a_update_sum_speed, insert records from test_spped to test_sum_speed -------------
-- FUNCTION: public.a_update_sum_speed(bigint, bigint)
-- DROP FUNCTION public.a_update_sum_speed(bigint, bigint);

CREATE OR REPLACE FUNCTION public.a_update_sum_speed(
	p_from_id bigint DEFAULT 0,
	p_to_id bigint DEFAULT 0)
RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100.0
    VOLATILE NOT LEAKPROOF 
AS $function$

DECLARE
  rec RECORD;
  cnt bigint;
  v_uid bigint;
  v_ii bigint;
BEGIN
   cnt:=0;
   p_from_id:=COALESCE(p_from_id,0); p_to_id:=COALESCE(p_to_id,0);
   FOR rec IN SELECT test.uid FROM  test where test.status='FINISHED' and test.uid>p_from_id and (test.uid<=p_to_id or p_to_id<=0) order by test.uid 
   LOOP
     v_uid:=rec.uid;
     if (v_uid is not null) then 
        BEGIN
          v_ii:=(select a_sum_speed(v_uid));
          cnt:=cnt+1;
        exception when others then 
        END;
     end if;
   END LOOP;
  RETURN cnt;
END;

$function$;

ALTER FUNCTION public.a_update_sum_speed(bigint, bigint) OWNER TO rmbt;

-- add trigger function , insert data to test_sum_speed
CREATE FUNCTION a_trigger_au_test() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
 DECLARE  
  -- by function: a_sum_speed();
  i bigint;  j bigint;
  cnt bigint;
  cc0 refcursor; cc1 refcursor; cc2 refcursor; 
  cc3 refcursor; cc4 refcursor; cc5 refcursor; cc6 refcursor;
  maxTh bigint;
  newB bigint[];
  oldB bigint[];
  deltaB bigint[];
  newMS bigint[];
  oldMS bigint[];
  deltaMS bigint[];
  newID bigint[];
  oldID bigint[];
  Total bigint;
  v_id bigint;
  v_b bigint;
  v_bytes bigint;
  v_ms bigint;
  v_up boolean;
  v_speed real;
  maxMs bigint;
  ix bigint;
  KB bigint;
  v_xx bigint;
  v_continue boolean;
  v_tt bigint;
  v_downloadSpeedKB bigint;
  v_uploadSpeedKB bigint;
  v_maxDownloadSpeedKB bigint;
  v_maxUploadSpeedKB bigint;
  v_nsecDownload bigint;
  v_nsecUpload bigint;
  v_downloadB bigint;
  v_uploadB bigint;
  p_id bigint;
BEGIN
  IF (TG_OP <> 'UPDATE' OR OLD.STATUS<>'STARTED' OR NEW.STATUS<>'FINISHED') then RETURN NEW; end if;
  IF (NEW.uid is null) then RETURN NEW; end if;
  p_id:=New.uid;
  cnt:=0;v_uploadB:=0; 
  v_downloadB:=0;v_downloadSpeedKB:=0;v_maxDownloadSpeedKB:=0; v_nsecDownload:=0; 
  v_uploadB:=0;  v_uploadSpeedKB:=0;v_maxUploadSpeedKB:=0; v_nsecUpload:=0; 
  maxTh:=(select Max(thread) th from test_speed where test_id=p_id);
  if (maxTh is null) then RETURN NEW; end if; -- if is empty data 
  j:=0;
  while j<=1 LOOP
     Total:=0;
     v_up:=(j=1);
     v_tt:=(select Min(AA.ms) ms from (SELECT Max(time /1000000) ms from test_speed where test_id=p_id and upload=v_up group by thread) AA );
     if j=1 then 
          -- close all cursors 
          i:=0;
          WHILE i<=maxTh LOOP 
           if i=0 then close cc0; end if;
           if i=1 then close cc1; end if;
           if i=2 then close cc2; end if;
           if i=3 then close cc3; end if;
           if i=4 then close cc4; end if;
           if i=5 then close cc5; end if;
           if i=6 then close cc6; end if;    
           i:=i+1;
          END LOOP; 
      end if;
      j:=j+1;
    
     -- open cursors for all threads, and zero fill arrays 
      i:=0;
      WHILE (i<=maxTh) LOOP 
        if i=0 then open cc0 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=1 then open cc1 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=2 then open cc2 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=3 then open cc3 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=4 then open cc4 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=5 then open cc5 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        if i=6 then open cc6 for select uid, bytes, time/1000000 as ms from test_speed where test_id=p_id and upload=v_up and thread=i and bytes>0 order by time; end if;
        newB[i]=0; newMS[i]:=0; newID[i]:=0; deltaMS[i]:=0; deltaB[i]:=0; oldB[i]:=0; oldMS[i]:=0; oldID[i]:=0; 
        i:=i+1;
      END LOOP;
      
      -- load data from threads 
      i:=0;
      WHILE (i<=maxTh) LOOP 
        if i=0 then FETCH cc0 INTO v_id,v_b,v_ms;   end if;
        if i=1 then FETCH cc1 INTO v_id,v_b,v_ms;  end if;
        if i=2 then FETCH cc2 INTO v_id,v_b,v_ms;  end if;
        if i=3 then FETCH cc3 INTO v_id,v_b,v_ms;  end if;
        if i=4 then FETCH cc4 INTO v_id,v_b,v_ms;  end if;
        if i=5 then FETCH cc5 INTO v_id,v_b,v_ms;  end if;
        if i=6 then FETCH cc6 INTO v_id,v_b,v_ms;  end if;
        oldID[i]:=newID[i]; oldB[i]:=newB[i]; oldMS[i]:=newMS[i]; 
        if (v_id is not null) then
            newID[i]:=v_id; newB[i]:=v_b; newMS[i]:=v_ms; 
            deltaB[i]:=newB[i]-oldB[i]; deltaMS[i]:=newMS[i]-oldMS[i];
            if deltaB[i]<0 then deltaB[i]:=0; end if;
        end if;
        i:=i+1;
      END LOOP;

      v_continue:=true;
      WHILE v_continue LOOP
          -- find minimum ms 
          i:=0; ix:=-1; maxMs:=9999999999999999;
          WHILE (i<=maxTh) LOOP 
            if (newMS[i] is not null) and (newMS[i]>0) and (newMS[i]<maxMs) then maxMs:=newMS[i]; ix:=i; end if;
            i:=i+1;
          END LOOP;  
          if (ix>-1) then 
              -- claculate Total bytes and aliquot bytes form other threads (KB)
              KB:=0; i:=0; 
              WHILE (i<=maxTh) LOOP        
                if (newMS[i] is not null) then  
                    if (newMS[i]=newMS[ix]) then
                      if (i<>ix) then 
                        v_id:=newID[i]; v_ms:=newMS[i]; v_bytes:=newB[i];
                        insert into test_sum_speed(uid, test_id, upload, thread, time, bytes, sumbytes, speed) VALUES (v_id, p_id, v_up, i, v_ms, v_bytes, null, null);
                      end if;
                      Total:= Total + deltaB[i];
                    else 
                      if (newMS[ix]>oldMS[ix]) and (deltaMS[i]>0) then 
                        KB:=KB + round( deltaB[i]*(newMS[ix]-oldMS[i])/deltaMS[i] );
                      end if;
                    end if;
                 end if;
                 i:=i+1; 
               END LOOP;
               -- save data 
                v_id:=newID[ix]; v_ms:=newMS[ix]; v_bytes:=newB[ix]; v_b:=Total + KB; v_speed:=8 * (Total+ KB) /(newMS[ix]/1e3);
                if v_ms<=v_tt then 
                  if v_up=false then 
                    v_downloadSpeedKB:=round(v_speed/1e3); 
                    v_downloadB:=v_b;
                    if v_downloadSpeedKB>v_maxDownloadSpeedKB then  v_maxDownloadSpeedKB:=v_downloadSpeedKB; end if;
                    v_nsecDownload:=v_ms * 1000000;
                  else 
                    v_uploadSpeedKB:=round(v_speed/1e3);
                    v_uploadB:=v_b; 
                    if v_uploadSpeedKB>v_maxUploadSpeedKB then  v_maxUploadSpeedKB:=v_uploadSpeedKB; end if;
                    v_nsecUpload :=v_ms * 1000000;
                  end if;
                  insert into test_sum_speed(uid, test_id, upload, thread, time, bytes, sumbytes, speed) VALUES (v_id, p_id, v_up, ix, v_ms, v_bytes, v_b, v_speed);
                else
                  insert into test_sum_speed(uid, test_id, upload, thread, time, bytes, sumbytes, speed) VALUES (v_id, p_id, v_up, ix, v_ms, v_bytes, null, null);
                end if;
                cnt:=cnt+1;
               -- move to next row(s), and load data to arrays 
               v_xx:=newMS[ix];
               i:=0;
               WHILE (i<=maxTh) LOOP 
                 if (newMS[i] is not null) then
                   if (newMS[i]=v_xx) then 
                        if i=0 then FETCH cc0 INTO v_id,v_b,v_ms;  end if;
                        if i=1 then FETCH cc1 INTO v_id,v_b,v_ms;  end if;
                        if i=2 then FETCH cc2 INTO v_id,v_b,v_ms;  end if;
                        if i=3 then FETCH cc3 INTO v_id,v_b,v_ms;  end if;
                        if i=4 then FETCH cc4 INTO v_id,v_b,v_ms;  end if;
                        if i=5 then FETCH cc5 INTO v_id,v_b,v_ms;  end if;
                        if i=6 then FETCH cc6 INTO v_id,v_b,v_ms;  end if;
                        oldID[i]:=newID[i]; oldB[i]:=newB[i]; oldMS[i]:=newMS[i]; 
                        newID[i]:=v_id; newB[i]:=v_b; newMS[i]:=v_ms; 
                        if (newMS[i] is not null) then 
                          deltaB[i]:=newB[i]-oldB[i]; deltaMS[i]:=newMS[i]-oldMS[i];
                          if deltaB[i]<0 then deltaB[i]:=0; end if;
                        end if;
                   end if;
                 end if;
                 i:=i+1;
               END LOOP;
          else 
            v_continue:=false;
          end if;
      END LOOP;
  END LOOP;
  -- close all cursors 
  i:=0;
  WHILE (i<=maxTh) LOOP 
   if i=0 then close cc0; end if;
   if i=1 then close cc1; end if;
   if i=2 then close cc2; end if;
   if i=3 then close cc3; end if;
   if i=4 then close cc4; end if;
   if i=5 then close cc5; end if;
   if i=6 then close cc6; end if;    
   i:=i+1;
  END LOOP; 
  -- data to time v_tt (t*), SpecureNettest-Product-Description_v1.7.pdf
  /*
  update test set 
    additional_report_fields=json_object_update_key(json_object_update_key(additional_report_fields,'peak_up_kbit',v_maxUploadSpeedKB),'peak_down_kbit',v_maxDownloadSpeedKB),
    speed_download=v_downloadSpeedKB, 
    nsec_download=v_nsecDownload, 
    bytes_download=v_downloadB,
    speed_upload=v_uploadSpeedKB, 
    nsec_upload=v_nsecUpload,  
    bytes_upload=v_uploadB
  where uid=p_id;
  */
   NEW.additional_report_fields:=json_object_update_key(json_object_update_key(Old.additional_report_fields,'peak_up_kbit',v_maxUploadSpeedKB),'peak_down_kbit',v_maxDownloadSpeedKB);
   NEW.speed_download:=v_downloadSpeedKB;
   NEW.nsec_download:=v_nsecDownload; 
   NEW.bytes_download:=v_downloadB;
   NEW.speed_upload:=v_uploadSpeedKB; 
   NEW.nsec_upload:=v_nsecUpload;  
   NEW.bytes_upload:=v_uploadB;
  RETURN NEW;
END;  $$

CREATE TRIGGER trigger_au_test
    AFTER UPDATE 
    ON public.test
    FOR EACH ROW
    WHEN ((((old.status)::text = 'STARTED'::text) AND ((new.status)::text = 'FINISHED'::text)))
    EXECUTE PROCEDURE a_trigger_au_test();



-- ----------------------------------End add user:tj 2017.03.21 ---------------------------------------------
-- ----------------------------------End add user:tj 2017.06.29 ---------------------------------------------
-- Table: public.tsumvalues

-- DROP TABLE public.tsumvalues;

CREATE TABLE public.tsumvalues
(
    tsv_name character varying(10) COLLATE pg_catalog."default" NOT NULL,
    tsv_date date DEFAULT '2000-01-01'::date,
    tsv_value numeric(13, 2) NOT NULL DEFAULT 0.0,
    CONSTRAINT tsumvalues_pkey PRIMARY KEY (tsv_name)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.tsumvalues OWNER to rmbt;

insert into public.tsumvalues (tsv_name) VALUES ('clients');
insert into public.tsumvalues (tsv_name) VALUES ('tests');

CREATE OR REPLACE FUNCTION public.a_clients_count()
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100.0
    VOLATILE NOT LEAKPROOF 
AS $function$

DECLARE
  v_users bigint;
  v_add bigint;
  v_date date;
  v_now date;
  v_value bigint;
  v_name varchar(15);
  rec RECORD;
BEGIN
  v_users:=0; v_add:=0;
  v_now:= (select current_date);
  v_date:=(select tsv_date from tsumvalues where tsv_name='clients');
  if (v_date<v_now) or (v_date is null) then 
	  FOR rec IN SELECT tsv_name,tsv_date,tsv_value FROM  tsumvalues where tsv_name='clients' 
	  LOOP
	    v_name:=rec.tsv_name;
	    v_date:=rec.tsv_date;
	    v_value:=round(rec.tsv_value,0)::INT;
	    if v_name='clients' then 
	      if v_date>to_date('2000-01-01','YYYY-MM-DD') then
	        v_users:=(select count(DISTINCT open_uuID) cntUser  from test where time>v_date and time<=current_date);
	        v_users:=v_value+v_users;
	      else 
	        v_users:=(select count(DISTINCT open_uuID) cntUser  from test where time<=current_date);
	      end if;
	      update tsumvalues set tsv_value=v_users, tsv_date=v_now where tsv_name='clients';
	    end if;
	  END LOOP;
  else 
    v_users:=(select round(tsv_value,0)::INT from tsumvalues where tsv_name='clients');
  end if;
  v_add:=(select count(DISTINCT open_uuID) cntUser  from test where time>current_date);
  v_users:=v_users + v_add;
  RETURN v_users;
END;

$function$;

ALTER FUNCTION public.a_clients_count() OWNER TO rmbt;

GRANT EXECUTE ON FUNCTION public.a_clients_count() TO rmbt_control;

GRANT EXECUTE ON FUNCTION public.a_clients_count() TO PUBLIC;

GRANT EXECUTE ON FUNCTION public.a_clients_count() TO rmbt;


CREATE OR REPLACE FUNCTION public.a_tests_count()
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100.0
    VOLATILE NOT LEAKPROOF 
AS $function$

DECLARE
  v_add bigint;
  v_tests bigint;
  v_date date;
  v_now date;
  v_value bigint;
  v_name varchar(15);
  rec RECORD;
BEGIN
  v_tests:=0; v_add:=0;
  v_now:= (select current_date);
  v_date:=(select tsv_date from tsumvalues where tsv_name='tests');
  if (v_date<v_now) or (v_date is null) then 
	  FOR rec IN SELECT tsv_name,tsv_date,tsv_value FROM  tsumvalues where tsv_name='tests'
	  LOOP
	    v_name:=rec.tsv_name;
	    v_date:=rec.tsv_date;
	    v_value:=round(rec.tsv_value,0)::INT;
	    if v_name='tests' then 
	      if v_date>to_date('2000-01-01','YYYY-MM-DD') then
	        v_tests:=(select count(*) cnt  from test where time>v_date and time<=current_date);
	        v_tests:=v_value+v_tests;
	      else 
	        v_tests:=(select count(*) cnt  from test where time<=current_date);
	      end if;
	      update tsumvalues set tsv_value=v_tests, tsv_date=v_now where tsv_name='tests';
	    end if;
	  END LOOP;
  else 
    v_tests:=(select round(tsv_value,0)::INT from tsumvalues where tsv_name='tests');
  end if;
  v_add:=(select count(*) cnt  from test where time>current_date);
  v_tests:=v_tests + v_add;
  RETURN v_tests;
END;
 
$function$;

ALTER FUNCTION public.a_tests_count()
    OWNER TO rmbt;

GRANT EXECUTE ON FUNCTION public.a_tests_count() TO rmbt_control;

GRANT EXECUTE ON FUNCTION public.a_tests_count() TO PUBLIC;

GRANT EXECUTE ON FUNCTION public.a_tests_count() TO rmbt;



CREATE OR REPLACE FUNCTION public.a_sumvalues()
    RETURNS bigint
    LANGUAGE 'plpgsql'
    COST 100.0
    VOLATILE NOT LEAKPROOF 
AS $function$
DECLARE
  v_tests bigint;
  v_users bigint;
  v_now date;
BEGIN
  v_tests:=0; v_users:=0; v_tests:=0;
  v_now:= (select current_date);
  v_users:=(select count(DISTINCT open_uuID) cntUser  from test where time<=v_now);
  v_tests:=(select count(*) cnt  from test where time<=v_now);
  update tsumvalues set tsv_value=v_tests, tsv_date=v_now where tsv_name='tests';
  update tsumvalues set tsv_value=v_users, tsv_date=v_now where tsv_name='clients';
  RETURN 2;
END;

$function$;

ALTER FUNCTION public.a_sumvalues() OWNER TO rmbt;

GRANT EXECUTE ON FUNCTION public.a_sumvalues() TO rmbt_control;

GRANT EXECUTE ON FUNCTION public.a_sumvalues() TO PUBLIC;

GRANT EXECUTE ON FUNCTION public.a_sumvalues() TO rmbt;


--
-- by KGB
--
CREATE TABLE public.qos_test_result
(
    uid integer NOT NULL DEFAULT nextval('qos_test_result_uid_seq'::regclass),
    test_uid bigint,
    qos_test_uid bigint,
    success_count integer NOT NULL DEFAULT 0,
    failure_count integer NOT NULL DEFAULT 0,
    implausible boolean DEFAULT false,
    deleted boolean DEFAULT false,
    result json NOT NULL DEFAULT '{}'::json,
    CONSTRAINT qos_test_result_pkey PRIMARY KEY (uid),
    CONSTRAINT qos_test_result_qos_test_uid_fkey FOREIGN KEY (qos_test_uid)
        REFERENCES public.qos_test_objective (uid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT qos_test_result_test_uid FOREIGN KEY (test_uid)
        REFERENCES public.test (uid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.qos_test_result
    OWNER to rmbt;

GRANT INSERT, SELECT, UPDATE, DELETE ON TABLE public.qos_test_result TO rmbt_control;

GRANT ALL ON TABLE public.qos_test_result TO rmbt;

GRANT INSERT, SELECT, UPDATE, DELETE ON TABLE public.qos_test_result TO rmbt_web_admin;

GRANT SELECT ON TABLE public.qos_test_result TO rmbt_group_read_only;

GRANT INSERT, UPDATE ON TABLE public.qos_test_result TO rmbt_group_control;

-- Index: fki_qos_test_result_qos_test_uid_fkey

-- DROP INDEX public.fki_qos_test_result_qos_test_uid_fkey;

CREATE INDEX fki_qos_test_result_qos_test_uid_fkey
    ON public.qos_test_result USING btree
    (qos_test_uid)
    TABLESPACE pg_default;

-- Index: fki_qos_test_result_test_uid

-- DROP INDEX public.fki_qos_test_result_test_uid;

CREATE INDEX fki_qos_test_result_test_uid
    ON public.qos_test_result USING btree
    (test_uid)
    TABLESPACE pg_default;


--
-- author: Tomas Hreben
-- email: tomas.hreben@martes-specure.com
-- date: 29.1.2018
--
CREATE TABLE survey_result(
	uid bigint not null,
	client_uuid UUID not null,
	email VARCHAR(256) not null,
	"time" timestamp with time zone not null,
	 questionnaire json not null,

  CONSTRAINT survey_result_pkey PRIMARY KEY (uid)
);

ALTER TABLE survey_result OWNER TO rmbt;

CREATE SEQUENCE survey_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE survey_uid_seq OWNER TO rmbt;

ALTER SEQUENCE survey_uid_seq OWNED BY survey_result.uid;

ALTER TABLE ONLY survey_result ALTER COLUMN uid SET DEFAULT nextval('survey_uid_seq'::regclass);

-- removal of constraint NOT NULL from column definition
ALTER TABLE survey_result ALTER COLUMN email DROP NOT NULL;


--
-- PostgreSQL database dump complete
--

--- Our own implementation of median
create or replace function "_final_median"(numeric[]) returns numeric
	immutable
	language sql
as $$
SELECT AVG(val)
   FROM (
     SELECT val
     FROM unnest($1) val
     ORDER BY 1
     LIMIT  2 - MOD(array_upper($1, 1), 2)
     OFFSET CEIL(array_upper($1, 1) / 2.0) - 1
   ) sub;
$$
;

create or replace function "_final_median"(anyarray) returns double precision
	immutable
	language sql
as $$
WITH q AS
  (
     SELECT val
     FROM unnest($1) val
     WHERE VAL IS NOT NULL
     ORDER BY 1
  ),
  cnt AS
  (
    SELECT COUNT(*) AS c FROM q
  )
  SELECT AVG(val)::float8
  FROM 
  (
    SELECT val FROM q
    LIMIT  2 - MOD((SELECT c FROM cnt), 2)
    OFFSET GREATEST(CEIL((SELECT c FROM cnt) / 2.0) - 1,0)  
  ) q2;
$$
;


CREATE AGGREGATE rmbt_median(anyelement) (
  SFUNC=array_append,
  STYPE=anyarray,
  FINALFUNC=_final_median,
  INITCOND='{}'
);

CREATE AGGREGATE rmbt_median(NUMERIC) (
  SFUNC=array_append,
  STYPE=NUMERIC[],
  FINALFUNC=_final_median,
  INITCOND='{}'
);


-- new functionality -> advertising

CREATE TABLE advertising(
  uid bigint not null,
  country VARCHAR(3) not null,
  adprovider VARCHAR(256) not null,
  bannerid VARCHAR(256) not null,
  appid VARCHAR(256) not null,
  active boolean DEFAULT false,

  CONSTRAINT advertising_pkey PRIMARY KEY (uid)
);

ALTER TABLE advertising OWNER TO rmbt;

CREATE SEQUENCE advertising_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE advertising OWNER TO rmbt;
ALTER SEQUENCE advertising_uid_seq OWNER TO rmbt;
ALTER TABLE ONLY advertising ALTER COLUMN uid SET DEFAULT nextval('advertising_uid_seq'::regclass);
CREATE INDEX advertising_country_idx ON advertising USING btree (country);


-- new functionality -> cell infos

CREATE TABLE cell_info (
    uid bigint NOT NULL,
    test_uid bigint NOT NULL,
    time bigint,
    type varchar(20),
    arfcn_number integer,
    result_band integer,
    result_band_name varchar(50),
    result_frequency_download double precision,
    result_frequency_upload double precision,
    result_bandwidth double precision
);

ALTER TABLE cell_info OWNER TO rmbt;
--ALTER TABLE cell_info OWNER TO root;

CREATE SEQUENCE cell_info_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE cell_info_uid_seq OWNER TO rmbt;
--ALTER TABLE cell_info_uid_seq OWNER TO root;

ALTER SEQUENCE cell_info_uid_seq OWNED BY cell_info.uid;

ALTER TABLE ONLY cell_info ALTER COLUMN uid SET DEFAULT nextval('cell_info_uid_seq'::regclass);

ALTER TABLE ONLY cell_info ADD CONSTRAINT cell_info_pkey PRIMARY KEY (uid);

CREATE INDEX cell_info_test_uid_idx ON cell_info USING btree (test_uid);

ALTER TABLE ONLY cell_info ADD CONSTRAINT cell_info_test_uid_fkey FOREIGN KEY (test_uid) REFERENCES test(uid) ON DELETE CASCADE;


-- new columnt loop_measurement for table test

ALTER TABLE test ADD COLUMN loop_measurement uuid DEFAULT NULL;


-- NT-1408 - New service that would determine the legal age for GDPR consent
create table if not exists gdpr_legal_age
(
	country_code varchar(10) not null,
	age integer not null
)
;

alter table gdpr_legal_age owner to root
;

create unique index if not exists gdpr_legal_age_country_code_uindex
	on gdpr_legal_age (country_code)
;

insert into gdpr_legal_age(country_code, age) values ('default', 16);

-- Badges

CREATE SEQUENCE public.badge_id_seq;

ALTER SEQUENCE public.badge_id_seq
    OWNER TO root;

CREATE SEQUENCE public.badge_terms_id_seq;

ALTER SEQUENCE public.badge_terms_id_seq
    OWNER TO root;

CREATE TABLE public.badge
(
    id integer NOT NULL DEFAULT nextval('badge_id_seq'::regclass),
    title character varying COLLATE pg_catalog."default" NOT NULL,
    description character varying COLLATE pg_catalog."default" NOT NULL,
    category character varying COLLATE pg_catalog."default" NOT NULL,
    image_link character varying COLLATE pg_catalog."default" NOT NULL,
    terms_operator character varying COLLATE pg_catalog."default" NOT NULL,
    language character varying(2) COLLATE pg_catalog."default" NOT NULL,
    en_id integer,
    CONSTRAINT badge_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.badge
    OWNER to root;

GRANT ALL ON TABLE public.badge TO root;


CREATE TABLE public.badge_criteria
(
    id integer NOT NULL DEFAULT nextval('badge_terms_id_seq'::regclass),
    badge_id bigint NOT NULL,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    operator character varying COLLATE pg_catalog."default" NOT NULL,
    value character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT badge_terms_pkey PRIMARY KEY (id),
    CONSTRAINT fk_badge FOREIGN KEY (badge_id)
        REFERENCES public.badge (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.badge_criteria
    OWNER to root;

GRANT ALL ON TABLE public.badge_criteria TO rmbt;



--- END of Badges


-- sql limit for selecting points on map
INSERT INTO public.settings ("key", "lang", "value") VALUES ('point_sql_limit', null, '500');
