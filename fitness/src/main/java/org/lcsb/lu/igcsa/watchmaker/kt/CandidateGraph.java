package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedPseudograph;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.lcsb.lu.igcsa.utils.CandidateUtils;

import java.util.*;

/**
 * org.lcsb.lu.igcsa.watchmaker
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class CandidateGraph
  {
  static Logger log = Logger.getLogger(CandidateGraph.class.getName());
  private static CandidateGraph ourInstance = null;

  WeightedGraph<KaryotypeCandidate, DefaultWeightedEdge> graph;

  public static CandidateGraph getInstance()
    {
    if (ourInstance == null)
      ourInstance = new CandidateGraph();

    return ourInstance;
    }

  public static void updateGraph(KaryotypeCandidate kc, List<KaryotypeCandidate> candidateList)
    {
    for (KaryotypeCandidate kc2 : candidateList)
      {
      if (!kc.equals(kc2))
        getInstance().addEdge(kc, kc2, CandidateUtils.getNCD(kc, kc2));
      }
    }

  private CandidateGraph()
    {
    graph = new WeightedPseudograph<KaryotypeCandidate, DefaultWeightedEdge>(DefaultWeightedEdge.class);
    }

  /**
   * Add edge if it doesn't already exist.
   *
   * @param kc
   */
  protected boolean addNode(KaryotypeCandidate kc)
    {
    return graph.addVertex(kc);
    }

  protected Set<DefaultWeightedEdge> getEdges(KaryotypeCandidate kc)
    {
    return graph.edgesOf(kc);
    }

  /**
   * If edge doesn't exist add it.  If it does, update the edge weight.
   *
   * @param kc1
   * @param kc2
   * @param edgeWt
   */
  protected boolean addEdge(KaryotypeCandidate kc1, KaryotypeCandidate kc2, double edgeWt)
    {
    boolean added = false;
    if (kc1 != kc2) // no self-edges
      {
      addNode(kc1);
      addNode(kc2);

      DefaultWeightedEdge edge = graph.getEdge(kc1, kc2);
      if (edge == null)
        {
        edge = graph.addEdge(kc1, kc2);
        added = true;
        }
      graph.setEdgeWeight(edge, edgeWt);
      }
    else
      throw new RuntimeException("Identical candidates");

    return added;
    }

  protected boolean removeEdge(DefaultWeightedEdge edge)
    {
    return graph.removeEdge(edge);
    }

  protected boolean removeNode(KaryotypeCandidate kc)
    {
    return graph.removeVertex(kc);
    }

  public List<KaryotypeCandidate> getNodes(DefaultWeightedEdge edge)
    {
    List<KaryotypeCandidate> nodes = new ArrayList<KaryotypeCandidate>();
    nodes.add(graph.getEdgeSource(edge));
    nodes.add(graph.getEdgeTarget(edge));
    return nodes;
    }

  public double getEdgeWeight(DefaultWeightedEdge edge)
    {
    return graph.getEdgeWeight(edge);
    }

  public Iterator<KaryotypeCandidate> nodeIterator()
    {
    return graph.vertexSet().iterator();
    }

  public Iterator<DefaultWeightedEdge> edgeIterator()
    {
    return graph.edgeSet().iterator();
    }

  public int edgeCount()
    {
    return graph.edgeSet().size();
    }

  public int nodeCount()
    {
    return graph.vertexSet().size();
    }

  public Iterator<DefaultWeightedEdge> weightSortedEdgeIterator()
    {
    TreeMap<Double, DefaultWeightedEdge> map = new TreeMap<Double, DefaultWeightedEdge>();
    for (DefaultWeightedEdge edge : graph.edgeSet())
      map.put(graph.getEdgeWeight(edge), edge);

    return map.values().iterator();
    }

  }
