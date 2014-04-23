package org.lcsb.lu.igcsa.karyotype.database;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class StreamingStatementCreator implements PreparedStatementCreator
  {
  static Logger log = Logger.getLogger(StreamingStatementCreator.class.getName());

  private final String sql;
  private int size = 1;

  public StreamingStatementCreator(String sql, int size)
    {
    this.sql = sql;
    this.size = size;
    }

  public StreamingStatementCreator(String sql)
    {
    this.sql = sql;
    }

  @Override
  public PreparedStatement createPreparedStatement(Connection connection) throws SQLException
    {
    final PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    statement.setFetchSize(size);
    return statement;
    }

  }
