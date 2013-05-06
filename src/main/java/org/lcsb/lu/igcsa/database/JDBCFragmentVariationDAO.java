package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.database.normal.FragmentVariationDAO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.Connection;
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
public class JDBCFragmentVariationDAO implements FragmentVariationDAO
  {
  static Logger log = Logger.getLogger(FragmentVariationDAO.class.getName());

  private DataSource dataSource;
  private JdbcTemplate jdbcTemplate;
  private String tableName = "variation_per_bin";

  public void setDataSource(DataSource dataSource)
    {
    this.dataSource = dataSource;
    jdbcTemplate = new JdbcTemplate(dataSource);
    }

  public Integer getVariationCount(String chr, int binId, String variation, int fragmentNum)
    {
    String sql = "SELECT vpb.chr, vpb.bin_id, vpb.count, v.name FROM " +
      this.tableName + " as vpb INNER JOIN variation as v on v.id = vpb.variation_id " +
      "WHERE vpb.chr = ? AND vpb.bin_id = ? and v.name = ? LIMIT ?, 1";

    return (Integer) jdbcTemplate.query(sql, new Object[]{chr, binId, variation, fragmentNum}, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        int count = 0;
        if (resultSet.next())
          count = resultSet.getInt("count");
        return count;
        }
      });
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
    for (String var: variations)
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

  // TODO this could be caches for each chr/bin combination as it's fairly small
  private List<String> variationsInBin(String chr, int binId)
    {
    String sql = "SELECT distinct v.name FROM " + this.tableName + " as vpb " +
      "INNER JOIN variation as v ON v.id = vpb.variation_id " +
      "WHERE vpb.chr = ? AND vpb.bin_id = ?";

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

//  private Fragment createFragment(ResultSet rs) throws SQLException
//    {
//    Fragment fragment = new Fragment();
//    fragment.setChr(rs.getString("chr"));
//    fragment.setBinId(rs.getInt("bin_id"));
//    fragment.setSNV(rs.getInt("SNV"));
//    fragment.setDeletion(rs.getInt("deletion"));
//    fragment.setIndel(rs.getInt("indel"));
//    fragment.setInsertion(rs.getInt("insertion"));
//    fragment.setSeqAlt(rs.getInt("sequence_alteration"));
//    fragment.setSubstitution(rs.getInt("substitution"));
//    fragment.setTandemRepeat(rs.getInt("tandem_repeat"));
//    return fragment;
//    }

  }
