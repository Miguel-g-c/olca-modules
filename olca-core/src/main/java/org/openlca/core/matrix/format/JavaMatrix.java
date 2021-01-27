package org.openlca.core.matrix.format;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class JavaMatrix implements Matrix {

	private final RealMatrix matrix;

	public JavaMatrix(RealMatrix matrix) {
		this.matrix = matrix;
	}

	public JavaMatrix(RealVector vector) {
		matrix = new Array2DRowRealMatrix(vector.getDimension(), 1);
		matrix.setColumnVector(0, vector);
	}

	public JavaMatrix(int rowSize, int colSize) {
		matrix = new Array2DRowRealMatrix(rowSize, colSize);
	}

	public static JavaMatrix of(double[][] data) {
		int rows = data.length;
		int cols = 0;
		for (var rowData : data) {
			cols = Math.max(cols, rowData.length);
		}
		var m = new JavaMatrix(rows, cols);
		for (int row = 0; row < rows; row++) {
			var rowData = data[row];
			for (int col = 0; col < rowData.length; col++) {
				m.set(row, col, rowData[col]);
			}
		}
		return m;
	}

	public RealMatrix getRealMatrix() {
		return matrix;
	}

	@Override
	public int columns() {
		return matrix.getColumnDimension();
	}

	@Override
	public int rows() {
		return matrix.getRowDimension();
	}

	@Override
	public void set(int row, int col, double val) {
		matrix.setEntry(row, col, val);
	}

	@Override
	public double get(int row, int col) {
		return matrix.getEntry(row, col);
	}

	@Override
	public double[] getColumn(int i) {
		return matrix.getColumn(i);
	}

	@Override
	public double[] getRow(int i) {
		return matrix.getRow(i);
	}

	@Override
	public Matrix copy() {
		return new JavaMatrix(matrix.copy());
	}

	@Override
	public String toString() {
		if (matrix == null)
			return super.toString();
		StringBuilder s = new StringBuilder();
		int max = 10;
		int rows = matrix.getRowDimension();
		int cols = matrix.getColumnDimension();
		s.append("[");
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				if (col >= max) {
					s.append(" ...");
					break;
				}
				double val = matrix.getEntry(row, col);
				s.append(val);
				if (col < (cols - 1))
					s.append(" ");
			}
			if (row >= max) {
				s.append("; ...");
				break;
			}
			if (row < (rows - 1))
				s.append("; ");
		}
		s.append("]");
		return s.toString();
	}
}
