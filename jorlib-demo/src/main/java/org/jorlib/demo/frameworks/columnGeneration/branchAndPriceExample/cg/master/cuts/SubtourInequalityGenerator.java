/* ==========================================
 * jORLib : a free Java OR library
 * ==========================================
 *
 * Project Info:  https://github.com/jkinable/jorlib
 * Project Creator:  Joris Kinable (https://github.com/jkinable)
 *
 * (C) Copyright 2015, by Joris Kinable and Contributors.
 *
 * This program and the accompanying materials are licensed under GPLv3
 *
 */
/* -----------------
 * SubtourInequalityGenerator.java
 * -----------------
 * (C) Copyright 2015, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 *
 */
package org.jorlib.demo.frameworks.columnGeneration.branchAndPriceExample.cg.master.cuts;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;

import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jorlib.alg.tsp.separation.SubtourSeparator;
import org.jorlib.demo.frameworks.columnGeneration.branchAndPriceExample.cg.Matching;
import org.jorlib.demo.frameworks.columnGeneration.branchAndPriceExample.cg.master.TSPMasterData;
import org.jorlib.demo.frameworks.columnGeneration.branchAndPriceExample.model.MatchingColor;
import org.jorlib.demo.frameworks.columnGeneration.branchAndPriceExample.model.TSP;
import org.jorlib.frameworks.columnGeneration.master.cutGeneration.CutGenerator;
import org.jorlib.frameworks.columnGeneration.master.cutGeneration.Inequality;
import org.jorlib.io.tspLibReader.graph.Edge;

/**
 * 
 * @author Joris Kinable
 * @version 13-4-2015
 *
 */
public class SubtourInequalityGenerator extends CutGenerator<TSP, TSPMasterData> {

	//Graph which is used to calculate any violated subtour inequalities
	private final Graph<Integer, DefaultEdge> completeGraph;
	//We use the subtour separator provided in jORLib
	private final SubtourSeparator<Integer, DefaultEdge> separator;
	
	public SubtourInequalityGenerator(TSP modelData) {
		super(modelData);
		
		//Create a complete graph using the CompleteGraphGenerator in the JGraphT package
		completeGraph=new SimpleGraph<>(DefaultEdge.class);
		CompleteGraphGenerator<Integer, DefaultEdge> completeGenerator =new CompleteGraphGenerator<Integer, DefaultEdge>(modelData.N);
		completeGenerator.generateGraph(completeGraph, new IntegerVertexFactory(), null);
		//Create a subtour separator 
		separator=new SubtourSeparator<Integer, DefaultEdge>(completeGraph);
	}

	@Override
	public boolean generateInqualities() {
		//Get the edge weights as a map
		double[][] edgeValues=masterData.getEdgeValues();
//		System.out.println("Separating subtours. Edge values: ");
//		for(int i=0; i<edgeValues.length; i++)
//			System.out.println(Arrays.toString(edgeValues[i]));
		Map<DefaultEdge,Double> edgeValueMap=new HashMap<>();
		for(int i=0; i<modelData.N-1; i++){
			for(int j=i+1; j<modelData.N; j++){
				edgeValueMap.put(completeGraph.getEdge(i, j), edgeValues[i][j]);
			}
		}
//		for(DefaultEdge e : edgeValueMap.keySet()){
//			if(edgeValueMap.get(e) >0){
//				System.out.println("Edge: ("+completeGraph.getEdgeSource(e)+","+completeGraph.getEdgeTarget(e)+") value: "+edgeValueMap.get(e));
//			}
//			edgeValueMap.put(completeGraph.getEdge(1,2), 0.0);
//			System.out.println("edgeValueMap.put(completeGraph.getEdge("+completeGraph.getEdgeSource(e)+","+completeGraph.getEdgeTarget(e)+"), "+edgeValueMap.get(e)+");");
//		}
		//Check for violated subtours. When found, generate an inequality
		separator.separateSubtour(edgeValueMap);
		if(separator.hasSubtour()){
//			System.out.println("Found subtour! :)");
			Set<Integer> cutSet=separator.getCutSet();
			SubtourInequality inequality=new SubtourInequality(this, cutSet);
			this.addCut(inequality);
			return true;
		}
//		else{
//			System.out.println("Didn't find subtour :(!");
//		}
		return false;
	}

	private void addCut(SubtourInequality subtourInequality){
		if(masterData.subtourInequalities.containsKey(subtourInequality))
			throw new RuntimeException("Error, duplicate subtour cut is being generated! This cut should already exist in the master problem: "+subtourInequality);
		//Create the inequality in cplex
		try {
			IloLinearNumExpr expr=masterData.cplex.linearNumExpr();
			//Register the columns with this constraint.
			for(MatchingColor color : MatchingColor.values()){
				for(Matching matching: masterData.matchingVars.get(color).keyList()){
					//Test how many edges in the matching enter/leave the cutSet (edges with exactly one endpoint in the cutSet)
					int crossings=0;
					for(Edge edge: matching.edges){
						if(subtourInequality.cutSet.contains(edge.getId1()) ^ subtourInequality.cutSet.contains(edge.getId2()))
							crossings++;
					}
					if(crossings>0){
						IloNumVar var=masterData.matchingVars.get(color).get(matching);
						expr.addTerm(crossings, var);
					}
				}
			}
			IloRange subtourConstraint = masterData.cplex.addGe(expr, 2, "subtour");
			masterData.subtourInequalities.put(subtourInequality, subtourConstraint);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addCut(Inequality cut) {
		if(!(cut instanceof SubtourInequality))
			throw new IllegalArgumentException("This CutGenerator can ONLY add SubtourInequalities");
		SubtourInequality subtourInequality=(SubtourInequality) cut;
		this.addCut(subtourInequality);
	}

	@Override
	public List<Inequality> getCuts() {
		return new ArrayList<>(masterData.subtourInequalities.keySet());
	}

	@Override
	public void close() {} //Nothing to do here

	/**
	 * Simple factory class which produces integers as vertices
	 */
	private class IntegerVertexFactory implements VertexFactory<Integer>{
		private int counter=0;
		@Override
		public Integer createVertex() {
			return new Integer(counter++);
		}
		
	}
}
