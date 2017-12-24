DROP DATABASE IF EXISTS `lod`;

CREATE DATABASE `lod` CHARSET=UTF8;

use lod;

-- http://stackoverflow.com/questions/15738013/making-unique-key-case-insensitive


-- So Start MySQL like this:  mysql -u username -p --local-infile

-- MySQL dump 10.13  Distrib 5.1.62, for apple-darwin10.3.0 (i386)
--
-- Host: localhost    Database: p2p
-- ------------------------------------------------------
-- Server version	5.1.62

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;



-- mysql -u username -p --local-infile

--
-- Table structure for table `link`
--
DROP TABLE IF EXISTS `link`;

CREATE TABLE `link` (
  `uri1` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `uri2` varchar(255) character set utf8 collate utf8_bin NOT NULL,
   PRIMARY KEY (`uri1`,`uri2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `domain`
--
LOCK TABLES `link` WRITE;
UNLOCK TABLES;


--
-- Table structure for table `domain`
--
DROP TABLE IF EXISTS `domain`;

CREATE TABLE `domain` (
  `seed` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `uri` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `domain` varchar(255) NOT NULL,
  `iteration` int NOT NULL,
   PRIMARY KEY (`seed`,`uri`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `domain`
--
LOCK TABLES `domain` WRITE;
UNLOCK TABLES;


--
-- Table structure for table `evaluation`
--
DROP TABLE IF EXISTS `evaluation`;

CREATE TABLE `evaluation` (
  `uri` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `correct` varchar(255) NOT NULL,
  `incorrect` varchar(255) NOT NULL,
  `sim` varchar(255) NOT NULL,
  `userid` int NOT NULL,
   PRIMARY KEY (`uri`,`userid`,`sim`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `evaluation`
--
LOCK TABLES `evaluation` WRITE;
UNLOCK TABLES;

--
-- Table structure for table `prediction`
--
DROP TABLE IF EXISTS `prediction`;

CREATE TABLE `prediction` (
  `seed` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `uri` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `evaluationlabel` varchar(255) NOT NULL,
  `predictedlabel` varchar(255) NOT NULL,
  `sim` varchar(255) NOT NULL,
  `score` DOUBLE NOT NULL,
  `userid` int NOT NULL,
   PRIMARY KEY (`seed`,`uri`,`userid`,`sim`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
--
-- Dumping data for table `prediction`
--
LOCK TABLES `prediction` WRITE;
UNLOCK TABLES;

--
-- Table structure for table `related`
--
DROP TABLE IF EXISTS `related`;


CREATE TABLE `related` (
  `uri1` varchar(255) NOT NULL,
  `uri2` varchar(255) NOT NULL,
  `userid` int NOT NULL,
  PRIMARY KEY (`uri1`,`uri2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `semantic`
--

LOCK TABLES `related` WRITE;
UNLOCK TABLES;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;

CREATE TABLE `book` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uri` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Dumping data for table `book`
--

LOCK TABLES `book` WRITE;

UNLOCK TABLES;


DROP TABLE IF EXISTS `music`;

CREATE TABLE `music` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uri` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Dumping data for table `music`
--

LOCK TABLES `music` WRITE;

UNLOCK TABLES;


DROP TABLE IF EXISTS `movie`;

CREATE TABLE `movie` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uri` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Dumping data for table `movie`
--

LOCK TABLES `movie` WRITE;

UNLOCK TABLES;



DROP TABLE IF EXISTS `movie_like`;

CREATE TABLE `movie_like` (
  `userid` bigint(20) NOT NULL,
  `movieid` bigint(20) NOT NULL,
  PRIMARY KEY (`userid`,`movieid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Dumping data for table `book_like`
--

LOCK TABLES `movie_like` WRITE;

UNLOCK TABLES;


DROP TABLE IF EXISTS `book_like`;

CREATE TABLE `book_like` (
  `userid` bigint(20) NOT NULL,
  `bookid` bigint(20) NOT NULL,
  PRIMARY KEY (`userid`,`bookid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Dumping data for table `movie`
--

LOCK TABLES `book_like` WRITE;

UNLOCK TABLES;



DROP TABLE IF EXISTS `music_like`;

CREATE TABLE `music_like` (
  `userid` bigint(20) NOT NULL,
  `musicid` bigint(20) NOT NULL,
  PRIMARY KEY (`userid`,`musicid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Dumping data for table `movie`
--

LOCK TABLES `music_like` WRITE;

UNLOCK TABLES;


DROP TABLE IF EXISTS `music_test`;

CREATE TABLE `music_test` (
  `musicid` bigint(20) NOT NULL,
  PRIMARY KEY (`musicid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Dumping data for table `movie`
--

LOCK TABLES `music_test` WRITE;
UNLOCK TABLES;



DROP TABLE IF EXISTS `book_test`;

CREATE TABLE `book_test` (
  `bookid` bigint(20) NOT NULL,
  PRIMARY KEY (`bookid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Dumping data for table `movie`
--

LOCK TABLES `book_test` WRITE;

UNLOCK TABLES;




DROP TABLE IF EXISTS `movie_test`;

CREATE TABLE `movie_test` (
  `movieid` bigint(20) NOT NULL,
   PRIMARY KEY (`movieid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;	


--
-- Dumping data for table `movie`
--

LOCK TABLES `movie_test` WRITE;
UNLOCK TABLES;




--
-- Dumping data for table `movie`
--

DROP TABLE IF EXISTS `semantic`;


CREATE TABLE `semantic` (
  `uri1` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `uri2` varchar(255) character set utf8 collate utf8_bin NOT NULL,
  `sim` varchar(10) NOT NULL,
  `score` DOUBLE NOT NULL,
  PRIMARY KEY (`uri1`,`uri2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `semantic`
--

LOCK TABLES `semantic` WRITE;
UNLOCK TABLES;



-- http://stackoverflow.com/questions/12485942/mysql-load-data-infile-cant-get-the-stat-of-file


-- SET @path = 'C:/Users/fdurao/workspace/LODWEB/';
/*

LOAD DATA LOCAL INFILE '/home/fred/workspace/LODWEB/sql/items_books.txt'  INTO TABLE lod.book FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE '/home/fred/workspace/LODWEB/sql/items_musics.txt' INTO TABLE lod.music FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE '/home/fred/workspace/LODWEB/sql/items_movies.txt' INTO TABLE lod.movie FIELDS TERMINATED BY ',';


LOAD DATA LOCAL INFILE '/home/fred/workspace/LODWEB/sql/test_set_books.txt' INTO TABLE lod.book_test FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE '/home/fred/workspace/LODWEB/sql/test_set_movies.txt' INTO TABLE lod.movie_test FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE '/home/fred/workspace/LODWEB/sql/test_set_musics.txt' INTO TABLE lod.music_test FIELDS TERMINATED BY ',';



LOAD DATA LOCAL INFILE '/home/fred/workspace/LODWEB/sql/training_likes_books.txt' INTO TABLE lod.book_like FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE '/home/fred/workspace/LODWEB/sql/training_likes_movies.txt' INTO TABLE lod.movie_like FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE '/home/fred/workspace/LODWEB/sql/training_likes_musics.txt' INTO TABLE lod.music_like FIELDS TERMINATED BY ',';
*/

LOAD DATA LOCAL INFILE 'C:/Users/fdurao/workspace/LODWEB/sql/items_books.txt'  INTO TABLE lod.book  FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE 'C:/Users/fdurao/workspace/LODWEB/sql/items_musics.txt' INTO TABLE lod.music FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE 'C:/Users/fdurao/workspace/LODWEB/sql/items_movies.txt' INTO TABLE lod.movie FIELDS TERMINATED BY ',';


LOAD DATA LOCAL INFILE 'C:/Users/fdurao/workspace/LODWEB/sql/test_set_books.txt' INTO TABLE lod.book_test FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE 'C:/Users/fdurao/workspace/LODWEB/sql/test_set_movies.txt' INTO TABLE lod.movie_test FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE 'C:/Users/fdurao/workspace/LODWEB/sql/test_set_musics.txt' INTO TABLE lod.music_test FIELDS TERMINATED BY ',';



LOAD DATA LOCAL INFILE 'C:/Users/fdurao/workspace/LODWEB/sql/training_likes_books.txt' INTO TABLE lod.book_like FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE 'C:/Users/fdurao/workspace/LODWEB/sql/training_likes_movies.txt' INTO TABLE lod.movie_like FIELDS TERMINATED BY ',';

LOAD DATA LOCAL INFILE 'C:/Users/fdurao/workspace/LODWEB/sql/training_likes_musics.txt' INTO TABLE lod.music_like FIELDS TERMINATED BY ',';




SET SQL_SAFE_UPDATES = 0;
UPDATE lod.book SET lod.book.uri = TRIM(TRAILING '\r' FROM lod.book.uri);
UPDATE lod.music SET lod.music.uri = TRIM(TRAILING '\r' FROM lod.music.uri);
UPDATE lod.movie SET lod.movie.uri = TRIM(TRAILING '\r' FROM lod.movie.uri);
SET SQL_SAFE_UPDATES = 1;

SET SQL_SAFE_UPDATES=0;
DELETE FROM `lod`.`prediction`;
SET SQL_SAFE_UPDATES=1;