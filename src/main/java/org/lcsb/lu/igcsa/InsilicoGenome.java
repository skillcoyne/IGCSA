package org.lcsb.lu.igcsa;


import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.lcsb.lu.igcsa.utils.GenomeProperties;
import org.lcsb.lu.igcsa.genome.ReferenceGenome;
import org.lcsb.lu.igcsa.variation.SNP;
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
  protected Genome cancerGenome;

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
    setupReferenceGenome();

    int generations = Integer.valueOf(normalProperties.getProperty("generations"));

    }

  public void createGenomeGenerations(GenomeProperties.GenomeType type)
    {
    Genome genome = referenceGenome;
    if (type.equals(GenomeProperties.GenomeType.CANCER)) genome = cancerGenome;



    }


  protected void setupCancerGenome()
    {
    //cancerGenome = new Genome()
    }

  protected void setupReferenceGenome() throws FileNotFoundException, ProbabilityException, IllegalAccessException, InstantiationException
    {
    referenceGenome = new ReferenceGenome(normalProperties.getProperty("assembly"));
    referenceGenome.addChromosomes(FileUtils.getChromosomesFromFASTA(new File(normalProperties.getProperty("dir.assembly"))));

    // this would probably be a good candidate for spring injection of classes...
    for (String variation : normalProperties.getProperty("variations").split(";"))
      {
      // TODO I know there's a more general method for this but at the moment I don't know what that is
      if (variation.equals(VariantType.SNP.getShortName()))
        {
        setupSNPs(normalProperties.getVariationProperty("snp"), this.referenceGenome);
        }
      else
        {
        VariantType vt = VariantType.fromShortName(variation);
        if (vt == null)
          { print("No VariantType defined for '" + variation + "'"); }
        else
          {
          Class<Variation> var = vt.getVariation();
          setupSizeVariation(normalProperties.getVariationProperty(variation), this.referenceGenome, var.newInstance());
          }
        }
      }
    }

  private void initProperties() throws IOException
    {
    normalProperties = GenomeProperties.readPropertiesFile(propertyFile, GenomeProperties.GenomeType.NORMAL);
    cancerProperties = GenomeProperties.readPropertiesFile(propertyFile, GenomeProperties.GenomeType.CANCER);
    }

  /*
  Deletions/insertions/inversions all have a size related probability.
   */
  private void setupSizeVariation(GenomeProperties varPropertySet, Genome genome, Variation variation) throws ProbabilityException
    {
    ProbabilityList pList = new ProbabilityList();

    GenomeProperties sizeProps = varPropertySet.getPropertySet("size");
    for (String size : sizeProps.stringPropertyNames())
      {
      pList.add(new Probability(size, Double.valueOf(sizeProps.getProperty(size)), Double.valueOf(varPropertySet.getProperty("freq"))  ));
      }
    if (!pList.isSumOne()) throw new ProbabilityException(variation.getClass().toString() + " size probabilities do not sum to 1");

    genome.addVariationType(variation, pList);
    }

  /*
  For SNPs there is a probability for each nucleotide being any one of the others
  ProbabilityList per base, e.g. A has a list that encompasses A->G, A->C, A->T, A->A
   */
  private void setupSNPs(GenomeProperties snpPropertySet, Genome genome) throws ProbabilityException
    {
    double frequency = Double.valueOf(snpPropertySet.getProperty("freq"));

    for (char base : "ACTG".toCharArray())
      {
      String baseFrom = Character.toString(base);
      GenomeProperties baseProps = snpPropertySet.getPropertySet("base").getPropertySet(baseFrom);

      ProbabilityList pList = new ProbabilityList();
      for (String baseTo : baseProps.stringPropertyNames())
        {
        pList.add(new Probability(baseTo, Double.valueOf(baseProps.getProperty(baseTo)), frequency));
        }
      if (!pList.isSumOne()) throw new ProbabilityException("SNP probabilities for " + baseFrom + " do not sum to 1");

      genome.addVariationType(new SNP(new DNASequence(baseFrom)), pList);
      }
    }


  }
