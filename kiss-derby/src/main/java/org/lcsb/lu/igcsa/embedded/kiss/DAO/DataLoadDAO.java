/**
 * org.lcsb.lu.igcsa.embedded.kiss
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.embedded.kiss.DAO;

import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * org.lcsb.lu.igcsa.embedded.DAO
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class DataLoadDAO
  {
  static Logger log = Logger.getLogger(DataLoadDAO.class.getName());

  protected JdbcTemplate jdbcTemplate;
  protected String tableName;

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);
    }

  public void setTableName(String tableName)
    {
    this.tableName = tableName;
    }

  public int getTableCount()
    {
    String sql = "SELECT COUNT(*) FROM " + tableName;
    log.debug(sql);

    return jdbcTemplate.queryForInt(sql);
    }

  public abstract void insertData(String file) throws IOException;
  }
