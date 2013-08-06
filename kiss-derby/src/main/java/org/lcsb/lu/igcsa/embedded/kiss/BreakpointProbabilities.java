/**
 * org.lcsb.lu.igcsa.embedded.kiss
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.embedded.kiss;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.embedded.kiss.DAO.DataLoadDAO;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BreakpointProbabilities
  {
  static Logger log = Logger.getLogger(BreakpointProbabilities.class.getName());

  private ClassPathXmlApplicationContext context;
  private Properties properties;

  public static void main(String[] args) throws Exception
    {
    BreakpointProbabilities bp = new BreakpointProbabilities();
    bp.loadDatabase();
    }

  public BreakpointProbabilities()
    {
    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml"});
    properties = (Properties) context.getBean("dbProperties");
    }

  void loadDatabase() throws IOException
    {
    log.info("Setting up embedded database.");
    if (Boolean.valueOf(properties.getProperty("db.create"))) loadData();
    else log.info("Database create set to false. Skipping load.");

    }

  void loadData() throws IOException
    {
    Map<String, String> loadPairs = new HashMap<String, String>();
    loadPairs.put("chrDAO", "chr.instability");
    loadPairs.put("centromereDAO", "centromere.probabilities");
    loadPairs.put("bpDAO", "bp.probabilities");
    loadPairs.put("ploidyDAO", "ploidy.probabilities");
    loadPairs.put("karyotypeDAO", "karyotype.probabilities");

    for(Map.Entry<String, String> entry: loadPairs.entrySet())
      {
      DataLoadDAO dao = (DataLoadDAO) context.getBean(entry.getKey());
      dao.insertData(properties.getProperty(entry.getValue()));
      log.info(dao.getTableCount());
      }
    }

    }
