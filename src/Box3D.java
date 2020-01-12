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
 *  Box3D object with 1 mesh. 
 *  
 */

public class Box3D {

	private Point3D center;
	private float r;
	public Mesh3D mesh;

	public Box3D(float _x, float _y, float _z, float _r)
	{
		center = new Point3D(_x,_y,_z);
		r = _r;
		initMesh();
	}

	public void set_center(float _x, float _y, float _z)
	{
		center.x=_x;
		center.y=_y;
		center.z=_z;
		fillMesh();  // update the triangle mesh
	}

	
	private void initMesh()
	{
		mesh = new Mesh3D(12, 3);
		fillMesh();  // set the mesh vertices and normals
	}
	
	// fill the triangle mesh vertices and normals
	// using the current parameters for the box
	private void fillMesh()
	{
				
		// Cut box into 6 faces and 12 triangles
		Point3D p0 = new Point3D(center.x-r,center.y-r,center.z-r);
		Point3D p1 = new Point3D(center.x-r,center.y-r,center.z+r);
		Point3D p2 = new Point3D(center.x+r,center.y-r,center.z+r);
		Point3D p3 = new Point3D(center.x+r,center.y-r,center.z-r);
		Point3D p4 = new Point3D(center.x-r,center.y+r,center.z-r);
		Point3D p5 = new Point3D(center.x-r,center.y+r,center.z+r);
		Point3D p6 = new Point3D(center.x+r,center.y+r,center.z+r);
		Point3D p7 = new Point3D(center.x+r,center.y+r,center.z-r);
		
		for (int i=0; i<3; i++) {
			for (int j=0; j<3; j++) {
				mesh.v[2*i][j] = p0;
				mesh.v[2*i+6][j] = p6;
			}			
		}
		
		for (int c=0; c<3; c++) {
			mesh.n[0][c] = new Point3D(0, -1, 0);	
			mesh.n[1][c] = new Point3D(0, -1, 0);
			mesh.n[2][c] = new Point3D(-1, 0, 0);
			mesh.n[3][c] = new Point3D(-1, 0, 0);
			mesh.n[4][c] = new Point3D(0, 0, -1);
			mesh.n[5][c] = new Point3D(0, 0, -1);
			mesh.n[6][c] = new Point3D(0, 1, 0);
			mesh.n[7][c] = new Point3D(0, 1, 0);
			mesh.n[8][c] = new Point3D(1, 0, 0);
			mesh.n[9][c] = new Point3D(1, 0, 0);
			mesh.n[10][c] = new Point3D(0, 0, 1);
			mesh.n[11][c] = new Point3D(0, 0, 1);
		}
		
		mesh.v[1][0] = p1;
		mesh.v[1][1] = p2;
		mesh.v[1][2] = p3;
		mesh.v[3][0] = p1;
		mesh.v[3][1] = p5;
		mesh.v[3][2] = p4;
		mesh.v[5][0] = p4;
		mesh.v[5][1] = p7;
		mesh.v[5][2] = p3;
		mesh.v[7][0] = p5;
		mesh.v[7][1] = p4;
		mesh.v[7][2] = p7;
		mesh.v[9][0] = p2;
		mesh.v[9][1] = p3;
		mesh.v[9][2] = p7;
		mesh.v[11][0] = p2;
		mesh.v[11][1] = p1;
		mesh.v[11][2] = p5;
		
	}
}
