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
