package org.lcsb.lu.igcsa.database.normal.sql;

import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.database.normal.Bin;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
import org.lcsb.lu.igcsa.sql.StreamingStatementCreator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
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
  private final String tableName = "gc_bins";

  private Map<String, Integer> maxBins = new HashMap<String, Integer>();
  private Map<String, Bin[]> chrBins = new HashMap<String, Bin[]>();

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);
    }

  private int maxBin(String chr)
    {
    log.debug("maxBin(" + chr + ")");
    if (!maxBins.containsKey(chr))
      {
      String sql = "SELECT * FROM " + tableName + " WHERE chr = ? ORDER BY maximum DESC";
      log.debug(sql);

      final String chromosome = chr;
      Bin gcBin = (Bin) jdbcTemplate.query(new StreamingStatementCreator(sql, 1), new PreparedStatementSetter()
          {
          @Override
          public void setValues(PreparedStatement preparedStatement) throws SQLException
            {
            preparedStatement.setString(1, chromosome);
            }
          }, new ResultSetExtractor<Object>()
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

    Bin match = null;
    if (maxBin < gcContent)
      {
      for (Bin bin : getBins(chr))
        {
        if (bin.getMax() == maxBin)
          {
          match = bin;
          break;
          }
        }
      }
    else
      {
      for (Bin bin : getBins(chr))
        {
        if (bin.getRange().containsInteger(gcContent))
          {
          match = bin;
          break;
          }
        }
      }


//    long elapse = System.currentTimeMillis() - start;
//    log.info(chr + " " + gcContent + " time: " + elapse);
    return match;
    }

  public Bin getBinById(String chr, int binId)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? AND bin_id = ?";
    log.debug("getBinById(" + chr + ", " + binId + "): " + sql);

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
    if (!chrBins.containsKey(chr))
      {

      String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ?";
      log.debug("getBins(" + chr + "): " + sql);

      Bin[] bins = (Bin[]) jdbcTemplate.query(sql, new Object[]{chr}, new ResultSetExtractor<Object>()
      {
      public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
        Collection<Bin> bins = new ArrayList<Bin>();
        while (resultSet.next())
          bins.add(createBin(resultSet));
        return bins.toArray(new Bin[bins.size()]);
        }
      });
      chrBins.put(chr, bins);
      }
    return chrBins.get(chr);
    }

  private Bin createBin(ResultSet rs) throws SQLException
    {
    Bin gcBin = new Bin();
    gcBin.setChr(rs.getString("chr"));
    gcBin.setBinId(rs.getInt("bin_id"));
    gcBin.setMin(rs.getInt("minimum"));
    gcBin.setMax(rs.getInt("maximum"));
    gcBin.setSize(rs.getInt("total_fragments"));
    return gcBin;
    }


  }


