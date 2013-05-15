package org.lcsb.lu.igcsa.embedded.DAO.impl;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.embedded.DAO.DataLoadDAO;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * org.lcsb.lu.igcsa.embedded.DAO.impl
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCSNVProbabilityDAO extends DataLoadDAO
  {
  static Logger log = Logger.getLogger(JDBCSNVProbabilityDAO.class.getName());

  @Override
  public void insertData(String dataFile) throws IOException
    {
    log.info("Inserting data to " + tableName + " from file " + dataFile);

    BufferedReader reader = new BufferedReader(new FileReader(dataFile));

    String sql = "INSERT INTO " + tableName + "(nucleotide, prob_A, prob_C, prob_G, prob_T) VALUES (?,?,?,?,?)";
    String line;
    while ((line = reader.readLine()) != null)
      {
      String[] db = line.split("\t");

      jdbcTemplate.update(sql, new Object[]{db[0], Double.valueOf(db[1]), Double.valueOf(db[2]), Double.valueOf(db[3]),
          Double.valueOf(db[4])});
      }
    }


  }
