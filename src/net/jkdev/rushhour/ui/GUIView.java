package net.jkdev.rushhour.ui;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;

import net.jkdev.rushhour.RushHour;

public abstract class GUIView{

	public abstract void render(RushHour game, GUI gui, NkContext ctx);

	public abstract boolean pauseGame();

	public abstract boolean controlHovered();

	public abstract void destroy();
	
	protected void centerRect(RushHour game, NkRect rect){
		int w = game.getWidth(), h = game.getHeight();
		float rw = rect.w(), rh = rect.h();
		rect.x((w - rw) / 2.0F).y((h - rh) / 2.0F);
	}
}
