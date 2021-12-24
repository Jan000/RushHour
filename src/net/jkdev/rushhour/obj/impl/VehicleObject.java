package net.jkdev.rushhour.obj.impl;

import net.jkdev.rushhour.model.impl.VehicleModel;
import net.jkdev.rushhour.obj.model.ModelWorldObject;

public class VehicleObject extends ModelWorldObject{
	
	@Override
	public VehicleModel getModel(){
		return (VehicleModel) super.getModel();
	}
}
