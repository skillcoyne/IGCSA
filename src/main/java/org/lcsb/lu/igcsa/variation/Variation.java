package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.prob.ProbabilityException;
import org.lcsb.lu.igcsa.prob.ProbabilityList;

/**
 * org.lcsb.lu.igcsa.variation
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface Variation
    {
    public void setProbability(Probability p) throws ProbabilityException;

    public void setProbabilityList(ProbabilityList pl) throws ProbabilityException;

    public Probability getProbability();

    public DNASequence mutateSequence(String s);

    public DNASequence mutateSequence(DNASequence s);
    }
