/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

package org.lcsb.lu.igcsa;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.database.ChromosomeBandDAO;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.Properties;

public class BandIndex
  {
  static Logger log = Logger.getLogger(BandIndex.class.getName());

  public static void main(String[] args) throws Exception
    {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml", "classpath*:/conf/database-config.xml"});

    Properties props = (Properties) context.getBean("genomeProperties");

    File dir = new File(props.getProperty("dir.assembly"));
    String outdir = "/tmp/Test/19";

    File fastaFile = FileUtils.getFASTA("19", dir);

    FASTAReader reader = new FASTAReader(fastaFile);

    ChromosomeBandDAO dao = (ChromosomeBandDAO) context.getBean("bandDAO");

    Band[] bands = dao.getBands("19");

    for (Band b: bands)
      {
      log.info("Writing " + b.toString());

      String filename = b.getLocation().getStart() + "-" + b.getLocation().getEnd();
      FASTAWriter writer = new FASTAWriter(new File(outdir, filename), new FASTAHeader( reader.getHeader().getDB(), reader.getHeader().getAccession(), reader.getHeader().getLocus(), b.getLocation().toString()   ));

      reader.streamToWriter(b.getLocation().getStart(), b.getLocation().getLength(), writer);
      }




    }


  }
