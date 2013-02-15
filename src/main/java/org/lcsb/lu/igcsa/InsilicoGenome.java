package org.lcsb.lu.igcsa;


import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.utils.GenomeProperties;
import org.lcsb.lu.igcsa.genome.ReferenceGenome;
import org.lcsb.lu.igcsa.variation.SNP;

import java.io.IOException;


/**
 * org.lcsb.lu.igcsa
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class InsilicoGenome
  {
  private static final String propertyFile = "genome.properties";
  protected GenomeProperties normalProperties;
  protected GenomeProperties cancerProperties;
  protected ReferenceGenome referenceGenome;

  public static void main(String[] args) throws Exception
    {
    InsilicoGenome igAp = new InsilicoGenome(args);
    }

  public InsilicoGenome(String[] args) throws Exception
    {
    initProperties();
    }


  protected void setupReferenceGenome()
    {
    referenceGenome = new ReferenceGenome( normalProperties.getProperty("assembly"), normalProperties.getProperty("dir.assembly") );

    // this would probably be a good candidate for spring injection of classes...
    for (String variation: normalProperties.getProperty("variations").split(";"))
      {
      if (variation == "snp") setupSNPs(normalProperties.getVariationProperty("snp").getPropertySet("base"), this.referenceGenome);
      else
        {

        }

      }


    }


  protected void initProperties() throws IOException
    {
    normalProperties = GenomeProperties.readPropertiesFile(propertyFile, GenomeProperties.GenomeType.NORMAL);
    cancerProperties = GenomeProperties.readPropertiesFile(propertyFile, GenomeProperties.GenomeType.CANCER);
    }

  private void setupSNPs(Genome genome, GenomeProperties props)
    {
    for (String baseFrom: "ACTG".split(""))
      {
      // ProbabilityList per base, e.g. A has a list that encompasses A->G, A->C, A->T, A->A
      ProbabilityList pList = new ProbabilityList();
      for (String baseTo: props.getPropertySet(baseFrom).stringPropertyNames())
        {
        pList.add( new Probability(baseTo, Double.valueOf( props.getProperty(baseFrom + "." + baseTo) )) );
        }
      genome.addVariationType(new SNP(new DNASequence(baseFrom)), pList);
      }
    }


  }
