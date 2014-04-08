/**
 * org.lcsb.lu.igcsa.database.sql
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.database.sql;

//import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang3.Range;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.GeneralKarytoypeDAO;
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

public class JDBCGeneralProbabilityDAO implements GeneralKarytoypeDAO
  {
  static Logger log = Logger.getLogger(JDBCGeneralProbabilityDAO.class.getName());

  private JdbcTemplate jdbcTemplate;

  private boolean isDerby = false;

  public static final String bp =  "breakpoint";
  public static final String pdy = "aneuploidy";
  public static final String abr = "aberration";

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
  public Probability getProbabilityClass(String type) throws ProbabilityException
    {
    String tableName = "karyotype_probabilities";

    String sql = "SELECT min_count, max_count, prob FROM " + tableName + " WHERE prob_type = ? ORDER BY prob DESC";

    Map<Object, Double> probabilities = (Map<Object, Double>) jdbcTemplate.query(sql, new Object[]{type}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Map<Object, Double> probs = new HashMap<Object, Double>();

      while (resultSet.next())
        {
        Range<Integer> intRange = Range.between(resultSet.getInt("min_count"), resultSet.getInt("max_count"));

        probs.put(intRange, resultSet.getDouble("prob"));
        //probs.put(new IntRange(resultSet.getInt("min_count"), resultSet.getInt("max_count")), resultSet.getDouble("prob"));
        }

      return probs;
      }
    });

    return new Probability(probabilities, 5);
    }

  @Override
  public Probability getChromosomeInstability() throws ProbabilityException
    {
    final String tableName = "chromosomes";
    String sql = "SELECT chr, prob FROM " + tableName + " ORDER BY prob DESC";
    Map<Object, Double> probabilities = (Map<Object, Double>) jdbcTemplate.query(sql, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Map<Object, Double> probs = new HashMap<Object, Double>();

      while(resultSet.next())
        probs.put(resultSet.getString("chr"), resultSet.getDouble("prob"));

      return probs;
      }
    });
    return new Probability(probabilities, 5);
    }

  @Override
  public Probability getOverallBandProbabilities() throws ProbabilityException
    {
    final String tableName = "breakpoints";
    String sql = "SELECT chr, band, bp_prob FROM " + tableName + " ORDER BY bp_prob DESC";
    Map<Object, Double> probabilities = (Map<Object, Double>) jdbcTemplate.query(sql, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Map<Object, Double> probs = new HashMap<Object, Double>();

      while (resultSet.next())
        probs.put( new Band(resultSet.getString("chr"), resultSet.getString("band")), resultSet.getDouble("bp_prob"));

      return probs;
      }
    });

    return new Probability(probabilities, 5);
    }

  @Override
  public Probability getBandProbabilities(String chr) throws ProbabilityException
    {
    final String tableName = "breakpoints";
    String sql = "SELECT chr, band, per_chr_prob FROM " + tableName + " WHERE chr = ? ORDER BY per_chr_prob DESC";

    Map<Object, Double> probabilities = (Map<Object, Double>) jdbcTemplate.query(sql, new Object[]{chr}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Map<Object, Double> probs = new HashMap<Object, Double>();

      while (resultSet.next())
        probs.put(resultSet.getString("band"), resultSet.getDouble("per_chr_prob"));

      return probs;
      }
    });

    return new Probability(probabilities, 5);
    }






  }
