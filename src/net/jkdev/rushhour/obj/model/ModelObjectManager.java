package net.jkdev.rushhour.obj.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jkdev.rushhour.RushHour;
import net.jkdev.rushhour.model.Model;
import net.jkdev.rushhour.model.impl.VehicleModel;

public class ModelObjectManager{
	
	private final Map<String, ModelObjectData> dataMap = new HashMap<>();
	
	public ModelObjectData getData(String name){
		return dataMap.get(name);
	}
	
	public List<ModelObjectData> getVehicles(int xUnits, int zUnits){
		List<ModelObjectData> results = new ArrayList<>();
		for(ModelObjectData data : dataMap.values()){
			if(data.getModel() instanceof VehicleModel){
				VehicleModel m = (VehicleModel) data.getModel();
				if(m.getUnitsX() == xUnits && m.getUnitsZ() == zUnits){
					results.add(data);
				}
			}
		}
		return results;
	}
	
	public ModelWorldObject create(String name){
		return create(getData(name));
	}
	
	public ModelWorldObject create(ModelObjectData data){
		try{
			ModelWorldObject object = data.getType().getConstructor().newInstance();
			object.init(data.getModel());
			return object;
		}catch(InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException
				| NoSuchMethodException e){
			RushHour.handleError(e);
			return null;
		}
	}
	
	public void register(String name, Class<? extends ModelWorldObject> type, Model model){
		dataMap.put(name, new ModelObjectData(type, model));
	}
	
	public void unregister(String name){
		dataMap.remove(name);
	}
	
	public void destroy(){
		for(ModelObjectData data : dataMap.values()){
			data.getModel().destroy();
		}
		dataMap.clear();
	}
}
