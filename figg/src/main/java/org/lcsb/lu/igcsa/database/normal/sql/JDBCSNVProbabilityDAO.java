package org.lcsb.lu.igcsa.database.normal.sql;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.SNVProbabilityDAO;
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
  private final String tableName = "snv_prob";

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);
    }

  public Map<Character, Probability> getAll() throws ProbabilityException
    {
    String sql = "SELECT * FROM " + this.tableName;
    log.debug(sql);

    return (Map<Character, Probability>) jdbcTemplate.query(sql, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Map<Character, Probability> frequencyMap = new HashMap<Character, Probability>();
        while (resultSet.next())
          {
          Map<Object, Double> probs = new HashMap<Object, Double>();
          probs.put('A', resultSet.getDouble("prob_A"));
          probs.put('C', resultSet.getDouble("prob_C"));
          probs.put('G', resultSet.getDouble("prob_G"));
          probs.put('T', resultSet.getDouble("prob_T"));
          try
            {
            frequencyMap.put( resultSet.getString("nucleotide").charAt(0), new Probability(probs) );
            }
          catch (ProbabilityException e)
            {
            log.error(e);
            }
          }
        return frequencyMap;
        }
      });
    }

  public Probability getByNucleotide(String nucleotide) throws ProbabilityException
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE nucleotide = ?";
    log.debug("getByNucleotide(" + nucleotide + "): " + sql);

    Map<Object, Double> probabilities = (Map<Object, Double>) jdbcTemplate.query(sql, new Object[]{nucleotide}, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Map<Object, Double> probs = new HashMap<Object, Double>();
        if (resultSet.next())
          {
          probs.put('A', resultSet.getDouble("prob_A"));
          probs.put('C', resultSet.getDouble("prob_C"));
          probs.put('G', resultSet.getDouble("prob_G"));
          probs.put('T', resultSet.getDouble("prob_T"));
          }
        return probs;
        }
      });

    return new Probability(probabilities);
    }

  }
