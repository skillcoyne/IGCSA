create table karyotypes (
  id int not null auto_increment,
  source enum('ncbi', 'cam', 'mitelman') not null,
  karyotype text not null,
  primary key(id)
);

create table aberrations (
  id int not null auto_increment,
  aberration text not null,
  frequency int not null,
  primary key (id)
);

create table karyotype_aberration (
  karotype_id int not null,
  aberration_id int not null
);

