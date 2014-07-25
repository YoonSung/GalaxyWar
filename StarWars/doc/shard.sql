DROP DATABASE IF EXISTS yoda;
CREATE DATABASE IF NOT EXISTS yoda;
USE yoda;

/* CREATE USER */
CREATE USER 'jedi'@'%' IDENTIFIED BY 'jedi';
GRANT ALL PRIVILEGES ON  yoda.* to 'jedi';
GRANT EXECUTE ON yoda.* TO 'jedi';

/* Create table */
CREATE TABLE galaxy (
	GID TINYINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
	NAME VARCHAR(16),
	HP INT
);

CREATE TABLE user (
	UID INT NOT NULL,
	GID TINYINT,
	FOREIGN KEY (GID) REFERENCES galaxy(GID),
	PRIMARY KEY(UID, GID)
);

CREATE TABLE slog (
	LOGID INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
	UID INT,
	LOG VARCHAR(64),
	LOG_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ship (
	SUD INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
	UID INT,
	GID TINYINT,
	ATK INT,
	FOREIGN KEY (UID, GID) REFERENCES user(UID, GID)
);

/* Shard Procedure */

/* Add User in Shard DB */
DROP PROCEDURE IF EXISTS ADDUSER_SHARD;
DELIMITER &&
CREATE PROCEDURE ADDUSER_SHARD(IN UID INT, IN GID TINYINT)
	BEGIN
		DECLARE idx INT DEFAULT 0;
		START TRANSACTION;
		INSERT INTO user VALUES(UID, GID);
		
		WHILE idx < 10 DO
			INSERT INTO ship(UID, GID, ATK) VALUES(UID, GID, ROUND((RAND() * (100-5))+5));
			SET idx = idx + 1;
		END WHILE;
		COMMIT;
	END &&
DELIMITER ;

/* Logging */
DROP PROCEDURE IF EXISTS LOG;
DELIMITER $$
CREATE PROCEDURE LOG(IN UID INT, IN LOGTXT VARCHAR(64))
	BEGIN
		INSERT INTO slog(UID, LOG, LOG_TIME) VALUES (UID, LOGTXT, current_time());
	END $$
DELIMITER ;