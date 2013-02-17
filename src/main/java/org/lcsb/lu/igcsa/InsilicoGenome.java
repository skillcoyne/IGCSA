package org.lcsb.lu.igcsa;


import org.lcsb.lu.igcsa.genome.AbstractGenome;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.prob.ProbabilitySumException;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.lcsb.lu.igcsa.utils.GenomeProperties;
import org.lcsb.lu.igcsa.genome.ReferenceGenome;
import org.lcsb.lu.igcsa.variation.SNP;
import org.lcsb.lu.igcsa.variation.VariantException;
import org.lcsb.lu.igcsa.variation.VariantType;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.File;
import java.io.FileNotFoundException;
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
  protected Genome referenceGenome;

  protected void print(String s)
    {
    System.out.println(s);
    }

  public static void main(String[] args) throws Exception
    {
    InsilicoGenome igAp = new InsilicoGenome(args);
    }

  public InsilicoGenome(String[] args) throws Exception
    {
    initProperties();
    try
      {
      setupReferenceGenome();
      }
    catch (VariantException ve)
      {
      print(ve.getMessage());
      }
    }

  protected void setupReferenceGenome() throws FileNotFoundException, ProbabilitySumException, IllegalAccessException,
  InstantiationException, VariantException
    {
    referenceGenome = new ReferenceGenome(normalProperties.getProperty("assembly"));
    referenceGenome.addChromosomes(FileUtils.getChromosomesFromFASTA(new File(normalProperties.getProperty("dir.assembly"))));

    // this would probably be a good candidate for spring injection of classes...
    for (String variation : normalProperties.getProperty("variations").split(";"))
      {
      // TODO I know there's a more general method for this but at the moment I don't know what that is
      if (variation.equals(VariantType.SNP.getShortName()))
        { setupSNPs(normalProperties.getVariationProperty("snp").getPropertySet("base"), this.referenceGenome); }
      else
        {
        VariantType vt = VariantType.fromShortName(variation);
        if (vt != null)
          {
          Class<Variation> var = vt.getVariation();
          setupSizeVariation(normalProperties.getVariationProperty(variation).getPropertySet("size"), this.referenceGenome, var.newInstance());
          }
        else
          { throw new VariantException("No VariantType defined for '" + variation + "'"); }
        }
      }
    }

  private void initProperties() throws IOException
    {
    normalProperties = GenomeProperties.readPropertiesFile(propertyFile, GenomeProperties.GenomeType.NORMAL);
    cancerProperties = GenomeProperties.readPropertiesFile(propertyFile, GenomeProperties.GenomeType.CANCER);
    }

  private void setupSizeVariation(GenomeProperties sizePropertySet, Genome genome, Variation variation) throws ProbabilitySumException
    {
    ProbabilityList pList = new ProbabilityList();
    for (String size : sizePropertySet.stringPropertyNames())
      { pList.add(new Probability(size, Double.valueOf(sizePropertySet.getProperty(size)))); }
    if (!pList.isSumOne()) throw new ProbabilitySumException(variation.getClass().toString() + " size probabilities do not sum to 1");
    genome.addVariationType(variation, pList);
    }

  private void setupSNPs(GenomeProperties snpPropertySet, Genome genome) throws ProbabilitySumException
    {
    for (char base : "ACTG".toCharArray())
      {
      String baseFrom = Character.toString(base);
      GenomeProperties baseProps = snpPropertySet.getPropertySet(baseFrom);
      // ProbabilityList per base, e.g. A has a list that encompasses A->G, A->C, A->T, A->A
      ProbabilityList pList = new ProbabilityList();
      for (String baseTo : baseProps.stringPropertyNames())
        {
        pList.add(new Probability(baseTo, Double.valueOf(baseProps.getProperty(baseTo))));
        }
      if (!pList.isSumOne()) throw new ProbabilitySumException("SNP probabilities for " + baseFrom + " do not sum to 1");
      genome.addVariationType(new SNP(new DNASequence(baseFrom)), pList);
      }
    }


  }
