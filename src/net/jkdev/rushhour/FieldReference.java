package net.jkdev.rushhour;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.jkdev.rushhour.obj.impl.VehicleObject;

/**
 * Speichert die Referenz eines Fahrzeuges gemeinsam mit dessen zugehöriger Position und Rotation.
 * 
 * @author Jan Kiefer
 */
public class FieldReference{

	private final Vector2i		units	= new Vector2i();
	private final float			rotationY;
	private final VehicleObject	vehicle;
	private final boolean		horizontal;

	public FieldReference(VehicleObject vehicle, float rotation){
		this.vehicle = vehicle;
		rotationY = rotation;
		horizontal = Rotation.isHorizontal(rotation);
		this.vehicle.rotation.y = rotationY;
	}

	public boolean isHorizontal(){
		return horizontal;
	}

	public VehicleObject getVehicle(){
		return vehicle;
	}

	public Vector2ic getLocation(){
		return units;
	}

	public void setLocation(int x, int z){
		units.set(x, z);
	}

	public int getVehicleUnitsX(){
		return horizontal ? vehicle.getModel().getUnitsX() : vehicle.getModel().getUnitsZ();
	}

	public int getVehicleUnitsZ(){
		return horizontal ? vehicle.getModel().getUnitsZ() : vehicle.getModel().getUnitsX();
	}

	public float getPositionXOffset(){
		return horizontal ? (vehicle.getModel().getWidth() - vehicle.getModel().getLength()) / 2.0F : 0;
	}

	public float getPositionZOffset(){
		return horizontal ? 0 : (vehicle.getModel().getWidth() - vehicle.getModel().getLength()) / 2.0F;
	}
}