package net.jkdev.rushhour.obj.model;

import net.jkdev.rushhour.model.Model;

public class ModelObjectData{

	private Class<? extends ModelWorldObject>	type;
	private Model								model;

	public ModelObjectData(Class<? extends ModelWorldObject> type, Model model){
		this.type = type;
		this.model = model;
	}

	public Class<? extends ModelWorldObject> getType(){
		return type;
	}

	public Model getModel(){
		return model;
	}
}
