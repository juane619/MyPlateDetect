package org.juane.platedetect.model;

import org.opencv.core.Scalar;

import javafx.scene.paint.Color;

public class Filters {
	private double minThresold = 0;
	private double maxThresold = 0;
	private int blurKernelSize = 0;
	boolean sobelKernel = false;
	private int sobelKernelSize = 0;
	private double sigma = 0;
	private Scalar minColor = null;
	private Scalar maxColor = null;
	private double erosionSize = 0;
	private double dilationSize = 0;

	public Filters() {
		minThresold = 100;
		maxThresold = 100;
		blurKernelSize = 3;
		sobelKernel = false;
		sobelKernelSize = 5;
		sigma = 2;
		minColor = new Scalar(0, 0, 0);
		maxColor = new Scalar(255, 255, 255);
		minThresold = 3;
		maxThresold = 2;
	}

	public Filters(final double minThresold, final double maxThresold, final int blurKernelSize,
			final boolean sobelKernel, final int sobelKernelSize, final double sigma, final Color minColor,
			final Color maxColor, final double erosionSize, final double dilationSize) {
		updateFilters(minThresold, maxThresold, blurKernelSize, sobelKernel, sobelKernelSize, sigma, minColor, maxColor,
				erosionSize, dilationSize);
	}

	public void updateFilters(final double minThresold, final double maxThresold, final int blurKernelSize,
			final boolean sobelKernel, final int sobelKernelSize, final double sigma, final Color minColor,
			final Color maxColor, final double erosionSize, final double dilationSize) {
		this.minThresold = minThresold;
		this.maxThresold = maxThresold;
		this.blurKernelSize = blurKernelSize;
		this.sobelKernel = sobelKernel;
		this.sobelKernelSize = sobelKernelSize;
		this.sigma = sigma;
		this.minColor = new Scalar(minColor.getRed(), minColor.getGreen(), minColor.getBlue());
		this.maxColor = new Scalar(maxColor.getRed(), maxColor.getGreen(), maxColor.getBlue());
		this.erosionSize = erosionSize;
		this.dilationSize = dilationSize;
	}

	public boolean isSobelKernel() {
		return sobelKernel;
	}

	public void setSobelKernel(final boolean sobelKernel) {
		this.sobelKernel = sobelKernel;
	}

	public int getBlurKernelSize() {
		return blurKernelSize;
	}

	public void setBlurKernelSize(final int blurKernelSize) {
		this.blurKernelSize = blurKernelSize;
	}

	public int getSobelKernelSize() {
		return sobelKernelSize;
	}

	public void setSobelKernelSize(final int sobelKernelSize) {
		this.sobelKernelSize = sobelKernelSize;
	}

	public double getMinThresold() {
		return minThresold;
	}

	public void setMinThresold(final double minThresold) {
		this.minThresold = minThresold;
	}

	public double getMaxThresold() {
		return maxThresold;
	}

	public void setMaxThresold(final double maxThresold) {
		this.maxThresold = maxThresold;
	}

	public double getSigma() {
		return sigma;
	}

	public void setSigma(final double sigma) {
		this.sigma = sigma;
	}

	public Scalar getMinColor() {
		return minColor;
	}

	public void setMinColor(final Scalar minColor) {
		this.minColor = minColor;
	}

	public Scalar getMaxColor() {
		return maxColor;
	}

	public void setMaxColor(final Scalar maxColor) {
		this.maxColor = maxColor;
	}

	public double getErosionSize() {
		return erosionSize;
	}

	public void setErosionSize(final double erosionSize) {
		this.erosionSize = erosionSize;
	}

	public double getDilationSize() {
		return dilationSize;
	}

	public void setDilationSize(final double dilationSize) {
		this.dilationSize = dilationSize;
	}

}
