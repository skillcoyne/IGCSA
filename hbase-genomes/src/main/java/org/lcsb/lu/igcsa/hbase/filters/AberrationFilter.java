package org.lcsb.lu.igcsa.hbase.filters;

/**
 * org.lcsb.lu.igcsa.hbase.filters
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public enum AberrationFilter
  {

    //DUPLICATION("dup", "org.lcsb.lu.igcsa.hbase.filters.Duplication"),
    INVERSION("inv", Inversion.class);
//    ADDITION("add", "org.lcsb.lu.igcsa.hbase.filters.Addition"),
//    ISOCENTRIC("iso", "org.lcsb.lu.igcsa.hbase.filters.Iso"),
//    DELETION("del", "org.lcsb.lu.igcsa.hbase.filters.Deletion"),

    //INSERTION     ("ins",   "org.lcsb.lu.igcsa.aberrations.multiple.Insertion"),  // Insertion requires that 2 derivative chromosomes are created.  One is a translocation, the other is a deletion. Current implementation doesn't easily allow for this
//    DICENTRIC("dic", "org.lcsb.lu.igcsa.hbase.filters.Dicentric"),
//    TRANSLOCATION("trans", "org.lcsb.lu.igcsa.hbase.filters.Translocation");

  private String cytogenic;
  private Class<? extends  AberrationLocationFilter> filterClass;

  private AberrationFilter(String cytogenic, Class<? extends AberrationLocationFilter> filterClass)
    {
    this.cytogenic = cytogenic;
    this.filterClass = filterClass;
    }

  public AberrationLocationFilter getInstance() throws IllegalAccessException, InstantiationException
    {
    return this.filterClass.newInstance();
    }

  }
