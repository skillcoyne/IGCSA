package org.lcsb.lu.igcsa.database.sql;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Breakpoint;
import org.lcsb.lu.igcsa.database.BreakpointDAO;
import org.lcsb.lu.igcsa.genome.Location;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * org.lcsb.lu.igcsa.database.sql.karyotype
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCBreakpointDAO implements BreakpointDAO
  {
  static Logger log = Logger.getLogger(JDBCBreakpointDAO.class.getName());

  private JdbcTemplate jdbcTemplate;
  private final String tableName = "breakpoints";

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);
    }

  @Override
  public Breakpoint[] getBreakpointsForClass(int bpclass)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE class = ?";

    return (Breakpoint[]) jdbcTemplate.query(sql, new Object[]{bpclass}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      ArrayList<Breakpoint> bpList = new ArrayList<Breakpoint>();
      while (resultSet.next())
        {
        bpList.add(createBP(resultSet));
        }
      return bpList.toArray(new Breakpoint[bpList.size()]);
      }
    });
    }

  @Override
  public Breakpoint[] getBreakpointsForChr(String chr)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ?";

    return (Breakpoint[]) jdbcTemplate.query(sql, new Object[]{chr}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      ArrayList<Breakpoint> bpList = new ArrayList<Breakpoint>();
      while (resultSet.next())
        {
        bpList.add(createBP(resultSet));
        }
      return bpList.toArray(new Breakpoint[bpList.size()]);
      }
    });
    }

  @Override
  public Breakpoint getBreakpoint(String chr, String band, int bpclass)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? AND band = ? AND class = ?";

    return (Breakpoint) jdbcTemplate.query(sql, new Object[]{chr, band, bpclass}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Breakpoint bp = new Breakpoint();
      if (resultSet.next())
        bp = createBP(resultSet);
      return bp;
      }
    });
    }

  private Breakpoint createBP(ResultSet resultSet) throws SQLException
    {
    Breakpoint bp = new Breakpoint();
    bp.setChromosome(resultSet.getString("chr"));
    bp.setBand(resultSet.getString("band"));
    bp.setLocation(new Location(resultSet.getInt("start"), resultSet.getInt("end")));
    bp.setProbability(resultSet.getDouble("prob"));
    return bp;
    }

  }
