package org.lcsb.lu.igcsa.embedded.DAO.impl;

import org.lcsb.lu.igcsa.embedded.DAO.DataLoadDAO;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCFragmentDAO extends DataLoadDAO
  {
  static Logger log = Logger.getLogger(JDBCFragmentDAO.class.getName());

  @Override
  public void insertData(String dataFile) throws IOException
    {
    log.info("Inserting data to " + tableName + " from file " + dataFile);

      BufferedReader reader = new BufferedReader(new FileReader(dataFile));

      String sql = "INSERT INTO " + tableName + "(chr, bin_id, variation_id, var_count) VALUES (?,?,?,?)";
      List<Object[]> batchInserts = new ArrayList<Object[]>();
      String line;
      while  ((line = reader.readLine()) != null)
        {
        String[] db = line.split("\t");
        if (db[0].equals("chr")) continue;

        batchInserts.add(new Object[]{db[0], Integer.valueOf(db[1]), Integer.valueOf(db[2]), Integer.valueOf(db[3])} );

        if (batchInserts.size() == 10000)
          {
          log.debug("Inserting " + batchInserts.size() + " records ");
          jdbcTemplate.batchUpdate(sql, batchInserts);
          batchInserts.clear();
          }
        }
      log.debug("Inserting " + batchInserts.size() + " records ");
      jdbcTemplate.batchUpdate(sql, batchInserts);


    log.debug("Adding index");
    addIndex();
    }

  public void addIndex()
    {
    jdbcTemplate.execute("CREATE INDEX vpb_index ON " + tableName + " (chr, bin_id, variation_id)" );
    }


  }
