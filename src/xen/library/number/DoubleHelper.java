package xen.library.number;

public class DoubleHelper {
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    double tmp = Math.round(value);
	    return (double) tmp / factor;
	}
}
