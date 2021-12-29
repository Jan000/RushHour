package net.jkdev.rushhour.ui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.*;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

import net.jkdev.rushhour.RushHour;
import net.jkdev.rushhour.ShaderProgram;
import net.jkdev.rushhour.util.IOUtil;

public class GUI{
	
	private static final int BUFFER_INITIAL_SIZE = 4 * 1024;
	
	private static final int	MAX_VERTEX_BUFFER	= 512 * 1024;
	private static final int	MAX_ELEMENT_BUFFER	= 128 * 1024;
	
	private static final NkAllocator ALLOCATOR;
	
	private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;
	
	private ShaderProgram uiShader;
	
	// Storage for font data
	private ByteBuffer ttf;
	// Create a Nuklear context, it is used everywhere.
	private NkContext ctx = NkContext.create();
	// This is the Nuklear font object used for rendering text.
	private NkUserFont default_font = NkUserFont.create();
	// Stores a list of drawing commands that will be passed to OpenGL to render the interface.
	private NkBuffer cmds = NkBuffer.create();
	// An empty texture used for drawing.
	private NkDrawNullTexture null_texture = NkDrawNullTexture.create();
	
	private int	vbo, vao, ebo;
	private int	uniform_tex;
	private int	uniform_proj;
	
	private RushHour game;
	
	public final List<GUIView>	viewList		= new ArrayList<>();
	private final List<GUIView>	renderViewList	= new ArrayList<>();
	
	private boolean				lastHoverState		= false;
	private Consumer<Boolean>	hoverStateListener	= null;
	
	public NkColor colorWhite;
	
	static{
		ALLOCATOR = NkAllocator.create().alloc((handle, old, size) -> nmemAllocChecked(size)).mfree((handle, ptr) -> nmemFree(ptr));
		
		VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4).position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
				.position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8).position(2).attribute(NK_VERTEX_COLOR)
				.format(NK_FORMAT_R8G8B8A8).offset(16).position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
				.flip();
	}
	
	public GUI(RushHour game){
		this.game = game;
		
		try{
			byte[] fontData = IOUtil.readResource("fonts/Sansumi-DemiBold.ttf");
			ttf = BufferUtils.createByteBuffer(fontData.length);
			ttf.put(fontData).flip();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	public void setHoverStateChangeListener(Consumer<Boolean> c){
		hoverStateListener = c;
	}
	
	public void showView(GUIView view){
		synchronized(viewList){
			viewList.add(view);
		}
	}
	
	public void showView(GUIView view, int index){
		synchronized(viewList){
			viewList.add(index, view);
		}
	}
	
	public void closeView(GUIView view){
		synchronized(viewList){
			if(viewList.remove(view)){
				view.destroy();
			}
		}
	}
	
	public void closeViews(){
		synchronized(viewList){
			for(GUIView view : viewList){
				view.destroy();
			}
			viewList.clear();
		}
	}
	
	public List<GUIView> getViews(){
		return viewList;
	}
	
	public boolean shouldPauseGame(){
		for(GUIView view : viewList){
			if(view.pauseGame()){
				return true;
			}
		}
		return false;
	}
	
	public boolean isControlHovered(){
		for(GUIView view : viewList){
			if(view.controlHovered()){
				return true;
			}
		}
		return false;
	}
	
	public void onWindowScroll(long window, double xoffset, double yoffset){
		try(MemoryStack stack = stackPush()){
			NkVec2 scroll = NkVec2.mallocStack(stack).x((float) xoffset).y((float) yoffset);
			nk_input_scroll(ctx, scroll);
		}
	}
	
	public void onCharInput(long window, int codepoint){
		nk_input_unicode(ctx, codepoint);
	}
	
	public void onKeyInput(long window, int key, int scancode, int action, int mods){
		boolean press = action == GLFW_PRESS;
		switch(key){
			case GLFW_KEY_DELETE:
				nk_input_key(ctx, NK_KEY_DEL, press);
				break;
			case GLFW_KEY_ENTER:
				nk_input_key(ctx, NK_KEY_ENTER, press);
				break;
			case GLFW_KEY_TAB:
				nk_input_key(ctx, NK_KEY_TAB, press);
				break;
			case GLFW_KEY_BACKSPACE:
				nk_input_key(ctx, NK_KEY_BACKSPACE, press);
				break;
			case GLFW_KEY_UP:
				nk_input_key(ctx, NK_KEY_UP, press);
				break;
			case GLFW_KEY_DOWN:
				nk_input_key(ctx, NK_KEY_DOWN, press);
				break;
			case GLFW_KEY_HOME:
				nk_input_key(ctx, NK_KEY_TEXT_START, press);
				nk_input_key(ctx, NK_KEY_SCROLL_START, press);
				break;
			case GLFW_KEY_END:
				nk_input_key(ctx, NK_KEY_TEXT_END, press);
				nk_input_key(ctx, NK_KEY_SCROLL_END, press);
				break;
			case GLFW_KEY_PAGE_DOWN:
				nk_input_key(ctx, NK_KEY_SCROLL_DOWN, press);
				break;
			case GLFW_KEY_PAGE_UP:
				nk_input_key(ctx, NK_KEY_SCROLL_UP, press);
				break;
			case GLFW_KEY_LEFT_SHIFT:
			case GLFW_KEY_RIGHT_SHIFT:
				nk_input_key(ctx, NK_KEY_SHIFT, press);
				break;
			case GLFW_KEY_LEFT_CONTROL:
			case GLFW_KEY_RIGHT_CONTROL:
				if(press){
					nk_input_key(ctx, NK_KEY_COPY, glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_PASTE, glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_CUT, glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_TEXT_UNDO, glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_TEXT_REDO, glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_TEXT_WORD_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_TEXT_WORD_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_TEXT_LINE_START, glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_TEXT_LINE_END, glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS);
				}else{
					nk_input_key(ctx, NK_KEY_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
					nk_input_key(ctx, NK_KEY_COPY, false);
					nk_input_key(ctx, NK_KEY_PASTE, false);
					nk_input_key(ctx, NK_KEY_CUT, false);
					nk_input_key(ctx, NK_KEY_SHIFT, false);
				}
				break;
		}
	}
	
	public void onCursorInput(long window, double xpos, double ypos){
		nk_input_motion(ctx, (int) xpos, (int) ypos);
	}
	
	public void onMouseButtonInput(long window, int button, int action, int mods){
		try(MemoryStack stack = stackPush()){
			DoubleBuffer cx = stack.mallocDouble(1);
			DoubleBuffer cy = stack.mallocDouble(1);
			
			glfwGetCursorPos(window, cx, cy);
			
			int x = (int) cx.get(0);
			int y = (int) cy.get(0);
			
			int nkButton;
			switch(button){
				case GLFW_MOUSE_BUTTON_RIGHT:
					nkButton = NK_BUTTON_RIGHT;
					break;
				case GLFW_MOUSE_BUTTON_MIDDLE:
					nkButton = NK_BUTTON_MIDDLE;
					break;
				default:
					nkButton = NK_BUTTON_LEFT;
			}
			
			nk_input_button(ctx, nkButton, x, y, action == GLFW_PRESS);
		}
	}
	
	public void setupWindow(long win){
		nk_init(ctx, ALLOCATOR, null);
		
		NkClipboard clipboard = ctx.clip();
		clipboard.copy((handle, text, len) -> {
			if(len == 0){
				return;
			}
			
			try(MemoryStack stack = stackPush()){
				ByteBuffer str = stack.malloc(len + 1);
				memCopy(text, memAddress(str), len);
				str.put(len, (byte) 0);
				
				glfwSetClipboardString(win, str);
			}
		});
		clipboard.paste((handle, edit) -> {
			long text = nglfwGetClipboardString(win);
			if(text != NULL){
				nnk_textedit_paste(edit, text, nnk_strlen(text));
			}
		});
		
		setupContext();
	}
	
	private void setupContext(){
		try{
			uiShader = ShaderProgram.createProgram();
			uiShader.attachVertexShaderResource("shaders/gui.vs.glsl");
			uiShader.attachFragmentShaderResource("shaders/gui.fs.glsl");
			uiShader.link();
			uiShader.printStatus("UI Shader");
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		
		nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);
		
		uniform_proj = 0;
		uniform_tex = 1;
		
		{
			// buffer setup
			vao = glCreateVertexArrays();
			vbo = glCreateBuffers();
			ebo = glCreateBuffers();
			
			glVertexArrayVertexBuffer(vao, 0, vbo, 0, 20);
			glVertexArrayVertexBuffer(vao, 1, vbo, 8, 20);
			glVertexArrayVertexBuffer(vao, 2, vbo, 16, 20);
			glVertexArrayBindingDivisor(vao, 0, 0);
			
			//Position
			glVertexArrayAttribBinding(vao, 0, 0);
			glVertexArrayAttribFormat(vao, 0, 2, GL_FLOAT, false, 0);
			glEnableVertexArrayAttrib(vao, 0);
			
			//TexCoord
			glVertexArrayAttribBinding(vao, 1, 1);
			glVertexArrayAttribFormat(vao, 1, 2, GL_FLOAT, false, 0);
			glEnableVertexArrayAttrib(vao, 1);
			
			//Color
			glVertexArrayAttribBinding(vao, 2, 2);
			glVertexArrayAttribFormat(vao, 2, 4, GL_UNSIGNED_BYTE, true, 0);
			glEnableVertexArrayAttrib(vao, 2);
			
			glVertexArrayElementBuffer(vao, ebo);
		}
		
		{
			// null texture setup
			int nullTexID = glGenTextures();
			
			null_texture.texture().id(nullTexID);
			null_texture.uv().set(0.5f, 0.5f);
			
			glBindTexture(GL_TEXTURE_2D, nullTexID);
			try(MemoryStack stack = stackPush()){
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
			}
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		}
		
		glBindTexture(GL_TEXTURE_2D, 0);
		
		colorWhite = NkColor.create().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
	}
	
	public void setDefaultStyle(){
		NkStyleButton btnStyle = ctx.style().button();
		btnStyle.border(1.5F).border_color().set((byte) 0x7F, (byte) 0x7F, (byte) 0x7F, (byte) 0x5F);
		btnStyle.rounding(5.0F);
		btnStyle.text_alignment(NK_TEXT_ALIGN_CENTERED);
		btnStyle.text_background().set((byte) 0xFF, (byte) 0x4F, (byte) 0xFF, (byte) 0xFF);
		btnStyle.text_normal().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
		btnStyle.normal().data().color().set((byte) 0x60, (byte) 0x60, (byte) 0x60, (byte) 0x4F);
		btnStyle.hover().data().color().set((byte) 0x60, (byte) 0x60, (byte) 0x60, (byte) 0x8F);
		btnStyle.active().data().color().set((byte) 0x60, (byte) 0x60, (byte) 0x60, (byte) 0xDF);
		btnStyle.text_active().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
		btnStyle.text_hover().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
		
		NkStyleToggle toggleStyle = ctx.style().checkbox();
		toggleStyle.text_normal().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xDF);
		toggleStyle.text_hover().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
		toggleStyle.text_active().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
		toggleStyle.normal().data().color().set((byte) 0x30, (byte) 0xCF, (byte) 0x30, (byte) 0x5F);
		toggleStyle.hover().data().color().set(toggleStyle.normal().data().color());
		toggleStyle.active().data().color().set((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF);

		NkStyleWindow winStyle = ctx.style().window();
		winStyle.header().label_active().set((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
		winStyle.header().active().data().color().set((byte) 0x5F, (byte) 0x5F, (byte) 0x5F, (byte) 0xDF);
		winStyle.fixed_background().data().color().set((byte) 0x8F, (byte) 0x8F, (byte) 0x8F, (byte) 0xD2);
		winStyle.border_color().set((byte) 0xAF, (byte) 0xAF, (byte) 0xAF, (byte) 0xFF);
		winStyle.header().hover().data().color().set((byte) 0xFF, (byte) 0xAF, (byte) 0xAF, (byte) 0xFF);
		winStyle.padding().set(4.0F, 8.0F);
		winStyle.spacing().set(8.0F, 8.0F);
	}
	
	public NkContext context(){
		return ctx;
	}
	
	public void setupDefaultFont(){
		int BITMAP_W = 1024;
		int BITMAP_H = 1024;
		
		int FONT_HEIGHT = 28;
		int fontTexID = glCreateTextures(GL_TEXTURE_2D);
		
		STBTTFontinfo fontInfo = STBTTFontinfo.create();
		STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(255);
		
		float scale;
		float descent;
		
		try(MemoryStack stack = stackPush()){
			
			stbtt_InitFont(fontInfo, ttf);
			scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);
			
			IntBuffer d = stack.mallocInt(1);
			stbtt_GetFontVMetrics(fontInfo, null, d, null);
			descent = d.get(0) * scale;
			
			ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);
			
			STBTTPackContext pc = STBTTPackContext.mallocStack(stack);
			stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
			stbtt_PackSetOversampling(pc, 4, 4);
			stbtt_PackFontRange(pc, ttf, 0, FONT_HEIGHT, 32, cdata);
			stbtt_PackEnd(pc);
			// Convert R8 to RGBA8
			ByteBuffer texture = memAlloc(BITMAP_W * BITMAP_H * 4);
			for(int i = 0; i < bitmap.capacity(); i++){
				texture.putInt(bitmap.get(i) << 24 | 0x00FFFFFF);
			}
			texture.flip();
			
			glBindTexture(GL_TEXTURE_2D, fontTexID);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			
			memFree(texture);
			memFree(bitmap);
		}
		
		default_font.width((handle, h, text, len) -> {
			float text_width = 0;
			try(MemoryStack stack = stackPush()){
				IntBuffer unicode = stack.mallocInt(1);
				
				int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
				int text_len = glyph_len;
				
				if(glyph_len == 0){
					return 0;
				}
				
				IntBuffer advance = stack.mallocInt(1);
				while(text_len <= len && glyph_len != 0){
					if(unicode.get(0) == NK_UTF_INVALID){
						break;
					}
					
					/* query currently drawn glyph information */
					stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
					text_width += advance.get(0) * scale;
					
					/* offset next glyph */
					glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
					text_len += glyph_len;
				}
			}
			return text_width;
		}).height(FONT_HEIGHT).query((handle, font_height, glyph, codepoint, next_codepoint) -> {
			try(MemoryStack stack = stackPush()){
				FloatBuffer x = stack.floats(0.0f);
				FloatBuffer y = stack.floats(0.0f);
				
				STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);
				IntBuffer advance = stack.mallocInt(1);
				
				stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
				stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);
				
				NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);
				
				ufg.width(q.x1() - q.x0());
				ufg.height(q.y1() - q.y0());
				ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
				ufg.xadvance(advance.get(0) * scale);
				ufg.uv(0).set(q.s0(), q.t0());
				ufg.uv(1).set(q.s1(), q.t1());
			}
		}).texture(it -> it.id(fontTexID));
		
		nk_style_set_font(ctx, default_font);
	}
	
	private void render(int AA, int max_vertex_buffer, int max_element_buffer){
		try(MemoryStack stack = stackPush()){
			// setup global state
			glBlendEquation(GL_FUNC_ADD);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glDisable(GL_CULL_FACE);
			glDisable(GL_DEPTH_TEST);
			glEnable(GL_SCISSOR_TEST);
			glActiveTexture(GL_TEXTURE0);
			
			// setup program
			uiShader.bind();
			glUniform1i(uniform_tex, 0);
			
			glUniformMatrix4fv(uniform_proj, false, stack.floats(2.0f / game.getWidth(), 0.0f, 0.0f, 0.0f, 0.0f, -2.0f / game.getHeight(),
					0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f));
		}
		
		{
			// convert from command queue into draw list and draw to screen
			
			// allocate vertex and element buffer
			glBindVertexArray(vao);
			
			glNamedBufferData(vbo, max_vertex_buffer, GL_STREAM_DRAW);
			glNamedBufferData(ebo, max_element_buffer, GL_STREAM_DRAW);
			
			// load draw vertices & elements directly into vertex + element buffer
			ByteBuffer vertices = Objects.requireNonNull(glMapNamedBuffer(vbo, GL_WRITE_ONLY, max_vertex_buffer, null));
			ByteBuffer elements = Objects.requireNonNull(glMapNamedBuffer(ebo, GL_WRITE_ONLY, max_element_buffer, null));
			try(MemoryStack stack = stackPush()){
				// fill convert configuration
				NkConvertConfig config = NkConvertConfig.callocStack(stack).vertex_layout(VERTEX_LAYOUT).vertex_size(20).vertex_alignment(4)
						.null_texture(null_texture).circle_segment_count(22).curve_segment_count(22).arc_segment_count(22)
						.global_alpha(1.0f).shape_AA(AA).line_AA(AA);
				
				// setup buffers to load vertices and elements
				NkBuffer vbuf = NkBuffer.mallocStack(stack);
				NkBuffer ebuf = NkBuffer.mallocStack(stack);
				
				nk_buffer_init_fixed(vbuf, vertices/*, max_vertex_buffer*/);
				nk_buffer_init_fixed(ebuf, elements/*, max_element_buffer*/);
				nk_convert(ctx, cmds, vbuf, ebuf, config);
			}
			glUnmapNamedBuffer(ebo);
			glUnmapNamedBuffer(vbo);
			
			// iterate over and execute each draw command
			//float fb_scale_x = (float) display_width / (float) width;
			//float fb_scale_y = (float) display_height / (float) height;
			float fb_scale_x = 1.0F;
			float fb_scale_y = 1.0F;
			
			long offset = NULL;
			for(NkDrawCommand cmd = nk__draw_begin(ctx, cmds); cmd != null; cmd = nk__draw_next(cmd, cmds, ctx)){
				if(cmd.elem_count() == 0){
					continue;
				}
				glBindTexture(GL_TEXTURE_2D, cmd.texture().id());
				glScissor((int) (cmd.clip_rect().x() * fb_scale_x),
						(int) ((game.getHeight() - (int) (cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y),
						(int) (cmd.clip_rect().w() * fb_scale_x), (int) (cmd.clip_rect().h() * fb_scale_y));
				glDrawElements(GL_TRIANGLES, cmd.elem_count(), GL_UNSIGNED_SHORT, offset);
				offset += cmd.elem_count() * 2;
			}
			nk_clear(ctx);
		}
		
		// default OpenGL state
		ShaderProgram.unbind();
		glBindVertexArray(0);
		glDisable(GL_BLEND);
		glDisable(GL_SCISSOR_TEST);
	}
	
	public void frameStart(double delta){
		nk_input_begin(ctx);
	}
	
	double d = 0.0D;
	
	public void postEventPoll(double delta){
		long windowID = game.getWindowID();
		NkMouse mouse = ctx.input().mouse();
		if(mouse.grab()){
			glfwSetInputMode(windowID, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
		}else
			if(mouse.grabbed()){
				float prevX = mouse.prev().x();
				float prevY = mouse.prev().y();
				glfwSetCursorPos(windowID, prevX, prevY);
				mouse.pos().x(prevX);
				mouse.pos().y(prevY);
			}else
				if(mouse.ungrab()){
					glfwSetInputMode(windowID, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
				}
			
		nk_input_end(ctx);
		
		renderViews();
		
		if(hoverStateListener != null){
			boolean anyControlHovered = isControlHovered();
			if(anyControlHovered != lastHoverState){
				hoverStateListener.accept(anyControlHovered);
			}
			lastHoverState = anyControlHovered;
		}
	}
	
	public void renderViews(){
		synchronized(viewList){
			renderViewList.clear();
			renderViewList.addAll(viewList);
			for(GUIView view : renderViewList){
				view.render(game, this, ctx);
			}
		}
	}
	
	public void render(double delta){
		render(NK_ANTI_ALIASING_ON, MAX_VERTEX_BUFFER, MAX_ELEMENT_BUFFER);
	}
	
	public void destroy(){
		colorWhite.free();
		uiShader.delete();
		glDeleteTextures(default_font.texture().id());
		glDeleteTextures(null_texture.texture().id());
		glDeleteBuffers(vbo);
		glDeleteBuffers(ebo);
		nk_buffer_free(cmds);
	}
	
	public void shutdown(){
		Objects.requireNonNull(ctx.clip().copy()).free();
		Objects.requireNonNull(ctx.clip().paste()).free();
		nk_free(ctx);
		destroy();
		Objects.requireNonNull(default_font.query()).free();
		Objects.requireNonNull(default_font.width()).free();
		
		Objects.requireNonNull(ALLOCATOR.alloc()).free();
		Objects.requireNonNull(ALLOCATOR.mfree()).free();
	}
}
