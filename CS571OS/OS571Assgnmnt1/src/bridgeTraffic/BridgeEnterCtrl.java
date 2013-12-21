/**
 * OS571 Assignment 1
 * BridgeEnterCtrl.java
 * 
 * Maofei Chen
 * G00709508
 * 
 *  The Monitor to control vehicle entering the bridge
 */
package bridgeTraffic;

import java.util.Vector;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mchen
 *
 */
public class BridgeEnterCtrl {
	private static int weightBear;
	Vector<Vechicle> vehiOnBrdg = new Vector<Vechicle>(); // vehicles on bridge
	private int numNorthVehi = 0; // number of NORTH vehicles, used to control enter priority
	
	private final Lock lock = new ReentrantLock();
	private Condition north = lock.newCondition(); // north bound lock
	private Condition south = lock.newCondition(); // south bound lock
	
	public BridgeEnterCtrl(Bridge bridge){
		weightBear = bridge.getWeightBear();
		System.out.println("The bridge bear weight is " + weightBear + " unit.");
	}

	public int getNumNorthVehi(){
		return numNorthVehi;
	}
	
	public void incrsOneNorthVehi(){ // if a north vehicle initialized, increase 1
		numNorthVehi++ ;
	}
	
	// arrive method, check all rules that if can enter or not
	// if can, the continue execute; otherwise wait until notify
	/* public synchronized void arrive(Vechicle vehi) { */
	public void arrive(Vechicle vehi) {
		lock.lock();
		try{
			// P1 northbound traffic will have absolute priority
			// if there is north vehicle, south vehicle must wait
			while ((numNorthVehi > 0 && vehi.getDirc().equals(Direction.SOUTH))) {
				try {
					south.await();
					System.out.println(vehi.vehiType + " #" + vehi.getId()
							+ " " + vehi.dirc + " is waiting.");					
				} catch (InterruptedException e) {}
			}

			// Test north vehicles to satisfy all rules
			while ((!vehiOnBrdg.isEmpty() && !vehi.getDirc().equals(vehiOnBrdg.lastElement().getDirc())) // R1 One direction at a time
					|| ((weightBear - vehi.getWeight()) < 0) // R2 can not exceed 1300 unit 
					// P3 vehicle in one direction should cross in alternative order
					|| (!vehiOnBrdg.isEmpty() && (vehi.getVehiType().equals(vehiOnBrdg.lastElement().getVehiType())))) {
					if( vehi.getDirc().equals(Direction.NORTH)){
					try {
						System.out.println(vehi.vehiType + " #" + vehi.getId()
							+ " " + vehi.dirc + " is waiting.");					
						north.await();
					} catch (InterruptedException e) {}
				}
			}

			// Test south vehicles to satisfy all rule
			while ((!vehiOnBrdg.isEmpty() && !vehi.getDirc().equals(vehiOnBrdg.lastElement().getDirc()))
					|| ((weightBear - vehi.getWeight()) < 0)
					|| (!vehiOnBrdg.isEmpty() && (vehi.getVehiType().equals(vehiOnBrdg.lastElement().getVehiType())))) {
					if( vehi.getDirc().equals(Direction.SOUTH)){
					try {
						System.out.println(vehi.vehiType + " #" + vehi.getId()
							+ " " + vehi.dirc + " is waiting.");					
						south.await();
					} catch (InterruptedException e) {}
				}
			}
		} 
		finally{
			lock.unlock();
		}
	}
	
	public void cross(Vechicle vehi){
		lock.lock();
		try {
			weightBear -= vehi.getWeight();
			System.out.println(vehi.vehiType + " #" + vehi.getId() + " "
					+ vehi.dirc + " is now crossing the bridges, takes 2 seconds.");
			vehiOnBrdg.addElement(vehi);
			dispVehiOnBrdg();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		} finally {
			lock.unlock();
		}
	}
	
	public void leave(Vechicle vehi){
		lock.lock();
		try {
			weightBear += vehi.getWeight();
			System.out.println(vehi.vehiType + " #" + vehi.getId() + " "
					+ vehi.dirc + " is now leaving the bridges.");
			vehiOnBrdg.removeElement(vehi);
			dispVehiOnBrdg();

			if (vehi.getDirc().equals(Direction.NORTH)) // one NORTH vehicle leave
				numNorthVehi--;

			north.signal();
			south.signal();
		} finally {
			lock.unlock();
		}
	}
	
	public void dispVehiOnBrdg(){
		System.out.println("----------Bridge Info----------");
		System.out.println(vehiOnBrdg.size() + " vehicles on bridge: ");
		for(int vehiOnBrdgIdx = 0; vehiOnBrdgIdx < vehiOnBrdg.size(); vehiOnBrdgIdx++){
			Vechicle aVehiOnBrdg = vehiOnBrdg.get(vehiOnBrdgIdx);
			System.out.println("#" + aVehiOnBrdg.getId() + " " + aVehiOnBrdg.getVehiType() + " " 
					+ aVehiOnBrdg.getDirc() + " is on bridge");
		}
		System.out.println("-------------------------------");
	}
}
