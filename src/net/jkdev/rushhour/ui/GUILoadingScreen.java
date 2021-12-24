package net.jkdev.rushhour.ui;

import static org.lwjgl.nuklear.Nuklear.*;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;

import net.jkdev.rushhour.RushHour;

public class GUILoadingScreen extends GUIView{
	
	private NkRect	rect;
	private int		windowSettings	= NK_WINDOW_NO_SCROLLBAR;

	@Override
	public void render(RushHour game, GUI gui, NkContext ctx){
		if(rect == null){
			rect = NkRect.create().x(0).y(0);
		}

		rect.w(game.getWidth()).h(game.getHeight());
		gui.setDefaultStyle();

		if(nk_begin(ctx, "loading_screen", rect, windowSettings)){

			nk_layout_row_dynamic(ctx, game.getHeight(), 1);

			nk_label_colored(ctx, "Laden...", NK_TEXT_ALIGN_CENTERED | NK_TEXT_ALIGN_MIDDLE, gui.colorWhite);
		}
		nk_end(ctx);
	}

	@Override
	public boolean pauseGame(){
		return true;
	}

	@Override
	public void destroy(){
		rect.free();
	}
	
	@Override
	public boolean controlHovered(){
		return false;
	}

}
