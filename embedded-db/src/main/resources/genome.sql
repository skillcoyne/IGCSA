
CREATE TABLE gc_bins (
  id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  chr VARCHAR(12) NOT NULL,
  bin_id INT NOT NULL,
  minimum INT NOT NULL,
  maximum INT NOT NULL,
  total_fragments INT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE snv_prob (
  nucleotide VARCHAR(12) CONSTRAINT nuc_ck CHECK (nucleotide IN ('A', 'C', 'G', 'T')) NOT NULL,
  prob_A DECIMAL(5,2) NOT NULL,
  prob_C DECIMAL(5,2) NOT NULL,
  prob_G DECIMAL(5,2) NOT NULL,
  prob_T DECIMAL(5,2) NOT NULL,
  PRIMARY KEY(nucleotide)
);

CREATE TABLE variation (
  id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  name  VARCHAR(112) NOT NULL,
  class VARCHAR(224) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE variation_per_bin (
  chr VARCHAR(12) NOT NULL,
  bin_id INT NOT NULL,
  variation_id INT NOT NULL,
  var_count INT NOT NULL
);

CREATE TABLE variation_size_prob (
  maxbp INT NOT NULL,
  variation_id INT NOT NULL,
  frequency DECIMAL(5,4) NOT NULL,
  id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  PRIMARY KEY (id)
);

CREATE TABLE variation_to_chr (
  chr VARCHAR(12) NOT NULL,
  variation_id INT NOT NULL,
  CONSTRAINT vtc_fk FOREIGN KEY(variation_id) REFERENCES variation(id)
);


