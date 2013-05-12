package org.lcsb.lu.igcsa.embedded.DAO.impl;

import org.lcsb.lu.igcsa.embedded.DAO.DataLoadDAO;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCGCBinDAO extends DataLoadDAO
  {
  static Logger log = Logger.getLogger(JDBCGCBinDAO.class.getName());

  @Override
  public void insertData(String dataFile) throws IOException
    {
    log.info("Inserting data to " + tableName + " from file " + dataFile);

    BufferedReader reader = new BufferedReader(new FileReader(dataFile));

    String sql = "INSERT INTO " + tableName + "(chr, bin_id, minimum, maximum, total_fragments) VALUES (?,?,?,?,?)";
    String line;
    while ((line = reader.readLine()) != null)
      {
      String[] db = line.split("\t");
      if (db[0] == "") continue;

      jdbcTemplate.update(sql, new Object[]{db[1], Integer.valueOf(db[2]), Double.valueOf(db[3]), Double.valueOf(db[4]),
          Integer.valueOf(db[5])});
      }
    }

  }

