package net.jkdev.rushhour.obj.impl;

import org.joml.Matrix4f;

import net.jkdev.rushhour.Rotation;
import net.jkdev.rushhour.RushHour;

public class TruckObject extends VehicleObject{
	
	@Override
	public void applyTransform(Matrix4f matrix){
		matrix.translate(4.1F, 0.6F, 3.9F);
		if(Rotation.isHorizontal(rotation.y)){
			matrix.translate(1.7F, 0.0F, 0.0F);
		}
		super.applyTransform(matrix);
		matrix.rotateY(RushHour.PI);
	}
}
