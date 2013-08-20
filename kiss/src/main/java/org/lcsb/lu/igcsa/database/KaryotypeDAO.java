package org.lcsb.lu.igcsa.database;

import org.lcsb.lu.igcsa.database.sql.JDBCAneuploidyDAO;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class KaryotypeDAO
  {
  private AneuploidyDAO aneuploidyDAO;
  private GeneralKarytoypeDAO generalKarytoypeDAO;
  private ChromosomeBandDAO bandDAO;

  public ChromosomeBandDAO getBandDAO()
    {
    return bandDAO;
    }

  public void setBandDAO(ChromosomeBandDAO bandDAO)
    {
    this.bandDAO = bandDAO;
    }

  public GeneralKarytoypeDAO getGeneralKarytoypeDAO()
    {
    return generalKarytoypeDAO;
    }

  public void setGeneralKarytoypeDAO(GeneralKarytoypeDAO generalKarytoypeDAO)
    {
    this.generalKarytoypeDAO = generalKarytoypeDAO;
    }

  public AneuploidyDAO getAneuploidyDAO()
    {
    return aneuploidyDAO;
    }

  public void setAneuploidyDAO(AneuploidyDAO aneuploidyDAO)
    {
    this.aneuploidyDAO = aneuploidyDAO;
    }

  }
