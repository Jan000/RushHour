package net.jkdev.rushhour.ui;

import static org.lwjgl.nuklear.Nuklear.*;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;

import net.jkdev.rushhour.RushHour;

public class GUIMainMenu extends GUIView{
	
	private NkRect	rect;
	private int		windowSettings	= NK_WINDOW_TITLE | NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_MOVABLE;
	private boolean	btnHovered		= false;
	
	@Override
	public void render(RushHour game, GUI gui, NkContext ctx){
		if(rect == null){
			rect = NkRect.create().set(50, 50, 350, 200); //248 152
			
			nk_begin(ctx, "Hauptmenü", rect, windowSettings);
			nk_end(ctx);
		}else
			if(!nk_window_is_closed(ctx, "Hauptmenü")){

				gui.setDefaultStyle();

				if(nk_begin(ctx, "Hauptmenü", rect, windowSettings)){

					nk_layout_row_dynamic(ctx, 40.0F, 1);

					btnHovered = false;

					btnHovered |= nk_widget_is_hovered(ctx);
					if(nk_button_label(ctx, "Spielen")){
						System.out.println("UI: Spielen");

						gui.closeView(this);
						gui.showView(new GUILevelSelection());
					}
					
					btnHovered |= nk_widget_is_hovered(ctx);
					if(nk_button_label(ctx, "Einstellungen")){
						System.out.println("UI: Einstellungen");

						gui.closeView(this);
						gui.showView(new GUISettings());
					}

					btnHovered |= nk_widget_is_hovered(ctx);
					if(nk_button_label(ctx, "Beenden")){
						System.out.println("UI: Beenden");

						game.closeGame();
					}
				}
				nk_end(ctx);
			}
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
		return btnHovered;
	}
}
