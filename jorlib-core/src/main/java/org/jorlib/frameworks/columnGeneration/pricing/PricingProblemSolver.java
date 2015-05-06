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
 * PricingProblemSolver.java
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
package org.jorlib.frameworks.columnGeneration.pricing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.jorlib.frameworks.columnGeneration.branchAndPrice.branchingDecisions.BranchingDecision;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.branchingDecisions.BranchingDecisionListener;
import org.jorlib.frameworks.columnGeneration.colgenMain.AbstractColumn;
import org.jorlib.frameworks.columnGeneration.io.TimeLimitExceededException;
import org.jorlib.frameworks.columnGeneration.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class representing a pricing problem
 * 
 * @author Joris Kinable
 * @version 13-4-2015
 *
 */
public abstract class PricingProblemSolver<T,U extends AbstractColumn<T,U,V>, V extends AbstractPricingProblem<T,U,V>> implements Callable<Void>, BranchingDecisionListener{
	
	protected final Logger logger = LoggerFactory.getLogger(PricingProblemSolver.class);
	protected final Configuration config=Configuration.getConfiguration();

	//Name of the pricing problem solver
	protected String name="solverName";
	//Data model
	protected final T dataModel;
	//Pricing problem
	protected final V pricingProblem;
	//Time by which the algorithm needs to be finished.
	protected long timeLimit;
	//Objective of pricing problem (best column)
	protected double objective;
	//Columns generated
	protected List<U> columns;
	
	protected boolean pricingProblemInfeasible;
	
	
	public PricingProblemSolver(T dataModel/*, String name*/, V pricingProblem){
		this.dataModel=dataModel;
//		this.name=name;
		this.pricingProblem=pricingProblem;
		this.columns=new ArrayList<>();
	}
	
	
	/**
	 * Method needed for parallelization. Solves the pricing problem in a separate thread
	 * @return 
	 */
	@Override
	public Void call() throws Exception {
		columns.clear();
		this.setObjective();
		this.solve();
		return null;
	}
	
	/**
	 * Solves the pricing problem
	 * @throws TimeLimitExceededException
	 */
	protected void solve() throws TimeLimitExceededException{
		columns.addAll(this.generateNewColumns());
	}
	
	protected abstract List<U> generateNewColumns() throws TimeLimitExceededException;
	
	/**
	 * Returns the name of the pricing problem
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Set time limit
	 */
	public void setTimeLimit(long timeLimit){
		this.timeLimit=timeLimit;
	}
	
	//-------Abstract methods -------------
	
	/**
	 * Returns the cost of the most negative reduced cost column. Since the pricing problem is an maximization problem, any feasible solution is
	 * a lower bound. 
	 */
	public double getObjective(){
		return objective;
	}
	
	protected abstract void setObjective();
	
	/**
	 * Returns an upperbound on the most negative reduced cost column whenever available.
	 */
	public double getUpperbound(){
		throw new UnsupportedOperationException("Not implemented");
	}
	
	public List<U> getColumns(){
		return columns;
	}
	
	
	
	/**
	 * Returns whether the pricing problem is feasible. For example due to branching decisions, no feasible solution may exist for a particular pricing problem.
	 */
	
	public boolean pricingProblemIsFeasible(){
		return !pricingProblemInfeasible;
	}
	/**
	 * Close the pricing problem and perform cleanup
	 */
	public abstract void close();

	@Override
	public void branchingDecisionPerformed(BranchingDecision bd) {
	}

	@Override
	public void branchingDecisionRewinded(BranchingDecision bd) {
	}
	
}
