# RushHour
## About
A 3D game inspired by the board game [Rush Hour](https://en.wikipedia.org/wiki/Rush_Hour_(puzzle)) 
invented by [Nob Yoshigahara](https://en.wikipedia.org/wiki/Nob_Yoshigahara)
in the 1970s and manufactured and sold by ThinkFun since 1996.

### Getting started
First, you need to install [Java](https://www.java.com/) (tested on Java 8 and 17).
Next, download the [latest RushHour release .jar from GitHub](https://github.com/Jan000/RushHour/releases/tag/release).
To run this program you have to execute the RushHourXXX.jar file,
either by double clicking it or utilizing the command line:
> `java -jar RushHourXXX.jar`.

*Don't forget to replace XXX with the version string from the file you are using.*

### Objective
To win the game, the red car needs to be dragged out of the parking lot.<br>
The challenge is to find a way through by moving all vehicles out of the way.<br>
No vehicle can change its direction, they just move forwards and backwards.

### Controls
| Button		| Description						|
| :-: 			| - 								|
| W / A / S / D	| Horizontal movement of the camera	|
| Shift			| Move camera down					|
| Space			| Move camera up					|
| F3 			| Toggle debug mode					|
| F12 		| Take a screenshot					|
| ESC     | Open pause menu           |

**To move any car, hover over a field it is standing on. Then left-click and drag with your mouse.**
**You can see whether you can move a car, when it is fully opaque.**

### Dependencies
This program has been written in [Java](https://en.wikipedia.org/wiki/Java)
and uses the following libraries from [LWJGL](https://www.lwjgl.org/):
* [OpenGL](https://www.opengl.org/)
* [GLFW](https://www.glfw.org/)
* [Nuklear](https://github.com/Immediate-Mode-UI/Nuklear)
* [stb](https://github.com/nothings/stb)
* [JOML](http://joml-ci.github.io/JOML/)

And also:
* [java-data-front](https://github.com/mokiat/java-data-front)
* [PNGDecoder](https://github.com/MatthiasMann/twl/blob/master/src/de/matthiasmann/twl/utils/PNGDecoder.java)

### Notes
This program has yet only been tested on Windows 10 pro 64 bit running Java 17.

### TODO
* Exchange the java-data-front library with Assimp, which is included in LWJGL.
* Show instructions for controls and make them changable through the settings GUI.
* Introduce a scoreboard with world-wide highscores.
* Add an app icon
* Create an installer
* Internationalization
* Add more defined and randomly generated levels
* Add a level builder
* Allow users to share their own levels

### Contact
Any suggestions, ideas or bugs found?<br>
Please feel free to leave a comment here on GitHub or contact me at [contact@jandev.net](contact@jandev.net).

*Made by Jan Kiefer*

### Donate
If you like what I do and you want to support me,
you can donate to me via PayPal [@JanKiefer](https://paypal.me/JanKiefer).<br>
Special thanks in advance to every supporter! 💖

### ***Have fun!***
