package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.DNASequence;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class Variation
    {
    protected Location location;
    protected Probability probability;
    protected DNASequence sequence;

    protected Variation(DNASequence seq)
      {
      this.sequence = seq;
      }

    protected Variation(Location loc, Probability prob, DNASequence seq)
        {
        this.location = loc;
        this.probability = prob;
        this.sequence = seq;
        }

    public void setLocation(Location location)
      {
      this.location = location;
      }

    public void setProbability(Probability probability)
      {
      this.probability = probability;
      }

    public void setSequence(DNASequence sequence)
      {
      this.sequence = sequence;
      }

    public Probability getProbability()
        {
        return this.probability;
        }

    public Location getLocation()
        {
        return this.location;
        }

    public DNASequence getDNASequence()
        {
        return this.sequence;
        }

    public abstract DNASequence mutateSequence();

    }
