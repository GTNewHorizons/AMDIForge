/*
SQLyog Ultimate v12.5.1 (64 bit)
MySQL - 5.7.20 : Database - mcdata
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`mcdata` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `mcdata`;

/*Table structure for table `dimensionref` */

DROP TABLE IF EXISTS `dimensionref`;

CREATE TABLE `dimensionref` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `DimName` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8;

/*Table structure for table `graves` */

DROP TABLE IF EXISTS `graves`;

CREATE TABLE `graves` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `playerID` int(11) NOT NULL,
  `graveDim` int(11) NOT NULL,
  `graveFullPath` text NOT NULL,
  `gravedata` json NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_graves_mcuserprofiles` (`playerID`),
  KEY `FK_graves_dimensionref` (`graveDim`),
  CONSTRAINT `FK_graves_dimensionref` FOREIGN KEY (`graveDim`) REFERENCES `dimensionref` (`ID`),
  CONSTRAINT `FK_graves_mcuserprofiles` FOREIGN KEY (`playerID`) REFERENCES `mcuserprofiles` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8;

/*Table structure for table `itemref` */

DROP TABLE IF EXISTS `itemref`;

CREATE TABLE `itemref` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ItemID` int(11) NOT NULL DEFAULT '0',
  `Name` text NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `lookupcodes` */

DROP TABLE IF EXISTS `lookupcodes`;

CREATE TABLE `lookupcodes` (
  `ID` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `code` char(10) NOT NULL,
  `created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `targetUID` char(36) NOT NULL,
  `targetName` varchar(100) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

/*Table structure for table `mcprofiles` */

DROP TABLE IF EXISTS `mcprofiles`;

CREATE TABLE `mcprofiles` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `playerID` int(11) NOT NULL,
  `PlayerDat` json NOT NULL,
  `crc` char(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`),
  KEY `FK_mcjsondata_mcuserprofiles` (`playerID`),
  CONSTRAINT `FK_mcjsondata_mcuserprofiles` FOREIGN KEY (`playerID`) REFERENCES `mcuserprofiles` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3066 DEFAULT CHARSET=utf8;

/*Table structure for table `mcstats` */

DROP TABLE IF EXISTS `mcstats`;

CREATE TABLE `mcstats` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `playerID` int(11) NOT NULL,
  `statsJson` json NOT NULL,
  `crc` char(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`),
  KEY `FK__mcuserprofiles` (`playerID`),
  CONSTRAINT `FK__mcuserprofiles` FOREIGN KEY (`playerID`) REFERENCES `mcuserprofiles` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=904 DEFAULT CHARSET=utf8;

/*Table structure for table `mcuserprofiles` */

DROP TABLE IF EXISTS `mcuserprofiles`;

CREATE TABLE `mcuserprofiles` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `UUID` varchar(36) NOT NULL,
  `LastName` varchar(50) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UUID` (`UUID`)
) ENGINE=InnoDB AUTO_INCREMENT=4495 DEFAULT CHARSET=utf8;

/* Procedure structure for procedure `show_graves_for_player` */

/*!50003 DROP PROCEDURE IF EXISTS  `show_graves_for_player` */;

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `show_graves_for_player`(IN `playerName` VARCHAR(50) CHARSET utf8, IN `playerUUID` VARCHAR(50) CHARSET utf8)
    READS SQL DATA
SELECT
  `dr`.`DimName`       AS `DimName`,
  `mcup`.`LastName`    AS `LastName`,
  FROM_UNIXTIME(SUBSTR(JSON_EXTRACT(`gr`.`gravedata`,'$.grave.Created'),1,(LENGTH(JSON_EXTRACT(`gr`.`gravedata`,'$.grave.Created')) - 3))) AS `Created`,
  JSON_EXTRACT(`gr`.`gravedata`,'$.grave.GraveLocation.X') AS `posX`,
  JSON_EXTRACT(`gr`.`gravedata`,'$.grave.GraveLocation.Y') AS `posY`,
  JSON_EXTRACT(`gr`.`gravedata`,'$.grave.GraveLocation.Z') AS `posZ`,
  JSON_EXTRACT(`gr`.`gravedata`,'$.grave.Placed') AS `Spawned`,
  `gr`.`graveFullPath` AS `GraveFile`
FROM ((`graves` `gr`
    JOIN `mcuserprofiles` `mcup`
      ON ((`mcup`.`ID` = `gr`.`playerID`)))
   JOIN `dimensionref` `dr`
     ON ((`dr`.`ID` = `gr`.`graveDim`)))
where mcup.LastName = playerName or mcup.UUID = playerUUID */$$
DELIMITER ;

/* Procedure structure for procedure `show_graves_for_player_enjin` */

/*!50003 DROP PROCEDURE IF EXISTS  `show_graves_for_player_enjin` */;

DELIMITER $$

/*!50003 CREATE DEFINER=`root`@`localhost` PROCEDURE `show_graves_for_player_enjin`(IN `pLookupCode` VARCHAR(10) CHARSET utf8)
    MODIFIES SQL DATA
begin
  
  declare tLookupUID varchar(36) default '';
  declare tLookupName varchar(100) default '';
  
  /* Delete codes older than 10 Minutes */
  delete from lookupcodes where lookupcodes.`created` < (date_sub(now(), interval 10 minute));
  /* Select code with usename/uid params to search for */
  select `lucd`.`targetUID`/*, `lucd`.`targetName` */into tLookupUID/*, tLookupName*/ from lookupcodes as lucd where `lucd`.`code` = pLookupCode;
  
  
  if not (tLookupUID = '')
  then
    SELECT
      `dr`.`DimName`       AS `DimName`,
      JSON_EXTRACT(`gr`.`gravedata`,'$.grave.GraveLocation.X') AS `X`,
      JSON_EXTRACT(`gr`.`gravedata`,'$.grave.GraveLocation.Y') AS `Y`,
      JSON_EXTRACT(`gr`.`gravedata`,'$.grave.GraveLocation.Z') AS `Z`,
      if(JSON_EXTRACT(`gr`.`gravedata`,'$.grave.Placed')=1,"Yes","No") AS `Grave spawned?`,
      DATE_ADD(FROM_UNIXTIME(SUBSTR(JSON_EXTRACT(`gr`.`gravedata`,'$.grave.Created'),1,(LENGTH(JSON_EXTRACT(`gr`.`gravedata`,'$.grave.Created')) - 3))), INTERVAL 14 DAY) AS `Expires at`
    FROM ((`graves` `gr`
      JOIN `mcuserprofiles` `mcup`
        ON ((`mcup`.`ID` = `gr`.`playerID`)))
     JOIN `dimensionref` `dr`
       ON ((`dr`.`ID` = `gr`.`graveDim`)))
    where `mcup`.`UUID` = tLookupUID
    ORDER BY FROM_UNIXTIME(SUBSTR(JSON_EXTRACT(`gr`.`gravedata`,'$.grave.Created'),1,(LENGTH(JSON_EXTRACT(`gr`.`gravedata`,'$.grave.Created')) - 3))) ASC;
  else
   select 'Code unknown or expired' as 'Result';
  end if;
  end */$$
DELIMITER ;

/*View structure for view show_latest_graves */

/*!50001 DROP TABLE IF EXISTS `show_latest_graves` */;
/*!50001 DROP VIEW IF EXISTS `show_latest_graves` */;

/*!50001 CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `show_latest_graves` AS select `dr`.`DimName` AS `DimName`,`mcup`.`LastName` AS `LastName`,from_unixtime(substr(json_extract(`gr`.`gravedata`,'$.grave.Created'),1,(length(json_extract(`gr`.`gravedata`,'$.grave.Created')) - 3))) AS `Created`,json_extract(`gr`.`gravedata`,'$.grave.GraveLocation.X') AS `posX`,json_extract(`gr`.`gravedata`,'$.grave.GraveLocation.Y') AS `posY`,json_extract(`gr`.`gravedata`,'$.grave.GraveLocation.Z') AS `posZ`,json_extract(`gr`.`gravedata`,'$.grave.Placed') AS `Spawned`,`gr`.`graveFullPath` AS `GraveFile` from ((`graves` `gr` join `mcuserprofiles` `mcup` on((`mcup`.`ID` = `gr`.`playerID`))) join `dimensionref` `dr` on((`dr`.`ID` = `gr`.`graveDim`))) order by from_unixtime(substr(json_extract(`gr`.`gravedata`,'$.grave.Created'),1,(length(json_extract(`gr`.`gravedata`,'$.grave.Created')) - 3))) desc */;

/*View structure for view users_offlinedays_hoursplayed */

/*!50001 DROP TABLE IF EXISTS `users_offlinedays_hoursplayed` */;
/*!50001 DROP VIEW IF EXISTS `users_offlinedays_hoursplayed` */;

/*!50001 CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `users_offlinedays_hoursplayed` AS select `mcup`.`UUID` AS `UUID`,`mcup`.`LastName` AS `LastName`,timestampdiff(DAY,now(),from_unixtime(substr(json_extract(`mcp`.`PlayerDat`,'$.grave.bukkit.lastPlayed'),1,(length(json_extract(`mcp`.`PlayerDat`,'$.grave.bukkit.lastPlayed')) - 3)))) AS `offlineDays`,from_unixtime(substr(json_extract(`mcp`.`PlayerDat`,'$.grave.bukkit.firstPlayed'),1,(length(json_extract(`mcp`.`PlayerDat`,'$.grave.bukkit.firstPlayed')) - 3))) AS `firstTimeJoined`,(json_extract(`mcs`.`statsJson`,'$.statplayOneMinute') / 72000) AS `hoursPlayed` from ((`mcprofiles` `mcp` join `mcstats` `mcs` on((`mcp`.`playerID` = `mcs`.`playerID`))) join `mcuserprofiles` `mcup` on((`mcup`.`ID` = `mcp`.`playerID`))) order by timestampdiff(DAY,now(),from_unixtime(substr(json_extract(`mcp`.`PlayerDat`,'$.grave.bukkit.lastPlayed'),1,(length(json_extract(`mcp`.`PlayerDat`,'$.grave.bukkit.lastPlayed')) - 3)))),(json_extract(`mcs`.`statsJson`,'$.statplayOneMinute') / 72000) desc */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
