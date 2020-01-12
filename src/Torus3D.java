/*
 * 
 * Name: Xinyuan Zhang
 * Class: CS480
 * 
 * Assignment 4
 * Due: 2019/12/10
 * Problem Number: /
 * 
 * Description: 
 *  Torus3D object with 1 mesh. 
 *  
 */

public class Torus3D {
	
	private Point3D center;
	private float r, r_axial;
	private int stacks,slices;
	public Mesh3D mesh;
	
	public Torus3D(float _x, float _y, float _z, float _r, float _r_axial, int _stacks, int _slices)
	{
		center = new Point3D(_x,_y,_z);
		r = _r;
		r_axial = _r_axial;
		stacks = _stacks;
		slices = _slices;
		initMesh();
	}
	
	public void set_center(float _x, float _y, float _z)
	{
		center.x=_x;
		center.y=_y;
		center.z=_z;
		fillMesh();  // update the triangle mesh
	}
	
	public void set_radius(float _r, float _r_axial)
	{
		r = _r;
		r_axial = _r_axial;
		fillMesh(); // update the triangle mesh
	}
	
	public void set_stacks(int _stacks)
	{
		stacks = _stacks;
		initMesh(); // resized the mesh, must re-initialize
	}
	
	public void set_slices(int _slices)
	{
		slices = _slices;
		initMesh(); // resized the mesh, must re-initialize
	}
	
	public int get_n()
	{
		return slices;
	}
	
	public int get_m()
	{
		return stacks;
	}

	private void initMesh()
	{
		mesh = new Mesh3D(stacks,slices);
		fillMesh();  // set the mesh vertices and normals
	}
		
	// fill the triangle mesh vertices and normals
	// using the current parameters for the torus
	private void fillMesh()
	{
		int i, j;
		float theta, phi;
		float d_theta = (float)(2*Math.PI)/(float)(slices-1);
		float d_phi = (float)(2*Math.PI)/(float)(stacks-1);
		float cos_theta, sin_theta;
		float cos_phi, sin_phi;
		Point3D du = new Point3D();
		Point3D dv = new Point3D();
		
		for(i=0, theta=(float)(-Math.PI); i<slices; i++, theta+=d_theta) {
			cos_theta = (float)Math.cos(theta);
			sin_theta = (float)Math.sin(theta);
			
			for(j=0, phi=(float)(-Math.PI); j<stacks; j++, phi+=d_phi) {
				cos_phi = (float)Math.cos(phi);
				sin_phi = (float)Math.sin(phi);
				
				mesh.v[i][j].x = center.x+(r_axial+r*cos_phi)*cos_theta;
				mesh.v[i][j].y = center.y+(r_axial+r*cos_phi)*sin_theta;
				mesh.v[i][j].z = center.z+r*sin_phi;
				
				du.x = (-r_axial+r*cos_phi)*sin_theta;
				du.y = (r_axial+r*cos_phi)*cos_theta;
				du.z = 0;
				
				dv.x = -r*sin_phi*cos_theta;
				dv.y = -r*sin_phi*sin_theta;
				dv.z = r*cos_phi;
				
				mesh.n[i][j] = du.crossProduct(dv).normalize();
			}
			
		}
	}
}
