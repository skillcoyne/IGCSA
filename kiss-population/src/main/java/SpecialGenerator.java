import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collection;


/**
 * PACKAGE_NAME
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SpecialGenerator
  {
  static Logger log = Logger.getLogger(SpecialGenerator.class.getName());
  private static ClassPathXmlApplicationContext context;

  public static void main(String[] args) throws Exception
    {
    new SpecialGenerator().run();
    }

  public SpecialGenerator()
    {
    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/karyotype.xml"});
    }

  public void run() throws ProbabilityException
    {
    KaryotypeDAO dao = (KaryotypeDAO) context.getBean("karyotypeDAO");

    Probability bpCountProb = dao.getGeneralKarytoypeDAO().getProbabilityClass("aberration"); // not really used right now
    Probability bandProbability = dao.getGeneralKarytoypeDAO().getOverallBandProbabilities();

    log.info( dao.getGeneralKarytoypeDAO().getBandProbabilities("5").getRawProbabilities() );

    log.info( dao.getGeneralKarytoypeDAO().getBandProbabilities("7").getRawProbabilities() );


//    Collection<Band> allPossibleBands = new ArrayList<Band>();
//    for (Object b : bandProbability.getRawProbabilities().keySet())
//      allPossibleBands.add((Band) b);
//
//    log.info(allPossibleBands);

    }

  }
