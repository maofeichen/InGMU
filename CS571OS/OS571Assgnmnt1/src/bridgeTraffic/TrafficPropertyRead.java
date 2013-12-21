/**
 * OS571 Assignment 1
 * TrafficPropertyReadjava
 * Maofei Chen
 * G00709508
 * 
 * read data from trafficConfig.properties file
 */
package bridgeTraffic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

/**
 * @author mchen
 * 
 */
public class TrafficPropertyRead {
	private Properties trafficConfig;
	
	private Vector<Integer> vecNumOfVehicle = new Vector<Integer>();
	private Vector<Integer> vecDelay = new Vector<Integer>();
	private Vector<Float> vecTypeProba = new Vector<Float>();
	private Vector<Float> vecDircProba = new Vector<Float>();

	private String[] strNumOfVehi;
	private String[] strDelay;
	private String[] strTypeProba;
	private String[] strDircProba;

	public TrafficPropertyRead() {
		trafficConfig = new Properties();
		try {
			InputStream in = getClass().getResourceAsStream("trafficConfig.properties");
			trafficConfig.load(in);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Load properties file fail!");
		}

		strNumOfVehi = trafficConfig.getProperty("numOfVehicle").split(",");
		for (String value : strNumOfVehi) {
			vecNumOfVehicle.add(Integer.parseInt(value));
		}

		strDelay = trafficConfig.getProperty("delay").split(",");
		for (String value : strDelay) {
			vecDelay.add(Integer.parseInt(value));
		}

		strTypeProba = trafficConfig.getProperty("typeProbability").split(",");
		for (String val : strTypeProba) {
			vecTypeProba.add(Float.parseFloat(val));
		}

		strDircProba = trafficConfig.getProperty("directionProbability").split(
				",");
		for (String val : strDircProba) {
			vecDircProba.add(Float.parseFloat(val));
		}
	}

	public Vector<Integer> getVecNumOfVehicle() {
		return vecNumOfVehicle;
	}

	public Vector<Integer> getVecDelay() {
		return vecDelay;
	}

	public Vector<Float> getVecTypeProba() {
		return vecTypeProba;
	}

	public Vector<Float> getVecDircProba() {
		return vecDircProba;
	}
}
