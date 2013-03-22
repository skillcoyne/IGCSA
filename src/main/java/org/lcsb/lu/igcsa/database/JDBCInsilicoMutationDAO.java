package org.lcsb.lu.igcsa.database;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.insilico.Genome;
import org.lcsb.lu.igcsa.database.insilico.Mutation;
import org.lcsb.lu.igcsa.database.insilico.MutationDAO;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * org.lcsb.lu.igcsa.database
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCInsilicoMutationDAO implements MutationDAO
  {
  static Logger log = Logger.getLogger(JDBCInsilicoMutationDAO.class.getName());

  private DataSource dataSource;
  private String tableName;

  public void setDataSource(DataSource dataSource)
    {
    this.dataSource = dataSource;
    }

  public void setTableName(String tableName)
    {
    this.tableName = tableName;
    }

  public int insertMutation(Mutation mutation)
    {
    String sql = "INSERT INTO " + tableName + " (genome_id, chr, fragment, location, variation, sequence) VALUES (?,?,?,?,?,?)";
    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, mutation.getGenomeId());
      ps.setString(2, mutation.getChromosome());
      ps.setInt(3, mutation.getFragment());
      ps.setInt(4, mutation.getStartLocation());
      ps.setString(5, mutation.getVariationType());
      ps.setString(6, mutation.getSequence());

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

  public Mutation[] getMutationsByGenome(Genome genome)
    {
    String sql = "SELECT * FROM " + tableName + " WHERE genome_id = ?";
    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, genome.getId());

      ResultSet rs = ps.executeQuery();
      Mutation[] mutations = createMutations(rs);
      rs.close();
      ps.close();
      return mutations;
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

  public Mutation[] getMutationsByChromosome(Genome genome, String chromosome)
    {
    String sql = "SELECT * FROM " + tableName + " WHERE genome_id = ? AND chromosome = ?";
    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, genome.getId());
      ps.setString(2, chromosome);

      ResultSet rs = ps.executeQuery();
      Mutation[] mutations = createMutations(rs);
      rs.close();
      ps.close();
      return mutations;
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

  public Mutation[] getMutationsByFragment(Genome genome, String chromosome, int fragment)
    {
    String sql = "SELECT * FROM " + tableName + " WHERE genome_id = ? AND chromosome = ? AND fragment = ?";
    Connection conn = null;
    try
      {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, genome.getId());
      ps.setString(2, chromosome);
      ps.setInt(3, fragment);

      ResultSet rs = ps.executeQuery();
      Mutation[] mutations = createMutations(rs);
      rs.close();
      ps.close();
      return mutations;
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


  private Mutation[] createMutations(ResultSet rs) throws SQLException
    {
    Collection<Mutation> mutations = new ArrayList<Mutation>();
    while (rs.next()) mutations.add(createMutation(rs));
    return mutations.toArray(new Mutation[mutations.size()]);
    }


  private Mutation createMutation(ResultSet rs) throws SQLException
    {
    Mutation mutation = new Mutation();
    mutation.setGenomeId(rs.getInt("genome_id"));
    mutation.setChromosome(rs.getString("chromosome"));
    mutation.setFragment(rs.getInt("fragment"));
    mutation.setStartLocation(rs.getInt("location"));
    mutation.setVariationType(rs.getString("variation"));
    mutation.setSequence(rs.getString("sequence"));
    return mutation;
    }

  }
