-- Script creating the ModisSense database 
-- The default user is "root" authorized by password "root"
-- The database is called modissense.
--
-- Author: 	ggian@cslab.ece.ntua.gr
-- Date: 	26/4/2013
-- Updated: 	25/7/2013


-- User/Database creation and privileges granting
CREATE DATABASE modissense;
CREATE USER root WITH PASSWORD 'root' CREATEDB CREATEUSER;
GRANT ALL PRIVILEGES ON DATABASE modissense TO root;

-- Select database
\c modissense;

-- Table creation
CREATE TABLE users (
	id		serial primary key,
	username	varchar(100) UNIQUE,
	map_center 	point	-- right?
);

CREATE TABLE friends (
	usera 		integer REFERENCES users(id),
	userb		integer REFERENCES users(id),

	PRIMARY KEY(userA, userB)
);

CREATE TABLE sn_list (
	user_id 	integer REFERENCES users(id),
	sn_username	varchar(100),
	sn_token	varchar(100),
	sn_identifier	varchar(100),
	
	PRIMARY KEY (user_id, sn_name)
);

--CREATE TABLE poi (
--	poi_id 		varchar(100) PRIMARY KEY,
--	coordinates 	point,
--	interest 	integer, 
--	hotness 	integer, 
--	publicity 	boolean
--);

--CREATE TABLE poi_other_characteristics (
--	property_name 	varchar(100),
--	poi_id 		varchar(100) REFERENCES poi(poi_id),
--	property_value 	varchar(100),
----	
--	PRIMARY KEY(property_name, poi_id)
--);
--
--
--
--
--CREATE TABLE poi_list (
--	user_id 	varchar(100) REFERENCES users(user_id),
--	poi_id		varchar(100) REFERENCES poi(poi_id),
--	tmstamp		timestamp,
--
--	PRIMARY KEY (user_id, tmstamp)
--);
--
--
--CREATE TABLE sem_trajectories (
--	traj_id		integer PRIMARY KEY,
--	user_id		varchar(100) REFERENCES users(user_id),
--	start_loc	varchar(100) REFERENCES poi(poi_id),
--	end_loc		varchar(100) REFERENCES poi(poi_id),
--	start_time	timestamp,
--	end_time	timestamp,
--	traj_type	varchar(100)
--);
--
--CREATE TABLE traj_points (
--	traj_id 	integer	REFERENCES sem_trajectories(traj_id),
--	poi_id		varchar(100) REFERENCES poi(poi_id),
--	seq_num 	integer,
--	
--	PRIMARY KEY (traj_id, poi_id, seq_num)
--);
