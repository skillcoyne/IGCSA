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
    protected Location Location;
    protected Probability Probability;
    protected DNASequence DNASequence;

    protected Variation(Location loc, Probability prob, DNASequence seq)
        {
        this.Location = loc;
        this.Probability = prob;
        this.DNASequence = seq;
        }

    public Probability getProbability()
        {
        return this.Probability;
        }

    public Location getLocation()
        {
        return this.Location;
        }

    public DNASequence getDNASequence()
        {
        return this.DNASequence;
        }

    public abstract DNASequence mutateSequence();

    }
