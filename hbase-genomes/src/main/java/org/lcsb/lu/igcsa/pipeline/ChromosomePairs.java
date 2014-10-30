/**
 * org.lcsb.lu.igcsa.pipeline
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.pipeline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.job.MiniChromosomeJob;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;
import org.lcsb.lu.igcsa.population.MinimalKaryotype;
import org.lcsb.lu.igcsa.population.PopulationGenerator;
import org.lcsb.lu.igcsa.population.watchmaker.kt.Evaluator;
import org.lcsb.lu.igcsa.population.watchmaker.kt.Observer;
import org.lcsb.lu.igcsa.population.watchmaker.kt.statistics.CandidateBreakpoints;

import java.util.*;
import java.util.regex.Pattern;


public class ChromosomePairs extends SearchPipeline
  {
  private static final Log log = LogFactory.getLog(ChromosomePairs.class);

  private Set<String> chrs;

  public static void main(String[] args) throws Exception
    {
    new ChromosomePairs().runSearch(args);
    }

  @Override
  protected void usage()
    {
    HelpFormatter help = new HelpFormatter();
    if (!commandLine.hasOption("c") | !commandLine.hasOption("g") | !commandLine.hasOption("o"))
      {
      help.printHelp(this.getClass().getSimpleName() + ":Missing required option. ", this.getOptions());
      System.exit(-1);
      }
    }

  @Override
  public void setOptions()
    {
    options = new Options();
    Option opt = new Option("c", "chromosomes", true, "chromosome pairs ex. 5,12.");
    opt.setRequired(true);
    options.addOption(opt);

    opt = new Option("o", "output", true, "output path");
    opt.setRequired(true);
    options.addOption(opt);

    opt = new Option("g", "genome", true, "parent genome name for sequence generation");
    opt.setRequired(true);
    options.addOption(opt);

    options.addOption(new Option("m", "max-pairs", true, "max pairs to generate, default = 10"));
    options.addOption(new Option("b", "bwa", true, "bwa archive location"));
    }

  @Override
  public void runSearch(String[] args) throws Exception
    {
    CommandLine cl = parseCommandLine(args);

    chrs = new HashSet<String>();

    for (String c : cl.getOptionValue("c").split(","))
      chrs.add(c);

    BandGenerator bg = new BandGenerator();
    //BandGenerator.FILTER_CENT = false;
    bg.run(cl.getOptionValue("c").split(","));

    int top = (cl.hasOption("m"))? Integer.parseInt(cl.getOptionValue("m")): 10;
    List<BandGenerator.Candidate> candidates = bg.getTopCandidates(top);

    for (BandGenerator.Candidate cand: candidates)
      {
      List<String> bands = new ArrayList<String>();
      for (Band band: cand.getBands())
        {
        bands.add("-band");
        bands.add(band.getFullName());
        }

      MiniChromosomeJob mcj = null;
      try
        {
        mcj = generateMiniAbrs((String[]) ArrayUtils.addAll(new String[]{"-b", cl.getOptionValue("b"), "-g", cl.getOptionValue("g"), "-n", "chrs" + StringUtils.join(chrs.iterator(), "-"), "-o", cl.getOptionValue("o")}, bands.toArray(new String[bands.size()])));
        log.info(mcj.getIndexPath().toString());
        if (mcj == null)
          {
          log.info("ERROR: Failed to generate mini chromosome \" + abr.getBands())");
          log.error("Failed to generate mini chromosome " + cand.getBands());
          }
        }
      catch (Exception e)
        {
        log.info("ERROR: Failed to finish generate or align for " + cand.getBands() + e);
        log.error("Failed to finish generate or align for " + cand.getBands() + e);
        }

      }



    }



  }
