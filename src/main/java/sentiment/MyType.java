package sentiment;

public class MyType implements Comparable<MyType> {
	String token;
	double value;
 
	public MyType(String t, double s) {
		token = t; 
		value = s;
	}
 
	@Override
	public int compareTo(MyType o) {
		double comparedSize = o.value;
		if (this.value < comparedSize) {
			return 1;
		} else if (this.value == comparedSize) {
			return 0;
		} else {
			return -1;
		}
	}
}
