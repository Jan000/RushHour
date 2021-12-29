package net.jkdev.rushhour;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL45.*;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import net.jkdev.rushhour.Level.LevelData;
import net.jkdev.rushhour.model.Material;
import net.jkdev.rushhour.model.Model;
import net.jkdev.rushhour.model.ModelShader;
import net.jkdev.rushhour.model.impl.VehicleModel;
import net.jkdev.rushhour.obj.WorldObject;
import net.jkdev.rushhour.obj.impl.*;
import net.jkdev.rushhour.obj.model.ModelObjectData;
import net.jkdev.rushhour.obj.model.ModelObjectManager;
import net.jkdev.rushhour.obj.model.ModelWorldObject;
import net.jkdev.rushhour.ui.*;

/**
 * Die Hauptklasse des Spiels.
 * 
 * @author Jan Kiefer
 */
public class RushHour implements Runnable{
	
	public static final String	NAME	= "RushHour";
	public static final float	VERSION	= 1.9F;
	public static final String	STAGE	= "release";
	public static final String	FQ_NAME	= NAME + " v" + VERSION + " " + STAGE;
	
	public static final String LOG_PREFIX = "[" + NAME + "] ";
	
	public static final float	PI		= (float) Math.PI;
	public static final float	PI_2	= (float) (Math.PI * 2.0D);
	public static final float	PI_HALF	= (float) (Math.PI / 2.D);
	
	public static final float	UNIT_SIZE_X		= 8.08F;
	public static final float	UNIT_SIZE_Z		= 7.1F;
	public static final int		UNIT_COUNT_X	= 6;
	public static final int		UNIT_COUNT_Z	= 6;
	
	public static final Vector3fc	MAP_OFFSET			= new Vector3f(0.8F, 0.2F, 0.0F);
	public static final int			MAP_EXIT_X			= 3;
	public static final float		MAP_HOVER_OFFSET_Y	= 1.0F;
	
	public static final Vector3fc UP = new Vector3f(0.0F, 1.0F, 0.0F);
	
	public static final float[] COLOR_WHITE = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
	
	public static final Vector3fc	DEFAULT_CAMERA_POS		= new Vector3f(37.0F, 57.0F, 60.0F);
	public static final float		DEFAULT_CAMERA_YAW		= 0.3F;
	public static final float		DEFAULT_CAMERA_PITCH	= 0.95F;
	public static final float		DEFAULT_CAMERA_ROLL		= 0.0F;
	
	private long	windowID;
	private long	handCursor;
	
	private Callback	debugProc;
	private boolean		debug	= false;
	
	//Matrizen für die Berechnung der Perspektive & Kamera & Modellpositionen
	private final Matrix4f		mvpMatrix			= new Matrix4f();
	private final Matrix4f		projectionMatrix	= new Matrix4f();
	public final Matrix4fStack	modelViewMatrix		= (Matrix4fStack) new Matrix4fStack(8).identity();
	
	//Objekt zum Laden von 2D Texturen in OpenGL
	private TextureLoader textureLoader;
	
	//Shader zum Rendern von 3D Modellen
	private ModelShader modelShader;
	
	//Shader Storage Buffer Object in OpenGL, auf welches die mvpMatrix für Shader gespeichert wird
	private int mvpSsbo;
	
	/* Kameravariablen */
	private final Vector3f	cameraPosition				= new Vector3f(DEFAULT_CAMERA_POS);
	private float			cameraYaw					= DEFAULT_CAMERA_YAW;
	private float			cameraPitch					= DEFAULT_CAMERA_PITCH;
	private float			cameraRoll					= DEFAULT_CAMERA_ROLL;
	private float			cameraRotationSpeed			= 0.0025F;
	private float			cameraMovementSpeed			= 50.0F;
	private float			cameraFieldOfView			= 80.0F;
	private float			cameraZNear					= 0.1F;
	private float			cameraZFar					= 250.0F;
	private boolean			cameraRotationEnabled		= false;
	private float			cameraTrackballMinHeight	= 3.0F;
	private float			cameraTrackballMaxHeight	= 120.0F;
	private float			cameraTrackballMinDistance	= 0.01F;
	private float			cameraTrackballMaxDistance	= 33.5F;
	private float			cameraTrackballSpeed		= 2.0F;
	private float			cameraTrackballDistance		= cameraTrackballMaxDistance;
	private Vector3f		cameraTrackballCenter		= new Vector3f(MAP_OFFSET.x() + 3.0F * UNIT_SIZE_X, 0.0F,
			MAP_OFFSET.z() + 3.0F * UNIT_SIZE_Z);
	
	/* Lichtposition und Farbe */
	private float[]	lightPos	= new float[]{127.0F, 107.0F, 4.7F};
	private float[]	lightColor	= new float[]{1.0F, 1.0F, 1.0F};
	//Bewegungsgeschwindigkeit der Lichtposition im Debug-Modus
	private float lightPosMovementSpeed = 30.0F;
	
	//Zahlwerte für aktuell gedrückte Tasten
	private final Set<Integer> activeInputs = new HashSet<>();
	
	//Zwischenspeicher-Vektoren, um Objektinstanzierung zu vermeiden
	private final Vector3f	tempVec3	= new Vector3f();
	private final Vector3f	tempVec3_1	= new Vector3f();
	private final Vector3f	tempVec3_2	= new Vector3f();
	
	//Zwischenspeicher für das Hochladen von Matrizen über OpenGL
	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	//Variablen für Raytracing Berechnungen, um feststellen zu können, welches Feld mit der Maus berührt wird
	private double			cursorX, cursorY;
	private final Vector3f	cursorNear					= new Vector3f();
	private final Vector3f	cursorFar					= new Vector3f();
	private final Vector3f	cursorLookAt				= new Vector3f();
	public final Ray		cursorRay					= new Ray();
	private final Vector3f	yPlanePoint					= new Vector3f(0.0F, 0.0F, 0.0F);
	private final Vector3f	yPlaneIntersectionMutable	= new Vector3f();
	public final Vector3fc	yPlaneIntersection			= yPlaneIntersectionMutable;
	private final Vector2i	hoveredQuad					= new Vector2i();
	
	//Fenster: x, y, Breite, Höhe
	private int[] viewport = new int[]{0, 0, 1000, 650};
	
	//Wird pro Frame ausgewertet - falls der Wert wahr ist, wird die Perspektivenmatrix neu berechnet
	private boolean perspectiveDirty = false;
	
	//Spielbrett
	private MapObject map;
	
	//Alle Objekte, die gerendert werden sollen
	public final List<WorldObject> worldObjects = new ArrayList<>();
	
	//Objektmanager, um eine bessere Ordnung aller geladenen 3D Modelle zu ermöglichen
	public final ModelObjectManager objectManager = new ModelObjectManager();
	
	//True, wenn das Spiel pausiert werden soll. Kamerabewegungen und Spielbrettinteraktionen werden ignoriert.
	private boolean gamePaused = false;
	
	//Nuklear GUI: In dem GUI Objekt werden alle GUIs registriert und gerendert.
	public final GUI				gui				= new GUI(this);
	public final GUIIngameOverlay	ingameOverlay	= new GUIIngameOverlay();
	
	//Alle erstellten Level werden dieser Liste hinzugefügt. Level 0 ist das Hauptmenü und kann nicht ausgewählt werden.
	public final List<Level>	levelList		= new ArrayList<>();
	private int					currentLevel	= 0;
	
	public final GameSettings settings = new GameSettings();
	
	public RushHour() throws IOException{
		cameraPosition.x = (float) (cameraTrackballCenter.x() + Math.sin(cameraYaw) * cameraTrackballDistance);
		cameraPosition.z = (float) (cameraTrackballCenter.z() + Math.cos(cameraYaw) * cameraTrackballDistance);
		
		/*
		Vector3fc mapOffset = MAP_OFFSET;
		for(int i = 0; i < quads.length; i++){
			Quad[] arr = quads[i];
			for(int j = 0; j < arr.length; j++){
				arr[j] = new Quad(new Vector3f(mapOffset.x() + i * UNIT_SIZE_X, mapOffset.y() + 1, mapOffset.z() + j * UNIT_SIZE_Z),
						new Vector3f(mapOffset.x() + (i + 1) * UNIT_SIZE_X, mapOffset.y() + 1, mapOffset.z() + j * UNIT_SIZE_Z),
						new Vector3f(mapOffset.x() + i * UNIT_SIZE_X, mapOffset.y() + 1, mapOffset.z() + (j + 1) * UNIT_SIZE_Z));
			}
		}
		*/
		
		//=== GLFW Fenster erstellen ===
		
		//Fenstereigenschaften festlegen
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_SAMPLES, 4);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
		
		long windowID = glfwCreateWindow(getWidth(), getHeight(), FQ_NAME, 0L, 0L);
		if(windowID != 0L){
			//Fenster erfolgreich erstellt
			this.windowID = windowID;
		}else{
			throw new RuntimeException("Could not create GLFW window handle.");
		}
		
		//Fenster in der Mitte des Monitors zentrieren
		try(MemoryStack stack = MemoryStack.stackPush()){
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			glfwSetWindowPos(windowID, (vidmode.width() - getWidth()) / 2, (vidmode.height() - getHeight()) / 2);
		}
		
		//Grafikkontext in GLFW für OpenGL initialisieren
		glfwMakeContextCurrent(windowID);
		
		GL.createCapabilities();
		
		//V-Sync aktivieren
		glfwSwapInterval(1);
		
		//=== OpenGL initialisieren ===
		glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glEnable(GL_BLEND);
		glBlendEquationSeparate(GL_FUNC_ADD, GL_FUNC_ADD);
		glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
		glDisable(GL_CULL_FACE);
		
		//Falls debug aktiviert ist, wird hier ein Errorlog registriert, um OpenGL Fehler auszugeben
		setDebug(debug);
		
		//Nuklear initialisieren
		gui.setupWindow(windowID);
		gui.setupDefaultFont();
		//Rendern des Ladebildschirms
		glfwShowWindow(windowID);
		gui.showView(new GUILoadingScreen());
		gui.renderViews();
		gui.render(0);
		glfwSwapBuffers(windowID);
		
		//Cursor erstellen, welcher beim hovern über buttons in einem GUI dargestellt wird
		handCursor = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
		gui.setHoverStateChangeListener(hoverState -> {
			if(hoverState){
				glfwSetCursor(windowID, handCursor);
			}else{
				glfwSetCursor(windowID, 0L);
			}
		});
		
		//Neuberechnung der Perspektive, wenn die Fenstergröße verändert wird
		glfwSetWindowSizeCallback(windowID, (w, width, height) -> {
			viewport[2] = width;
			viewport[3] = height;
			perspectiveDirty = true;
		});
		
		//Tastendrücke interpretieren
		glfwSetKeyCallback(windowID, (w, key, scancode, action, mods) -> {
			gui.onKeyInput(w, key, scancode, action, mods);
			
			if(action == GLFW_PRESS){
				activeInputs.add(key);
				switch(key){
					case GLFW_KEY_F3:
						setDebug(!isDebug());
						break;
					case GLFW_KEY_ESCAPE:
						if(!isGamePaused()){
							gui.showView(new GUIPauseMenu());
						}
						break;
				}
			}else
				if(action == GLFW_RELEASE){
					activeInputs.remove(key);
				}
		});
		
		//Mausklicks interpretieren
		glfwSetMouseButtonCallback(windowID, (w, button, action, mods) -> {
			gui.onMouseButtonInput(w, button, action, mods);
			
			if(!isGamePaused() && button == GLFW_MOUSE_BUTTON_LEFT){
				if(map != null){
					if(action == GLFW_PRESS){
						map.onMouseClick(this);
					}else
						if(action == GLFW_RELEASE){
							map.onMouseRelease(this);
						}
					if(!map.isVehicleMoving()){
						cameraRotationEnabled = action == GLFW_PRESS;
					}
				}else{
					cameraRotationEnabled = action == GLFW_PRESS;
				}
			}
		});
		
		//Mausbewegungen interpretieren
		glfwSetCursorPosCallback(windowID, (w, cursorX, cursorY) -> {
			gui.onCursorInput(w, cursorX, cursorY);
			
			float deltaX = (float) (cursorX - this.cursorX);
			float deltaY = (float) (cursorY - this.cursorY);
			
			if(!isGamePaused()){
				if(map != null){
					map.onCursorMove(this, cursorX, cursorY);
				}
				
				if(cameraRotationEnabled){
					cameraYaw = (cameraYaw + deltaX * cameraRotationSpeed) % PI_2;
					cameraPitch = Math.min(PI_HALF, Math.max(-PI_HALF, cameraPitch + deltaY * cameraRotationSpeed));
					perspectiveDirty = true;
				}
			}
			
			this.cursorX = cursorX;
			this.cursorY = cursorY;
		});
		
		//Zeicheneingabe interpretieren
		glfwSetCharCallback(windowID, (w, scanCode) -> {
			gui.onCharInput(w, scanCode);
		});
		
		//Mausscrolleingabe interpretieren
		glfwSetScrollCallback(windowID, (w, ox, oy) -> {
			gui.onWindowScroll(w, ox, oy);
		});
		
		//=== Spiel initialisieren ===
		
		//Texture Loader
		textureLoader = new TextureLoader();
		
		//Shader initialisieren
		try{
			modelShader = ModelShader.createProgram();
			modelShader.attachVertexShaderResource("shaders/model.vs.glsl");
			modelShader.attachFragmentShaderResource("shaders/model.fs.glsl");
			modelShader.link();
			modelShader.printStatus("Model Shader");
			modelShader.init();
			Material defaultMaterial = new Material("default");
			float[] white = new float[]{1.0F, 1.0F, 1.0F};
			float[] black = new float[]{0.0F, 0.0F, 0.0F};
			defaultMaterial.load(modelShader, textureLoader, null, white, white, black, black, 0.0F, 1.0F, null, null, null, null, null);
			modelShader.defineDefaultMaterial(defaultMaterial);
		}catch(IOException e){
			System.err.println("Shader konnte nicht gelesen werden.");
			throw e;
		}
		
		//Shader Storage Buffer Objects
		mvpSsbo = glCreateBuffers();
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, mvpSsbo);
		
		//3D Modelle laden & registrieren im Objektmanager
		try{
			objectManager.register("truck", TruckObject.class,
					Model.loadOBJ(modelShader, textureLoader, "models/vehicles/truck4.obj", VehicleModel.class));
			
			objectManager.register("car", CarObject.class,
					Model.loadOBJ(modelShader, textureLoader, "models/vehicles/car1.obj", VehicleModel.class));
			
			objectManager.register("maincar", MainCarObject.class,
					Model.loadOBJ(modelShader, textureLoader, "models/vehicles/maincar6.obj", VehicleModel.class));
			
			objectManager.register("arrow", ModelWorldObject.class,
					Model.loadOBJ(modelShader, textureLoader, "models/Arrow2.obj", Model.class));
			
			objectManager.register("ground", ModelWorldObject.class,
					Model.loadOBJ(modelShader, textureLoader, "models/ground2.obj", Model.class));
			
			objectManager.register("map", MapObject.class, Model.loadOBJ(modelShader, textureLoader, "models/map6.obj", Model.class));
			
			objectManager.register("tree", ModelWorldObject.class,
					Model.loadOBJ(modelShader, textureLoader, "models/tree.obj", Model.class));
			
			objectManager.register("bush", ModelWorldObject.class,
					Model.loadOBJ(modelShader, textureLoader, "models/bush.obj", Model.class));
		}catch(IOException e){
			System.err.println("Fehler beim Laden eines 3D-Modells.");
			throw e;
		}
		
		{
			//Büsche und Bäume zur Map hinzufügen
			
			for(int i = 0; i < 12; i++){
				ModelWorldObject bush = objectManager.create("bush");
				bush.position.set(i * (4.5F + Math.random() * 0.2F - 0.1F), 0.2F, 46.5F + Math.random() * 0.3F - 0.15F);
				worldObjects.add(bush);
			}
			
			for(int i = 0; i < 4; i++){
				ModelWorldObject tree = objectManager.create("tree");
				tree.position.set(55.5F + Math.random() * 0.6F - 0.3F, 0.2F, 8 + i * 10 + Math.random() * 0.2F - 0.1F);
				worldObjects.add(tree);
			}
		}
		
		settings.loadSettings(this);
		
		{
			//Hauptmenü-Level
			levelList.add(new Level().addData(2, 0, 3, 1, true).addData(5, 0, 3, 1, false).addData(2, 3, 3, 1, false)
					.addData(1, 0, 2, 1, false).addData(0, 1, 2, 1, false).addData(2, 1, 2, 1, true).addData(3, 2, 2, 1, true)
					.addData(0, 3, 2, 1, true).addData(0, 4, 2, 1, false).addData(4, 5, 2, 1, true).addMainVehicleData(4));
			
			//Level 1
			levelList.add(new Level().addData(2, 0, 3, 1, true).addData(5, 0, 3, 1, false).addData(2, 3, 3, 1, false)
					.addData(1, 0, 2, 1, false).addData(0, 1, 2, 1, false).addData(2, 1, 2, 1, true).addData(3, 2, 2, 1, true)
					.addData(0, 3, 2, 1, true).addData(0, 4, 2, 1, false).addData(4, 5, 2, 1, true).addMainVehicleData(4));
			
			//Level 2
			levelList.add(new Level().addData(2, 1, 3, 1, true).addData(2, 2, 3, 1, false).addData(3, 2, 3, 1, true)
					.addData(2, 5, 3, 1, true).addData(0, 0, 2, 1, false).addData(1, 0, 2, 1, false).addData(2, 0, 2, 1, true)
					.addData(4, 0, 2, 1, true).addData(5, 4, 2, 1, false).addData(0, 5, 2, 1, true).addMainVehicleData(3));
			
			//Level 3
			levelList.add(
					new Level().addData(1, 1, 3, 1, true).addData(2, 2, 3, 1, false).addData(0, 5, 3, 1, true).addData(0, 0, 2, 1, true)
							.addData(2, 0, 2, 1, true).addData(4, 0, 2, 1, false).addData(5, 0, 2, 1, false).addData(3, 2, 2, 1, true)
							.addData(5, 2, 2, 1, false).addData(0, 3, 2, 1, true).addData(4, 4, 2, 1, true).addMainVehicleData(4));
			
			//Level 4
			levelList.add(
					new Level().addData(0, 0, 3, 1, true).addData(5, 0, 3, 1, false).addData(0, 2, 3, 1, false).addData(1, 1, 2, 1, false)
							.addData(2, 1, 2, 1, false).addData(3, 1, 2, 1, true).addData(1, 3, 2, 1, true).addData(4, 3, 2, 1, true)
							.addData(1, 4, 2, 1, true).addData(4, 4, 2, 1, false).addData(2, 5, 2, 1, true).addMainVehicleData(3));
		}
	}
	
	public boolean isDebug(){
		return debug;
	}
	
	public void setDebug(boolean state){
		debug = state;
		if(debug){
			if(debugProc == null){
				debugProc = GLUtil.setupDebugMessageCallback();
				GLCapabilities caps = GL.getCapabilities();
				if(caps.OpenGL43){
					GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_API, GL43.GL_DEBUG_TYPE_OTHER, GL43.GL_DEBUG_SEVERITY_NOTIFICATION,
							(IntBuffer) null, false);
				}else
					if(caps.GL_KHR_debug){
						KHRDebug.glDebugMessageControl(KHRDebug.GL_DEBUG_SOURCE_API, KHRDebug.GL_DEBUG_TYPE_OTHER,
								KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null, false);
					}else
						if(caps.GL_ARB_debug_output){
							ARBDebugOutput.glDebugMessageControlARB(ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB,
									ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB, ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB, (IntBuffer) null,
									false);
						}
			}
		}else{
			if(debugProc != null){
				debugProc.free();
				debugProc = null;
			}
		}
	}
	
	public long getWindowID(){
		return windowID;
	}
	
	public int getWidth(){
		return viewport[2];
	}
	
	public int getHeight(){
		return viewport[3];
	}
	
	public int getLevelCount(){
		return levelList.size();
	}
	
	public void closeGame(){
		//Spiel beenden: Renderschleife wird durch setzen dieses Wertes unterbrochen
		glfwSetWindowShouldClose(windowID, true);
	}
	
	public void updateView(){
		//Berechnung der Perspektive: Im Debugmodus mit freier Kamera, sonst mit Trackball-Kamera
		glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
		if(isDebug()){
			projectionMatrix.identity()
					.perspective((float) Math.toRadians(cameraFieldOfView), (float) getWidth() / getHeight(), cameraZNear, cameraZFar)
					.rotateZ(cameraRoll).rotateX(cameraPitch).rotateY(cameraYaw).translate(cameraPosition.negate(tempVec3));
		}else{
			projectionMatrix.identity()
					.perspective((float) Math.toRadians(cameraFieldOfView), (float) getWidth() / getHeight(), cameraZNear, cameraZFar)
					.lookAt(cameraPosition, cameraTrackballCenter, UP);
		}
		updateMvp();
	}
	
	public void updateMvp(){
		//Hochladen der mvpMatrix zu OpenGL in das Shader Storage Buffer Object mvpSsbo
		projectionMatrix.mul(modelViewMatrix, mvpMatrix);
		mvpMatrix.get(matrixBuffer);
		glNamedBufferData(mvpSsbo, matrixBuffer, GL_DYNAMIC_DRAW);
	}
	
	public void setModelMaterial(Material material){
		modelShader.setMaterial(material);
	}
	
	public void resetModelMaterial(){
		modelShader.setDefaultMaterial();
	}
	
	public ModelWorldObject createWorldObject(String type){
		//Erstellen eines im objectManager registrierten Objektes
		ModelWorldObject wo = objectManager.create(type);
		worldObjects.add(wo);
		return wo;
	}
	
	public VehicleObject createVehicleObject(String type){
		return (VehicleObject) createWorldObject(type);
	}
	
	public void removeAllVehicles(){
		//Alle Objekte vom Spielbrett und aus der Renderliste entfernen, bspw. beim Laden eines neuen Levels
		map.removeAllVehicles();
		List<WorldObject> list = new ArrayList<>();
		for(WorldObject wo : worldObjects){
			if(wo instanceof VehicleObject){
				list.add(wo);
			}
		}
		worldObjects.removeAll(list);
	}
	
	public int getCurrentLevel(){
		return currentLevel;
	}
	
	public void setupLevel(int level){
		//Laden eines Levels; Parameter level gibt den Index in der levelList an
		System.out.println("Lade Level " + level + "...");
		
		cameraPosition.set(DEFAULT_CAMERA_POS);
		cameraYaw = DEFAULT_CAMERA_YAW;
		cameraPitch = DEFAULT_CAMERA_PITCH;
		cameraRoll = DEFAULT_CAMERA_ROLL;
		perspectiveDirty = true;
		
		removeAllVehicles();
		
		currentLevel = level;
		
		Level lvl = levelList.get(level);
		
		ModelObjectData mainCarData = objectManager.getData("maincar");
		
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		
		VehicleObject mainCar = (VehicleObject) objectManager.create(mainCarData);
		worldObjects.add(mainCar);
		mainCar.setColor(COLOR_WHITE);
		map.addVehicle(mainCar, Rotation.NORTH, 3, lvl.mainVehicleZ);
		
		for(LevelData data : lvl.dataList){
			List<ModelObjectData> matchingVehicles = objectManager.getVehicles(data.xUnits, data.zUnits);
			matchingVehicles.remove(mainCarData);
			if(!matchingVehicles.isEmpty()){
				VehicleObject v = (VehicleObject) objectManager.create(matchingVehicles.get(rnd.nextInt(matchingVehicles.size())));
				worldObjects.add(v);
				float r = rnd.nextFloat() * 0.5F;
				float g = 0.3F + rnd.nextFloat() * 0.7F;
				float b = 0.3F + rnd.nextFloat() * 0.7F;
				v.setColor(r, g, b);
				float rot = rnd.nextBoolean() ? data.horizontal ? Rotation.WEST : Rotation.NORTH
						: data.horizontal ? Rotation.EAST : Rotation.SOUTH;
				map.addVehicle(v, rot, data.x, data.z);
				matchingVehicles.clear();
			}else{
				System.out.println("x-units=" + data.xUnits + ", z-units=" + data.zUnits + ": kein passendes Fahrzeug dieser Größe registriert.");
			}
		}
		
		ingameOverlay.reset();
		
		System.out.println("Fertig!");
	}
	
	public boolean isKeyPressed(int key){
		return activeInputs.contains(key);
	}
	
	public boolean isGamePaused(){
		return gui.shouldPauseGame() || gamePaused;
	}
	
	public void setGamePaused(boolean state){
		gamePaused = state;
	}
	
	public boolean getGamePaused(){
		return gamePaused;
	}
	
	public boolean isIngame(){
		return getCurrentLevel() > 0;
	}
	
	public void createScreenshot() {
		int w = getWidth(), h = getHeight();
		
		ByteBuffer data = BufferUtils.createByteBuffer(w * h * 4);
		GL11.glReadPixels(0, 0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
		data.flip();
		
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				img.setRGB(x, h - y - 1, (data.get() << 24) | (data.get() ) | (data.get() << 8) | (data.get() << 16));
			}
		}
		
		try{
			ImageIO.write(img, "PNG", new File("Screenshot-" + System.currentTimeMillis() + ".png"));
		}catch(IOException e){
			e.printStackTrace();
		}
		
		/*
		ByteBuffer swapedScreenData = BufferUtils.createByteBuffer(getWidth() * getHeight() * 4);
		for (int i=0; i<swapedScreenData.capacity(); i+=3) {
			final byte red = screenData.get();
			final byte blue = screenData.get();
			final byte green = screenData.get();
			swapedScreenData.put(green);     // G
			swapedScreenData.put(blue);     // B
			swapedScreenData.put(red);     // R
		}
		swapedScreenData.flip();
		writeScreenShot(swapedScreenData);
	*/
	}
/*
	private void writeScreenShot(ByteBuffer data) {
		byte[] tgaHeader = new byte[] { 0,0,2,0,0,0,0,0,0,0,0,0 };        
		final Date date = new Date();
		File screenFile = new File("Screenshot_"+date.getDate()+date.getMonth()+date.getYear()+"_"+date.getHours()+date.getMinutes()+".tga");
		try {
			FileOutputStream fos = new FileOutputStream(screenFile);
			DataOutputStream dos = new DataOutputStream(fos);
			// write the header
			fos.write(tgaHeader);
			dos.write(128);
			dos.write(2);
			dos.write(224);
			dos.write(1);
			dos.write(24);
			dos.write(0);
			// write the image data
			fos.getChannel().write(data);           
			fos.close();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}
	*/
	
	/**
	 * Renderschleife starten
	 */
	@Override
	public void run(){
		long windowID = this.windowID;
		
		//Schließe den Ladebildschirm
		gui.closeViews();
		
		gui.showView(ingameOverlay);
		gui.showView(new GUIMainMenu());
		
		MapObject map = this.map = (MapObject) objectManager.create("map");
		
		WorldObject ground = createWorldObject("ground");
		ground.scale.set(1.5F);
		ground.position.set(0.0F, -0.5F, 0.0F);
		
		WorldObject arrow = createWorldObject("arrow");
		arrow.position.set(MAP_OFFSET.x() + UNIT_SIZE_X * 3.51F, MAP_OFFSET.y() + 3.5F, MAP_OFFSET.z() - 1.0F);
		arrow.rotation.set(-PI_HALF, -PI_HALF, 0.0F);
		arrow.scale.set(8.0F);
		arrow.setColor(255, 0, 0, 255);
		final float arrowOffsetZ = arrow.position.z;
		final float arrowBounceMax = 4.0F;
		final float arrowBounceSpeed = 2.5F;
		double arrowBounce = 0.0F;
		
		setupLevel(0);
		
		double delta, currentFrame, lastFrame = glfwGetTime();
		
		updateView();
		
		//Führe die Schleife so lange aus,
		//bis das Fenster geschlossen wird
		//(durch Benutzer oder System)
		while(!glfwWindowShouldClose(windowID)){
			currentFrame = glfwGetTime();
			delta = currentFrame - lastFrame;
			lastFrame = currentFrame;
			
			gui.frameStart(delta);
			
			//Abarbeiten von Fensterinteraktionen
			glfwPollEvents();
			
			gui.postEventPoll(delta);
			
			boolean gamePaused = isGamePaused();
			
			if(!gamePaused){
				arrowBounce = (arrowBounce + delta * arrowBounceSpeed) % Math.PI;
				arrow.position.z = (float) (arrowOffsetZ - Math.sin(arrowBounce) * arrowBounceMax);
			}
			arrow.color[3] = isIngame() ? 255 : 0;
			
			if(!gamePaused){
				float movementSpeed = (float) (cameraMovementSpeed * delta);
				float lightMovementSpeed = (float) (lightPosMovementSpeed * delta);
				if(isDebug()){
					//Freie Kamerabewegungen, wenn Debug eingeschaltet ist.
					for(int key : activeInputs){
						switch(key){
							case GLFW_KEY_Q:
								float horizontal = (float) (Math.cos(cameraPitch) * movementSpeed);
								float x = (float) (-Math.sin(cameraYaw) * horizontal);
								float y = (float) (Math.sin(cameraPitch) * movementSpeed);
								float z = (float) (Math.cos(cameraYaw) * horizontal);
								cameraPosition.sub(x, y, z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_E:
								horizontal = (float) (Math.cos(cameraPitch) * movementSpeed);
								x = (float) (-Math.sin(cameraYaw) * horizontal);
								y = (float) (Math.sin(cameraPitch) * movementSpeed);
								z = (float) (Math.cos(cameraYaw) * horizontal);
								cameraPosition.add(x, y, z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_W:
								x = (float) (-Math.sin(cameraYaw) * movementSpeed);
								z = (float) (Math.cos(cameraYaw) * movementSpeed);
								cameraPosition.sub(x, 0.0F, z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_S:
								x = (float) (-Math.sin(cameraYaw) * movementSpeed);
								z = (float) (Math.cos(cameraYaw) * movementSpeed);
								cameraPosition.add(x, 0.0F, z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_A:
								x = (float) (-Math.sin(cameraYaw + PI_HALF) * movementSpeed);
								z = (float) (Math.cos(cameraYaw + PI_HALF) * movementSpeed);
								cameraPosition.add(x, 0.0F, z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_D:
								x = (float) (-Math.sin(cameraYaw + PI_HALF) * movementSpeed);
								z = (float) (Math.cos(cameraYaw + PI_HALF) * movementSpeed);
								cameraPosition.sub(x, 0.0F, z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_SPACE:
								cameraPosition.add(0.0F, movementSpeed, 0.0F);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_LEFT_SHIFT:
								cameraPosition.sub(0.0F, movementSpeed, 0.0F);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_LEFT:
								lightPos[0] -= lightMovementSpeed;
								break;
							case GLFW_KEY_RIGHT:
								lightPos[0] += lightMovementSpeed;
								break;
							case GLFW_KEY_UP:
								lightPos[2] -= lightMovementSpeed;
								break;
							case GLFW_KEY_DOWN:
								lightPos[2] += lightMovementSpeed;
								break;
							case GLFW_KEY_PAGE_UP:
								lightPos[1] += lightMovementSpeed;
								break;
							case GLFW_KEY_PAGE_DOWN:
								lightPos[1] -= lightMovementSpeed;
								break;
						}
					}
				}else{
					//Trackball-Kamera im normalen Spielmodus
					float trackballSpeed = (float) (delta * cameraTrackballSpeed);
					for(int key : activeInputs){
						switch(key){
							case GLFW_KEY_W:
								cameraTrackballDistance = Math.max(cameraTrackballMinDistance, cameraTrackballDistance - movementSpeed);
								float x = (float) (Math.sin(cameraYaw) * cameraTrackballDistance);
								float z = (float) (Math.cos(cameraYaw) * cameraTrackballDistance);
								cameraPosition.set(cameraTrackballCenter.x() + x, cameraPosition.y(), cameraTrackballCenter.z() + z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_S:
								cameraTrackballDistance = Math.min(cameraTrackballMaxDistance, cameraTrackballDistance + movementSpeed);
								x = (float) (Math.sin(cameraYaw) * cameraTrackballDistance);
								z = (float) (Math.cos(cameraYaw) * cameraTrackballDistance);
								cameraPosition.set(cameraTrackballCenter.x() + x, cameraPosition.y(), cameraTrackballCenter.z() + z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_A:
								cameraYaw = (cameraYaw - trackballSpeed) % PI_2;
								x = (float) (Math.sin(cameraYaw) * cameraTrackballDistance);
								z = (float) (Math.cos(cameraYaw) * cameraTrackballDistance);
								cameraPosition.set(cameraTrackballCenter.x() + x, cameraPosition.y(), cameraTrackballCenter.z() + z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_D:
								cameraYaw = (cameraYaw + trackballSpeed) % PI_2;
								x = (float) (Math.sin(cameraYaw) * cameraTrackballDistance);
								z = (float) (Math.cos(cameraYaw) * cameraTrackballDistance);
								cameraPosition.set(cameraTrackballCenter.x() + x, cameraPosition.y(), cameraTrackballCenter.z() + z);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_SPACE:
								cameraPosition.y = Math.min(cameraTrackballMaxHeight, cameraPosition.y + movementSpeed);
								perspectiveDirty = true;
								break;
							case GLFW_KEY_LEFT_SHIFT:
							case GLFW_KEY_RIGHT_SHIFT:
								cameraPosition.y = Math.max(cameraTrackballMinHeight, cameraPosition.y - movementSpeed);
								perspectiveDirty = true;
								break;
						}
					}
				}
			}
			
			//Grafikpuffer leeren (Farbe, Tiefe)
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			if(perspectiveDirty){
				perspectiveDirty = false;
				updateView();
			}
			
			if(!cameraRotationEnabled && !gamePaused){
				//Raytracing Berechnungen, um festzustellen, welches Spielfeld mit der Maus berührt wird
				projectionMatrix.unproject((float) cursorX, viewport[3] - (float) cursorY, 0.0F, viewport, cursorNear);
				projectionMatrix.unproject((float) cursorX, viewport[3] - (float) cursorY, 1.0F, viewport, cursorFar);
				
				cursorFar.sub(cursorNear, cursorLookAt);
				
				cursorRay.position.set(cameraPosition);
				cursorRay.direction.set(cursorLookAt);
				
				//yPlaneIntersection speichert die Koordinate, bei welcher der "Boden" berührt wird
				if(UP.dot(cursorLookAt.normalize()) != 0){
					float t = (UP.dot(yPlanePoint) - UP.dot(cameraPosition)) / UP.dot(cursorLookAt);
					cameraPosition.add(cursorLookAt.mul(t), yPlaneIntersectionMutable);
				}
				
				boardHover: {
					
					Vector3f v0 = tempVec3, v1 = tempVec3_1, v2 = tempVec3_2;
					Vector3fc mapOffset = MAP_OFFSET;
					
					for(int x = 0; x < UNIT_COUNT_X; x++){
						for(int z = 0; z < UNIT_COUNT_Z; z++){
							
							v0.set(mapOffset.x() + x * UNIT_SIZE_X, mapOffset.y() + MAP_HOVER_OFFSET_Y, mapOffset.z() + z * UNIT_SIZE_Z);
							v1.set(mapOffset.x() + (x + 1) * UNIT_SIZE_X, mapOffset.y() + MAP_HOVER_OFFSET_Y, mapOffset.z() + z * UNIT_SIZE_Z);
							v2.set(mapOffset.x() + x * UNIT_SIZE_X, mapOffset.y() + MAP_HOVER_OFFSET_Y, mapOffset.z() + (z + 1) * UNIT_SIZE_Z);
							
							if(cursorRay.intersectsQuad(v0, v1, v2)){
								//Das Spielfeld bei x und z wird mit der Maus überfahren
								hoveredQuad.set(x, z);
								map.onHover(this, x, z);
								break boardHover;
							}
						}
					}
					
					//Kein Spielfeld wird mit dem Mauszeiger berührt
					hoveredQuad.set(-1, -1);
					map.onHover(this, -1, -1);
				}
			}
			
			//Modelshader wird in OpenGL aktiviert, um 3D Modelle zeichnen zu können
			modelShader.bind();
			glUniform3fv(1, lightPos);
			glUniform3fv(2, lightColor);
			glUniform1i(3, settings.isLightingEnabled() ? 1 : 0);
			
			//ModelViewMatrix wird zurückgesetzt.
			//Diese gibt die Transformationen (Position, Rotation, Skalierung) von Objekten an
			modelViewMatrix.identity();
			
			//Spielbrett rendern
			map.render(this, delta);
			//Alle Fahrzeuge auf dem Spielbrett rendern
			//Diese werden getrennt von allen anderen Objekten gerendert,
			//da die Transformation relativ zum Spielbrett beibehalten wird
			for(WorldObject wo : worldObjects){
				if(wo instanceof VehicleObject){
					modelViewMatrix.pushMatrix();
					wo.render(this, delta);
					modelViewMatrix.popMatrix();
				}
			}
			
			//Alle restlichen Objekte rendern
			for(WorldObject wo : worldObjects){
				if(!(wo instanceof VehicleObject)){
					modelViewMatrix.identity();
					wo.render(this, delta);
				}
			}
			
			//Alle sichtbaren Nuklear GUIs rendern
			gui.render(delta);
			
			//GL State nach rendern des GUIs zurücksetzen
			glEnable(GL_DEPTH_TEST);
			glDepthFunc(GL_LEQUAL);
			glEnable(GL_BLEND);
			glBlendEquationSeparate(GL_FUNC_ADD, GL_FUNC_ADD);
			glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
			glDisable(GL_CULL_FACE);
			
			//Wechsel des Front/Back-Grafikpuffers
			//(Nicht sichtbarer Puffer wird beschrieben
			// und hier sichtbar gemacht)
			glfwSwapBuffers(windowID);
		}
		
		//Debug wird abgeschaltet, falls eingeschaltet, um den error callback freizugeben
		setDebug(false);
		
		//Fenster schließen
		glfwSetCursor(windowID, 0L);
		glfwDestroyCursor(handCursor);
		glfwSetWindowSizeCallback(windowID, null).free();
		glfwSetKeyCallback(windowID, null).free();
		glfwSetMouseButtonCallback(windowID, null).free();
		glfwSetCursorPosCallback(windowID, null).free();
		glfwDestroyWindow(windowID);
		
		//GUI Ressourcen freigeben
		gui.shutdown();
		
		//Shaderresourcen freigeben
		modelShader.delete();
		
		//Modelresourcen freigeben (Vertex Array Objects und Vertex Buffer Objects)
		objectManager.destroy();
	}
	
	public static void initGLFW(){
		//GLFW initialisieren
		System.out.println("Initialisiere GLFW...");
		
		if(!glfwInit()){
			throw new RuntimeException("GLFW konnte nicht initialisiert werden.");
		}
		GLFWErrorCallback.createPrint(System.err).set();
		System.out.println("GLFW wurde initialisiert.");
	}
	
	/**
	 * Ressourcen freigeben
	 */
	public static void cleanup(){
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	public static synchronized void log(String s){
		System.out.print(LOG_PREFIX);
		System.out.println(s);
	}
	
	public static synchronized void errlog(String s){
		System.err.print(LOG_PREFIX);
		System.err.println(s);
	}
	
	public static synchronized void handleError(Throwable t){
		errlog("An error occurred:");
		t.printStackTrace();
		
		StringWriter stringWriter = new StringWriter();
		t.printStackTrace(new PrintWriter(stringWriter));
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(3, 3, 3, 3), 0, 0);
		
		JLabel lblErrorMessage = new JLabel("Es ist ein Fehler aufgetreten: " + t.getMessage());
		lblErrorMessage.setBorder(BorderFactory.createEmptyBorder(3, 0, 10, 0));
		
		panel.add(lblErrorMessage, c);
		
		JButton btnCopy = new JButton("Kopieren");
		
		c.gridx++;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		panel.add(btnCopy, c);
		
		JTextArea txtStacktrace = new JTextArea();
		txtStacktrace.setEditable(false);
		txtStacktrace.setFont(UIManager.getFont("Label.font"));
		txtStacktrace.setText(stringWriter.toString());
		txtStacktrace.setCaretPosition(0);
		
		JScrollPane spStacktrace = new JScrollPane(txtStacktrace);
		spStacktrace.setPreferredSize(new Dimension(300, 150));
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.VERTICAL;
		c.weighty = 1.0;
		panel.add(spStacktrace, c);
		
		btnCopy.addActionListener(e -> {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(txtStacktrace.getText()), null);
		});
		
		JOptionPane.showMessageDialog(null, panel, FQ_NAME, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void main(String...args){
		System.out.println("Starte " + FQ_NAME + "...");
		initGLFW();
		try{
			new RushHour().run();
		}catch(Throwable t){
			handleError(t);
		}finally{
			cleanup();
		}
	}
}
