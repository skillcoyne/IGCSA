drop database if exists `normal_variation`;
create database `normal_variation`;

use `normal_variation`;

create table `gc_bins` (
  `id` int(11) not null auto_increment,
  `chr` varchar(12) not null,
  `bin_id` int(11) not null,
  `min` int(11) not null,
  `max` int(11) not null,
  `total_fragments` int(24) not null,
  primary key(`id`),
  index binindex (`chr`, `bin_id`)
);

create table `snv_prob` (
  `nucleotide` ENUM('A', 'C', 'G', 'T') not null,
  `prob_A` decimal(5,2) not null,
  `prob_C` decimal(5,2) not null,
  `prob_G` decimal(5,2) not null,
  `prob_T` decimal(5,2) not null,
  primary key(`nucleotide`)
);

create table `variation` (
  `id` int(11) not null auto_increment,
  `name` varchar(112) not null,
  `class` varchar(224) not null,
  primary key(`id`)
);

create table `variation_per_bin` (
  `chr` varchar(12) not null,
  `bin_id` int(11) not null,
  `variation_id` int(11) not null,
  `count` int(24) not null,
  index vp_index(`chr`, `bin_id`, `variation_id`)
);

create table `variation_size_prob` (
  `maxbp` int(11) not null,
  `variation_id` int(11) not null,
  `id` int(11) not null auto_increment,
  primary key(`id`)
);

## load data local infile 'gc_bins.txt' into table normal_variation.gc_bins ignore 1 lines;

## load data local infile 'variation.txt' into table normal_variation.variation;

## load data local infile 'variation_size_prob.txt' into table normal_variation.variation_size_prob ignore 1 lines;

## load data local infile 'snv_table.txt' into table normal_variation.snv_prob;

## load data local infile 'variation_per_bin.txt' into table normal_variation.variation_per_bin ignore 1 lines;

