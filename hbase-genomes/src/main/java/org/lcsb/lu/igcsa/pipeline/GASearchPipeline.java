/**
 * org.lcsb.lu.igcsa.mapreduce.pipeline
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.pipeline;

import org.apache.commons.cli.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.population.MinimalKaryotype;
import org.lcsb.lu.igcsa.population.PopulationGenerator;
import org.lcsb.lu.igcsa.job.*;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;
import org.lcsb.lu.igcsa.population.watchmaker.kt.Observer;
import org.lcsb.lu.igcsa.population.watchmaker.kt.statistics.CandidateBreakpoints;
import org.lcsb.lu.igcsa.prob.ProbabilityException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


//** Need to update this to be a pipeline that starts from random selection
public class GASearchPipeline extends SearchPipeline
  {
  private static final Log log = LogFactory.getLog(GASearchPipeline.class);

  @Override
  public void setOptions()
    {
    options = new Options();
    options.addOption(new Option("b", "bwa-path", true, "bwa archive location"));
    options.addOption(new Option("o", "output", true, "output path"));
    options.addOption(new Option("g", "genome", true, "parent genome name for sequence generation"));
    options.addOption(new Option("r", "reads", true, "read path for tsv"));
    options.addOption(new Option("s", "size", true, "population size, no less than 10 DEFAULT: 50"));
    options.addOption(new Option("t", "generations", true, "number of generations DEFAULT: 1000"));
    }

  @Override
  protected void usage()
    {
    HelpFormatter help = new HelpFormatter();
    if (!commandLine.hasOption("b") | !commandLine.hasOption("o") | !commandLine.hasOption("g") | !commandLine.hasOption("r"))
      {
      help.printHelp(this.getClass().getSimpleName() + ":\nMissing required option. ", this.getOptions());
      System.exit(-1);
      }
    }

  //  # 1. Generate mini aberrations
  //  # 2. Index & Align
  //  # 3. Score
  //  # 4. Generate neighboring aberrations & ...?
  //  # 5. Repeat 2-4 until??
  public static void main(String[] args) throws Exception
    {
    new GASearchPipeline().runSearch(args);
    }

  @Override
  public void runSearch(String[] args) throws ProbabilityException, ParseException, IOException
    {
    CommandLine cl = parseCommandLine(args);

    int popSize = (cl.hasOption("size")) ? Integer.parseInt(cl.getOptionValue("size")) : 50;
    if (popSize < 10) popSize = 10;
    int generations = (cl.hasOption("generations")) ? Integer.parseInt(cl.getOptionValue("generations")) : 1000;

    PopulationGenerator pg = new PopulationGenerator();
    Observer.QUIET = true;
    Observer ob = new Observer(new CandidateBreakpoints());
    pg.setObserver(ob);
    pg.removeMatchingBands(Pattern.compile("^.*(p|q)(11|12)$"));
    List<MinimalKaryotype> pop = pg.run(generations, popSize);

    for (MinimalKaryotype kt : pop)
      {
      for (Aberration abr : kt.getAberrations())
        {
        for (Band band : abr.getBands())
          if (band.isCentromere()) log.info(kt);
        }
      }
    ob.finalUpdate();

    /* TODO
    For DELETION events the two adjacent bands could be combined as a TRANS
    For INVERSION events you'd made two aberrations with the bands that are adjacent to the INV
     */
    Collection<Aberration> randomBandPairSet = this.filterBands(pop);
    log.info("Total bands to generate: " + randomBandPairSet.size());

    for (Aberration abr : randomBandPairSet)
      {
      List<String> bands = new ArrayList<String>();
      for (Band band : abr.getBands())
        {
        bands.add("-band");
        bands.add(band.getFullName());
        }

      MiniChromosomeJob mcj = null;
      try
        {
        mcj = generateMiniAbrs((String[]) ArrayUtils.addAll(new String[]{"-b", cl.getOptionValue("b"), "-g", cl.getOptionValue("g"),
            "-n", "mini", "-o", cl.getOptionValue("o")}, bands.toArray(new String[bands.size()])));
        log.info(mcj.getIndexPath().toString());

        String alignPath = alignReads(mcj.getIndexPath().toString(), mcj.getName());
        log.info(alignPath);
        }
      catch (Exception e)
        {
        log.info("ERROR: Failed to finish generate/index/align for " + abr.getBands() + e);
        log.error("Failed to finish generate/index/align for " + abr.getBands() + e);
        }

      }
    }

  // avoid duplicating a band
  private Collection<Aberration> filterBands(List<MinimalKaryotype> population)
    {
    Set<Band> bandSet = new HashSet<Band>();
    Set<Aberration> randomBandPairSet = new HashSet<Aberration>();
    for (MinimalKaryotype mk : population)
      {
      for (Aberration abr : mk.getAberrations())
        {
        if (abr.getAberration().equals(AberrationTypes.TRANSLOCATION))
          {
          for (Band band : abr.getBands())
            {
            if (bandSet.contains(band)) continue;
            else bandSet.add(band);
            }
          randomBandPairSet.add(abr);
          }
        }
      }
    return randomBandPairSet;
    }

  }
