package org.lcsb.lu.igcsa.embedded.kiss.DAO.impl;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.embedded.kiss.DAO.DataLoadDAO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.embedded.kiss.DAO.impl
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCBandDAO extends DataLoadDAO
  {
  static Logger log = Logger.getLogger(JDBCBandDAO.class.getName());

  @Override
  public void insertData(String file) throws IOException
    {
    log.info("Inserting data to " + tableName + " from file " + file);

    BufferedReader reader = new BufferedReader(new FileReader(file));

    String sql = "INSERT INTO " + tableName + "(chr, band, start_loc, end_loc) VALUES (?,?,?,?)";
    String line;
    while ((line = reader.readLine()) != null)
      {
      if (line.startsWith("#")) continue;
      String[] db = line.split("\t");
      if (!db[0].matches("\\d+")) continue;

      jdbcTemplate.update( sql, new Object[]{db[0], db[1], Integer.parseInt(db[2]), Integer.parseInt(db[3])} );
      }
    }

  }
