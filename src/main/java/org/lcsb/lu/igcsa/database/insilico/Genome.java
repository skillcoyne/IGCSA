package org.lcsb.lu.igcsa.database.insilico;

import org.apache.log4j.Logger;

/**
 * org.lcsb.lu.igcsa.database.insilico
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class Genome
  {
  static Logger log = Logger.getLogger(Genome.class.getName());

  private int id;
  private int name;
  private String location;

  public int getId()
    {
    return id;
    }

  public void setId(int id)
    {
    this.id = id;
    }

  public int getName()
    {
    return name;
    }

  public void setName(int name)
    {
    this.name = name;
    }

  public String getLocation()
    {
    return location;
    }

  public void setLocation(String location)
    {
    this.location = location;
    }
  }
