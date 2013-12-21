package bridgeTraffic;
/**
 * OS571 Assignment 1
 * Vehicle.java
 * Maofei Chen
 * G00709508
 * 
 * Define vehicle attribute and method
 */

/**
 * @author mchen
 *
 */
public class Vechicle implements Runnable{
	private int weight;
	private int id;
	private static int numVechi = 0; 
	private BridgeEnterCtrl brdgEntrCtrl;

	final private int weightTruck = 400;
	final private int weightCar = 200;
	
	Direction dirc;
	VehicleType vehiType;
	
	public Vechicle(BridgeEnterCtrl brdgEnCtrl, ChooseVehicle choosVehi){
		id = ++numVechi;
		this.dirc = choosVehi.getDirc();
		this.vehiType = choosVehi.getVehiType();
		
		if(this.vehiType.equals(VehicleType.TRUCK))
			weight = weightTruck;
		else if(this.vehiType.equals(VehicleType.CAR))
			weight = weightCar;
		
		this.brdgEntrCtrl = brdgEnCtrl;
		if(this.dirc.equals(Direction.NORTH))
			brdgEnCtrl.incrsOneNorthVehi();
		// System.out.println("Number of vehicles in NORTH is" + brdgEnCtrl.getNumNorthVehi());
		
		System.out.println(vehiType + " #" + id + " " + dirc + " arrived."); 
	}
	
	public int getWeight(){
		return weight;
	}
	
	public int getId(){
		return id;
	}
	
	public Direction getDirc(){
		return dirc;
	}
	
	public VehicleType getVehiType(){
		return vehiType;
	}
	
	public void run(){
		brdgEntrCtrl.arrive(this);
		brdgEntrCtrl.cross(this);
		
		brdgEntrCtrl.leave(this);
	}
}
