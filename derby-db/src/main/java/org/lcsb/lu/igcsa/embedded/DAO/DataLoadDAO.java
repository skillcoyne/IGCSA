package org.lcsb.lu.igcsa.embedded.DAO;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;


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
