package net.jkdev.rushhour;

/**
 * Diese Klasse dient dazu, die möglichen Fahrzeugrotationen
 * in Radianten zu speichern und um testen zu können,
 * auf welcher Achse sich ein Fahrzeug bewegt.
 *
 * @author Jan Kiefer
 */
public class Rotation{
	
	public static final float	EAST	= 0.0F;
	public static final float	WEST	= (float) Math.PI;
	public static final float	SOUTH	= (float) (-Math.PI / 2.0D);
	public static final float	NORTH	= (float) (Math.PI / 2.0D);
	
	private Rotation(){}
	
	public static boolean isHorizontal(float rotation){
		return (int) (Math.abs(rotation) / NORTH) % 2 == 0;
	}
}
