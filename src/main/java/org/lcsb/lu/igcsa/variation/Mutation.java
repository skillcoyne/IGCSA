package org.lcsb.lu.igcsa.variation;

import org.lcsb.lu.igcsa.prob.Probability;
import org.lcsb.lu.igcsa.genome.Location;

/**
 * Created with IntelliJ IDEA.
 * User: skillcoyne
 * Date: 9/16/12
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Mutation
    {
    protected Location location;
    protected Probability probability;
    protected String sequence;

    public Mutation (Location l, Probability p, String s)
        {
        location = l;
        probability = p;
        sequence = s;
        }

    public Probability getProbability()
        {
        return probability;
        }

    public Location getLocation()
        {
        return location;
        }

    public String getSequence()
        {
        return sequence;
        }

    public abstract void mutateSequence();

    }
