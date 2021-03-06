/* CREATE USER 
CREATE USER 'jedi'@'%' IDENTIFIED BY 'jedi';
GRANT ALL PRIVILEGES ON  yoda.* to 'jedi';
GRANT EXECUTE ON yoda.* TO 'jedi';
*/



/*==========================================*/

DROP DATABASE IF EXISTS yoda;
CREATE DATABASE IF NOT EXISTS yoda;
USE yoda;		

DROP TABLE IF EXISTS ship;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS galaxy;
DROP TABLE IF EXISTS slog;
CREATE TABLE IF NOT EXISTS galaxy (
	GID TINYINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
	NAME VARCHAR(16),
	HP INT
);
CREATE TABLE IF NOT EXISTS user (
	UID INT NOT NULL,
	GID TINYINT,
	FOREIGN KEY (GID) REFERENCES galaxy(GID),
	PRIMARY KEY(UID, GID)
);
CREATE TABLE IF NOT EXISTS ship (
	SUD INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
	UID INT,
	GID TINYINT,
	ATK INT,
	FOREIGN KEY (UID, GID) REFERENCES user(UID, GID)
);
CREATE TABLE IF NOT EXISTS slog (
	LOGID INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
	UID INT,
	LOG VARCHAR(64),
	LOG_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


DROP PROCEDURE IF EXISTS INIT_TABLE;
DELIMITER $$
CREATE PROCEDURE INIT_TABLE()
	BEGIN
		DELETE FROM ship;
		DELETE FROM user;
		DELETE FROM galaxy;
		DELETE FROM slog;

		/* Initial Data set */
		INSERT INTO galaxy values(1, "MILKYWAY", 100000);
		INSERT INTO galaxy values(2, "ANDROMEDA", 100000);
		INSERT INTO galaxy values(3, "CENTAURI", 100000);
		INSERT INTO galaxy values(4, "MAGELLAN", 100000);
	END $$
DELIMITER ;
CALL INIT_TABLE();

/* Create table */


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


/* Get user's attack power */
DROP PROCEDURE IF EXISTS GET_ATTACK_POWER;
DELIMITER &&
CREATE PROCEDURE GET_ATTACK_POWER(IN PUID INT, OUT RATK INT)
	BEGIN
		SELECT sum(ATK) INTO RATK FROM ship WHERE UID = PUID;
	END &&
DELIMITER ;

/* Logging */
DROP PROCEDURE IF EXISTS LOG;
DELIMITER $$
CREATE PROCEDURE LOG(IN PUID INT, IN PGID TINYINT, IN DAMAGE INT, OUT LOGTXT VARCHAR(64))
	BEGIN
		DECLARE GNAME VARCHAR(16);
		START TRANSACTION;
			SELECT NAME INTO GNAME FROM galaxy WHERE GID = PGID;
			SET LOGTXT = CONCAT("USER ", PUID, " ATTACKED ", GNAME, "! ", GNAME, " damaged ", DAMAGE, "!");
			INSERT INTO slog(UID, LOG, LOG_TIME) VALUES (PUID, LOGTXT, current_time());
		COMMIT;
	END $$
DELIMITER ;

/* Galaxy data */
DROP PROCEDURE IF EXISTS GET_DATA_FOR_WEB;
DELIMITER $$
CREATE PROCEDURE GET_DATA_FOR_WEB(IN PGID INT, OUT RNAME VARCHAR(16), OUT RHP INT)
	BEGIN
		SELECT NAME, HP INTO RNAME, RHP FROM galaxy WHERE GID = PGID;
	END $$
DELIMITER ;

/* Attack */
DROP PROCEDURE IF EXISTS ATTACK;
DELIMITER &&
CREATE PROCEDURE ATTACK(IN PGID TINYINT, IN PATK INT, OUT RHP INT)
	BEGIN
		START TRANSACTION;
		UPDATE galaxy SET HP = HP-PATK WHERE GID = PGID;
		SELECT HP INTO RHP FROM galaxy WHERE GID = PGID;
		COMMIT;
	END &&
DELIMITER ;

SHOW PROCEDURE STATUS;