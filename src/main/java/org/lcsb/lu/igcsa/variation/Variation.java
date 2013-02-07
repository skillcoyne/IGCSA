package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.Sequence;

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
    protected Sequence Sequence;

    protected Variation(Location loc, Probability prob, Sequence seq)
        {
        this.Location = loc;
        this.Probability = prob;
        this.Sequence = seq;
        }

    public Probability getProbability()
        {
        return this.Probability;
        }

    public Location getLocation()
        {
        return this.Location;
        }

    public Sequence getSequence()
        {
        return this.Sequence;
        }

    public abstract Sequence mutateSequence();

    }
