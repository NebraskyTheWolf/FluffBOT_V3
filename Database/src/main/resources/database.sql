SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Database name :  `fluffbot`
--
CREATE DATABASE IF NOT EXISTS `fluffbot` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `fluffbot`;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `players`;
CREATE TABLE `players` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` varchar(64) NOT NULL,
    `experience` int(11) NOT NULL DEFAULT '0',
    `level` int(11) NOT NULL DEFAULT '0',
    `coins` int(11) NOT NULL DEFAULT '0',
    `tokens` int(11) NOT NULL DEFAULT '0',
    `events` int(11) NOT NULL DEFAULT '0',
    `upvote` int(11) NOT NULL DEFAULT '0',
    `karma` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_roman_ci;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `players_birthday`;
CREATE TABLE `players_birthday` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` varchar(64) NOT NULL,
    `month` int(11) NOT NULL,
    `day` int(11) NOT NULL,
    `year` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_roman_ci;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `players_clan`;
CREATE TABLE `players_clan` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` varchar(64) NOT NULL,
    `clanId` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_roman_ci;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `developers`;
CREATE TABLE `developers` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_roman_ci;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `sanctions`;
CREATE TABLE `sanctions` (
    `sanction_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` varchar(64) NOT NULL,
    `type_id` tinyint(4) NOT NULL,
    `reason` varchar(255) COLLATE utf8_roman_ci NOT NULL,
    `author_id` varchar(64) NOT NULL,
    `expiration_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `creation_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_roman_ci;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `sanctions_ref`;
CREATE TABLE `sanctions_ref` (
    `type_id` tinyint(4) NOT NULL,
    `text` varchar(255) COLLATE utf8_roman_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_roman_ci;

-- --------------------------------------------------------

INSERT INTO `sanctions_ref` (`type_id`, `text`) VALUES
    (1, 'Warn'),
    (2, 'Ban'),
    (3, 'Kick'),
    (4, 'Mute');

-- --------------------------------------------------------

DROP TABLE IF EXISTS `transaction_shop`;
CREATE TABLE `transaction_shop` (
    `transaction_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `item_id` int(11) DEFAULT NULL,
    `price_coins` int(11) DEFAULT NULL,
    `price_tokens` int(11) DEFAULT NULL,
    `transaction_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
    `user_id` binary(16) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

DROP TABLE IF EXISTS `achievement_categories`;
CREATE TABLE `achievement_categories` (
    `category_id` int(11) NOT NULL AUTO_INCREMENT,
    `category_name` varchar(45) NOT NULL,
    `category_description` varchar(255) NOT NULL,
    `icon` varchar(255) NOT NULL,
    `parent_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_roman_ci;

-- --------------------------------------------------------

INSERT INTO `achievement_categories` (`category_name`, `category_description`, `icon`, `parent_id`) VALUES
            ('Messages', 'Category related for messages achievements', 'fox', 0);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements` (
    `achievement_id` int(11) NOT NULL AUTO_INCREMENT,
    `achievement_name` varchar(45) NOT NULL,
    `achievement_description` varchar(255) NOT NULL,
    `achievement_iconURL` varchar(255) DEFAULT NULL,
    `progress_target` int(11) NOT NULL,
    `parent_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_roman_ci;

-- --------------------------------------------------------

INSERT INTO `achievements` (`achievement_id`, `achievement_name`, `achievement_description`, `achievement_iconURL`, `progress_target`, `parent_id`) VALUES
    (1, '250 Messages', 'Have sent 250 message on the server', 'message1', 250, 1),
    (2, '1,000 Messages', 'Have sent 1,000 message on the server', 'message2', 1000, 1),
    (3, '10,000 Messages', 'Have sent 10,000 message on the server', 'message3', 10000, 1),
    (4, '25,000 Messages', 'Have sent 25,000 message on the server', 'message3', 25000, 1),
    (5, '50,000 Messages', 'Have sent 50,000 message on the server', 'message3', 50000, 1);

-- --------------------------------------------------------

DROP TABLE IF EXISTS `achievement_progresses`;
CREATE TABLE `achievement_progresses` (
    `progress_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `achievement_id` int(11) NOT NULL,
    `progress` int(11) NOT NULL,
    `start_date` timestamp  DEFAULT CURRENT_TIMESTAMP,
    `unlock_date` timestamp DEFAULT CURRENT_TIMESTAMP ,
    `user_id` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_roman_ci;

-- --------------------------------------------------------


DROP TABLE IF EXISTS `item_description`;
CREATE TABLE `item_description` (
    `item_id` int(11) NOT NULL AUTO_INCREMENT,
    `item_name` tinytext,
    `item_desc` text,
    `price_coins` int(11) DEFAULT NULL,
    `price_stars` int(11) DEFAULT NULL,
    `item_rarity` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------