package net.jkdev.rushhour.obj.impl;

import org.joml.Matrix4f;

public class CarObject extends VehicleObject{
	
	@Override
	public void applyTransform(Matrix4f matrix){
		matrix.translate(4.3F, 1.0F, 3.2F);
		super.applyTransform(matrix);
		
	}
}
