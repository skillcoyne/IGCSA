package org.lcsb.lu.igcsa;


import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
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

import static org.lcsb.lu.igcsa.utils.GenomeUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


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

    createGenomeGenerations(GenomeProperties.GenomeType.NORMAL);
    }

  public void createGenomeGenerations(GenomeProperties.GenomeType type) throws IOException
    {
    File[] directories = setupDirectories(type);
//    for (File f : directories)
//      print(f.getAbsolutePath());


    // TODO spring injection for the properties files?  might be simpler than what I'm doing...
    int generations = Integer.valueOf(normalProperties.getProperty("generations"));
    int individuals = Integer.valueOf(normalProperties.getProperty("individuals"));
    int window = Integer.valueOf(normalProperties.getProperty("window"));

    Map<Variation, ProbabilityList> rgVariations = referenceGenome.getVariations();
    int genFileIndex = 0;
    for (int g = 1; g <= generations; g++)
      {
      if (g > 1) genFileIndex += individuals;
      int length = (genFileIndex + individuals >= directories.length)? directories.length: individuals;

      File[] generationDirs = Arrays.copyOfRange(directories, genFileIndex, length);
      //TODO this could be done in threads
      for (Chromosome chr : referenceGenome.getChromosomes())
        {
        for (int i = 1; i <= individuals; i++)
          {
          //referenceGenome.mutate(chr, window);
          print("Generation " + g + " individual " + i + " chromosome " + chr.getName());
          FASTAHeader header = new FASTAHeader(">chromosome|" + chr.getName() + "|Generation " + g + " individual " + i);
          FASTAWriter writer = new FASTAWriter(new File(generationDirs[i-1], "chr" + chr.getName() + ".fa"),  header);
          referenceGenome.mutate(chr, window, writer);
          writer.close();
          }
        break;
        }
      break;
      }
    }


  protected void setupCancerGenome()
    {
    //cancerGenome = new Genome()
    }

  /*
   * Sets up the reference genome based on the fasta files for the current build.
   */
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
        referenceGenome = setupSNPs(normalProperties.getVariationProperty("snp"), this.referenceGenome);
        }
      else
        {
        VariantType vt = VariantType.fromShortName(variation);
        if (vt == null)
          { print("No VariantType defined for '" + variation + "'"); }
        else
          {
          Class<Variation> var = vt.getVariation();
          referenceGenome = setupSizeVariation(normalProperties.getVariationProperty(variation), this.referenceGenome, var.newInstance());
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
   * Creates the directories where the generated fasta files will be written
   */
  private File[] setupDirectories(GenomeProperties.GenomeType type) throws IOException
    {
    int generations = Integer.valueOf(normalProperties.getProperty("generations"));
    int individuals = Integer.valueOf(normalProperties.getProperty("individuals"));
    ArrayList<File> directories = new ArrayList<File>();

    // Directories for the generations
    String[] genomeDirs = new String[generations];
    String[] orderedSubDirs = {type.getName(), "generations"};
    for (int i = 0; i < generations; i++) genomeDirs[i] = FileUtils.directory(orderedSubDirs, i + 1);
    Collection<File> generationDirs = FileUtils.createDirectories(new File(normalProperties.getProperty("dir.insilico")), genomeDirs);
    // Directories for individuals
    for (File generation : generationDirs)
      {
      genomeDirs = new String[individuals];
      for (int i = 0; i < individuals; i++) genomeDirs[i] = FileUtils.directory("individual", i + 1);
      directories.addAll(FileUtils.createDirectories(generation, genomeDirs));
      }
    return directories.toArray(new File[directories.size()]);
    }


  }
