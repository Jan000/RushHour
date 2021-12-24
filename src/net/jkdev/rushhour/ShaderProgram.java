package net.jkdev.rushhour;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL43.*;

import java.io.IOException;

import net.jkdev.rushhour.util.IOUtil;

/**
 * Diese Klasse dient zum Management eines OpenGL Shader Program Objektes.
 * 
 * @author Jan Kiefer
 */
public class ShaderProgram{

	public final int program;
	
	public ShaderProgram(int program) {
		this.program = program;
	}

	public int attachVertexShaderResource(String resourceName) throws IOException{
		return attachShaderResource(resourceName, GL_VERTEX_SHADER);
	}

	public int attachFragmentShaderResource(String resourceName) throws IOException{
		return attachShaderResource(resourceName, GL_FRAGMENT_SHADER);
	}

	public int attachGeometryShaderResource(String resourceName) throws IOException{
		return attachShaderResource(resourceName, GL_GEOMETRY_SHADER);
	}

	public int attachTessControlShaderResource(String resourceName) throws IOException{
		return attachShaderResource(resourceName, GL_TESS_CONTROL_SHADER);
	}

	public int attachTessEvaluationShaderResource(String resourceName) throws IOException{
		return attachShaderResource(resourceName, GL_TESS_EVALUATION_SHADER);
	}

	public int attachComputeShaderResource(String resourceName) throws IOException{
		return attachShaderResource(resourceName, GL_COMPUTE_SHADER);
	}
	
	public int attachShaderResource(String resourceName, int shaderType) throws IOException {
		return attachShader(IOUtil.readResourceUTF8(resourceName), shaderType);
	}
	
	public int attachVertexShader(String source) {
		return attachShader(source, GL_VERTEX_SHADER);
	}
	
	public int attachFragmentShader(String source) {
		return attachShader(source, GL_FRAGMENT_SHADER);
	}
	
	public int attachGeometryShader(String source) {
		return attachShader(source, GL_GEOMETRY_SHADER);
	}
	
	public int attachTessControlShader(String source) {
		return attachShader(source, GL_TESS_CONTROL_SHADER);
	}
	
	public int attachTessEvaluationShader(String source) {
		return attachShader(source, GL_TESS_EVALUATION_SHADER);
	}
	
	public int attachComputeShader(String source) {
		return attachShader(source, GL_COMPUTE_SHADER);
	}
	
	public int attachShader(String source, int shaderType) {
		int shader = glCreateShader(shaderType);
		glShaderSource(shader, source);
		glCompileShader(shader);
		glAttachShader(program, shader);
		return shader;
	}
	
	public void attachShader(int shader) {
		glAttachShader(program, shader);
	}
	
	public int[] getAttachedShaders() {
		int[] count = new int[1];
		glGetProgramiv(program, GL_ATTACHED_SHADERS, count);
		int[] shaders = new int[count[0]];
		glGetAttachedShaders(program, null, shaders);
		return shaders;
	}

	public boolean hasValidationError(){
		return glGetProgrami(program, GL_VALIDATE_STATUS) == GL_FALSE;
	}
	
	public boolean hasLinkError() {
		return glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE;
	}

	public String getInfoLog(){
		return glGetProgramInfoLog(program);
	}
	
	public int getInfoLogLength() {
		return glGetProgrami(program, GL_INFO_LOG_LENGTH);
	}

	public void link(){
		glLinkProgram(program);
	}

	public void validate(){
		glValidateProgram(program);
	}

	public void bind(){
		glUseProgram(program);
	}

	public void deleteShaders(){
		for(int shader : getAttachedShaders()) {
			glDeleteShader(shader);
		}
	}

	public void delete(){
		deleteShaders();
		glDeleteProgram(program);
	}

	public int getUniformLocation(String uniform){
		return glGetUniformLocation(program, uniform);
	}
	
	public String getLinkStatusString(){
		return hasLinkError() ? "FEHLER: " + glGetProgramInfoLog(program) : "OK";
	}
	
	public void printStatus(String name){
		System.out.print("Shader status - " + name + ": ");
		int errCount = 0;
		for(int shader : getAttachedShaders()){
			if(hasShaderCompileError(shader)){
				if(errCount++ == 0){
					System.out.println();
				}
				System.err.println(glGetShaderi(shader, GL_SHADER_TYPE) + ": " + getShaderInfoLog(shader));
			}
		}
		if(errCount == 0){
			System.out.println("OK");
		}else{
			System.out.println(errCount + " shader konnte(n) nicht kompiliert werden.");
		}
		if(hasValidationError() || hasLinkError()){
			System.err.println(getInfoLog());
		}else{
			System.out.println("Shader-Programm erfolgreich gelinkt und validiert.");
		}
	}
	
	public static void unbind(){
		glUseProgram(0);
	}
	
	public static String getShaderInfoLog(int shader){
		return glGetShaderInfoLog(shader);
	}
	
	public static boolean hasShaderCompileError(int shader){
		return shader != 0 && glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE;
	}
	
	public static ShaderProgram createProgram() {
		return new ShaderProgram(glCreateProgram());
	}
	
}
