drop database if exists `igcsa`;
create database `igcsa`;

use `igcsa`;


create table `genome` (
  `id` int(11) not null auto_increment,
  `name` int(32) not null,
  `location` varchar(234) not null,
  primary key(`id`),
  index gindex (`name`)
);

create table `mutation` (
  `id` int(11) not null auto_increment,
  `genome_id` int (11) not null,
  `chr` varchar(12) not null,
  `fragment` int(24) not null,
  `location` int(12) not null,
  `variation` varchar(112) not null,
  `sequence` text not null,
  primary key(`id`),
  index gmindex(`genome_id`, `chr`, `fragment`),
  index mindex(`genome_id`, `chr`, `variation`)
);

