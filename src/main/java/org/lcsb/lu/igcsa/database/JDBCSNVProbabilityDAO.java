package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.SNVProbabilityDAO;
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

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCSNVProbabilityDAO implements SNVProbabilityDAO
  {
  static Logger log = Logger.getLogger(JDBCSNVProbabilityDAO.class.getName());

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

  public Frequency getByNucleotide(String nucleotide)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE nucleotide = ?";

    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, nucleotide);

      ResultSet rs = ps.executeQuery();

      Map<Object, Double> probabilities = new HashMap<Object, Double>();

      while (rs.next())
        {
        probabilities.put('A', rs.getDouble("prob_A"));
        probabilities.put('C', rs.getDouble("prob_C"));
        probabilities.put('G', rs.getDouble("prob_G"));
        probabilities.put('T', rs.getDouble("prob_T"));
        }
      rs.close();
      ps.close();
      return new Frequency(probabilities);
      }
    catch (SQLException e)
      {
      throw new RuntimeException(e);
      }
    catch (ProbabilityException e)
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
