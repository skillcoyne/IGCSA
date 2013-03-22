package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.database.normal.FragmentVariationDAO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
  private String tableName;

  public void setTableName(String tableName)
    {
    this.tableName = tableName;
    }

  public void setDataSource(DataSource dataSource)
    {
    this.dataSource = dataSource;
    }

  public Fragment getFragment(String chr, int binId, int fragmentNum)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? AND bin_id = ? LIMIT " + fragmentNum + ", 1";
    log.debug(sql);
    log.debug("chr = " + chr + " bin_id = " + binId);

    Connection conn = null;

    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, chr);
      ps.setInt(2, binId);


      Fragment fragment = null;
      ResultSet rs = ps.executeQuery();
      if (rs.next())
        {
        fragment = createFragment(rs);
        }
      rs.close();
      ps.close();
      return fragment;
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


  private Fragment createFragment(ResultSet rs) throws SQLException
    {
    Fragment fragment = new Fragment();
    fragment.setChr(rs.getString("chr"));
    fragment.setBinId(rs.getInt("bin_id"));
    fragment.setSNV(rs.getInt("SNV"));
    fragment.setDeletion(rs.getInt("deletion"));
    fragment.setIndel(rs.getInt("indel"));
    fragment.setInsertion(rs.getInt("insertion"));
    fragment.setSeqAlt(rs.getInt("sequence_alteration"));
    fragment.setSubstitution(rs.getInt("substitution"));
    fragment.setTandemRepeat(rs.getInt("tandem_repeat"));
    return fragment;
    }

  }
