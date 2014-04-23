/**
 * org.lcsb.lu.igcsa.database.util
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.database.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lcsb.lu.igcsa.karyotype.database.*;
import org.lcsb.lu.igcsa.karyotype.database.sql.JDBCAneuploidyDAO;
import org.lcsb.lu.igcsa.karyotype.database.sql.JDBCBreakpointDAO;
import org.lcsb.lu.igcsa.karyotype.database.sql.JDBCChromosomeBandDAO;
import org.lcsb.lu.igcsa.karyotype.database.sql.JDBCGeneralProbabilityDAO;


public class DerbyConnection
  {
  private static final Log log = LogFactory.getLog(DerbyConnection.class);

  private org.springframework.jdbc.datasource.DriverManagerDataSource dataSource;

  public DerbyConnection(String driverName, String url, String user, String pwd)
    {
    dataSource = new org.springframework.jdbc.datasource.DriverManagerDataSource(driverName, url, user, pwd);
    }

  public KaryotypeDAO getKaryotypeDAO()
    {
    return new KaryotypeDAO( getAneuploidyDAO(), getGeneralProbabilityDAO(), getBandDAO() );
    }

  public BreakpointDAO getBreakpointDAO()
    {
    JDBCBreakpointDAO dao = new JDBCBreakpointDAO();
    dao.setDataSource(dataSource);
    return dao;
    }

  public ChromosomeBandDAO getBandDAO()
    {
    JDBCChromosomeBandDAO dao = new JDBCChromosomeBandDAO();
    dao.setDataSource(dataSource);
    return dao;
    }

  public GeneralKarytoypeDAO getGeneralProbabilityDAO()
    {
    JDBCGeneralProbabilityDAO dao = new JDBCGeneralProbabilityDAO();
    dao.setDataSource(dataSource);
    return dao;
    }

  public AneuploidyDAO getAneuploidyDAO()
    {
    JDBCAneuploidyDAO dao = new JDBCAneuploidyDAO();
    dao.setDataSource(dataSource);
    return dao;
    }


//  <bean id="ktDataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
//  <property name="driverClassName" value="${karyotype.genome.driver}"/>
//  <property name="url" value="${karyotype.genome.url}"/>
//  <property name="username" value="${karyotype.genome.user}"/>
//  <property name="password" value="${karyotype.genome.pwd}"/>
//  </bean>



  }
