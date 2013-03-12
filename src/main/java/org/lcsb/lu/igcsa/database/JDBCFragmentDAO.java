package org.lcsb.lu.igcsa.database;

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
public class JDBCFragmentDAO implements FragmentDAO
  {


  private DataSource dataSource;

  public void setDataSource(DataSource dataSource)
    {
    this.dataSource = dataSource;
    }

  public Fragment getByRow(int rowNum)
    {
    String sql = "SELECT * FROM Fragment WHERE id = ?";

    Connection conn = null;

    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, rowNum);
      Fragment fragment = null;
      ResultSet rs = ps.executeQuery();
      if (rs.next())
        {
        fragment = new Fragment();
        fragment.setSNV(rs.getInt("SNV"));
        fragment.setDeletion(rs.getInt("deletion")); // etc....
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
          }
        }
      }
    }

  public int insertFragment(Fragment fragment)
    {
    String sql = "INSERT INTO Fragment (SNV, deletion, indel, insertion, sequence_alteration, substitution, tandem_repeat, GCRatio, UnkRatio) " + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) ";

    Connection conn = null;

    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, fragment.getSNV());
      ps.setInt(2, fragment.getDeletion());
      //etc...
      int row = ps.executeUpdate();
      ps.close();
      return row;
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
          }
        }
      }
    }
  }
