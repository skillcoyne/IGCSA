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

public class JDBCBreakpointProbsDAO extends DataLoadDAO
  {
  static Logger log = Logger.getLogger(JDBCBreakpointProbsDAO.class.getName());

  @Override
  public void insertData(String file) throws IOException
    {
    log.info("Inserting data to " + tableName + " from file " + file);

    BufferedReader reader = new BufferedReader(new FileReader(file));

    String sql = "INSERT INTO " + tableName + "(chr, band, bp_prob, per_chr_prob) VALUES (?,?,?,?)";
    String line;
    while ((line = reader.readLine()) != null)
      {
      if (line.startsWith("#")) continue;
      String[] db = line.split("\t");
      if (db.length < 4) throw new IOException("Malformed file, 4 columns expected in each line.");
      if (db[0].equals("chr")) continue;
      log.info(db[0] + " " + db[1] + " " + db[2] + " " + db[3]);
      jdbcTemplate.update(sql, new Object[]{db[0], db[1], Double.valueOf(db[2]), Double.valueOf(db[3])});
      }
    }
  }
