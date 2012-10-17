package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.prob.Probability;

/**
 * Created with IntelliJ IDEA.
 * User: skillcoyne
 * Date: 9/16/12
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class SNP extends Mutation
    {

    public SNP (Location l, Probability p, String seq)
        {
        super(l,p,seq);
        }


    public void mutateSequence()
        {
        this.probability.getProbability(); //probability meaning...what exactly
        this.sequence
        }

    }
