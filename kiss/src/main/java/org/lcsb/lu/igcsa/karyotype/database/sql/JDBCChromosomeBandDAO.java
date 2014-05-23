package org.lcsb.lu.igcsa.karyotype.database.sql;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.karyotype.database.ChromosomeBandDAO;
import org.lcsb.lu.igcsa.genome.Location;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.database.sql
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class JDBCChromosomeBandDAO implements ChromosomeBandDAO
  {
  static Logger log = Logger.getLogger(JDBCChromosomeBandDAO.class.getName());

  private JdbcTemplate jdbcTemplate;
  private final String tableName = "chromosome_bands";

  private boolean isDerby = false;

  public void setDataSource(DataSource dataSource)
    {
    jdbcTemplate = new JdbcTemplate(dataSource);

    try
      {
      String dbName = dataSource.getConnection().getMetaData().getDatabaseProductName();
      if (dbName.equals("Apache Derby"))
        {
        log.info(dbName);
        isDerby = true;
        }
      }
    catch (SQLException e)
      {
      throw new RuntimeException(e);
      }
    }

  @Override
  public Band getBand(String chrBand)
    {
    String sql = "SELECT * FROM ( SELECT *, CONCAT(chr, band) AS chrloc FROM " + this.tableName + ") a " +
        "WHERE chrloc = ?";

    return (Band) jdbcTemplate.query(sql, new Object[]{chrBand}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Band band = null;
      if (resultSet.next())
        band = createBand(resultSet);
      return band;
      }
    });

    }

  @Override
  public Band getBandByChromosomeAndName(String chrName, String bandName)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? and band = ?";

    return (Band) jdbcTemplate.query(sql, new Object[]{chrName, bandName}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Band band = null;// = new Band();
      if (resultSet.next())
        band = createBand(resultSet);
      return band;
      }
    });
    }

  @Override
  public Band[] getBands(String chrName)
    {
    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ?";

    return (Band[]) jdbcTemplate.query(sql, new Object[]{chrName}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      List<Band> bands = new ArrayList<Band>();
      while (resultSet.next())
        {
        bands.add(createBand(resultSet));
        }
      return bands.toArray(new Band[bands.size()]);
      }
    });

    }

  public Band getFirstBand(String chrName)
    {
    Band[] bands = getBands(chrName);
    return bands[0];
    }

  public Band getLastBand(String chrName)
    {
    Band[] bands = getBands(chrName);
    return bands[bands.length-1];
    }


  @Override
  public Location getLocation(Band band)
    {
    return getBandByChromosomeAndName(band.getChromosomeName(), band.getBandName()).getLocation();
    }

  @Override
  public Band getTerminus(String chr, String arm)
    {
    String limit = " LIMIT 0,1";
    if (isDerby)
      limit = " OFFSET 0 ROWS 1";

    String sql = "SELECT * FROM " + this.tableName + " WHERE chr = ? ORDER BY ";
    if (arm.equals("p"))
      sql = sql + " start ASC " + limit;
    else
      sql = sql + " end DESC " + limit;

    return (Band) jdbcTemplate.query(sql, new Object[]{chr}, new ResultSetExtractor<Object>()
    {
    @Override
    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException
      {
      Band band = null; //new Band();
      if (resultSet.next())
        band = createBand(resultSet);
      return band;
      }
    });
    }

  private Band createBand(ResultSet resultSet) throws SQLException
    {
    Band band = new Band(resultSet.getString("chr"), resultSet.getString("band"));
    band.setLocation(new Location(band.getChromosomeName(), resultSet.getInt("start_loc"), resultSet.getInt("end_loc")));
    return band;
    }


  }
