package edu.cg.models.Car;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import edu.cg.models.IRenderable;
import edu.cg.models.SkewedBox;

public class FrontBumber implements IRenderable {
	private SkewedBox bumper = new SkewedBox(Specification.F_BUMPER_LENGTH, Specification.F_BUMPER_HEIGHT_1,
			Specification.F_BUMPER_HEIGHT_2, Specification.F_BUMPER_DEPTH, Specification.F_BUMPER_DEPTH);
	private SkewedBox bumperWing1 = new SkewedBox(Specification.F_BUMPER_LENGTH, Specification.F_BUMPER_WINGS_HEIGHT_1,
			Specification.F_BUMPER_WINGS_HEIGHT_2, Specification.F_BUMPER_WINGS_DEPTH,
			Specification.F_BUMPER_WINGS_DEPTH);
	private SkewedBox bumperWing2 = new SkewedBox(Specification.F_BUMPER_LENGTH, Specification.F_BUMPER_WINGS_HEIGHT_1,
			Specification.F_BUMPER_WINGS_HEIGHT_2, Specification.F_BUMPER_WINGS_DEPTH,
			Specification.F_BUMPER_WINGS_DEPTH);
	

	@Override
	public void render(GL2 gl) {
		// TODO: Render the front bumper relative to it's local coordinate system.
		// Remember the dimensions of the bumper, this is important when you combine the bumper with the hood.
		gl.glPushMatrix();
		//double hoodLength = Specification.F_HOOD_LENGTH_1 + Specification.F_HOOD_LENGTH_2;
		double bumperLength = Specification.F_BUMPER_LENGTH;
		//double bumperDepth = Specification.F_BUMPER_DEPTH * 3;
		// Render hood - Use Red Material.
		Materials.SetBlackMetalMaterial(gl);
		gl.glTranslated(bumperLength / 2.0, 0.0, 0.0);
		bumper.render(gl);
		gl.glTranslated(0.0, 0.0, - (Specification.F_BUMPER_DEPTH / 2.0 + Specification.F_BUMPER_WINGS_DEPTH / 2.0));
		GLU glu = new GLU();
		Materials.SetRedMetalMaterial(gl);
		GLUquadric quad = glu.gluNewQuadric();
		gl.glTranslated(0.0, 0.5 * Specification.TIRE_RADIUS,0.0);
		glu.gluSphere(quad, 0.025, 100, 100);
		
		gl.glTranslated(0.0, -0.5 * Specification.TIRE_RADIUS,0.0);
		Materials.SetBlackMetalMaterial(gl);
		bumperWing1.render(gl);
		

		gl.glTranslated(0.0, 0.0, (Specification.F_BUMPER_DEPTH + Specification.F_BUMPER_WINGS_DEPTH));
		bumperWing2.render(gl);
		gl.glTranslated(0.0, 0.5 * Specification.TIRE_RADIUS,0.0);
		Materials.SetRedMetalMaterial(gl);
		glu.gluSphere(quad, 0.025, 100, 100);
		glu.gluDeleteQuadric(quad); 
		gl.glPopMatrix();
	}

	@Override
	public void init(GL2 gl) {
	}

	@Override
	public void destroy(GL2 gl) {

	}

	@Override
	public String toString() {
		return "FrontBumper";
	}

}
