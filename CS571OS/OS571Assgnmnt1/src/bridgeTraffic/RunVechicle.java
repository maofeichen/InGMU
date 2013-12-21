package bridgeTraffic;
/**
 * OS571 Assignment 1
 * RunVechicle.java
 * Maofei Chen
 * G00709508
 * 
 * main function to run the program
 */

/**
 * @author mchen
 *
 */

public class RunVechicle {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TrafficPropertyRead trffcPropRd = new TrafficPropertyRead(); // config traffic pattern
		
		// name (index) for each traffic patterns 
		int[] idxOfTraffic = new int[trffcPropRd.getVecNumOfVehicle().size()];
		for (int i = 0; i < idxOfTraffic.length; i++){
			idxOfTraffic[i] = i;
		}
		
		Bridge bridge = new Bridge();
		BridgeEnterCtrl brdgEntrCtrl = new BridgeEnterCtrl(bridge);
		
		int numOfTraffic = trffcPropRd.getVecNumOfVehicle().size();
		for(int trafficInx = 0; trafficInx < numOfTraffic; trafficInx++){
			int numOfVehi = trffcPropRd.getVecNumOfVehicle().elementAt(trafficInx); // number of vehicle for each traffic
			Thread[] vehicle = new Thread[numOfVehi]; // thread array for the traffic
			
			for( int vehiIdx = 0; vehiIdx < numOfVehi; vehiIdx++){ // create thread
				vehicle[vehiIdx] = new Thread(
					new Vechicle(brdgEntrCtrl, new ChooseVehicle(
							trffcPropRd.getVecDircProba().elementAt(0), 
							trffcPropRd.getVecDircProba().elementAt(1), 
							trffcPropRd.getVecTypeProba().elementAt(1), 
							trffcPropRd.getVecTypeProba().elementAt(0)))); 
			}
			
			for( int vehiIdx = 0; vehiIdx < numOfVehi; vehiIdx++){
				vehicle[vehiIdx].start();
			}
			
			if( trafficInx < trffcPropRd.getVecDelay().size()){ // number of delay always one less than number of traffic
				try {
					Thread.sleep(trffcPropRd.getVecDelay().elementAt(trafficInx) * 1000);
				} catch (InterruptedException e) {}
			}
		}
	}
}
