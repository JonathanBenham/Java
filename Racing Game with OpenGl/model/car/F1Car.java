package edu.cg.models.Car;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.*;

import edu.cg.algebra.Point;
import edu.cg.models.BoundingSphere;
import edu.cg.models.IIntersectable;
import edu.cg.models.IRenderable;

/**
 * A F1 Racing Car.
 *
 */
public class F1Car implements IRenderable, IIntersectable {
	// TODO : Add new design features to the car.
	// Remember to include a ReadMe file specifying what you implemented.
	Center carCenter = new Center();
	Back carBack = new Back();
	Front carFront = new Front();

	@Override
	public void render(GL2 gl) {
		carCenter.render(gl);
		gl.glPushMatrix();
		gl.glTranslated(-Specification.B_LENGTH / 2.0 - Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		carBack.render(gl);
		gl.glPopMatrix();
		gl.glPushMatrix();
		gl.glTranslated(Specification.F_LENGTH / 2.0 + Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		carFront.render(gl);
		gl.glPopMatrix();
	}

	@Override
	public String toString() {
		return "F1Car";
	}

	@Override
	public void init(GL2 gl) {

	}

	@Override
	public void destroy(GL2 gl) {

	}

	@Override
	public List<BoundingSphere> getBoundingSpheres() {
		// TODO: Return a list of bounding spheres the list structure is as follow:
		// s1 -> s2 -> s3 -> s4
		// where:
		// s1 - sphere bounding the whole car
		// s2 - sphere bounding the car front
		// s3 - sphere bounding the car center
		// s4 - sphere bounding the car back
		//
		// * NOTE:
		// All spheres should be adapted so that they are place relative to
		// the car model coordinate system.
		LinkedList<BoundingSphere> res = new LinkedList<BoundingSphere>();
		Point center = new Point(0.0,Specification.C_FRONT_HEIGHT_2,0);
		double radius = ( Specification.C_LENGTH/2 + Specification.F_LENGTH)+0.01 ;
		//double radius = Specification.C_FRONT_DEPTH_2;
		BoundingSphere s1 = new BoundingSphere(radius, center);
		s1.setSphereColore3d(0,0, 0);
		res.add(s1);
		
		
		
		List<BoundingSphere> frontSphere = this.carFront.getBoundingSpheres();
		BoundingSphere s2 = frontSphere.get(0);
		s2.translateCenter(Specification.F_LENGTH / 2.0 + Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		res.add(s2);
		
		List<BoundingSphere> centerSphere = this.carCenter.getBoundingSpheres();
		BoundingSphere s3 = centerSphere.get(0);
		res.add(s3);
		
		List<BoundingSphere> backSphere = this.carBack.getBoundingSpheres();
		BoundingSphere s4 = backSphere.get(0);
		s4.translateCenter(-Specification.B_LENGTH / 2.0 - Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		res.add(s4);
		
		
		return res;
	}
}
