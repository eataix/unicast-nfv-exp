/*
 * Double Auction for Relay Assignment
 * Copyright (C) 2011 Zichuan Xu
 *
 */

package Utils;

import java.util.ArrayList;
import java.util.Random;

public class RanNum {

	private static Random random;
	private static long seed;

	static {
		seed = System.currentTimeMillis();
		random = new Random(seed);
	}
	
	/**
	 * This function returns true with a specified probability
	 */
	public static boolean getZeroOneRandomNumbers(double probability){
		double rN = random.nextDouble()*100;
		
		if(rN < probability*100){
			return true;
		}else
			return false;
	}
	
	public static ArrayList<Double> getSubRandomSize(double size, int subCount){
		
		ArrayList<Double> retAL = new ArrayList<Double>(subCount);
				
		double totalRanSize = 0;
		for(int i = 0; i < subCount; i ++){
			double subSize = random.nextDouble()*size;
			retAL.add(i, subSize);
			totalRanSize += subSize;
		}
		double scaleFactor = size/totalRanSize;
			
		for (int i = 0; i < subCount; i++) {
			retAL.set(i, retAL.get(i) * scaleFactor);
		}	
		
		return retAL;
	}
	
	public static int getRandomIntRange(int max, int min){
		int intValue = random.nextInt(max - min) + min;
		return intValue;
	}
	
	public static ArrayList<Integer> getDistinctInts(int max, int min, int num){
		
		// error
		if (max - min < num )
			return null;
		
		ArrayList<Integer> ints = new ArrayList<Integer>();
		
		int numGenerated = 0;
		while (numGenerated < num) {
			int intValue = random.nextInt(max - min) + min;
			if (ints.contains(intValue)){
				continue;
			} else {
				numGenerated ++;
				ints.add(intValue);
			}
		}
		return ints;
	}
	
	// not necesarrily distinct. 
	public static ArrayList<Integer> getInts(int max, int min, int num){
		
		ArrayList<Integer> ints = new ArrayList<Integer>();
		for (int i = 0; i < num; i ++){
			ints.add( random.nextInt(max - min) + min );
		}
		return ints;
	}
	
	public static ArrayList<Double> getDistinctDoubles(double max, double min, int num){
		
		ArrayList<Double> doubles = new ArrayList<Double>();
		
		int numGenerated = 0;
		while (numGenerated < num) {
			double doubleValue = random.nextDouble() * (max-min) + min;
			if (doubles.contains(doubleValue)){
				continue;
			} else {
				numGenerated ++;
				doubles.add(doubleValue);
			}
		}
		return doubles;
	}
	
	public static double getRandomDoubleRange(double max, double min){
		return Math.random() * (max - min) + min;
	}

	/**
	 * 返回一个满足标准正态分布的实数
	 */
	public static double gaussian() {
		double r, x, y;
		do {
			x = uniform(-1.0, 1.0);
			y = uniform(-1.0, 1.0);
			r = x * x + y * y;
		} while (r >= 1 || r == 0);
		return x * Math.sqrt(-2 * Math.log(r) / r);
	}

	/**
	 * 返回一个满足平均值为mean,标准差为stddev的正态分布的实数
	 * 
	 * @param mean
	 *            正态分布的平均值
	 * @param stddev
	 *            正太分布的标准差
	 */
	public static double gaussian(double mean, double stddev) {
		return mean + stddev * gaussian();
	}

	/**
	 * 返回一个满足几何分布的整型值 平均值为1/p
	 */
	public static int geometric(double p) {
		// Knuth
		return (int) Math.ceil(Math.log(uniform()) / Math.log(1.0 - p));
	}

	/**
	 * 根据指定的参数返回一个满足泊松分布的实数
	 */
	public static int poisson(double lambda) {
		// 使用 Knuth 的算法
		// 参见 http://en.wikipedia.org/wiki/Poisson_distribution
		int k = 0;
		double p = 1.0;
		double L = Math.exp(-lambda);
		do {
			k++;
			p *= uniform();
		} while (p >= L);
		return k - 1;
	}

	/**
	 * 根据指定的参数按返回一个满足帕雷托分布的实数
	 */
	public static double pareto(double alpha) {
		return Math.pow(1 - uniform(), -1.0 / alpha) - 1.0;
	}

	/**
	 * 返回一个满足柯西分布的实数
	 */
	public static double cauchy() {
		return Math.tan(Math.PI * (uniform() - 0.5));
	}
	
	/**
	 * 返回一个满足指数分布的实数，该指数分布比率为lambda
	 */
	public static double exp(double lambda) {
		return -Math.log(1 - uniform()) / lambda;
	}
	
	public static double uniform() {
		return random.nextDouble();
	}

	/**
	 * 返回一个随机的范围在[0,N)之间的int类型的数
	 */
	public static int uniform(int N) {
		return random.nextInt(N);
	}
	    	 
	/**
	 * 返回一个范围在 [0, 1)的实数
	 */
	public static double random() {
		return uniform();
	}

	/**
	 * 返回一个范围在 [a, b)的int类型值
	 */
	public static int uniform(int max, int min) {
		return min + uniform(max - min);
	}

	/**
	 * 返回一个范围在 [a, b)的实数
	 */
	public static double uniform(double max, double min) {
		return min + uniform() * (max - min);
	}

	/**
	 * 返回一个随机boolean值,该p表示此布尔值为真的概率
	 * 
	 * @param p
	 *            0~1 之间的double值,表示产生boolean真值的可能性
	 */
	public static boolean bernoulli(double p) {
		return uniform() < p;
	}

	/**
	 * 返回一个随机boolean值,此布尔值为真的概率为0.5
	 */
	public static boolean bernoulli() {
		return bernoulli(0.5);
	}
}
