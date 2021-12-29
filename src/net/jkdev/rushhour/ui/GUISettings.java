package net.jkdev.rushhour.ui;

import static org.lwjgl.nuklear.Nuklear.*;

import java.io.IOException;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;

import net.jkdev.rushhour.RushHour;

public class GUISettings extends GUIView{

	public static final String TITLE = "Einstellungen";

	private NkRect	rect;
	private int		windowSettings	= NK_WINDOW_TITLE | NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_MOVABLE;
	private boolean	btnHovered		= false;
	
	@Override
	public void render(RushHour game, GUI gui, NkContext ctx){
		if(rect == null){
			rect = NkRect.create().w(350).h(200);
			centerRect(game, rect);
			nk_begin(ctx, TITLE, rect, windowSettings);
			nk_end(ctx);
		}else
			if(!nk_window_is_closed(ctx, TITLE)){
				
				gui.setDefaultStyle();
				
				if(nk_begin(ctx, TITLE, rect, windowSettings)){

					btnHovered = false;

					nk_layout_row_dynamic(ctx, 40.0F, 1);
					
					nk_label_colored(ctx, "Grafik:", NK_TEXT_ALIGN_CENTERED | NK_TEXT_ALIGN_MIDDLE, gui.colorWhite);

					btnHovered |= nk_widget_is_hovered(ctx);
					if(nk_checkbox_label(ctx, "Lighting", game.settings.stateLighting)){
						System.out.println("UI: Lighting");

						try{
							game.settings.saveSettings(game);
						}catch(IOException e){
							RushHour.handleError(e);
						}
					}
					
					nk_layout_row_dynamic(ctx, 40.0F, 1);
					
					btnHovered |= nk_widget_is_hovered(ctx);
					if(nk_button_label(ctx, "Zurück")){
						System.out.println("UI: Zurück");
						
						gui.closeView(this);
						gui.showView(new GUIMainMenu());
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
	public boolean controlHovered(){
		return btnHovered;
	}
	
	@Override
	public void destroy(){
		rect.free();
	}
	
}
