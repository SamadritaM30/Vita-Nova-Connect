-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: medical_aid_tracker
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ambulance_services`
--

DROP TABLE IF EXISTS `ambulance_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ambulance_services` (
  `ambulance_id` int NOT NULL AUTO_INCREMENT,
  `driver_name` varchar(255) NOT NULL,
  `contact_number` varchar(20) NOT NULL,
  `last_location` text NOT NULL,
  `availability_status` enum('Available','Unavailable') NOT NULL DEFAULT 'Available',
  `patient_id` int DEFAULT NULL,
  PRIMARY KEY (`ambulance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ambulance_services`
--

LOCK TABLES `ambulance_services` WRITE;
/*!40000 ALTER TABLE `ambulance_services` DISABLE KEYS */;
INSERT INTO `ambulance_services` VALUES (1,'Rajesh Sharma','98765-43001','Paltan Bazar','Available',3),(2,'Amit Das','98765-43002','Dispur','Unavailable',NULL),(3,'Sunil Roy','98765-43003','Beltola','Available',4),(4,'Ravi Verma','98765-43004','Zoo Road','Available',NULL),(5,'Pankaj Gupta','98765-43005','Maligaon','Unavailable',5),(6,'Arun Kumar','98765-43006','Silpukhuri','Available',6),(7,'Rahul Goswami','98765-43007','Hatigaon','Available',NULL),(8,'Manoj Paul','98765-43008','Ganeshguri','Unavailable',7),(9,'Sandeep Boro','98765-43009','Six Mile','Available',NULL),(10,'Nikhil Choudhury','98765-43010','Jalukbari','Available',8),(11,'Kunal Nath','98765-43011','Panbazar','Unavailable',NULL),(12,'Deepak Deka','98765-43012','Narengi','Available',9),(13,'Ajay Das','98765-43013','Pandu','Available',NULL),(14,'Bikash Hazarika','98765-43014','Bhetapara','Unavailable',NULL),(15,'Rohit Sharma','98765-43015','Ulubari','Available',NULL),(16,'Anupam Medhi','98765-43016','Bamunimaidan','Available',NULL),(17,'Kiran Boro','98765-43017','Athgaon','Unavailable',NULL),(18,'Vikram Saikia','98765-43018','Kahilipara','Available',NULL),(19,'Sushil Das','98765-43019','Lachit Nagar','Available',NULL),(20,'Partha Sarathi','98765-43020','Rehabari','Unavailable',NULL),(21,'Harish Mahanta','98765-43021','Boragaon','Available',NULL),(22,'Debashish Dutta','98765-43022','Silchar Road','Available',NULL),(23,'Anil Choudhury','98765-43023','Chandmari','Unavailable',NULL),(24,'Jiten Kalita','98765-43024','VIP Road','Available',NULL),(25,'Mohan Dutta','98765-43025','Bhangagarh','Available',NULL),(26,'Gautam Sarma','98765-43026','GS Road','Unavailable',NULL),(27,'Hemanta Pathak','98765-43027','Santipur','Available',NULL),(28,'Tarun Bhattacharya','98765-43028','Fatasil Ambari','Available',NULL),(29,'Kishore Das','98765-43029','Lokhra','Unavailable',NULL),(30,'Jogen Rajbongshi','98765-43030','Azara','Available',NULL);
/*!40000 ALTER TABLE `ambulance_services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `appointments`
--

DROP TABLE IF EXISTS `appointments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointments` (
  `appointment_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int DEFAULT NULL,
  `doctor_id` int DEFAULT NULL,
  `hospital_id` int DEFAULT NULL,
  `appointment_date` date NOT NULL,
  `appointment_time` time NOT NULL,
  `status` enum('Booked','Completed','Cancelled') NOT NULL DEFAULT 'Booked',
  PRIMARY KEY (`appointment_id`),
  KEY `patient_id` (`patient_id`),
  KEY `doctor_id` (`doctor_id`),
  KEY `hospital_id` (`hospital_id`),
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`) ON DELETE CASCADE,
  CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`) ON DELETE CASCADE,
  CONSTRAINT `appointments_ibfk_3` FOREIGN KEY (`hospital_id`) REFERENCES `hospitals` (`hospital_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointments`
--

LOCK TABLES `appointments` WRITE;
/*!40000 ALTER TABLE `appointments` DISABLE KEYS */;
INSERT INTO `appointments` VALUES (3,5,4,2,'2004-08-30','11:00:00','Booked'),(4,4,11,4,'2025-04-05','11:00:00','Booked');
/*!40000 ALTER TABLE `appointments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bed_availability`
--

DROP TABLE IF EXISTS `bed_availability`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bed_availability` (
  `bed_id` int NOT NULL AUTO_INCREMENT,
  `hospital_id` int DEFAULT NULL,
  `ward_type` enum('ICU','General') NOT NULL,
  `status` enum('Available','Occupied') NOT NULL DEFAULT 'Available',
  PRIMARY KEY (`bed_id`),
  KEY `hospital_id` (`hospital_id`),
  CONSTRAINT `bed_availability_ibfk_1` FOREIGN KEY (`hospital_id`) REFERENCES `hospitals` (`hospital_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=405 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bed_availability`
--

LOCK TABLES `bed_availability` WRITE;
/*!40000 ALTER TABLE `bed_availability` DISABLE KEYS */;
INSERT INTO `bed_availability` VALUES (5,1,'ICU','Available'),(6,1,'General','Occupied'),(7,2,'ICU','Occupied'),(8,2,'General','Available'),(9,3,'ICU','Available'),(10,3,'General','Available'),(11,4,'ICU','Occupied'),(12,4,'General','Available'),(13,5,'ICU','Available'),(14,5,'General','Occupied'),(15,6,'ICU','Available'),(16,6,'General','Occupied'),(17,7,'ICU','Occupied'),(18,7,'General','Available'),(19,8,'ICU','Available'),(20,8,'General','Available'),(21,9,'ICU','Available'),(22,9,'General','Occupied'),(23,10,'ICU','Occupied'),(24,10,'General','Available'),(25,11,'ICU','Available'),(26,11,'General','Available'),(27,12,'ICU','Occupied'),(28,12,'General','Occupied'),(29,13,'ICU','Available'),(30,13,'General','Occupied'),(31,14,'ICU','Available'),(32,14,'General','Available'),(33,15,'ICU','Occupied'),(34,15,'General','Available'),(35,16,'ICU','Available'),(36,16,'General','Occupied'),(37,17,'ICU','Occupied'),(38,17,'General','Available'),(39,18,'ICU','Available'),(40,18,'General','Available'),(41,19,'ICU','Available'),(42,19,'General','Occupied'),(43,20,'ICU','Occupied'),(44,20,'General','Available'),(45,21,'ICU','Available'),(46,21,'General','Available'),(47,22,'ICU','Occupied'),(48,22,'General','Occupied'),(49,23,'ICU','Available'),(50,23,'General','Occupied'),(51,24,'ICU','Available'),(52,24,'General','Available'),(53,25,'ICU','Occupied'),(54,25,'General','Available'),(55,26,'ICU','Available'),(56,26,'General','Occupied'),(57,27,'ICU','Occupied'),(58,27,'General','Available'),(59,28,'ICU','Available'),(60,28,'General','Available'),(61,29,'ICU','Available'),(62,29,'General','Occupied'),(63,30,'ICU','Occupied'),(64,30,'General','Available'),(65,31,'ICU','Available'),(66,31,'General','Available'),(67,32,'ICU','Occupied'),(68,32,'General','Occupied'),(69,33,'ICU','Available'),(70,33,'General','Occupied'),(71,34,'ICU','Available'),(72,34,'General','Available'),(73,35,'ICU','Occupied'),(74,35,'General','Available'),(75,36,'ICU','Available'),(76,36,'General','Occupied'),(77,37,'ICU','Occupied'),(78,37,'General','Available'),(79,38,'ICU','Available'),(80,38,'General','Available'),(81,39,'ICU','Available'),(82,39,'General','Occupied'),(83,40,'ICU','Occupied'),(84,40,'General','Available'),(85,41,'ICU','Available'),(86,41,'General','Available'),(87,42,'ICU','Occupied'),(88,42,'General','Occupied'),(89,43,'ICU','Available'),(90,43,'General','Occupied'),(91,44,'ICU','Available'),(92,44,'General','Available'),(93,45,'ICU','Occupied'),(94,45,'General','Available'),(95,46,'ICU','Available'),(96,46,'General','Occupied'),(97,47,'ICU','Occupied'),(98,47,'General','Available'),(99,48,'ICU','Available'),(100,48,'General','Available'),(101,49,'ICU','Available'),(102,49,'General','Occupied'),(103,50,'ICU','Occupied'),(104,50,'General','Available'),(105,51,'ICU','Available'),(106,51,'General','Available'),(107,52,'ICU','Occupied'),(108,52,'General','Occupied'),(109,53,'ICU','Available'),(110,53,'General','Occupied'),(111,54,'ICU','Available'),(112,54,'General','Available'),(113,55,'ICU','Occupied'),(114,55,'General','Available'),(115,56,'ICU','Available'),(116,56,'General','Occupied'),(117,57,'ICU','Occupied'),(118,57,'General','Available'),(119,58,'ICU','Available'),(120,58,'General','Available'),(121,59,'ICU','Available'),(122,59,'General','Occupied'),(123,60,'ICU','Occupied'),(124,60,'General','Available'),(125,61,'ICU','Available'),(126,61,'General','Available'),(127,62,'ICU','Occupied'),(128,62,'General','Occupied'),(129,63,'ICU','Available'),(130,63,'General','Occupied'),(131,64,'ICU','Available'),(132,64,'General','Available'),(133,65,'ICU','Occupied'),(134,65,'General','Available'),(135,66,'ICU','Available'),(136,66,'General','Occupied'),(137,67,'ICU','Occupied'),(138,67,'General','Available'),(139,68,'ICU','Available'),(140,68,'General','Available'),(141,69,'ICU','Available'),(142,69,'General','Occupied'),(143,70,'ICU','Occupied'),(144,70,'General','Available'),(145,71,'ICU','Available'),(146,71,'General','Available'),(147,72,'ICU','Occupied'),(148,72,'General','Occupied'),(149,73,'ICU','Available'),(150,73,'General','Occupied'),(151,74,'ICU','Available'),(152,74,'General','Available'),(153,75,'ICU','Occupied'),(154,75,'General','Available'),(155,76,'ICU','Available'),(156,76,'General','Occupied'),(157,77,'ICU','Occupied'),(158,77,'General','Available'),(159,78,'ICU','Available'),(160,78,'General','Available'),(161,79,'ICU','Available'),(162,79,'General','Occupied'),(163,80,'ICU','Occupied'),(164,80,'General','Available'),(165,81,'ICU','Available'),(166,81,'General','Available'),(167,82,'ICU','Occupied'),(168,82,'General','Occupied'),(169,83,'ICU','Available'),(170,83,'General','Occupied'),(171,84,'ICU','Available'),(172,84,'General','Available'),(173,85,'ICU','Occupied'),(174,85,'General','Available'),(175,86,'ICU','Available'),(176,86,'General','Occupied'),(177,87,'ICU','Occupied'),(178,87,'General','Available'),(179,88,'ICU','Available'),(180,88,'General','Available'),(181,89,'ICU','Available'),(182,89,'General','Occupied'),(183,90,'ICU','Occupied'),(184,90,'General','Available'),(185,91,'ICU','Available'),(186,91,'General','Available'),(187,92,'ICU','Occupied'),(188,92,'General','Occupied'),(189,93,'ICU','Available'),(190,93,'General','Occupied'),(191,94,'ICU','Available'),(192,94,'General','Available'),(193,95,'ICU','Occupied'),(194,95,'General','Available'),(195,96,'ICU','Available'),(196,96,'General','Occupied'),(197,97,'ICU','Occupied'),(198,97,'General','Available'),(199,98,'ICU','Available'),(200,98,'General','Available'),(201,99,'ICU','Available'),(202,99,'General','Occupied'),(203,100,'ICU','Occupied'),(204,100,'General','Available'),(205,1,'ICU','Available'),(206,1,'General','Occupied'),(207,2,'ICU','Occupied'),(208,2,'General','Available'),(209,3,'ICU','Available'),(210,3,'General','Available'),(211,4,'ICU','Occupied'),(212,4,'General','Available'),(213,5,'ICU','Available'),(214,5,'General','Occupied'),(215,6,'ICU','Available'),(216,6,'General','Occupied'),(217,7,'ICU','Occupied'),(218,7,'General','Available'),(219,8,'ICU','Available'),(220,8,'General','Available'),(221,9,'ICU','Available'),(222,9,'General','Occupied'),(223,10,'ICU','Occupied'),(224,10,'General','Available'),(225,11,'ICU','Available'),(226,11,'General','Available'),(227,12,'ICU','Occupied'),(228,12,'General','Occupied'),(229,13,'ICU','Available'),(230,13,'General','Occupied'),(231,14,'ICU','Available'),(232,14,'General','Available'),(233,15,'ICU','Occupied'),(234,15,'General','Available'),(235,16,'ICU','Available'),(236,16,'General','Occupied'),(237,17,'ICU','Occupied'),(238,17,'General','Available'),(239,18,'ICU','Available'),(240,18,'General','Available'),(241,19,'ICU','Available'),(242,19,'General','Occupied'),(243,20,'ICU','Occupied'),(244,20,'General','Available'),(245,21,'ICU','Available'),(246,21,'General','Available'),(247,22,'ICU','Occupied'),(248,22,'General','Occupied'),(249,23,'ICU','Available'),(250,23,'General','Occupied'),(251,24,'ICU','Available'),(252,24,'General','Available'),(253,25,'ICU','Occupied'),(254,25,'General','Available'),(255,26,'ICU','Available'),(256,26,'General','Occupied'),(257,27,'ICU','Occupied'),(258,27,'General','Available'),(259,28,'ICU','Available'),(260,28,'General','Available'),(261,29,'ICU','Available'),(262,29,'General','Occupied'),(263,30,'ICU','Occupied'),(264,30,'General','Available'),(265,31,'ICU','Available'),(266,31,'General','Available'),(267,32,'ICU','Occupied'),(268,32,'General','Occupied'),(269,33,'ICU','Available'),(270,33,'General','Occupied'),(271,34,'ICU','Available'),(272,34,'General','Available'),(273,35,'ICU','Occupied'),(274,35,'General','Available'),(275,36,'ICU','Available'),(276,36,'General','Occupied'),(277,37,'ICU','Occupied'),(278,37,'General','Available'),(279,38,'ICU','Available'),(280,38,'General','Available'),(281,39,'ICU','Available'),(282,39,'General','Occupied'),(283,40,'ICU','Occupied'),(284,40,'General','Available'),(285,41,'ICU','Available'),(286,41,'General','Available'),(287,42,'ICU','Occupied'),(288,42,'General','Occupied'),(289,43,'ICU','Available'),(290,43,'General','Occupied'),(291,44,'ICU','Available'),(292,44,'General','Available'),(293,45,'ICU','Occupied'),(294,45,'General','Available'),(295,46,'ICU','Available'),(296,46,'General','Occupied'),(297,47,'ICU','Occupied'),(298,47,'General','Available'),(299,48,'ICU','Available'),(300,48,'General','Available'),(301,49,'ICU','Available'),(302,49,'General','Occupied'),(303,50,'ICU','Occupied'),(304,50,'General','Available'),(305,51,'ICU','Available'),(306,51,'General','Available'),(307,52,'ICU','Occupied'),(308,52,'General','Occupied'),(309,53,'ICU','Available'),(310,53,'General','Occupied'),(311,54,'ICU','Available'),(312,54,'General','Available'),(313,55,'ICU','Occupied'),(314,55,'General','Available'),(315,56,'ICU','Available'),(316,56,'General','Occupied'),(317,57,'ICU','Occupied'),(318,57,'General','Available'),(319,58,'ICU','Available'),(320,58,'General','Available'),(321,59,'ICU','Available'),(322,59,'General','Occupied'),(323,60,'ICU','Occupied'),(324,60,'General','Available'),(325,61,'ICU','Available'),(326,61,'General','Available'),(327,62,'ICU','Occupied'),(328,62,'General','Occupied'),(329,63,'ICU','Available'),(330,63,'General','Occupied'),(331,64,'ICU','Available'),(332,64,'General','Available'),(333,65,'ICU','Occupied'),(334,65,'General','Available'),(335,66,'ICU','Available'),(336,66,'General','Occupied'),(337,67,'ICU','Occupied'),(338,67,'General','Available'),(339,68,'ICU','Available'),(340,68,'General','Available'),(341,69,'ICU','Available'),(342,69,'General','Occupied'),(343,70,'ICU','Occupied'),(344,70,'General','Available'),(345,71,'ICU','Available'),(346,71,'General','Available'),(347,72,'ICU','Occupied'),(348,72,'General','Occupied'),(349,73,'ICU','Available'),(350,73,'General','Occupied'),(351,74,'ICU','Available'),(352,74,'General','Available'),(353,75,'ICU','Occupied'),(354,75,'General','Available'),(355,76,'ICU','Available'),(356,76,'General','Occupied'),(357,77,'ICU','Occupied'),(358,77,'General','Available'),(359,78,'ICU','Available'),(360,78,'General','Available'),(361,79,'ICU','Available'),(362,79,'General','Occupied'),(363,80,'ICU','Occupied'),(364,80,'General','Available'),(365,81,'ICU','Available'),(366,81,'General','Available'),(367,82,'ICU','Occupied'),(368,82,'General','Occupied'),(369,83,'ICU','Available'),(370,83,'General','Occupied'),(371,84,'ICU','Available'),(372,84,'General','Available'),(373,85,'ICU','Occupied'),(374,85,'General','Available'),(375,86,'ICU','Available'),(376,86,'General','Occupied'),(377,87,'ICU','Occupied'),(378,87,'General','Available'),(379,88,'ICU','Available'),(380,88,'General','Available'),(381,89,'ICU','Available'),(382,89,'General','Occupied'),(383,90,'ICU','Occupied'),(384,90,'General','Available'),(385,91,'ICU','Available'),(386,91,'General','Available'),(387,92,'ICU','Occupied'),(388,92,'General','Occupied'),(389,93,'ICU','Available'),(390,93,'General','Occupied'),(391,94,'ICU','Available'),(392,94,'General','Available'),(393,95,'ICU','Occupied'),(394,95,'General','Available'),(395,96,'ICU','Available'),(396,96,'General','Occupied'),(397,97,'ICU','Occupied'),(398,97,'General','Available'),(399,98,'ICU','Available'),(400,98,'General','Available'),(401,99,'ICU','Available'),(402,99,'General','Occupied'),(403,100,'ICU','Occupied'),(404,100,'General','Available');
/*!40000 ALTER TABLE `bed_availability` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctors`
--

DROP TABLE IF EXISTS `doctors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctors` (
  `doctor_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `specialization` varchar(255) NOT NULL,
  `availability_status` enum('Available','Unavailable') NOT NULL DEFAULT 'Available',
  `hospital_id` int DEFAULT NULL,
  `from_time` time NOT NULL DEFAULT '09:00:00',
  `to_time` time NOT NULL DEFAULT '17:00:00',
  PRIMARY KEY (`doctor_id`),
  KEY `hospital_id` (`hospital_id`),
  CONSTRAINT `doctors_ibfk_1` FOREIGN KEY (`hospital_id`) REFERENCES `hospitals` (`hospital_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctors`
--

LOCK TABLES `doctors` WRITE;
/*!40000 ALTER TABLE `doctors` DISABLE KEYS */;
INSERT INTO `doctors` VALUES (4,'Dr. Arjun Sharma','Cardiologist','Available',1,'08:00:00','14:00:00'),(5,'Dr. Nidhi Das','Neurologist','Unavailable',1,'10:00:00','16:00:00'),(6,'Dr. Rajesh Singh','Orthopedic','Available',2,'09:00:00','17:00:00'),(7,'Dr. Priya Mehta','Pediatrician','Available',2,'08:30:00','15:30:00'),(8,'Dr. Rohan Sen','Dermatologist','Unavailable',3,'11:00:00','18:00:00'),(9,'Dr. Sunita Rao','Gynecologist','Available',3,'07:00:00','14:00:00'),(10,'Dr. Vikram Choudhury','Oncologist','Available',4,'10:00:00','18:00:00'),(11,'Dr. Sneha Patil','ENT Specialist','Unavailable',4,'13:00:00','20:00:00'),(12,'Dr. Karan Bose','General Physician','Available',5,'06:00:00','12:00:00'),(13,'Dr. Simran Kaur','Psychiatrist','Unavailable',5,'14:00:00','21:00:00'),(14,'Dr. Aman Verma','Neurosurgeon','Available',6,'07:30:00','15:30:00'),(15,'Dr. Shruti Malhotra','Pulmonologist','Available',6,'09:00:00','16:00:00'),(16,'Dr. Sandeep Ghosh','Endocrinologist','Unavailable',7,'08:00:00','17:00:00'),(17,'Dr. Priyanka Banerjee','Nephrologist','Available',7,'10:00:00','18:00:00'),(18,'Dr. Abhishek Roy','Hematologist','Available',8,'06:00:00','14:00:00'),(19,'Dr. Kavita Nair','Urologist','Unavailable',8,'12:00:00','19:00:00'),(20,'Dr. Rakesh Jindal','Gastroenterologist','Available',9,'07:00:00','15:00:00'),(21,'Dr. Meenal Deshmukh','Ophthalmologist','Available',9,'09:00:00','17:00:00'),(22,'Dr. Aditya Pradhan','Radiologist','Unavailable',10,'08:00:00','16:00:00'),(23,'Dr. Alka Yadav','General Surgeon','Available',10,'10:00:00','18:00:00'),(24,'Dr. Sahil Kapoor','Cardiologist','Available',11,'07:30:00','14:30:00'),(25,'Dr. Ritu Bhardwaj','Neurologist','Unavailable',12,'13:00:00','20:00:00'),(26,'Dr. Dhruv Tandon','Orthopedic','Available',13,'08:00:00','17:00:00'),(27,'Dr. Anjali Iyer','Pediatrician','Available',14,'09:00:00','16:00:00'),(28,'Dr. Harsh Vardhan','Dermatologist','Unavailable',15,'10:00:00','18:00:00'),(29,'Dr. Manisha Rao','Gynecologist','Available',16,'07:00:00','15:00:00'),(30,'Dr. Varun Sinha','Oncologist','Available',17,'08:30:00','16:30:00'),(31,'Dr. Natasha Khanna','ENT Specialist','Unavailable',18,'12:00:00','19:00:00'),(32,'Dr. Chetan Mehta','General Physician','Available',19,'06:00:00','12:00:00'),(33,'Dr. Swati Verma','Psychiatrist','Unavailable',20,'14:00:00','21:00:00'),(34,'Dr. Vijay Saxena','Neurosurgeon','Available',21,'07:30:00','15:30:00'),(35,'Dr. Deepika Joshi','Pulmonologist','Available',22,'09:00:00','17:00:00'),(36,'Dr. Yashwant Patel','Endocrinologist','Unavailable',23,'08:00:00','16:00:00'),(37,'Dr. Shreya Dutta','Nephrologist','Available',24,'10:00:00','18:00:00'),(38,'Dr. Mohit Sharma','Hematologist','Available',25,'07:00:00','14:00:00'),(39,'Dr. Radhika Bose','Urologist','Unavailable',26,'11:00:00','19:00:00'),(40,'Dr. Dinesh Reddy','Gastroenterologist','Available',27,'08:30:00','16:30:00'),(41,'Dr. Snehal Pandey','Ophthalmologist','Available',28,'09:00:00','17:00:00'),(42,'Dr. Amitabh Singh','Radiologist','Unavailable',29,'10:00:00','18:00:00'),(43,'Dr. Komal Mahajan','General Surgeon','Available',30,'07:00:00','15:00:00'),(44,'Dr. Vinay Kumar','Cardiologist','Available',31,'08:00:00','16:00:00'),(45,'Dr. Ananya Chakraborty','Neurologist','Unavailable',32,'12:00:00','19:00:00'),(46,'Dr. Prakash Rao','Orthopedic','Available',33,'06:00:00','12:00:00'),(47,'Dr. Rekha Reddy','Pediatrician','Available',34,'14:00:00','21:00:00'),(48,'Dr. Saurav Banerjee','Dermatologist','Unavailable',35,'07:30:00','15:30:00'),(49,'Dr. Megha Verma','Gynecologist','Available',36,'09:00:00','17:00:00'),(50,'Dr. Rohan Malhotra','Oncologist','Available',37,'08:00:00','16:00:00'),(51,'Dr. Tanya Kapoor','ENT Specialist','Unavailable',38,'10:00:00','18:00:00'),(52,'Dr. Jayant Sharma','General Physician','Available',39,'07:00:00','14:00:00'),(53,'Dr. Roshni Gupta','Psychiatrist','Unavailable',40,'11:00:00','19:00:00'),(54,'Dr. Nikhil Chopra','Neurosurgeon','Available',41,'08:30:00','16:30:00'),(55,'Dr. Nivedita Sinha','Pulmonologist','Available',42,'09:00:00','17:00:00'),(56,'Dr. Sushant Das','Endocrinologist','Unavailable',43,'10:00:00','18:00:00'),(57,'Dr. Pooja Mehta','Nephrologist','Available',44,'07:00:00','15:00:00'),(58,'Dr. Kunal Saxena','Hematologist','Available',45,'08:00:00','16:00:00'),(59,'Dr. Sanjana Iyer','Urologist','Unavailable',46,'12:00:00','19:00:00'),(60,'Dr. Rajan Verma','Gastroenterologist','Available',47,'06:00:00','12:00:00'),(61,'Dr. Ishita Sen','Ophthalmologist','Available',48,'14:00:00','21:00:00'),(62,'Dr. Bharat Jha','Radiologist','Unavailable',49,'07:30:00','15:30:00'),(63,'Dr. Sneha Malhotra','General Surgeon','Available',50,'09:00:00','17:00:00'),(64,'Dr. Harish Choudhury','Cardiologist','Available',51,'08:00:00','16:00:00'),(65,'Dr. Arpita Ghosh','Neurologist','Unavailable',52,'10:00:00','18:00:00'),(66,'Dr. Tanmay Tiwari','Orthopedic','Available',53,'07:00:00','14:00:00'),(67,'Dr. Sangeeta Iyer','Pediatrician','Available',54,'11:00:00','19:00:00'),(68,'Dr. Subodh Sharma','Dermatologist','Unavailable',55,'08:30:00','16:30:00'),(69,'Dr. Mahima Patel','Gynecologist','Available',56,'09:00:00','17:00:00'),(70,'Dr. Deepak Rana','Oncologist','Available',57,'10:00:00','18:00:00'),(71,'Dr. Rhea Sen','ENT Specialist','Unavailable',58,'07:00:00','15:00:00'),(72,'Dr. Pranav Mishra','General Physician','Available',59,'08:00:00','16:00:00'),(73,'Dr. Anjali Chopra','Psychiatrist','Unavailable',60,'12:00:00','19:00:00'),(74,'Dr. Karthik Nair','Neurosurgeon','Available',61,'06:00:00','12:00:00'),(75,'Dr. Yamini Vyas','Pulmonologist','Available',62,'14:00:00','21:00:00'),(76,'Dr. Sidharth Goel','Endocrinologist','Unavailable',63,'07:30:00','15:30:00'),(77,'Dr. Ayesha Khan','Nephrologist','Available',64,'09:00:00','17:00:00'),(78,'Dr. Ramesh Malhotra','Hematologist','Available',65,'08:00:00','16:00:00'),(79,'Dr. Priyanka Bhardwaj','Urologist','Unavailable',66,'10:00:00','18:00:00'),(80,'Dr. Naveen Sinha','Gastroenterologist','Available',67,'07:00:00','14:00:00'),(81,'Dr. Simran Roy','Ophthalmologist','Available',68,'11:00:00','19:00:00'),(82,'Dr. Ashish Joshi','Radiologist','Unavailable',69,'08:30:00','16:30:00'),(83,'Dr. Pankaj Sharma','General Surgeon','Available',70,'09:00:00','17:00:00'),(84,'Dr. Rituparna Das','Cardiologist','Available',71,'10:00:00','18:00:00'),(85,'Dr. Manoj Tiwari','Neurologist','Unavailable',72,'07:00:00','15:00:00'),(86,'Dr. Kanika Chopra','Orthopedic','Available',73,'08:00:00','16:00:00'),(87,'Dr. Nisha Patel','Pediatrician','Available',74,'12:00:00','19:00:00'),(88,'Dr. Raghav Sinha','Dermatologist','Unavailable',75,'06:00:00','12:00:00'),(89,'Dr. Ishwar Rao','Gynecologist','Available',76,'14:00:00','21:00:00'),(90,'Dr. Chaitanya Das','Oncologist','Available',77,'07:30:00','15:30:00'),(91,'Dr. Smita Kapoor','ENT Specialist','Unavailable',78,'09:00:00','17:00:00'),(92,'Dr. Soham Dutta','General Physician','Available',79,'08:00:00','16:00:00'),(93,'Dr. Ritika Nair','Psychiatrist','Unavailable',80,'10:00:00','18:00:00'),(94,'Dr. Keshav Reddy','Neurosurgeon','Available',81,'07:00:00','14:00:00'),(95,'Dr. Anuradha Sharma','Pulmonologist','Available',82,'11:00:00','19:00:00'),(96,'Dr. Madhav Sinha','Endocrinologist','Unavailable',83,'08:30:00','16:30:00'),(97,'Dr. Soumya Roy','Nephrologist','Available',84,'09:00:00','17:00:00');
/*!40000 ALTER TABLE `doctors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hospitals`
--

DROP TABLE IF EXISTS `hospitals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hospitals` (
  `hospital_id` int NOT NULL AUTO_INCREMENT,
  `hospital_name` varchar(255) NOT NULL,
  `address` text NOT NULL,
  `contact_number` varchar(20) NOT NULL,
  PRIMARY KEY (`hospital_id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hospitals`
--

LOCK TABLES `hospitals` WRITE;
/*!40000 ALTER TABLE `hospitals` DISABLE KEYS */;
INSERT INTO `hospitals` VALUES (1,'Guwahati Medical College','Bhangagarh, Guwahati','0361-2540815'),(2,'Apollo Hospitals Guwahati','Christian Basti, Guwahati','0361-2345678'),(3,'Hayat Hospital','Odalbakra, Guwahati','0361-7101001'),(4,'Dispur Hospital','Ganeshguri, Guwahati','0361-2222333'),(5,'Nemcare Hospital','Bhangagarh, Guwahati','0361-2455906'),(6,'Downtown Hospital','G.S. Road, Guwahati','0361-2331000'),(7,'Excelcare Hospital','Boragaon, Guwahati','0361-2520011'),(8,'Marwari Hospitals','Athgaon, Guwahati','0361-2637050'),(9,'Ayursundra Hospital','Basistha Road, Guwahati','0361-2233445'),(10,'Swagat Super Speciality Hospital','Maligaon, Guwahati','0361-2510021'),(11,'Sun Valley Hospital','Lachit Nagar, Guwahati','0361-2522222'),(12,'Pratiksha Hospital','VIP Road, Guwahati','0361-7101600'),(13,'GNRC Hospital','Six Mile, Guwahati','0361-2227700'),(14,'Arya Hospital','Ulubari, Guwahati','0361-2529011'),(15,'Sanjeevani Hospital','Maligaon, Guwahati','0361-2678902'),(16,'Medicity Hospital','Hatigaon, Guwahati','0361-2556789'),(17,'Ayursundra Superspeciality Hospital','Beltola, Guwahati','0361-7101222'),(18,'Metro Hospital','Paltan Bazar, Guwahati','0361-2345556'),(19,'Sri Sankaradeva Nethralaya','Basistha Chariali, Guwahati','0361-2233440'),(20,'Amrit Hospital','Zoo Road, Guwahati','0361-2567890'),(21,'Brahmaputra Hospital','Lachit Nagar, Guwahati','0361-2534321'),(22,'Medicare Hospital','Silpukhuri, Guwahati','0361-2677889'),(23,'Swasti Hospital','Beltola Tiniali, Guwahati','0361-2701234'),(24,'Rahman Hospital','VIP Road, Guwahati','0361-2256789'),(25,'Suraksha Hospital','GS Road, Guwahati','0361-2203334'),(26,'Arunoday Hospital','Kahilipara, Guwahati','0361-2114455'),(27,'Kishan Hospital','GMC Road, Guwahati','0361-2675432'),(28,'Prayag Hospital','Bamunimaidan, Guwahati','0361-2777766'),(29,'Cure & Care Hospital','Chandmari, Guwahati','0361-2225566'),(30,'Sunshine Hospital','Lankeshwar, Guwahati','0361-2909876'),(31,'Neotia Hospital','Beltola, Guwahati','0361-2201122'),(32,'North East Hospital','Hatigaon, Guwahati','0361-2756655'),(33,'Akash Hospital','Bhangagarh, Guwahati','0361-2765544'),(34,'Radha Krishna Hospital','Khanapara, Guwahati','0361-2103344'),(35,'Life Line Hospital','Six Mile, Guwahati','0361-2678899'),(36,'Wellness Hospital','Zoo Road, Guwahati','0361-2559988'),(37,'Evercare Hospital','Ganeshguri, Guwahati','0361-2223344'),(38,'Health First Hospital','Silpukhuri, Guwahati','0361-2712211'),(39,'Phoenix Hospital','Paltan Bazar, Guwahati','0361-2789988'),(40,'Cure Point Hospital','Maligaon, Guwahati','0361-2998877'),(41,'Relief Hospital','Bamunimaidan, Guwahati','0361-2123344'),(42,'Jeevan Jyoti Hospital','Bhetapara, Guwahati','0361-2341111'),(43,'Assam Healing Hospital','Chandmari, Guwahati','0361-2434321'),(44,'Hope Hospital','Hatigaon, Guwahati','0361-2786543'),(45,'New Age Hospital','Khanapara, Guwahati','0361-2657777'),(46,'Prime Hospital','Silchar Road, Guwahati','0361-2235566'),(47,'Global Care Hospital','Dispur, Guwahati','0361-2778877'),(48,'Neocare Hospital','Bhangagarh, Guwahati','0361-2125566'),(49,'Sanjeevan Hospital','Athgaon, Guwahati','0361-2678989'),(50,'Arogya Hospital','Paltan Bazar, Guwahati','0361-2253344'),(51,'City Care Hospital','Paltan Bazar, Guwahati','0361-2217788'),(52,'Greenfield Hospital','Zoo Road, Guwahati','0361-2998899'),(53,'MedLife Hospital','Hatigaon, Guwahati','0361-2755544'),(54,'Nirmal Hospital','Beltola, Guwahati','0361-2234455'),(55,'Navajeevan Hospital','Silpukhuri, Guwahati','0361-2227788'),(56,'Bhagwati Hospital','Ganeshguri, Guwahati','0361-2213322'),(57,'Shanti Medicare','Kahilipara, Guwahati','0361-2901122'),(58,'Jeevandeep Hospital','Lachit Nagar, Guwahati','0361-2447789'),(59,'Vitality Hospital','Bamunimaidan, Guwahati','0361-2773344'),(60,'Surya Healthcare','Dispur, Guwahati','0361-2229900'),(61,'Sampurna Hospital','Chandmari, Guwahati','0361-2992233'),(62,'Jivika Hospital','Maligaon, Guwahati','0361-2785544'),(63,'Balaji Hospital','Narengi, Guwahati','0361-2349988'),(64,'Lifespire Hospital','Six Mile, Guwahati','0361-2444455'),(65,'Uttam Healthcare','Boragaon, Guwahati','0361-2213344'),(66,'Dhanvantari Hospital','Pandu, Guwahati','0361-2997766'),(67,'Medisure Hospital','Jalukbari, Guwahati','0361-2445533'),(68,'New Horizon Hospital','Bhetapara, Guwahati','0361-2112299'),(69,'Cure Well Hospital','Athgaon, Guwahati','0361-2996655'),(70,'Infinity Hospital','Ulubari, Guwahati','0361-2238999'),(71,'Shristi Hospital','Panbazar, Guwahati','0361-2332211'),(72,'Healing Hands Hospital','Bhangagarh, Guwahati','0361-2556677'),(73,'Gyanam Hospital','VIP Road, Guwahati','0361-2777788'),(74,'MediMax Hospital','Hatigaon, Guwahati','0361-2655544'),(75,'Nirmaya Hospital','Beltola Tiniali, Guwahati','0361-2558889'),(76,'Hope Care Hospital','Paltan Bazar, Guwahati','0361-2105544'),(77,'Aashray Hospital','Zoo Road, Guwahati','0361-2779990'),(78,'Raj Laxmi Hospital','Silchar Road, Guwahati','0361-2117788'),(79,'Mangalam Hospital','Dispur, Guwahati','0361-2661122'),(80,'Ankur Hospital','Kahilipara, Guwahati','0361-2994433'),(81,'Arogya Kendra Hospital','Lachit Nagar, Guwahati','0361-2113344'),(82,'Sparsh Hospital','Boragaon, Guwahati','0361-2339988'),(83,'Tranquil Hospital','Jalukbari, Guwahati','0361-2996655'),(84,'Vedanta Hospital','Bamunimaidan, Guwahati','0361-2114433'),(85,'Medilife Super Specialty','Maligaon, Guwahati','0361-2223344'),(86,'Paramount Hospital','Six Mile, Guwahati','0361-2556678'),(87,'Sanjeevani Multicare','VIP Road, Guwahati','0361-2443344'),(88,'Lush Green Hospital','Narengi, Guwahati','0361-2998822'),(89,'Jeevika Hospital','Pandu, Guwahati','0361-2774455'),(90,'Vibrant Care Hospital','Panbazar, Guwahati','0361-2334455'),(91,'Manas Hospital','Beltola, Guwahati','0361-2447799'),(92,'Sushruta Hospital','Hatigaon, Guwahati','0361-2771234'),(93,'Astha Hospital','Ulubari, Guwahati','0361-2337766'),(94,'Swasthya Hospital','Zoo Road, Guwahati','0361-2559987'),(95,'Arunodaya Hospital','Ganeshguri, Guwahati','0361-2998876'),(96,'Seva Sadan Hospital','Bamunimaidan, Guwahati','0361-2223456'),(97,'Sai Kripa Hospital','Maligaon, Guwahati','0361-2667788'),(98,'Aashirvad Hospital','Bhetapara, Guwahati','0361-2774433'),(99,'Manav Seva Hospital','Paltan Bazar, Guwahati','0361-2999988'),(100,'Kalpataru Hospital','Bhangagarh, Guwahati','0361-2101122');
/*!40000 ALTER TABLE `hospitals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patients` (
  `patient_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `dob` date NOT NULL,
  `contact_number` varchar(20) NOT NULL,
  `blood_group` varchar(3) DEFAULT NULL,
  PRIMARY KEY (`patient_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patients`
--

LOCK TABLES `patients` WRITE;
/*!40000 ALTER TABLE `patients` DISABLE KEYS */;
INSERT INTO `patients` VALUES (3,'Amit Kumar','1985-06-12','9876543210','AB+'),(4,'Priya Sharma','1992-03-25','9876543221','A-'),(5,'Rohan Das','2000-07-18','9876543232','B+'),(6,'Sneha Verma','1998-11-05','9876543243','O-'),(7,'Vikram Singh','1975-09-30','9876543254','AB-'),(8,'Ananya Chakraborty','1989-12-14','9876543265','A+'),(9,'Karan Mehta','1995-08-22','9876543276','B-'),(10,'Neha Patil','2002-04-10','9876543287','O+'),(11,'Siddharth Rao','1990-10-01','9876543298','AB+'),(12,'Meenal Joshi','1982-05-27','9876543209','A-'),(13,'Preeti Shukla','2004-09-16','1234567891','B+'),(14,'soni raj','2003-09-30','9876543299','O-'),(15,'khushi raj','2001-09-09','9876654321','AB-');
/*!40000 ALTER TABLE `patients` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-31 19:43:14
