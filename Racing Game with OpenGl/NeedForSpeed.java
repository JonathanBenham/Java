package edu.cg;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;
import edu.cg.models.BoundingSphere;
import edu.cg.models.Track;
import edu.cg.models.TrackSegment;
import edu.cg.models.Car.F1Car;
import edu.cg.models.Car.Specification;

/**
 * An OpenGL 3D Game.
 *
 */
public class NeedForSpeed implements GLEventListener {
	private GameState gameState = null; // Tracks the car movement and orientation
	private F1Car car = null; // The F1 car we want to render
	private Vec carCameraTranslation = null; // The accumulated translation that should be applied on the car, camera
												// and light sources
	private Track gameTrack = null; // The game track we want to render
	private FPSAnimator ani; // This object is responsible to redraw the model with a constant FPS
	private Component glPanel; // The canvas we draw on.
	private boolean isModelInitialized = false; // Whether model.init() was called.
	private boolean isDayMode = true; // Indicates whether the lighting mode is day/night.
	private boolean isBirdseyeView = false; // Indicates whether the camera is looking from above on the scene or
											// looking
	// towards the car direction.
	// TODO: add fields as you want. For example:
	// - Car initial position (should be fixed).
	// - Camera initial position (should be fixed)
	// - Different camera settings
	// - Light colors
	// Or in short anything reusable - this make it easier for your to keep track of your implementation
	Point initialPos_Car = new Point(0.0D, Specification.TIRE_RADIUS, 0);


	public NeedForSpeed(Component glPanel) {
		this.glPanel = glPanel;
		gameState = new GameState();
		gameTrack = new Track();
		carCameraTranslation = new Vec(0.0);
		car = new F1Car();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		if (!isModelInitialized) {
			initModel(gl);
		}
		if (isDayMode) {
			// TODO: Setup background when day mode is on
			// use gl.glClearColor() function.
			gl.glClearColor(0.5f,0.8f,0.91f,1.0f);
		} else {
			// TODO: Setup background when night mode is on
			gl.glClearColor(0.1f,0.1f,0.2f,1.0f);
		}
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		// TODO: This is the flow in which we render the scene.
		// Step (1) Update the accumulated translation that needs to be
		// applied on the car, camera and light sources.
		updateCarCameraTranslation(gl);
		// Step (2) Position the camera and setup its orientation
		setupCamera(gl);
		// Step (3) setup the lights.
		setupLights(gl);
		// Step (4) render the car.
		renderCar(gl);
		// Step (5) render the track.
		renderTrack(gl);
		// Step (6) check collision. Note this has nothing to do with OpenGL.
		if (checkCollision()) {
			JOptionPane.showMessageDialog(this.glPanel, "Game is Over");
			this.gameState.resetGameState();
			this.carCameraTranslation = new Vec(0.0);
		}

	}

	/**
	 * @return Checks if the car intersects the one of the boxes on the track.
	 */
	private boolean checkCollision(){
		// TODO: Implement this function to check if the car collides into one of the boxes.
		// You can get the bounding spheres of the track by invoking:
		List<BoundingSphere> trackBoundingSpheres = gameTrack.getBoundingSpheres();
		List<BoundingSphere> carBoundingSpheres = this.translateSpheres(this.car.getBoundingSpheres(), 4);
		for(BoundingSphere SphereTrack :  trackBoundingSpheres){

			if(carBoundingSpheres.get(0).checkIntersection(SphereTrack)){
				if(carBoundingSpheres.get(1).checkIntersection(SphereTrack)|| carBoundingSpheres.get(2).checkIntersection(SphereTrack)
				||carBoundingSpheres.get(3).checkIntersection(SphereTrack))return true;

			}
		}


		//Position working:
		//Point center = new Point(center_Box.x+carCameraTranslation.x,
		//				Specification.TIRE_RADIUS+center_Box.y+carCameraTranslation.y,
		//				center_Box.z+carCameraTranslation.z-8+(Specification.C_LENGTH-Specification.F_LENGTH)*4);
		return false;
	}

	//translate the car bounding spheres
	private List<BoundingSphere> translateSpheres(List<BoundingSphere> carSpheres, int scale){

		List<BoundingSphere> carBoundingSpheres = new LinkedList<BoundingSphere>();
		for(BoundingSphere sphere : carSpheres){
			double radius =  sphere.getRadius()*scale;
			Point center = sphere.getCenter();

			Point new_center = new Point(center.x+carCameraTranslation.x,
					Specification.TIRE_RADIUS+center.y+carCameraTranslation.y,
					center.z+carCameraTranslation.z-6.5-4*(Specification.F_LENGTH)+1);
			BoundingSphere curr_car = new BoundingSphere(radius,new_center);
			carBoundingSpheres.add(curr_car);
		}


		return carBoundingSpheres;
	}

	private void updateCarCameraTranslation(GL2 gl) {
		// Update the car and camera translation values (not the ModelView-Matrix).
		// - Always keep track of the car offset relative to the starting
		// point.
		// - Change the track segments here.
		Vec ret = gameState.getNextTranslation();
		carCameraTranslation = carCameraTranslation.add(ret);
		double dx = Math.max(carCameraTranslation.x, -TrackSegment.ASPHALT_TEXTURE_DEPTH / 2.0 - 2);
		carCameraTranslation.x = (float) Math.min(dx, TrackSegment.ASPHALT_TEXTURE_DEPTH / 2.0 + 2);
		if (Math.abs(carCameraTranslation.z) >= TrackSegment.TRACK_LENGTH + 10.0) {
			carCameraTranslation.z = -(float) (Math.abs(carCameraTranslation.z) % TrackSegment.TRACK_LENGTH);
			gameTrack.changeTrack(gl);
		}
	}

	private void setupCamera(GL2 gl) {
		// TODO: You are advised to use :
		//       GLU glu = new GLU();
		//       glu.gluLookAt();
		if (isBirdseyeView) {
			// TODO Setup camera for Birds-eye view
			GLU glu = new GLU();
			glu.gluLookAt(0.0-carCameraTranslation.x, 50.0, -32.0,
					0.0-carCameraTranslation.x, -1.0, -32.0,
					0.0, 0.0, -1.0);
		} else {
			// TODO Setup camera for Third-person view
			GLU glu = new GLU();
			glu.gluLookAt(0.0, 2.0, 0.0,
					0.0, 2.0, -1.0,
					0.0, 1.0, 0.0);

		}

	}

	void enableCarSpotlight(float[] lightArr, float[] dir, float[] spotLightPosition, int light, GL2 gl) {
		gl.glLightfv(light, GL2.GL_DIFFUSE, lightArr, 0);
		gl.glLightfv(light, GL2.GL_SPECULAR, lightArr, 0);
		gl.glLightfv(light, GL2.GL_SPOT_DIRECTION,dir , 0);
		gl.glLightfv(light, GL2.GL_POSITION, spotLightPosition, 0);
		gl.glLightf(light, GL2.GL_SPOT_CUTOFF, 90.f);
	}

	private void setupLights(GL2 gl) {
		if (isDayMode) {
			// TODO Setup day lighting.
			// * Remember: switch-off any light sources that were used in night mode and are not use in day mode.
			gl.glDisable(GL2.GL_LIGHT0);
			gl.glDisable(GL2.GL_LIGHT1);
			gl.glDisable(GL2.GL_LIGHT2);
			float[] sunIntensity =  new float[]{1.f,1.f,1.f,1.f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, sunIntensity,  0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, sunIntensity,  0);
			float[] sunDirection =  new float[]{0.f,1.f,1.f,0.f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, sunDirection,  0);
			gl.glEnable(GL2.GL_LIGHT0);


		} else {
			// TODO Setup night lighting.
			// * Remember: switch-off any light sources that are used in day mode
			// * Remember: spotlight sources also move with the camera.
			// * You may simulate moon-light using ambient light.
			gl.glDisable(GL2.GL_LIGHT0);
			gl.glDisable(GL2.GL_LIGHT1);
			gl.glDisable(GL2.GL_LIGHT2);
			float[] moonIntensity =  new float[]{0.25f,0.25f,0.25f,0.25f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, moonIntensity,  0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, moonIntensity,  0);
			float[] moonDirection =  new float[]{0.f,1.f,1.f,0.f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, moonDirection,  0);
			gl.glEnable(GL2.GL_LIGHT0);



			gl.glEnable(GL2.GL_LIGHT1);
			gl.glEnable(GL2.GL_LIGHT2);
			float[]spotLightPosition1={(float)(this.initialPos_Car.x-0.5 * Specification.TIRE_RADIUS),
					this.initialPos_Car.y+1.f, this.initialPos_Car.z-9.5f,1.0f};
			float[]spotLightPosition2={(float)(this.initialPos_Car.x+0.5 * Specification.TIRE_RADIUS),
					this.initialPos_Car.y+1.f, this.initialPos_Car.z-9.5f,1.0f};
			float[] lightArr = new float[]{ 0.45f,  0.45f, 0.45f, 0.45f};
			float z_rotation = -(float)(Math.cos(-Math.toRadians(gameState.getCarRotation())));
			float x_rotation = -(float)(Math.sin(-Math.toRadians(gameState.getCarRotation())));
			float[] direction = new float[]{x_rotation, 0.f , z_rotation};
			enableCarSpotlight(lightArr, direction, spotLightPosition1, GL2.GL_LIGHT1, gl );
			enableCarSpotlight(lightArr, direction, spotLightPosition2, GL2.GL_LIGHT2, gl );

				//float[] lightAmbiant =  new float[]{0.f,0.f,0.f,1.f };
				//float[] lightDiffuse =  new float[]{1.f,1.f,1.f,1.f };
				//float[] lightSpecular =  new float[]{1.f,1.f,1.f,1.f };
				//float[] lightPosition1 =  new float[]{1.f,1.f,this.initialPos_Car.z-7.0f+2.f,0.f };

				//gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_AMBIENT, lightAmbiant,  1);
				//gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_DIFFUSE, lightDiffuse,  1);
				//gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_SPECULAR, lightSpecular,  1);
				//gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_POSITION, lightPosition1, 1);



		}

	}

	private void renderTrack(GL2 gl) {
		// * Note: the track is not translated. It should be fixed.
		gl.glPushMatrix();
		gl.glTranslated(0-carCameraTranslation.x, 0-carCameraTranslation.y, 0-carCameraTranslation.z);
		gameTrack.render(gl);
		gl.glPopMatrix();
	}

	private void renderCar(GL2 gl) {
		// TODO: Render the car.
		// * Remember: the car position should be the initial position + the accumulated translation.
		//             This will simulate the car movement.
		// * Remember: the car was modeled locally, you may need to rotate/scale and translate the car appropriately.
		// * Recommendation: it is recommended to define fields (such as car initial position) that can be used during rendering.
		//do accumulated translation + rotation

		gl.glPushMatrix();
		gl.glTranslated(this.initialPos_Car.x, this.initialPos_Car.y, this.initialPos_Car.z-6.5);
		gl.glRotated(90-this.gameState.getCarRotation(), 0, 1,0);
		gl.glScaled(4.D, 4.d, 4.D);
		car.render(gl);
		gl.glPopMatrix();

	}

	public GameState getGameState() {
		return gameState;
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// Initialize display callback timer
		ani = new FPSAnimator(30, true);
		ani.add(drawable);
		glPanel.repaint();

		initModel(gl);
		ani.start();
	}

	public void initModel(GL2 gl) {
		gl.glCullFace(GL2.GL_BACK);
		gl.glEnable(GL2.GL_CULL_FACE);

		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_SMOOTH);

		car.init(gl);
		gameTrack.init(gl);
		isModelInitialized = true;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// TODO Setup the projection matrix here.
		GL2 gl = drawable.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		GLU glu = new GLU();
		double aspectRatio = (double)(width/height);
		glu.gluPerspective(60,aspectRatio, 2, 500);
	}

	/**
	 * Start redrawing the scene with 30 FPS
	 */
	public void startAnimation() {
		if (!ani.isAnimating())
			ani.start();
	}

	/**
	 * Stop redrawing the scene with 30 FPS
	 */
	public void stopAnimation() {
		if (ani.isAnimating())
			ani.stop();
	}

	public void toggleNightMode() {
		isDayMode = !isDayMode;
	}

	public void changeViewMode() {
		isBirdseyeView = !isBirdseyeView;
	}

}
