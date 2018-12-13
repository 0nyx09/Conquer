-- MySQL dump 10.13  Distrib 8.0.12, for Win64 (x86_64)
--
-- Host: localhost    Database: imdb_example
-- ------------------------------------------------------
-- Server version	8.0.12

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `movie_actor_example`
--

DROP TABLE IF EXISTS `movie_actor_example`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `movie_actor_example` (
  `actor` int(11) NOT NULL DEFAULT '0',
  `movie` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`actor`,`movie`),
  KEY `movie` (`movie`),
  CONSTRAINT `movie_actor_example_ibfk_1` FOREIGN KEY (`actor`) REFERENCES `imdb`.`member` (`id`),
  CONSTRAINT `movie_actor_example_ibfk_2` FOREIGN KEY (`movie`) REFERENCES `imdb`.`movie` (`id`),
  CONSTRAINT `movie_actor_example_ibfk_3` FOREIGN KEY (`actor`) REFERENCES `member_example` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movie_actor_example`
--

LOCK TABLES `movie_actor_example` WRITE;
/*!40000 ALTER TABLE `movie_actor_example` DISABLE KEYS */;
INSERT INTO `movie_actor_example` VALUES (1234,48750),(65159,152216),(2042,397241),(522884,1109104),(808485,1382115),(289972,2246373),(139632,5811388),(361127,6259478);
/*!40000 ALTER TABLE `movie_actor_example` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-12-12 19:31:28
