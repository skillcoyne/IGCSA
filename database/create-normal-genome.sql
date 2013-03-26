drop database if exists `normal_genome_variation`;
create database `normal_genome_variation`;

use `normal_genome_variation`;

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


## Might be faster to have a table for each chromosome...

create table `variations` (
  `chr` varchar(12) not null,
  `bin_id` int(11) not null,
  `SNV` int(11) not null,
  `deletion` int(11) not null,
  `indel` int(11) not null,
  `insertion` int(11) not null,
  `sequence_alteration` int(11) not null,
  `substitution` int(11) not null,
  `tandem_repeat` int(11) not null,
  `id` int(11) not null auto_increment,
  primary key(`id`),
  index varindex (`chr`, `bin_id`)
);


create table `variation_size_prob` (
  `maxbp` int(11) not null,
  `deletion` decimal(5,4) not null,
  `indel` decimal(5,4) not null,
  `insertion` decimal(5,4) not null,
  `sequence_alteration` decimal(5,4) not null,
  `substitution` decimal(5,4) not null,
  `tandem_repeat` decimal(5,4) not null,
  `id` int(11) not null auto_increment,
  primary key(`id`)
);




## load data local infile 'gc_bins.txt' into table normal_genome_variation.gc_bins ignore 1 lines;

## load data local infile 'variations-table.txt' into table normal_genome_variation.variations ignore 1 lines;

## load data local infile 'variation-size-table.txt' into table normal_genome_variation.variation_size_prob ignore 1 lines;