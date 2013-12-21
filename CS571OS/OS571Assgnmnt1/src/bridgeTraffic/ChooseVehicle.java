/**
 * OS571 Assignment 1
 * ChooseVehicle.java
 * Maofei Chen
 * G00709508
 * 
 * Given probability, choose direction and type of vehicle
 */
package bridgeTraffic;

import java.util.Random;
/**
 * @author mchen
 *
 */
public class ChooseVehicle {
	private float probaNorth_f;
	private float probaSouth_f;
	private float probaTruck_f;
	private float probaCar_f;

	private Direction dirc;
	private VehicleType vehiType;
	
	public ChooseVehicle(float probaNorth, float probaSouth, float probaTruck, float probaCar) {
		this.probaNorth_f = probaNorth;
		this.probaSouth_f = probaSouth;
		this.probaTruck_f = probaTruck;
		this.probaCar_f = probaCar;
	
		if( (this.probaNorth_f + this.probaSouth_f) != 1 || (this.probaTruck_f + this.probaCar_f) != 1){
			System.out.println("Given probability error!");
		}
		
		this.probaNorth_f *= 100;
		this.probaSouth_f *= 100;
		this.probaTruck_f *= 100;
		this.probaCar_f *= 100;

		int rand = new Random().nextInt(100);

		if (this.probaNorth_f <= this.probaSouth_f) {
			if (rand <= Math.round(probaNorth_f))
				dirc = Direction.NORTH;
			else
				dirc = Direction.SOUTH;
		} else {
			if (rand <= Math.round(probaSouth_f))
				dirc = Direction.SOUTH;
			else
				dirc = Direction.NORTH;
		}

		if (this.probaTruck_f <= this.probaCar_f) {
			if (rand <= Math.round(probaTruck_f))
				vehiType = VehicleType.TRUCK;
			else
				vehiType = VehicleType.CAR;
		} else {
			if (rand <= Math.round(probaCar_f))
				vehiType = VehicleType.CAR;
			else
				vehiType = VehicleType.TRUCK;
		}

	}
	
	public Direction getDirc(){
		return dirc;
	}
	
	public VehicleType getVehiType(){
		return vehiType;
	}
}

