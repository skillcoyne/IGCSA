package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.Bin;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.database.normal.FragmentDAO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCFragmentDAO implements FragmentDAO
  {
  static Logger log = Logger.getLogger(FragmentDAO.class.getName());

  private JdbcTemplate jdbcTemplate;
  private final String tableName = "variation_per_bin";

  // cache so that I can avoid expensive joins
  private Map<String, List<String>> variationPerBin = new HashMap<String, List<String>>();
  private Map<String, Integer> variations = new HashMap<String, Integer>();

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);

    String sql = "SELECT * FROM variation ORDER BY id";
    variations = (Map<String, Integer>) jdbcTemplate.query(sql, new ResultSetExtractor<Object>()
      {
      @Override
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Map<String, Integer> vars = new HashMap<String, Integer>();
        while (resultSet.next())
          {
          vars.put(resultSet.getString("name"), resultSet.getInt("id"));
          }
        return vars;
        }
      });
    }


  public Integer getVariationCount(String chr, int binId, String variation, int fragmentNum)
    {
    int varId = variations.get(variation);

    String sql = "SELECT var_count FROM " + tableName + " WHERE chr = ? AND bin_id = ? AND variation_id = ? LIMIT ?, 1";

    log.debug("getVariationCount(" + chr + ", " + binId + ", " + variation + ", " + fragmentNum + "): " + sql);

    int count = (Integer) jdbcTemplate.query(sql, new Object[]{chr, binId, varId, fragmentNum}, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        int count = 0;
        if (resultSet.next())
          count = resultSet.getInt("var_count");
        return count;
        }
      });
    return count;
    }

  public Fragment getVariationFragment(String chr, int binId, String variation, int fragmentNum)
    {
    int count = this.getVariationCount(chr, binId, variation, fragmentNum);
    Fragment fragment = new Fragment();
    fragment.setCount(count);
    fragment.setVariation(variation);
    fragment.setBinId(binId);
    fragment.setChr(chr);
    return fragment;
    }

  public Map<String, Fragment> getFragment(String chr, int binId, int fragmentNum)
    {
    // for the given fragment get the variations
    List<String> variations = variationsInBin(chr, binId);
    Map<String, Fragment> fragments = new HashMap<String, Fragment>();
    for (String var : variations)
      {
      Fragment fragment = new Fragment();
      fragment.setBinId(binId);
      fragment.setChr(chr);
      fragment.setVariation(var);
      fragment.setCount(this.getVariationCount(chr, binId, var, fragmentNum));
      fragments.put(var, fragment);
      }
    return fragments;
    }

  private List<String> variationsInBin(String chr, int binId)
    {
    if (!this.variationPerBin.containsKey(chr + binId))
      {
      String sql = "SELECT distinct v.name FROM " + this.tableName + " as vpb " +
          "INNER JOIN variation as v ON v.id = vpb.variation_id " +
          "WHERE vpb.chr = ? AND vpb.bin_id = ?";
      log.debug("CACHING getVariationsInBin(" + chr + ", " + binId + "): " + sql);

      List<String> vars = (List<String>) jdbcTemplate.query(sql, new Object[]{chr, binId}, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        List<String> vars = new ArrayList<String>();
        while (resultSet.next())
          {
          vars.add(resultSet.getString("name"));
          }
        return vars;
        }
      });
      variationPerBin.put(chr + binId, vars);
      }
    return variationPerBin.get(chr + binId);
    }

  }
