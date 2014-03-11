package org.lcsb.lu.igcsa.database;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class SpringJDBCUtility
  {



  private static SpringJDBCUtility ourInstance;

  public static SpringJDBCUtility getFIGGConnections()
    {
    if (ourInstance == null)
      ourInstance = new SpringJDBCUtility();
    return ourInstance;
    }

  private SpringJDBCUtility()
    {
    }
  }
