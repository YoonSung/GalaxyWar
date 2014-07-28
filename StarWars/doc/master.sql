/* Server Info 
 * 
 * <master>
 * 10.73.45.65 / db1004$
 * 
 * 
 * <shard1>
 * 10.73.45.66 / databaseyoda 
 * 
 * <shard2>
 * 10.73.45.67 / databaseyoda
 * 
 * */

/* Create User */
CREATE USER 'jedi'@'%' IDENTIFIED BY 'jedi';
CREATE USER 'jedi'@'localhost' IDENTIFIED BY 'jedi';
GRANT ALL PRIVILEGES ON  yoda.* to 'jedi';
GRANT EXECUTE ON yoda.* TO 'jedi';

/*
TODO
1. isolation level repetable read 격리수준 적용
2. transaction 적용
*/

/*
TODO
1. isolation level repetable read 격리수준 적용
2. transaction 적용
*/

/* 초기화 */
DROP DATABASE IF EXISTS yoda;
CREATE DATABASE IF NOT EXISTS yoda;
USE yoda;

/* Create table */
CREATE TABLE db (
	DBID TINYINT,
	DBNAME CHAR(10),
	IP CHAR(15)
);

CREATE TABLE user2db (
	UID INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
	GID TINYINT,
	DBID TINYINT
);

CREATE TABLE galaxy2db (
	GID TINYINT PRIMARY KEY NOT NULL,
	DBID TINYINT
);

/* Insert Initial Data */
INSERT INTO db VALUES(1, "SHARD1", "10.73.45.66");
INSERT INTO db VALUES(2, "SHARD2", "10.73.45.67");
INSERT INTO galaxy2db VALUES(1, 2);
INSERT INTO galaxy2db VALUES(2, 1);
INSERT INTO galaxy2db VALUES(3, 2);
INSERT INTO galaxy2db VALUES(4, 1);

/* Master Procedure */
DROP PROCEDURE IF EXISTS ADDUSER;
DELIMITER &&
CREATE PROCEDURE ADDUSER(OUT RUID INT, OUT RDBID INT, OUT RGID TINYINT)
	BEGIN
		START TRANSACTION;
		INSERT INTO user2db values();
		set RUID = last_insert_id();
		set RGID = RUID % 4 + 1;
		set RDBID = RGID % 2 + 1;
		UPDATE user2db SET DBID = RDBID, GID = RGID WHERE UID = RUID;
		COMMIT;
	END &&
DELIMITER ;

DROP PROCEDURE IF EXISTS RANDOM_ATTACKER;
DELIMITER $$
CREATE PROCEDURE RANDOM_ATTACKER(OUT RGID TINYINT, OUT RUID INT)
	BEGIN
		SELECT GID, UID INTO RGID, RUID FROM user2db ORDER BY rand() LIMIT 1;
	END $$
DELIMITER ;
