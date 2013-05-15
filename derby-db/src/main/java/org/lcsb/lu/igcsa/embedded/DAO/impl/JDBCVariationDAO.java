package org.lcsb.lu.igcsa.embedded.DAO.impl;

import org.lcsb.lu.igcsa.embedded.DAO.DataLoadDAO;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCVariationDAO extends DataLoadDAO
  {
  static Logger log = Logger.getLogger(JDBCVariationDAO.class.getName());


  @Override
  public void insertData(String dataFile) throws IOException
    {
    BufferedReader reader = new BufferedReader(new FileReader(dataFile));

    String sql = "INSERT INTO " + tableName + "(name, class) VALUES (?,?)";
    String line;

    while ((line = reader.readLine()) != null)
      {
      String[] db = line.split("\t");
      if (db[0].equals("")) continue;
      jdbcTemplate.update(sql, new Object[]{db[1], db[2]});
      }
    }

  }
