/**
 * org.lcsb.lu.igcsa.embedded.kiss.DAO.impl
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.embedded.kiss.DAO.impl;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.embedded.kiss.DAO.DataLoadDAO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JDBCKaryotypeGeneralProbDAO extends DataLoadDAO
  {
  static Logger log = Logger.getLogger(JDBCKaryotypeGeneralProbDAO.class.getName());

  @Override
  public void insertData(String file) throws IOException
    {
    log.info("Inserting data to " + tableName + " from file " + file);

    BufferedReader reader = new BufferedReader(new FileReader(file));

    String sql = "INSERT INTO " + tableName + "(prob_type, min_count, max_count, prob) VALUES (?,?,?,?)";
    String line;
    while ((line = reader.readLine()) != null)
      {
      if (line.startsWith("#")) continue;
      String[] db = line.split("\t");

      int min = Integer.parseInt(db[0].split("-")[0]);
      int max = Integer.parseInt(db[0].split("-")[1]);

      jdbcTemplate.update(sql, new Object[]{db[2], min, max, Double.valueOf(db[1])});
      }
    }
  }
