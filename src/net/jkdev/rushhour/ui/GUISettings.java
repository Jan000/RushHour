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
