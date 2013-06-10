drop database if exists `karyotype_variation`;
create database `karyotype_variation`;

use `karyotype_variation`;


create table `breakpoints` (
  `chr` varchar(12) not null,
  `band` varchar(24) not null,
  `start` int not null,
  `end` int not null,
  `prob` decimal(5,4) not null,
  `class` int not null,
  `id` int not null auto_increment,
  PRIMARY KEY (id),
  index cent_ix(`chr`,`band`)
);

## load data local infile 'bp-classes.txt' into table karyotype_variation.breakpoints;
