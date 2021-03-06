package com.jstarcraft.ai.math.algorithm.similarity;

import java.util.List;

import com.jstarcraft.ai.math.structure.matrix.MatrixScalar;
import com.jstarcraft.ai.math.structure.matrix.SparseMatrix;
import com.jstarcraft.ai.math.structure.matrix.SymmetryMatrix;
import com.jstarcraft.ai.math.structure.vector.MathVector;
import com.jstarcraft.core.utility.KeyValue;

/**
 * Constrained Pearson Correlation相似度
 * 
 * @author Birdy
 *
 */
public class CPCSimilarity extends AbstractSimilarity {

	private double median;

	@Override
	public SymmetryMatrix makeSimilarityMatrix(SparseMatrix trainMatrix, boolean transpose, float scale) {
		float maximum = 0F;
		float minimum = 0F;
		for (MatrixScalar term : trainMatrix) {
			if (term.getValue() > maximum) {
				maximum = term.getValue();
			}
			if (term.getValue() < minimum) {
				minimum = term.getValue();
			}
		}
		median = (maximum + minimum) / 2;
		return super.makeSimilarityMatrix(trainMatrix, transpose, scale);
	}

	private float getSimilarity(int count, List<KeyValue<Float, Float>> scoreList) {
		// compute similarity
		if (count == 0) {
			return Float.NaN;
		}
		double power = 0D, leftPower = 0D, rightPower = 0D;
		for (KeyValue<Float, Float> term : scoreList) {
			double leftDelta = term.getKey() - median;
			double rightDelta = term.getValue() - median;
			power += leftDelta * rightDelta;
			leftPower += leftDelta * leftDelta;
			rightPower += rightDelta * rightDelta;
		}
		return (float) (power / Math.sqrt(leftPower * rightPower));
	}

	@Override
	public float getCorrelation(MathVector leftVector, MathVector rightVector, float scale) {
		// compute similarity
		List<KeyValue<Float, Float>> scoreList = getScoreList(leftVector, rightVector);
		int count = scoreList.size();
		float similarity = getSimilarity(count, scoreList);
		// shrink to account for vector size
		if (!Double.isNaN(similarity)) {
			if (scale > 0) {
				similarity *= count / (count + scale);
			}
		}
		return similarity;
	}

}
