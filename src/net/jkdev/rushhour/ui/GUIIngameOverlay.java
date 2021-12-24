package net.jkdev.rushhour.ui;

import static org.lwjgl.nuklear.Nuklear.*;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;

import net.jkdev.rushhour.RushHour;

public class GUIIngameOverlay extends GUIView{

	private static final int	SECOND	= 1000;
	private static final int	MINUTE	= SECOND * 60;
	private static final int	HOUR	= MINUTE * 60;

	private NkRect	rect;
	private int		windowSettings	= NK_WINDOW_NO_SCROLLBAR;
	private boolean	btnHovered		= false;

	private long counter = 0L;

	private long lastFrame = 0L;

	@Override
	public void render(RushHour game, GUI gui, NkContext ctx){
		boolean paused = game.isGamePaused();
		if(!paused || gui.viewList.stream().filter(view -> view instanceof GUIPauseMenu).count() > 0){
			if(!paused && lastFrame != 0L){
				counter += System.currentTimeMillis() - lastFrame;
			}

			if(rect == null){
				rect = NkRect.create().x(0).y(0).h(90.0F);
			}
			rect.w(counter >= HOUR ? 230.0F : 180.0F);

			gui.setDefaultStyle();
			ctx.style().window().fixed_background().data().color().a((byte) 220);

			if(nk_begin(ctx, "ingame_overlay", rect, windowSettings)){
				nk_layout_row_dynamic(ctx, 40.0F, 1);

				btnHovered = false;
				nk_label_colored(ctx, "Level " + game.getCurrentLevel(), NK_TEXT_ALIGN_LEFT, gui.colorWhite);
				nk_label_colored(ctx, getTimeFormatted(), NK_TEXT_ALIGN_LEFT, gui.colorWhite);
			}
			nk_end(ctx);
		}
		lastFrame = System.currentTimeMillis();
	}

	public long getTime(){
		return counter;
	}

	public String getTimeFormatted(){
		long currentTime = counter;
		int hours = (int) (currentTime / HOUR);
		currentTime %= HOUR;
		int minutes = (int) (currentTime / MINUTE);
		currentTime %= MINUTE;
		int seconds = (int) (currentTime / SECOND);
		int millis = (int) (currentTime % SECOND);
		return String.format(hours != 0 ? "%1$2d:%2$02d:%3$02d.%4$03d" : "%2$02d:%3$02d.%4$03d", hours, minutes, seconds, millis);
	}

	public void reset(){
		counter = 0L;
		rect = null;
	}

	@Override
	public boolean pauseGame(){
		return false;
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
