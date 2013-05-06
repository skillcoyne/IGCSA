package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.Bin;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCGCBinDAO implements GCBinDAO
  {
  static Logger log = Logger.getLogger(GCBinDAO.class.getName());

  private JdbcTemplate jdbcTemplate;
  private String tableName = "gc_bins";

  private Map<String, Integer> maxBins = new HashMap<String, Integer>();


  public void setTableName(String tableName)
    {
    this.tableName = tableName;
    }

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);
    }

  private int maxBin(String chr)
    {
    if (!maxBins.containsKey(chr))
      {
      String sql = "SELECT * FROM " + tableName + " WHERE chr = ? ORDER BY max DESC LIMIT 0,1";
      Bin gcBin = (Bin) jdbcTemplate.query(sql, new Object[]{chr}, new ResultSetExtractor<Object>()
        {
        public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
          {
          Bin gcBin = null;
          if (resultSet.next())
            gcBin = createBin(resultSet);
          return gcBin;
          }
        });
      maxBins.put(chr, gcBin.getMax());
      }
    return maxBins.get(chr);
    }

  public Bin getBinByGC(String chr, int gcContent)
    {
    int maxBin = maxBin(chr);
    log.debug("MAX BIN FOR " + chr + " = " + maxBin);

    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? AND (min <= ? AND max >= ?)";
    Object[] queryObj = new Object[]{chr, gcContent, gcContent};

    if (maxBin < gcContent)
      {
      sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? AND max = ?";
      queryObj = new Object[]{chr, maxBin};
      }

    return (Bin) jdbcTemplate.query(sql, queryObj, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Bin gcBin = null;
        if (resultSet.next())
          gcBin = createBin(resultSet);
        return gcBin;
        }
      });
    }

  public Bin getBinById(String chr, int binId)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? AND bin_id = ?";

    return (Bin) jdbcTemplate.query(sql, new Object[]{chr, binId}, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Bin gcBin = null;
        if (resultSet.next())
          gcBin = createBin(resultSet);
        return gcBin;
        }
      });
    }

  public Bin[] getBins(String chr)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ?";

    return (Bin[]) jdbcTemplate.query(sql, new Object[]{chr}, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Collection<Bin> bins = new ArrayList<Bin>();
        while (resultSet.next())
          bins.add(createBin(resultSet));
        return bins.toArray(new Bin[bins.size()]);
        }
      });
    }

  private Bin createBin(ResultSet rs) throws SQLException
    {
    Bin gcBin = new Bin();
    gcBin.setChr(rs.getString("chr"));
    gcBin.setBinId(rs.getInt("bin_id"));
    gcBin.setMin(rs.getInt("min"));
    gcBin.setMax(rs.getInt("max"));
    gcBin.setSize(rs.getInt("total_fragments"));
    return gcBin;
    }

  }


