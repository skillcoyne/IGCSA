package org.lcsb.lu.igcsa.database.normal.sql;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.SizeDAO;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
  private final String tableName = "variation_size_prob";

  private List<String> variations = new ArrayList<String>();

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);
    }

  public Map<String, Probability> getAll() throws ProbabilityException
    {
    log.debug("getAll()");
    Map<String, Probability> frequencyMap = new HashMap<String, Probability>();
    if (variations.size() <= 0)
      getVariations();

    for (String var : variations)
      frequencyMap.put(var, this.getByVariation(var));
    return frequencyMap;
    }


  public Probability getByVariation(String variation) throws ProbabilityException
    {
    String sql = "SELECT vs.maxbp, vs.frequency " +
        "FROM " + this.tableName + " vs INNER JOIN variation v ON vs.variation_id = v.id WHERE v.name = ?";
    log.debug("getByVariation("+ variation + "): " + sql);

    Map<Object, Double> frequencies = (Map<Object, Double>) jdbcTemplate.query(sql, new Object[]{variation}, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Map<Object, Double> probs = new TreeMap<Object, Double>();

        while (resultSet.next())
          {
          probs.put(resultSet.getInt("maxbp"), resultSet.getDouble("frequency"));
          }
        return probs;
        }
      });

    return new Probability(frequencies, 4);
    }

  private void getVariations()
    {
    String sql = "SELECT vs.maxbp, vs.frequency, v.name, v.class " +
        "FROM " + this.tableName + " vs " +
        "INNER JOIN variation v " +
        "ON vs.variation_id = v.id ORDER BY v.name";
    log.debug(sql);

    jdbcTemplate.query(sql, new ResultSetExtractor<Object>()
    {
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      while (resultSet.next())
        {
        variations.add(resultSet.getString("name"));
        }
      return null;
      }
    });
    }


  }
