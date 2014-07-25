/*
TODO
1. isolation level repetable read 격리수준 적용
2. transaction 적용
*/

DROP DATABASE IF EXISTS yoda;
CREATE DATABASE IF NOT EXISTS yoda;
USE yoda;

/* Create table */

CREATE TABLE db (
	DBID TINYINT,
	DBNAME CHAR(10),
	IP CHAR(15),
	PORT INT
);

CREATE TABLE user2db (
	USERID INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
	GID TINYINT,
	DBID TINYINT
);

/* Insert Initial Data */
INSERT INTO db VALUES(1, "SHARD1", "10.73.45.65", 3306);
INSERT INTO db VALUES(2, "SHARD2", "10.73.45.67", 3306);

/* Master Procedure */
DROP PROCEDURE IF EXISTS ADDUSER;
DELIMITER &&
CREATE PROCEDURE ADDUSER(OUT RUID INT, OUT RDBID INT, OUT RGID TINYINT)
	BEGIN
		START TRANSACTION;
		INSERT INTO user2db values();
		set RUID = last_insert_id();
		set RGID = RUID % 4;
		set RDBID = RGID % 2 + 1;
		UPDATE user2db SET DBID = RDBID, GID = RGID WHERE USERID = RUID;
		COMMIT;
	END &&
DELIMITER ;

/* Test Code */
CALL ADDUSER(@RUID, @RDBID, @RGID);

SELECT @RUID;
SELECT @RDBID;
SELECT @RGID;