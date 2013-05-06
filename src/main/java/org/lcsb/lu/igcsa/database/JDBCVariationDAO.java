package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.VariationDAO;
import org.lcsb.lu.igcsa.database.normal.Variations;
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
public class JDBCVariationDAO implements VariationDAO
  {
  static Logger log = Logger.getLogger(JDBCVariationDAO.class.getName());

  private JdbcTemplate jdbcTemplate;
  private String tableName = "variation";

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);
    }

  public Variations getVariations()
    {
    String sql = "SELECT * FROM " + tableName;
    return (Variations) jdbcTemplate.query(sql, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Variations vars = new Variations();
        while (resultSet.next())
          {
          try
            {
            vars.addVariation(resultSet.getInt("id"), resultSet.getString("name"), Class.forName(resultSet.getString("class")));
            }
          catch (ClassNotFoundException e)
            {
            log.error(e);
            }
          }
        return vars;
        }
      });
    }

  public Map<String, Class> getVariationsByChromosome(String chr)
    {
    String sql = "SELECT DISTINCT vp.chr, vp.variation_id, v.name, v.class " +
        "FROM " + tableName + " v " +
        "INNER JOIN variation_per_bin vp ON v.id = vp.variation_id " +
        "WHERE vp.chr = ?";

    return (Map<String, Class>) jdbcTemplate.query(sql, new Object[]{chr}, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Map<String, Class> variations = new HashMap<String, Class>();
        while (resultSet.next())
          {
          try
            {
            variations.put(resultSet.getString("name"), Class.forName(resultSet.getString("class")));
            }
          catch (ClassNotFoundException e)
            {
            log.error(e);
            }
          }
        return variations;
        }
      });
    }
  }
