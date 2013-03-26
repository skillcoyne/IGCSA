package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.SizeDAO;
import org.lcsb.lu.igcsa.prob.Frequency;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * org.lcsb.lu.igcsa.database.normal
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCSizeDAO implements SizeDAO
  {
  static Logger log = Logger.getLogger(JDBCSizeDAO.class.getName());

  private JdbcTemplate jdbcTemplate;
  private DataSource dataSource;
  private String tableName;

  public DataSource getDataSource()
    {
    return dataSource;
    }

  public void setDataSource(DataSource dataSource)
    {
    this.dataSource = dataSource;
    //jdbcTemplate = new JdbcTemplate(dataSource);
    }

  public String getTableName()
    {
    return tableName;
    }

  public void setTableName(String tableName)
    {
    this.tableName = tableName;
    }


  public Map<String, Frequency> getAll() throws ProbabilityException
    {
    String[] variations = new String[]{"deletion", "indel", "insertion", "sequence_alteration", "substitution", "tandem_repeat"};
    Map<String, Frequency> frequencyMap = new HashMap<String, Frequency>();
    for (String var : variations)
      frequencyMap.put(var, this.getByVariation(var));
    return frequencyMap;
    }

  public Frequency getByVariation(String variation) throws ProbabilityException
    {
    String sql = "SELECT maxbp, " + variation + " FROM " + this.tableName;

    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();

      Map<Object, Double> probs = new TreeMap<Object, Double>();

      while (rs.next())
        probs.put(rs.getInt("maxbp"), rs.getDouble(variation));

      rs.close();
      ps.close();
      return new Frequency(probs);
      }
    catch (SQLException e)
      {
      throw new RuntimeException(e);
      }
    finally
      {
      if (conn != null)
        {
        try
          {
          conn.close();
          }
        catch (SQLException e)
          {
          log.warn(e.getMessage());
          }
        }
      }
    }

  }
