package org.lcsb.lu.igcsa;


import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Bin;
import org.lcsb.lu.igcsa.database.Fragment;
import org.lcsb.lu.igcsa.database.FragmentVariationDAO;
import org.lcsb.lu.igcsa.database.GCBinDAO;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.genome.MutableGenome;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.lcsb.lu.igcsa.variation.VariantType;
import org.lcsb.lu.igcsa.variation.Variation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

import static org.lcsb.lu.igcsa.utils.GenomeUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


/**
 * org.lcsb.lu.igcsa
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

//@ContextConfiguration (locations={"classpath:spring-config.xml"})
public class InsilicoGenome
  {
  public enum GenomeType
    {
      NORMAL("normal"), CANCER("cancer");

    private String name;

    private GenomeType(String s)
      {
      name = s;
      }

    public String getName()
      {
      return name;
      }
    }


  static Logger log = Logger.getLogger(InsilicoGenome.class.getName());

  protected FragmentVariationDAO fragmentDAO;
  protected GCBinDAO binDAO;

  protected Properties normalProperties;
  protected Properties cancerProperties;

  protected Genome referenceGenome;
  protected Genome cancerGenome;

  // Defaults
  private int generations = 5;
  private int individuals = 5;
  private int windowSize = 1000;


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
    init();

    log.debug(normalProperties.getProperty("assembly"));

    //setupReferenceGenome();


    //createGenomeGenerations(GenomeProperties.GenomeType.NORMAL);
    }

  private void init()
    {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
    fragmentDAO = (FragmentVariationDAO) context.getBean("FragmentDAO");
    binDAO = (GCBinDAO) context.getBean("GCBinDAO");

    normalProperties = (Properties) context.getBean("normalGenomeProperties");
    cancerProperties = (Properties) context.getBean("cancerGenomeProperties");

    if (normalProperties.containsKey("generations") && normalProperties.containsKey("individuals"))
      {
      this.generations = Integer.valueOf(normalProperties.getProperty("generations"));
      this.individuals = Integer.valueOf(normalProperties.getProperty("individuals"));
      }

    if (normalProperties.containsKey("window"))
      this.windowSize = Integer.valueOf(normalProperties.getProperty("window"));
    }


  public void createGenomeGenerations(GenomeType type) throws IOException
    {
    File[] directories = setupDirectories(type);

    final int generations = this.generations;
    final int individuals = this.individuals;
    final int window = this.windowSize;

    final Map<Variation, ProbabilityList> rgVariations = referenceGenome.getVariations();
    int genFileIndex = 0;
    for (int g = 1; g <= generations; g++)
      {
      if (g > 1)
        genFileIndex += individuals;
      int length = (genFileIndex + individuals >= directories.length) ? directories.length : individuals;

      final File[] generationDirs = Arrays.copyOfRange(directories, genFileIndex, length);
      //TODO this could be done in threads
      for (Chromosome chr : referenceGenome.getChromosomes())
        {
        for (int i = 1; i <= individuals; i++)
          {
          //referenceGenome.mutate(chr, window);
          print("Generation " + g + " individual " + i + " chromosome " + chr.getName());
          try
            {
            FASTAHeader header = new FASTAHeader(">chromosome|" + chr.getName() + "|Generation " + g + " individual " + i);
            FASTAWriter writer = new FASTAWriter(new File(generationDirs[i - 1], "chr" + chr.getName() + ".fa"), header);
            referenceGenome.mutate(chr, window, writer);
            writer.close();
            }
          catch (IOException e)
            {
            e.printStackTrace();
            }
          }
        break;
        }
      break;
      }
    }


  protected void setupCancerGenome()
    {
    cancerGenome = new MutableGenome(this.cancerProperties.getProperty("assembly"));
    }

  /*
   * Sets up the reference genome based on the fasta files for the current build.
   */
  //  protected void setupReferenceGenome() throws FileNotFoundException, ProbabilityException, IllegalAccessException, InstantiationException
  //    {
  //    referenceGenome = new MutableGenome(normalProperties.getProperty("assembly"));
  //    referenceGenome.addChromosomes(FileUtils.getChromosomesFromFASTA(new File(normalProperties.getProperty("dir.assembly"))));
  //
  //    // TODO this would probably be a good candidate for spring injection of classes...
  //    for (String variation : normalProperties.getProperty("variations").split(";"))
  //      {
  //      // I know there's a more general method for this but at the moment I don't know what that is
  //      if (variation.equals(VariantType.SNP.getShortName()))
  //        {
  //        referenceGenome = setupSNPs(normalProperties.getVariationProperty("snp"), this.referenceGenome);
  //        }
  //      else
  //        {
  //        VariantType vt = VariantType.fromShortName(variation);
  //        if (vt == null)
  //          { print("No VariantType defined for '" + variation + "'"); }
  //        else
  //          {
  //          Class<Variation> var = vt.getVariation();
  //          referenceGenome = setupSizeVariation(normalProperties.getVariationProperty(variation), this.referenceGenome, var.newInstance());
  //          }
  //        }
  //      }
  //    }

  /*
   * Creates the directories where the generated fasta files will be written
   */
  private File[] setupDirectories(GenomeType type) throws IOException
    {
    int generations = Integer.valueOf(normalProperties.getProperty("generations"));
    int individuals = Integer.valueOf(normalProperties.getProperty("individuals"));
    ArrayList<File> directories = new ArrayList<File>();

    // Directories for the generations
    String[] genomeDirs = new String[generations];
    String[] orderedSubDirs = {type.getName(), "generations"};
    for (int i = 0; i < generations; i++)
      genomeDirs[i] = FileUtils.directory(orderedSubDirs, i + 1);
    Collection<File> generationDirs = FileUtils.createDirectories(new File(normalProperties.getProperty("dir.insilico")), genomeDirs);
    // Directories for individuals
    for (File generation : generationDirs)
      {
      genomeDirs = new String[individuals];
      for (int i = 0; i < individuals; i++)
        genomeDirs[i] = FileUtils.directory("individual", i + 1);
      directories.addAll(FileUtils.createDirectories(generation, genomeDirs));
      }
    return directories.toArray(new File[directories.size()]);
    }


  }
