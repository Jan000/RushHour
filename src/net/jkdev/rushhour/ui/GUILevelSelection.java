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

public class GUILevelSelection extends GUIView{
	
	private NkRect	rect;
	private int		windowSettings	= NK_WINDOW_TITLE | NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_MOVABLE;
	private boolean	btnHovered		= false;
	private int		page			= 0;
	
	@Override
	public void render(RushHour game, GUI gui, NkContext ctx){
		if(rect == null){
			rect = NkRect.create().w(350).h(345);
			centerRect(game, rect);
			nk_begin(ctx, "Level", rect, windowSettings);
			nk_end(ctx);
		}else
			if(!nk_window_is_closed(ctx, "Level")){
				
				gui.setDefaultStyle();
				
				if(nk_begin(ctx, "Level", rect, windowSettings)){
					
					nk_layout_row_dynamic(ctx, 40.0F, 4);
					
					btnHovered = false;
					
					int pageContent = 0;
					for(int i = page * 16, lim = Math.min(game.getLevelCount() - 1, i + 16); i < lim; i++){
						btnHovered |= nk_widget_is_hovered(ctx);
						if(nk_button_label(ctx, String.valueOf(i + 1))){
							System.out.println("UI: Level " + (i + 1));
							
							gui.closeView(this);
							game.setupLevel(i + 1);
						}
						pageContent++;
					}
					while(pageContent++ < 16){
						nk_widget(rect, ctx);
					}
					
					nk_layout_row_begin(ctx, NK_DYNAMIC, 40.0F, 2);
					
					nk_layout_row_push(ctx, 0.5F);
					nk_widget(rect, ctx);
					
					nk_layout_row_push(ctx, 0.24F);
					if(page > 0){
						btnHovered |= nk_widget_is_hovered(ctx);
						if(nk_button_label(ctx, "<")){
							System.out.println("UI: List back");
							page--;
						}
					}else{
						nk_widget(rect, ctx);
					}
					
					if((page + 1) * 16 < game.getLevelCount()){
						nk_layout_row_push(ctx, 0.24F);
						btnHovered |= nk_widget_is_hovered(ctx);
						if(nk_button_label(ctx, ">")){
							System.out.println("UI: List forward");
							page++;
						}
					}
					
					nk_layout_row_end(ctx);
					
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
