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
 * IndependentSet.java
 * -----------------
 * (C) Copyright 2016, by Joris Kinable and Contributors.
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
package org.jorlib.demo.frameworks.columnGeneration.bapExample2.cg;

import org.jorlib.demo.frameworks.columnGeneration.bapExample2.model.ColoringGraph;
import org.jorlib.frameworks.columnGeneration.colgenMain.AbstractColumn;

import java.util.Set;

/**
 * Definition of a column.
 *
 * @author Joris Kinable
 * @version 29-6-2016
 */
public final class IndependentSet extends AbstractColumn<ColoringGraph, ChromaticNumberPricingProblem>{

    public final Set<Integer> vertices;
    public final int cost;

    /**
     * Constructs a new column
     *
     * @param associatedPricingProblem Pricing problem to which this column belongs
     * @param isArtificial             Is this an artificial column?
     * @param creator                  Who/What created this column?
     */
    public IndependentSet(ChromaticNumberPricingProblem associatedPricingProblem, boolean isArtificial, String creator, Set<Integer> vertices, int cost) {
        super(associatedPricingProblem, isArtificial, creator);
        this.vertices=vertices;
        this.cost=cost;
    }


    @Override
    public boolean equals(Object o) {
        if(this==o)
            return true;
        else if(!(o instanceof IndependentSet))
            return false;
        IndependentSet other=(IndependentSet) o;
        return this.vertices.equals(other.vertices) && this.isArtificialColumn == other.isArtificialColumn;
    }

    @Override
    public int hashCode() {
        return vertices.hashCode();
    }

    @Override
    public String toString() {
        String s="Value: "+this.value+" artificial: "+isArtificialColumn+" set: "+vertices.toString();
        return s;
    }

}
