/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalfinal;

/**
 *
 * @author Yangyang
 */
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartUtils;


public class Finalfinal {

    private static final int BINS = 256;
    private static final String dataPath = "C:/Users/Yangyang/Desktop/大三下/应用安全课设/应用安全课设2/images/";;
    private static final String imageExt = "JPG";;

	public static void main(String args[]) {
		String[] inputImage = {"face01","face02","face03","face04","face05","face06"};
		
		try (FileWriter fw = new FileWriter (new File(dataPath+"stdv.txt"), false)) {
			for (int i=0; i<inputImage.length; ++i) {
				// 1. get gray images
				BufferedImage img = getGrayImage(inputImage[i]);
				
				// 2. get histograms
				boolean pct = true;	// true to get percent, false to get count
				float[] histogram = getHistogram(img, pct);
				
				// 3. get charts 
				XYDataset dataset = createDataset(inputImage[i], histogram);
				getXYChart(inputImage[i], dataset);
				
				// 4. get standard deviations
				getStandardDeviation(fw, inputImage[i], histogram);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private static void getStandardDeviation(FileWriter fw, String imageName, float[] histogram) {
		try {
			float stdv = (float) Math.sqrt(variance(histogram));
			fw.write(imageName+": "+stdv +"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static float variance(float[] fData) {
		float fMean = mean(fData);
		float fSumSqr = 0.0f;
	    for (int i = 0; i < fData.length; i++)
	    	fSumSqr += sqr(fMean - fData[i]);
	    return fSumSqr / (fData.length);
	}

	public static float mean(float[] fData) {
	    float total = 0.0f;
	    for (int i = 0; i < fData.length; i++)
	    	total += fData[i];
	    return total / fData.length;
	}
	
	private static float sqr(float fValue) {
		   return fValue * fValue;
	}

	private static void getXYChart(String imageName, XYDataset dataset) {
		// Create chart
		JFreeChart chart = ChartFactory.createXYLineChart(imageName, "", "", dataset, PlotOrientation.VERTICAL, false,
				false, false);

		// Changes background color
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(new Color(200, 200, 200));

		// set Y axis to percentage
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		DecimalFormat pctFormat = new DecimalFormat("#.0%");
		rangeAxis.setNumberFormatOverride(pctFormat);

		// set line color
		StandardXYItemRenderer renderer = new StandardXYItemRenderer();
		renderer.setSeriesPaint(0, Color.BLACK);
		plot.setRenderer(renderer);

		try {
			ChartUtils.saveChartAsPNG(new File(dataPath+imageName+".png"), chart, 600, 400 );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static XYDataset createDataset(String imageName, float[] histogram) {
		final XYSeries series = new XYSeries(imageName);
		for (int i=0; i<BINS; ++i) {
			series.add(i, histogram[i]);
		}

		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		return dataset;
	}
	
	private static float[] getHistogram(BufferedImage img, boolean pct) {
		float[] histogram = new float[BINS];
		
		// get image's width and height
		int width = img.getWidth();
		int height = img.getHeight();
		
		// get histogram
		for (int i=0; i<width; ++i) {
			for (int j=0; j<height; ++j) {
				int p = img.getRGB(i, j);
				int value = p & 0xff;
				histogram[value]++;
			}
		}
		
		// get percentages
		if (pct) {
			int total = width * height;
			for (int i=0; i<BINS; ++i)
				histogram[i] = (float) histogram[i] / total;
		}
		return histogram;
	}

	private static BufferedImage getGrayImage(String inputImage) {
		BufferedImage img = null;
		File f = null;

		// read image
		try {
			f = new File(dataPath + inputImage + "." + imageExt);
			img = ImageIO.read(f);
		} catch (IOException e) {
			System.out.println(e);
		}

		// get image's width and height
		int width = img.getWidth();
		int height = img.getHeight();

		// convert to greyscale
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Here (x,y)denotes the coordinate of image
				// for modifying the pixel value.
				int p = img.getRGB(x, y);

				int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;

				// calculate average
				int avg = (r + g + b) / 3;
				// replace RGB value with avg
				p = (a << 24) | (avg << 16) | (avg << 8) | avg;
				img.setRGB(x, y, p);
			}
		}

		// write image
		try {
			f = new File(dataPath + inputImage + "g." + imageExt);
			ImageIO.write(img, imageExt, f);
		} catch (IOException e) {
			System.out.println(e);
		}
		
		return img;
	}
}