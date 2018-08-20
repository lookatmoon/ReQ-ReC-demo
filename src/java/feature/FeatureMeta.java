package feature;

public class FeatureMeta {
	public int freq;
	public double doubleFeatureValue;
	public boolean usingDoubleFeature;
	public String featureStr;
	public FeatureMeta(int freq, String featureStr, double doubleFeatureValue, boolean usingDoubleFeature){
		this.freq = freq;
		this.doubleFeatureValue = doubleFeatureValue;
		this.featureStr = featureStr;
		this.usingDoubleFeature = usingDoubleFeature;
	}
}
