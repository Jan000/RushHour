/**
MIT License

Copyright (c) 2021 Jan Kiefer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

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