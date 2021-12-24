package net.jkdev.rushhour.obj;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.jkdev.rushhour.RushHour;

public class WorldObject{

	public final Vector3f	position	= new Vector3f();
	public final Vector3f	rotation	= new Vector3f();
	public final Vector3f	scale		= new Vector3f(1.0F);
	public float[]			color		= new float[]{1.0F, 1.0F, 1.0F, 1.0F};

	public void setColor(float r, float g, float b, float a){
		color[0] = r;
		color[1] = g;
		color[2] = b;
		color[3] = a;
	}

	public void setColor(float r, float g, float b){
		color[0] = r;
		color[1] = g;
		color[2] = b;
	}

	public void setColor(int r, int g, int b, int a){
		color[0] = r / 255.0F;
		color[1] = g / 255.0F;
		color[2] = b / 255.0F;
		color[3] = a / 255.0F;
	}

	public void setColor(int r, int g, int b){
		color[0] = r / 255.0F;
		color[1] = g / 255.0F;
		color[2] = b / 255.0F;
	}

	public void setColor(int hex, boolean hasAlpha){
		color[0] = (hex >> 16 & 0xFF) / 255.0F;
		color[1] = (hex >> 8 & 0xFF) / 255.0F;
		color[2] = (hex & 0xFF) / 255.0F;
		if(hasAlpha){
			color[3] = (hex >> 24 & 0xFF) / 255.0F;
		}
	}

	public void setColor(float[] rgba){
		this.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
	}

	public void applyTransform(Matrix4f matrix){
		matrix.translate(position).rotateZ(rotation.z).rotateX(rotation.x).rotateY(rotation.y).scale(scale);
	}

	public void render(RushHour game, double delta){
		render(game, delta, color);
	}

	public void render(RushHour game, double delta, float[] color){
		applyTransform(game.modelViewMatrix);
		game.updateMvp();
	}
}
