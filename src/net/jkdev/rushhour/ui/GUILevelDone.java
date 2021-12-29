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

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;

import net.jkdev.rushhour.RushHour;

public class GUILevelDone extends GUIView{
	
	private NkRect	rect;
	private int		windowSettings	= NK_WINDOW_TITLE | NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_MOVABLE;
	private boolean	btnHovered		= false;
	
	@Override
	public void render(RushHour game, GUI gui, NkContext ctx){
		if(rect == null){
			rect = NkRect.create().set(50, 50, 350, 250);
			centerRect(game, rect);
			
			nk_begin(ctx, "Level abgeschlossen!", rect, windowSettings);
			nk_end(ctx);
		}else
			if(!nk_window_is_closed(ctx, "Level abgeschlossen!")){

				gui.setDefaultStyle();

				if(nk_begin(ctx, "Level abgeschlossen!", rect, windowSettings)){

					nk_layout_row_dynamic(ctx, 40.0F, 1);

					nk_label_colored(ctx, "Zeit: " + game.ingameOverlay.getTimeFormatted(), NK_TEXT_ALIGN_CENTERED, gui.colorWhite);

					btnHovered = false;

					if(game.getCurrentLevel() + 1 < game.getLevelCount()){
						btnHovered |= nk_widget_is_hovered(ctx);
						if(nk_button_label(ctx, "Nächstes Level")){
							System.out.println("UI: Nächstes Level");

							gui.closeView(this);
							game.setupLevel(game.getCurrentLevel() + 1);
						}
					}else{
						nk_widget(rect, ctx);
					}

					btnHovered |= nk_widget_is_hovered(ctx);
					if(nk_button_label(ctx, "Hauptmenü")){
						System.out.println("UI: Hauptmenü");

						gui.closeView(this);
						gui.showView(new GUIMainMenu());
						game.setupLevel(0);
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
