--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Data for Name: settings; Type: TABLE DATA; Schema: public; Owner: rmbt
--

COPY settings (uid, key, lang, value) FROM stdin;
11	rmbt_num_pings	  	10
12	rmbt_geo_accuracy_button_limit	  	2000
13	rmbt_geo_accuracy_detail_limit	  	10000
14	rmbt_geo_distance_detail_limit	  	3000
10	rmbt_num_threads	  	3
16	threshold_download	  	10000;2000
15	threshold_upload	  	2000;1000
6	url_statistics	  	https://nettest.specure.com/en/statistics
9	url_statistics	de	https://nettest.specure.com/en/statistics
18	url_statistics	sl	https://nettest.specure.com/sl/statistics
3	control_ipv4_only	\N	nettest4.specure.com
4	control_ipv6_only	\N	nettest6.specure.com
1	url_ipv4_check	  	https://nettest4.specure.com/RMBTControlServer/ip
2	url_ipv6_check	\N	https://nettest6.specure.com/RMBTControlServer/ip
7	url_open_data_prefix	\N	https://nettest.specure.com/en/opentest/
8	rmbt_duration	  	5
19	has_advertised_speed_option	\N	true
\.


--
-- Name: settings_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: rmbt
--

SELECT pg_catalog.setval('settings_uid_seq', 19, true);


--
-- PostgreSQL database dump complete
--

