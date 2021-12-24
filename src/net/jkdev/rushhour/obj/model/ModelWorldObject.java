package net.jkdev.rushhour.obj.model;

import net.jkdev.rushhour.RushHour;
import net.jkdev.rushhour.model.Model;
import net.jkdev.rushhour.obj.WorldObject;

public class ModelWorldObject extends WorldObject{
	
	private Model model;

	public void init(Model model){
		this.model = model;
	}

	@Override
	public void render(RushHour game, double delta){
		render(game, delta, color);
	}

	@Override
	public void render(RushHour game, double delta, float[] color){
		super.render(game, delta, color);
		model.render(game, delta, color);
	}

	public Model getModel(){
		return model;
	}
}
