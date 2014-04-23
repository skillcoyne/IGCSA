/**
 * org.lcsb.lu.igcsa.database.sql
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.database.sql;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.database.AneuploidyDAO;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class JDBCAneuploidyDAO implements AneuploidyDAO
  {
  static Logger log = Logger.getLogger(JDBCAneuploidyDAO.class.getName());

  private JdbcTemplate jdbcTemplate;

  private String tableName = "aneuploidy";
  private boolean isDerby = false;

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);

    try
      {
      String dbName = dataSource.getConnection().getMetaData().getDatabaseProductName();
      if (dbName.equals("Apache Derby"))
        {
        log.info(dbName);
        isDerby = true;
        }
      }
    catch (SQLException e)
      {
      throw new RuntimeException(e);
      }
    }

  @Override
  public Probability getGainLoss(String chromosome) throws ProbabilityException
    {
    String sql = "SELECT chr, gain, loss FROM " + this.tableName + " WHERE chr = ?";
    Map<Object, Double> probabilities = (Map<Object, Double>) jdbcTemplate.query(sql, new Object[]{chromosome}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Map<Object, Double> probs = new HashMap<Object, Double>();
      if (resultSet.next())
        {
        probs.put("gain", resultSet.getDouble("gain"));
        probs.put("loss", resultSet.getDouble("loss"));
        }
      return probs;
      }
    });
    return new Probability(probabilities);
    }

  @Override
  public Probability getChromosomeProbabilities() throws ProbabilityException
    {
    String sql = "SELECT chr, prob FROM " + this.tableName + " ORDER BY prob DESC";
    Map<Object, Double> probabilities = (Map<Object, Double>) jdbcTemplate.query(sql, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Map<Object, Double> probs = new HashMap<Object, Double>();
      while (resultSet.next())
        probs.put(resultSet.getString("chr"), resultSet.getDouble("prob"));

      return probs;
      }
    });
    return new Probability(probabilities);
    }



  }
