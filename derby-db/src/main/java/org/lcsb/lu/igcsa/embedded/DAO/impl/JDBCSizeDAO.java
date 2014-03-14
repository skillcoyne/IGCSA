package org.lcsb.lu.igcsa.embedded.DAO.impl;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.embedded.DAO.DataLoadDAO;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * org.lcsb.lu.igcsa.embedded.DAO.impl
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCSizeDAO extends DataLoadDAO
  {
  static Logger log = Logger.getLogger(JDBCSizeDAO.class.getName());

  @Override
  public void insertData(String dataFile) throws IOException
    {
    log.info("Inserting data to " + tableName + " from file " + dataFile);

    BufferedReader reader = new BufferedReader(new FileReader(dataFile));

    String sql = "INSERT INTO " + tableName + "(maxbp, variation_id, probability) VALUES (?,?,?)";
    String line;
    while ((line = reader.readLine()) != null)
      {
      String[] db = line.split("\t");
      if (db[0].equals("maxbp")) continue;

      jdbcTemplate.update(sql, new Object[]{Integer.valueOf(db[0]), Integer.valueOf(db[1]), Double.valueOf(db[2])});
      }


    }
  }
