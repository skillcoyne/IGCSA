package org.lcsb.lu.igcsa.database.sql;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.StreamingStatementCreator;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.database.normal.FragmentDAO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);
    }

  public Integer getVariationCount(String chromosome, int gcBinId, String variationName, int fragmentNumber)
    {
        String sql = "SELECT vpb.chr, vpb.bin_id, vpb.count, v.name FROM " +
          this.tableName + " as vpb INNER JOIN variation as v on v.id = vpb.variation_id " +
          "WHERE vpb.chr = ? AND vpb.bin_id = ? and v.name = ? LIMIT ?, 1";

    log.debug("getVariationCount(" + chromosome + ", " + gcBinId + ", " + variationName + ", " + fragmentNumber + "): " + sql);
    return (Integer) jdbcTemplate.query(sql, new Object[]{chromosome,  gcBinId, variationName, fragmentNumber}, new ResultSetExtractor<Object>()
        {
        public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
          {
          int count = 0;
          if (resultSet.next())
            count = resultSet.getInt("var_count");
          return count;
          }
        }
    );
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

  public List<Fragment> getFragment(String chr, int binId, int fragmentNum)
    {
    // for the given fragment get the variations
    List<String> variations = variationsInBin(chr, binId);

    List<Fragment> fragments = new ArrayList<Fragment>();
    for (String var : variations)
      {
      Fragment fragment = new Fragment();
      fragment.setBinId(binId);
      fragment.setChr(chr);
      fragment.setVariation(var);
      fragment.setCount(this.getVariationCount(chr, binId, var, fragmentNum));
      fragments.add(fragment);
      }

    return fragments;
    }

  // TODO this could be cached for each chr/bin combination as it's fairly small
  private List<String> variationsInBin(String chr, int binId)
    {
    String sql = "SELECT distinct v.name FROM " + this.tableName + " as vpb " +
        "INNER JOIN variation as v ON v.id = vpb.variation_id " +
        "WHERE vpb.chr = ? AND vpb.bin_id = ?";
    log.debug("getVariationsInBin(" + chr + ", " + binId + "): " + sql);

    return (List<String>) jdbcTemplate.query(sql, new Object[]{chr, binId}, new ResultSetExtractor<Object>()
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
    }

  }
