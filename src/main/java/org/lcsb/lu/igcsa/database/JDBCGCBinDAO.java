package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.Bin;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;

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

  private DataSource dataSource;
  private String tableName;

  private Map<String, Integer> maxBins = new HashMap<String, Integer>();


  public void setTableName(String tableName)
    {
    this.tableName = tableName;
    }

  public void setDataSource(DataSource dataSource)
    {
    this.dataSource = dataSource;
    }

  private int maxBin(String chr)
    {
    // this only really needs to be checked once
    if (!maxBins.containsKey(chr))
      {
      String sql = "SELECT * FROM gc_bins WHERE chr = ? order by max desc limit 0,1";

      Connection conn = null;
      try
        {
        conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, chr);
        ResultSet rs = ps.executeQuery();
        int max = 0;
        if (rs.next())
          {
          max = rs.getInt("max");
          }
        rs.close();
        ps.close();
        maxBins.put(chr, max);
        }
      catch (SQLException e)
        {
        throw new RuntimeException(e);
        }
      finally
        {
        if (conn != null)
          {
          try
            {
            conn.close();
            }
          catch (SQLException e)
            {
            log.warn(e.getMessage());
            }
          }
        }
      }
    return maxBins.get(chr);
    }

  public Bin getBinByGC(String chr, int gcContent)
    {
    int maxBin = maxBin(chr);
    log.debug("MAX BIN FOR " + chr + " = " + maxBin);

    Connection conn = null;

    try
      {
      conn = dataSource.getConnection();
      String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? AND (min <= ? AND max >= ?)";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, chr);
      ps.setInt(2, gcContent);
      ps.setInt(3, gcContent);

      if (maxBin < gcContent)
        {
        sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? AND max = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, chr);
        ps.setInt(2, maxBin);
        }

      log.debug(sql);
      log.debug(chr + ": gc " + gcContent);

      Bin gcBin = null;
      ResultSet rs = ps.executeQuery();

      if (rs.next())
        gcBin = createBin(rs);

      rs.close();
      ps.close();
      ;
      return gcBin;
      }
    catch (SQLException e)
      {
      throw new RuntimeException(e);
      }
    finally
      {
      if (conn != null)
        {
        try
          {
          conn.close();
          }
        catch (SQLException e)
          {
          log.warn(e.getMessage());
          }
        }
      }
    }

  public Bin getBinById(String chr, int binId)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? AND bin_id = ?";

    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, chr);
      ps.setInt(2, binId);
      Bin gcBin = null;
      ResultSet rs = ps.executeQuery();
      if (rs.next())
        {
        gcBin = createBin(rs);
        }
      rs.close();
      ps.close();
      return gcBin;
      }
    catch (SQLException e)
      {
      throw new RuntimeException(e);
      }
    finally
      {
      if (conn != null)
        {
        try
          {
          conn.close();
          }
        catch (SQLException e)
          {
          log.warn(e.getMessage());
          }
        }
      }
    }

  public Bin[] getBins(String chr)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ?";

    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, chr);

      Collection<Bin> bins = new ArrayList<Bin>();
      ResultSet rs = ps.executeQuery();
      while (rs.next())
        {
        bins.add(createBin(rs));
        }
      rs.close();
      ps.close();

      return bins.toArray(new Bin[bins.size()]);
      }
    catch (SQLException e)
      {
      throw new RuntimeException(e);
      }
    finally
      {
      if (conn != null)
        {
        try
          {
          conn.close();
          }
        catch (SQLException e)
          {
          log.warn(e.getMessage());
          }
        }
      }
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
