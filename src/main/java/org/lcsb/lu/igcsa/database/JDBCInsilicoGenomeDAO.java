package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.insilico.Genome;
import org.lcsb.lu.igcsa.database.insilico.GenomeDAO;
import org.lcsb.lu.igcsa.database.normal.Fragment;

import javax.sql.DataSource;
import java.sql.*;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCInsilicoGenomeDAO implements GenomeDAO
  {
  static Logger log = Logger.getLogger(JDBCInsilicoGenomeDAO.class.getName());

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

  public int insertGenome(int name, String location)
    {
    String sql = "INSERT INTO " + tableName + "(name, location) VALUES(?,?)";

    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, name);
      ps.setString(2, location);

      ps.executeUpdate();

      int lastId = 0;
      ResultSet rs = ps.getGeneratedKeys();
      if (rs.next())
        lastId = rs.getInt(1);
      return lastId;
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

  public Genome getGenomeByID(int id)
    {
    String sql = "SELECT * FROM " + tableName + " WHERE id = ?";

    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, id);

      Genome genome = null;

      ResultSet rs = ps.executeQuery();
      if (rs.next())
        genome = createGenome(rs);
      rs.close();
      ps.close();
      return genome;
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

  public Genome getGenomeByName(int name)
    {
    String sql = "SELECT * FROM " + tableName + " WHERE name = ?";

    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, name);

      Genome genome = null;

      ResultSet rs = ps.executeQuery();
      if (rs.next())
        genome = createGenome(rs);
      rs.close();
      ps.close();
      return genome;
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

  public Genome getGenomeByLocation(String location)
    {
    String sql = "SELECT * FROM " + tableName + " WHERE location = ?";

    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, location);

      Genome genome = null;

      ResultSet rs = ps.executeQuery();
      if (rs.next())
        genome = createGenome(rs);
      rs.close();
      ps.close();
      return genome;
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


  private Genome createGenome(ResultSet rs) throws SQLException
    {
    Genome genome = new Genome();
    genome.setId(rs.getInt("id"));
    genome.setName(rs.getInt("name"));
    genome.setLocation(rs.getString("location"));
    return genome;
    }


  }
