package net.jkdev.rushhour.obj.impl;

import org.joml.Matrix4f;

import net.jkdev.rushhour.Rotation;

public class MainCarObject extends VehicleObject{
	
	@Override
	public void applyTransform(Matrix4f matrix){
		matrix.translate(4.3F, 0.6F, 3.0F);
		if(Rotation.isHorizontal(rotation.y)){
			matrix.translate(0.0F, 0.0F, 0.4F);
		}
		super.applyTransform(matrix);
	}
}
