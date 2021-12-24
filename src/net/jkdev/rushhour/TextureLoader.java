package net.jkdev.rushhour;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL45.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.lwjgl.BufferUtils;

import net.jkdev.rushhour.util.PNGDecoder;

/**
 * Mit Hilfe dieser Klasse wird es ermöglicht, Texturen aus Dateien zu laden, in OpenGL hochzuladen
 * und diese dann für die Verwendung beim Rendern an eine OpenGL Texture Unit zu binden.
 * 
 * @author Jan Kiefer
 */
public class TextureLoader{

	public int sampler;

	public void loadDefaultSampler(){
		sampler = glCreateSamplers();
		glSamplerParameteri(sampler, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_NEAREST);
		glSamplerParameteri(sampler, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
		glSamplerParameteri(sampler, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(sampler, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	public void deleteDefaultSampler(){
		glDeleteSamplers(sampler);
	}

	public IntBuffer loadTextures(String...sysPaths) throws IOException{
		IntBuffer textures = BufferUtils.createIntBuffer(sysPaths.length);
		glCreateTextures(GL_TEXTURE_2D, textures);
		textures.rewind();

		for(int i = 0; i < sysPaths.length; i++){
			loadTexture(sysPaths[i], i, textures);
		}
		return textures;
	}

	public void loadTexture(String sysPath, int index, IntBuffer dest) throws IOException{
		System.out.println("Lade Textur '" + sysPath + "'...");
		try(InputStream in = ClassLoader.getSystemResourceAsStream(sysPath)){
			loadTexture(in, index, dest);
		}
	}

	public void loadTexture(Path p, int index, IntBuffer dest) throws IOException{
		try(InputStream in = Files.newInputStream(p)){
			loadTexture(in, index, dest);
		}
	}

	private void loadTexture(InputStream pngIn, int index, IntBuffer dest) throws IOException{
		PNGDecoder decoder = null;
		ByteBuffer buffer = null;
		decoder = new PNGDecoder(pngIn);
		buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
		decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
		buffer.flip();
		glBindTexture(GL_TEXTURE_2D, dest.get(index));
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glGenerateTextureMipmap(dest.get(index));
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void bindTextures(int first, IntBuffer textures){
		int index = 0;
		while(textures.hasRemaining()){
			glBindTextureUnit(first + index++, textures.get());
		}
		textures.rewind();
	}

	public void bindTexture(int unit, int texture){
		glBindTextureUnit(unit, texture);
	}

	public void unbindTextures(int first, int count){
		for(int i = 0; i < count; i++){
			glBindTextureUnit(first + i, 0);
		}
	}
}
