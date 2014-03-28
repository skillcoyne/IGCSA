package org.lcsb.lu.igcsa.embedded;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.embedded.DAO.*;
import org.lcsb.lu.igcsa.embedded.DAO.impl.JDBCFragmentDAO;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

@Deprecated /* Moved everything to HBase at this point */
public class GenomeVariation
  {
  static Logger log = Logger.getLogger(GenomeVariation.class.getName());

  private ClassPathXmlApplicationContext context;
  private Properties properties;

  public static void main(String[] args) throws Exception
    {
    GenomeVariation gv = new GenomeVariation();
    gv.loadDatabase();
    //gv.addIndex();
    }

  public GenomeVariation()
    {
    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml"});
    properties = (Properties) context.getBean("dbProperties");
    }

//  void addIndex()
//    {
//    log.info("Indexing fragment table");
//    JDBCFragmentDAO fragmentDAO = (JDBCFragmentDAO) context.getBean("fragmentDAO");
//    fragmentDAO.addIndex();
//    }

  void loadDatabase() throws SQLException, IOException
    {
    log.info("Setting up embedded database.");
    if (Boolean.valueOf(properties.getProperty("db.create"))) loadData();
    else log.info("Database create set to false. Skipping load.");
    }

  void loadData() throws IOException
    {
    DataLoadDAO variationDAO = (DataLoadDAO) context.getBean("variationDAO");
    variationDAO.insertData(properties.getProperty("variation"));
    log.info(variationDAO.getTableCount());

    DataLoadDAO chrVarDAO = (DataLoadDAO) context.getBean("chrVarDAO");
    chrVarDAO.insertData(properties.getProperty("variation.to.chr"));
    log.info(chrVarDAO.getTableCount());

    DataLoadDAO gcBinDAO = (DataLoadDAO) context.getBean("gcBinDAO");
    gcBinDAO.insertData(properties.getProperty("gc.bins"));
    log.info(gcBinDAO.getTableCount());

    DataLoadDAO sizeDAO = (DataLoadDAO) context.getBean("sizeDAO");
    sizeDAO.insertData(properties.getProperty("variation.size.prob"));
    log.info(sizeDAO.getTableCount());

    DataLoadDAO snvProbabilityDAO = (DataLoadDAO) context.getBean("snvDAO");
    snvProbabilityDAO.insertData(properties.getProperty("snv.prob"));
    log.info(snvProbabilityDAO.getTableCount());

    DataLoadDAO fragmentDAO = (DataLoadDAO) context.getBean("fragmentDAO");
    fragmentDAO.insertData(properties.getProperty("variation.per.bin"));
    log.info(fragmentDAO.getTableCount());
    }

  }