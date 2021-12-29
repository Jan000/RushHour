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

package net.jkdev.rushhour;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Der Status von Spieleinstellungen kann hier gespeichert, geladen und abgerufen werden.
 * Die Einstellungen werden in einer .properties Datei gespeichert unter %appdata%/RushHour/settings.properties
 *
 * @author Jan Kiefer
 */
public class GameSettings{

	public static final Path SETTINGS_FILE = Paths.get(System.getenv("APPDATA"), "RushHour", "settings.properties");

	private static final String PROP_LIGHTING = "lighting_disabled";

	private static final int DEFAULT_LIGHTING = 0;

	public final int[] stateLighting = new int[]{DEFAULT_LIGHTING};

	public void loadSettings(RushHour game) throws IOException{
		System.out.println("Konfiguration wird geladen");
		if(Files.exists(SETTINGS_FILE)){
			Properties prop = new Properties();
			try(InputStream in = Files.newInputStream(SETTINGS_FILE, StandardOpenOption.READ, StandardOpenOption.SYNC)){
				prop.load(in);
			}
			String lighting = prop.getProperty(PROP_LIGHTING);
			stateLighting[0] = lighting == null ? DEFAULT_LIGHTING : lighting.equals("1") ? 1 : 0;
		}else{
			stateLighting[0] = DEFAULT_LIGHTING;
		}
	}

	public void saveSettings(RushHour game) throws IOException{
		System.out.println("Konfiguration wird gespeichert");
		Properties prop = new Properties();
		prop.setProperty(PROP_LIGHTING, String.valueOf(stateLighting[0]));
		Files.createDirectories(SETTINGS_FILE.getParent());
		try(OutputStream out = Files.newOutputStream(SETTINGS_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE, StandardOpenOption.SYNC)){
			prop.store(out, "RushHour Settings");
		}
	}

	public boolean isLightingEnabled(){
		return stateLighting[0] == 0;
	}
}
