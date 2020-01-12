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
 *  File given for PA4 interface. 
 *  
 *  1. Implemented keyboard EventListeners for 
 *  translating camera, changing shadings and toggling lights.
 *  
 *  2. Added 3 different 3D rendering scenes.
 *  
 *  3. Modified shadeTest() by seperating scene definitions and methods shadeObject()/shadeBox().
 *  
 */

//****************************************************************************
//       Example Main Program for CS480 PA4
//****************************************************************************
// Description: 
//   
//   This is a template program for the sketching tool.  
//
//     LEFTMOUSE: draw line segments 
//     RIGHTMOUSE: draw triangles 
//
//     The following keys control the program:
//
//		Q,q: quit 
//		C,c: clear polygon (set vertex count=0)
//		R,r: randomly change the color
//		S,s: toggle the smooth shading for triangle 
//			 (no smooth shading by default)
//		T,t: show testing examples
//		>:	 increase the step number for examples
//		<:   decrease the step number for examples
//
//****************************************************************************
// History :
//   Aug 2004 Created by Jianming Zhang based on the C
//   code by Stan Sclaroff
//   Nov 2014 modified to include test cases
//   Nov 5, 2019 Updated by Zezhou Sun
//

import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.*; 
import java.awt.image.*;
//import java.io.File;
//import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

//import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;//for new version of gl
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

import com.jogamp.opengl.util.FPSAnimator;//for new version of gl


public class PA4 extends JFrame
	implements GLEventListener, KeyListener, MouseListener, MouseMotionListener
{
	
	private static final long serialVersionUID = 1L;
	private final int DEFAULT_WINDOW_WIDTH=512;
	private final int DEFAULT_WINDOW_HEIGHT=512;
	private final float DEFAULT_LINE_WIDTH=1.0f;

	private GLCapabilities capabilities;
	private GLCanvas canvas;
	private FPSAnimator animator;

	final private int numTestCase;
	private int testCase;
	private BufferedImage buff;
	@SuppressWarnings("unused")
	private ColorType color;
	private Random rng;
	
	 // specular exponent for materials
	private int ns=5; 
	
	private ArrayList<Point2D> lineSegs;
	private ArrayList<Point2D> triangles;
	private boolean doSmoothShading;
	private int Nsteps;

	/** The quaternion which controls the rotation of the world. */
    private Quaternion viewing_quaternion = new Quaternion();
    private Point3D viewing_center = new Point3D((float)(DEFAULT_WINDOW_WIDTH/2),(float)(DEFAULT_WINDOW_HEIGHT/2),(float)0.0);
    /** The last x and y coordinates of the mouse press. */
    private int last_x = 0, last_y = 0;
    /** Whether the world is being rotated. */
    private boolean rotate_world = false;
    
    /** Random colors **/
    private ColorType[] colorMap = new ColorType[100];
    private Random rand = new Random();

	private boolean flat, gouraud, phong;
	private boolean ambient, diffuse, specular;
	private boolean toggleLights;
	private LightCombined light;
	private ColorType ka, kd, ks, katemp, kdtemp, kstemp;

	public PA4() 
	{	
		capabilities = new GLCapabilities(null);
	    capabilities.setDoubleBuffered(true);  // Enable Double buffering

	    canvas  = new GLCanvas(capabilities);
	    canvas.addGLEventListener(this);
	    canvas.addMouseListener(this);
	    canvas.addMouseMotionListener(this);
	    canvas.addKeyListener(this);
	    canvas.setAutoSwapBufferMode(true); // true by default. Just to be explicit
	    canvas.setFocusable(true);
	    getContentPane().add(canvas);

	    animator = new FPSAnimator(canvas, 60); // drive the display loop @ 60 FPS

	    numTestCase = 3;
	    testCase = 0;
	    Nsteps = 12;

		setTitle("CS480/680 PA4");
	    setSize( DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setVisible(true);
	    setResizable(false);
	    
	    rng = new Random();
	    color = new ColorType(1.0f,0.0f,0.0f);
	    lineSegs = new ArrayList<Point2D>();
	    triangles = new ArrayList<Point2D>();
	    doSmoothShading = false;
	    
	    for (int i=0; i<100; i++) {
	    	this.colorMap[i] = new ColorType(i*0.005f+0.5f, i*-0.005f+1f, i*0.0025f+0.75f);
	    }
		
	    float r = rng.nextFloat();
	    float g = rng.nextFloat();
	    float b = rng.nextFloat();
	    
	    // define boolean parameters for shading and illumination models
	    gouraud = true;
	    phong = flat = false;
	    ambient = diffuse = specular = true;
	    ka = new ColorType(r/6, g/6, b/6);
	    ks = new ColorType(r/3, g/3, b/3);
	    kd = new ColorType(r, g, b); 
	    katemp = new ColorType(ka);
	    kdtemp = new ColorType(kd);
	    kstemp = new ColorType(ks);
	    
	    // define light and its switch
	    light = new LightCombined();
	    toggleLights = false;	
	}

	public void run()
	{
		animator.start();
	}

	public static void main( String[] args )
	{
	    PA4 P = new PA4();
	    P.run();
	}

	//*********************************************** 
	//  GLEventListener Interfaces
	//*********************************************** 
	public void init( GLAutoDrawable drawable) 
	{
	    GL gl = drawable.getGL();
	    gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f);
	    gl.glLineWidth( DEFAULT_LINE_WIDTH );
	    Dimension sz = this.getContentPane().getSize();
	    buff = new BufferedImage(sz.width,sz.height,BufferedImage.TYPE_3BYTE_BGR);
	    clearPixelBuffer();
	}

	// Redisplaying graphics
	public void display(GLAutoDrawable drawable)
	{
	    GL2 gl = drawable.getGL().getGL2();
	    WritableRaster wr = buff.getRaster();
	    DataBufferByte dbb = (DataBufferByte) wr.getDataBuffer();
	    byte[] data = dbb.getData();

	    gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);
	    gl.glDrawPixels (buff.getWidth(), buff.getHeight(),
                GL2.GL_BGR, GL2.GL_UNSIGNED_BYTE,
                ByteBuffer.wrap(data));
        drawTestCase();
	}

	// Window size change
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		// deliberately left blank
	}
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
	      boolean deviceChanged)
	{
		// deliberately left blank
	}
	
	void clearPixelBuffer()
	{
		lineSegs.clear();
    	triangles.clear();
		Graphics2D g = buff.createGraphics();
	    g.setColor(Color.BLACK);
	    g.fillRect(0, 0, buff.getWidth(), buff.getHeight());
	    g.dispose();
	}
	
	// drawTest
	void drawTestCase()
	{  
		/* clear the window and vertex state */
		clearPixelBuffer();
	  
		//System.out.printf("Test case = %d\n",testCase);

		switch (testCase){
		case 0:
			sceneOne(); /* ellipsoid, cylinder, and torus */
			break;
		case 1:
			sceneTwo(); /* superellipsoid, box, and torus */
			break;
		case 2:
			sceneThree(); /* sphere, cylinder, and torus */
			break;
		}
	}

	//*********************************************** 
	//          KeyListener Interfaces
	//*********************************************** 
	public void keyTyped(KeyEvent key)
	{
	//      Q,q:   quit 
	//		R,r:   randomly change the color	
	//      C,c:   clear polygon (set vertex count=0)
	//		T,t:   show testing examples (toggles between smooth shading and flat shading test cases)
	//		>:	   increase the step number for examples
	//		<:     decrease the step number for examples
	//      +,-:   increase or decrease spectral exponent
	// 		F,f:   toggle flat shading
	// 		G,g:   toggle gouraud shading	
	// 		P,p:   toggle phong shading
	// 		A,a:   toggle ambient illumination
	// 		D,d:   toggle diffuse illumination
	// 		S,s:   toggle specular illumination
	// 		L,l:   enable/disable toggle light
	// 		1,2,3: enable/disable light source
	//		4,5:   moving camera along x direction
	// 		6,7:   moving camera along y direction

		switch ( key.getKeyChar() ) 
	    {
	    case 'Q' :
	    case 'q' : 
	    	new Thread()
	    	{
	          	public void run() { animator.stop(); }
	        }.start();
	        System.exit(0);
	        break;
	    case 'R' :
	    case 'r' :
	    	kd = new ColorType(rng.nextFloat(), rng.nextFloat(),
					rng.nextFloat());
			ka = new ColorType(kd.r/6, kd.g/6, kd.b/6);
			kdtemp = new ColorType(kd);
			katemp = new ColorType(ka);
	    	break;
	    case 'C' :
	    case 'c' :
	    	clearPixelBuffer();
	    	break;
	    case 'T' :
	    case 't' : 
	    	testCase = (testCase+1)%numTestCase;
	    	light = new LightCombined();
	    	drawTestCase();
	        break; 
	    case '<':  
	        Nsteps = Nsteps < 4 ? Nsteps: Nsteps / 2;
	        System.out.printf( "Nsteps = %d \n", Nsteps);
	        drawTestCase();
	        break;
	    case '>':
	        Nsteps = Nsteps > 190 ? Nsteps: Nsteps * 2;
	        System.out.printf( "Nsteps = %d \n", Nsteps);
	        drawTestCase();
	        break;
	    case '+':
	    	ns++;
	        drawTestCase();
	    	break;
	    case '-':
	    	if(ns>0)
	    		ns--;
	        drawTestCase();
	    	break;
	    case 'F':
		case 'f':
			flat = true;
			gouraud = phong = false;
			drawTestCase();
			break;
	    case 'G':
		case 'g':
			gouraud = true;
			flat = phong = false;
			drawTestCase();
			break;
		case 'P':
		case 'p':
			phong = true;
			flat = gouraud = false;
			drawTestCase();
			break;
		case 'A':
		case 'a':
			if (ambient) {
				katemp = new ColorType(ka);
				ka = new ColorType(0.0, 0.0, 0.0);
			} else {
				ka = new ColorType(katemp);
			}
			ambient = !ambient;
			break;	
		case 'D':
		case 'd':
			if (diffuse) {
				kdtemp = new ColorType(kd);
				kd = new ColorType(0.0, 0.0, 0.0);
			} else {
				kd = new ColorType(kdtemp);
			}
			diffuse = !diffuse;
			break;
	    case 'S':
		case 's':
			if (specular) {
				kstemp = new ColorType(ks);
				ks = new ColorType(0.0, 0.0, 0.0);
			} else {
				ks = new ColorType(kstemp);
			}
			specular = !specular;
			break;
		case 'L':
		case 'l':
			toggleLights = !toggleLights;
			break;
		case '1':
			if (toggleLights) {
				if (light.allLights.size() >= 	1) {
					light.allLights.get(0).light_on = !light.allLights.get(0).light_on;
				}
			}
			break;
			
		case '2':
			if (toggleLights) {
				if (light.allLights.size() >= 2) {
					light.allLights.get(1).light_on = !light.allLights.get(1).light_on;
				}
			}
			break;
			
		case '3':
			if (toggleLights) {
				if (light.allLights.size() >= 3) {
					light.allLights.get(2).light_on = !light.allLights.get(2).light_on;
				}
			}
			break;
		case '4':
			viewing_center.x -= 5;
			break;
		case '5':
			viewing_center.x += 5;
			break;
		case '6':
			viewing_center.y -= 5;
			break;
		case '7':
			viewing_center.y += 5;
			break;	
		default :
	        break;
		}
	}

	public void keyPressed(KeyEvent key)
	{
	    switch (key.getKeyCode()) 
	    {
	    case KeyEvent.VK_ESCAPE:
	    	new Thread()
	        {
	    		public void run()
	    		{
	    			animator.stop();
	    		}
	        }.start();
	        System.exit(0);
	        break;
	      default:
	        break;
	    }
	}

	public void keyReleased(KeyEvent key)
	{
		// deliberately left blank
	}

	//************************************************** 
	// MouseListener and MouseMotionListener Interfaces
	//************************************************** 
	public void mouseClicked(MouseEvent mouse)
	{
		// deliberately left blank
	}
	  public void mousePressed(MouseEvent mouse)
	  {
	    int button = mouse.getButton();
	    if ( button == MouseEvent.BUTTON1 )
	    {
	      last_x = mouse.getX();
	      last_y = mouse.getY();
	      rotate_world = true;
	    }
	  }

	  public void mouseReleased(MouseEvent mouse)
	  {
	    int button = mouse.getButton();
	    if ( button == MouseEvent.BUTTON1 )
	    {
	      rotate_world = false;
	    }
	  }

	public void mouseMoved( MouseEvent mouse)
	{
		// Deliberately left blank
	}

	/**
	   * Updates the rotation quaternion as the mouse is dragged.
	   * 
	   * @param mouse
	   *          The mouse drag event object.
	   */
	  public void mouseDragged(final MouseEvent mouse) {
	    if (this.rotate_world) {
	      // get the current position of the mouse
	      final int x = mouse.getX();
	      final int y = mouse.getY();

	      // get the change in position from the previous one
	      final int dx = x - this.last_x;
	      final int dy = y - this.last_y;

	      // create a unit vector in the direction of the vector (dy, dx, 0)
	      final float magnitude = (float)Math.sqrt(dx * dx + dy * dy);
	      if(magnitude > 0.0001)
	      {
	    	  // define axis perpendicular to (dx,-dy,0)
	    	  // use -y because origin is in upper lefthand corner of the window
	    	  final float[] axis = new float[] { -(float) (dy / magnitude),
	    			  (float) (dx / magnitude), 0 };

	    	  // calculate appropriate quaternion
	    	  final float viewing_delta = 3.1415927f / 360.0f * magnitude;
	    	  final float s = (float) Math.sin(0.5f * viewing_delta);
	    	  final float c = (float) Math.cos(0.5f * viewing_delta);
	    	  final Quaternion Q = new Quaternion(c, s * axis[0], s * axis[1], s * axis[2]);
	    	  this.viewing_quaternion = Q.multiply(this.viewing_quaternion);

	    	  // normalize to counteract acccumulating round-off error
	    	  this.viewing_quaternion.normalize();

	    	  // save x, y as last x, y
	    	  this.last_x = x;
	    	  this.last_y = y;
	          drawTestCase();
	      }
	    }

	  }
	  
	public void mouseEntered( MouseEvent mouse)
	{
		// Deliberately left blank
	}

	public void mouseExited( MouseEvent mouse)
	{
		// Deliberately left blank
	} 


	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}
	
	//************************************************** 
	// Test Cases
	// Nov 9, 2014 Stan Sclaroff -- removed line and triangle test cases
	//************************************************** 

	// ellipsoid, cylinder, and torus under ambient, infinite, and point lights
	void sceneOne() {
		
		// initialize objects to render
		float radius = (float)50.0;
	    Ellipsoid3D ellipsoid = new Ellipsoid3D(viewing_center.x/2+125, viewing_center.y/2, viewing_center.z/2, 
	    		(float)1.5*radius, (float)1.2*radius, (float)radius, Nsteps, Nsteps);
		Cylinder3D cylinder = new Cylinder3D(viewing_center.x/2, viewing_center.y/2+250, viewing_center.z/2-15, (float)1.5*radius, 
				(float)1.5*radius, Nsteps, Nsteps, 30);
		Torus3D torus = new Torus3D(viewing_center.x/2+250, viewing_center.y/2+250, viewing_center.z/2, 
				(float)0.3*radius, (float)1.5*radius, Nsteps, Nsteps);
		
		// apply lights
		if (light.allLights.size() == 0) {
			light = new LightCombined();
			ColorType light_color = new ColorType(1.0, 1.0, 1.0);
			Point3D light_direction = new Point3D(0, -1, -1);
			Point3D light_position = new Point3D(500, 300, 600);
			LightInfinite iLight = new LightInfinite(light_color, light_direction);
			LightAmbient aLight = new LightAmbient(light_color, light_direction);
			LightPoint pLight = new LightPoint(light_color, light_direction, light_position);	
			light.addLight(aLight);
			light.addLight(iLight);
			light.addLight(pLight);
		}

		// initialize mesh and depth buffer
		Mesh3D mesh;
		float[][] depthBuffer = new float[DEFAULT_WINDOW_WIDTH][DEFAULT_WINDOW_HEIGHT];
		for (float[] row : depthBuffer) {
			Arrays.fill(row, -256);
		}

		int n, m;

		mesh = ellipsoid.mesh;
		n = ellipsoid.get_n();
		m = ellipsoid.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
		
		mesh = cylinder.mesh;
		n = cylinder.get_n();
		m = cylinder.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
		
		mesh = torus.mesh;
		n = torus.get_n();
		m = torus.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
	}
	
	// superellipsoid, box, and torus under ambient, infinite, and spot light(angular)
	void sceneTwo() {
		
		// initialize objects to render
		float radius = (float)50.0;
	    SuperEllipsoid3D superellipsoid1 = new SuperEllipsoid3D(viewing_center.x/2, viewing_center.y/2, viewing_center.z/2, 
	    		(float)1.5*radius, (float)1.2*radius, (float)radius, 2.5f, 1f, Nsteps, Nsteps);
		Box3D box = new Box3D(viewing_center.x/2+250, viewing_center.y/2, viewing_center.z/2, (float)1.5*radius);
		SuperEllipsoid3D superellipsoid2 = new SuperEllipsoid3D(viewing_center.x/2+250, viewing_center.y/2+250, viewing_center.z/2, 
	    		(float)1.5*radius, (float)1.5*radius, (float)radius, 0.5f, 1.5f, Nsteps, Nsteps);
		Torus3D torus = new Torus3D(viewing_center.x/2, viewing_center.y/2+250, viewing_center.z/2, 
				(float)0.8*radius, (float)1.2*radius, Nsteps, Nsteps);

		// apply lights
		if (light.allLights.size() == 0) {
			light = new LightCombined();
			ColorType light_color = new ColorType(1.0, 1.0, 1.0);
			Point3D light_direction = new Point3D(-1,-1,-1);
			Point3D light_position = new Point3D(0, -300, -200);
			LightInfinite iLight = new LightInfinite(light_color, light_direction);
			LightAmbient aLight = new LightAmbient(light_color, light_direction);
			LightSpot sLight = new LightSpot(light_color, light_direction, light_position);	
			sLight.angular = !sLight.angular;			
			light.addLight(iLight);
			light.addLight(aLight);
			light.addLight(sLight);
		}

		// initialize mesh and depth buffer
		Mesh3D mesh;
		float[][] depthBuffer = new float[DEFAULT_WINDOW_WIDTH][DEFAULT_WINDOW_HEIGHT];
		for (float[] row : depthBuffer) {
			Arrays.fill(row, -256);
		}

		int n, m;

		mesh = superellipsoid1.mesh;
		n = superellipsoid1.get_n();
		m = superellipsoid1.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
		
		mesh = box.mesh;
		n = 3;
		m = 12;
		shadeBox(mesh, n, m, light, depthBuffer);
				
		mesh = superellipsoid2.mesh;
		n = superellipsoid2.get_n();
		m = superellipsoid2.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
		
		mesh = torus.mesh;
		n = torus.get_n();
		m = torus.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
	}
	
	// sphere, cylinder, and torus under ambient, infinite, and spot light(radical)
	void sceneThree() {
		
		// initialize objects to render
		float radius = (float) 50.0;
		Sphere3D sphere = new Sphere3D(viewing_center.x, viewing_center.y+20, viewing_center.z,
				(float)2*radius, Nsteps, Nsteps);		
		Cylinder3D cylinder1 = new Cylinder3D(viewing_center.x-90, viewing_center.y-90, viewing_center.z, (float)radius, 
				(float)radius, Nsteps, Nsteps, 5);
		Cylinder3D cylinder2 = new Cylinder3D(viewing_center.x+90, viewing_center.y-90, viewing_center.z, (float)radius, 
				(float)radius, Nsteps, Nsteps, 5);
		Torus3D torus = new Torus3D(viewing_center.x, viewing_center.y-20, viewing_center.z, 
				(float)0.2*radius, (float)4*radius, Nsteps, Nsteps);
		
		// apply lights
		if (light.allLights.size() == 0) {
			light = new LightCombined();
			ColorType light_color = new ColorType(1.0, 1.0, 1.0);
			Point3D light_direction = new Point3D(0, 1, 1);
			Point3D light_position = new Point3D(0, -400, -600);
			LightInfinite iLight = new LightInfinite(light_color, light_direction);
			LightAmbient aLight = new LightAmbient(light_color, light_direction);
			LightSpot sLight = new LightSpot(light_color, light_direction, light_position);	
			sLight.radical = !sLight.radical;
			light.addLight(iLight);
			light.addLight(aLight);
			light.addLight(sLight);
		}

		// initialize mesh and depth buffer
		Mesh3D mesh;
		float[][] depthBuffer = new float[DEFAULT_WINDOW_WIDTH][DEFAULT_WINDOW_HEIGHT];
		for (float[] row : depthBuffer) {
			Arrays.fill(row, -256);
		}

		int n, m;

		mesh = sphere.mesh;
		n = sphere.get_n();
		m = sphere.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
		
		mesh = cylinder1.mesh;
		n = cylinder1.get_n();
		m = cylinder1.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
		
		mesh = cylinder2.mesh;
		n = cylinder2.get_n();
		m = cylinder2.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
		
		mesh = torus.mesh;
		n = torus.get_n();
		m = torus.get_m();
		shadeObject(mesh, n, m, light, depthBuffer);
		
	}
	
	private void shadeObject(Mesh3D mesh, int n, int m, LightCombined light, float[][] depthBuffer) {
		
		// view vector is defined along z axis
		// this example assumes simple othorgraphic projection
		// view vector is used in
		// (a) calculating specular lighting contribution
		// (b) backface culling / backface rejection
		Point3D view_vector = new Point3D((float) 0.0, (float) 0.0, (float) 1.0);
		
		// normal to the plane of a triangle
        // to be used in backface culling / backface rejection
		Point3D triangle_normal = new Point3D();
				
		int i, j;
		
		// temporary variables for triangle 3D vertices and 3D normals
		Point3D v0, v1, v2, n0, n1, n2;

		// projected triangle, with vertex colors
		Point3D[] tri = { new Point3D(), new Point3D(), new Point3D() };
		
		Point3D point = new Point3D();
		Material mat = new Material(ka, kd, ks, ns);
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
		
		// draw triangles for the current surface, using vertex colors
		for (i = 0; i < m - 1; ++i) {
			for (j = 0; j < n - 1; ++j) {
				
				v0 = mesh.v[i][j];
				v1 = mesh.v[i][j+1];
				v2 = mesh.v[i+1][j+1];
				triangle_normal = computeTriangleNormal(v0, v1, v2);

				if (view_vector.dotProduct(triangle_normal) > 0.0) // front-facing triangle?
				{
					if (flat) {
						// flat shading: use the normal to the triangle itself
						n2 = n1 = n0 = triangle_normal;
						point = new Point3D((v0.x + v1.x + v2.x)/3, (v0.y + v1.y + v2.y)/3, (v0.z + v1.z + v2.z)/3);
						tri[2].c = tri[1].c = tri[0].c = light.applyLight(mat, view_vector, triangle_normal, point);
					}
					else {
						// gouraud and phong shading: use the normal at each vertex
						n0 = mesh.n[i][j];
						n1 = mesh.n[i][j+1];
						n2 = mesh.n[i+1][j+1];
					}
					
					if (gouraud) {
						tri[0].c = light.applyLight(mat, view_vector, n0, v0);
						tri[1].c = light.applyLight(mat, view_vector, n1, v1);
						tri[2].c = light.applyLight(mat, view_vector, n2, v2);
					}

					tri[0].x = (int) v0.x;
					tri[0].y = (int) v0.y;
					tri[0].z = (int) v0.z;
					tri[1].x = (int) v1.x;
					tri[1].y = (int) v1.y;
					tri[1].z = (int) v1.z;
					tri[2].x = (int) v2.x;
					tri[2].y = (int) v2.y;
					tri[2].z = (int) v2.z;

					if (phong) {
						SketchBase.drawTriangleNormal(buff, tri[0], tri[1], tri[2], depthBuffer, n0, n1, n2, light, mat, view_vector);
					} 
					else {
						SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], gouraud, depthBuffer);
					}
				}
				

				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				triangle_normal = computeTriangleNormal(v0, v1, v2);

				if (view_vector.dotProduct(triangle_normal) > 0.0) // front-facing triangle?
				{
					if (flat) {
						// flat shading: use the normal to the triangle itself
						n2 = n1 = n0 = triangle_normal;
						point = new Point3D((v0.x + v1.x + v2.x)/3, (v0.y + v1.y + v2.y)/3, (v0.z + v1.z + v2.z)/3);
						tri[2].c = tri[1].c = tri[0].c = light.applyLight(mat, view_vector, triangle_normal, point);
					}
					else {
						// gouraud and phong shading: use the normal at each vertex
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					if (gouraud) {
						tri[0].c = light.applyLight(mat, view_vector, n0, v0);
						tri[1].c = light.applyLight(mat, view_vector, n1, v1);
						tri[2].c = light.applyLight(mat, view_vector, n2, v2);
					}

					tri[0].x = (int) v0.x;
					tri[0].y = (int) v0.y;
					tri[0].z = (int) v0.z;
					tri[1].x = (int) v1.x;
					tri[1].y = (int) v1.y;
					tri[1].z = (int) v1.z;
					tri[2].x = (int) v2.x;
					tri[2].y = (int) v2.y;
					tri[2].z = (int) v2.z;

					if (phong) {
						SketchBase.drawTriangleNormal(buff, tri[0], tri[1], tri[2], depthBuffer, n0, n1, n2, light, mat, view_vector);
					} 
					else {
						SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], gouraud, depthBuffer);		
					}
				}
			}
		}		
	}
	
	private void shadeBox(Mesh3D mesh, int n, int m, LightCombined light, float[][] depthBuffer) {
		
		// view vector is defined along z axis
		// this example assumes simple othorgraphic projection
		// view vector is used in
		// (a) calculating specular lighting contribution
		// (b) backface culling / backface rejection
		Point3D view_vector = new Point3D((float) 0.0, (float) 0.0, (float) 1.0);
		
		// normal to the plane of a triangle
		// to be used in backface culling / backface rejection
		Point3D triangle_normal = new Point3D();
				
		int i, j;
		
		// temporary variables for triangle 3D vertices and 3D normals
		Point3D v0, v1, v2, n0, n1, n2;
		
		// projected triangle, with vertex colors
		Point3D[] tri = { new Point3D(), new Point3D(), new Point3D() };
		
		Point3D point = new Point3D();
		Material mat = new Material(ka, kd, ks, ns);
		
		// rotate the surface's 3D mesh using quaternion
		mesh.rotateMesh(viewing_quaternion, viewing_center);
		
		// draw triangles for the current surface, using vertex colors
		for (i = 0; i < m - 1; i+=2) {
			for (j = 0; j < n - 1; ++j) {

				v0 = mesh.v[i][j];
				v1 = mesh.v[i+1][j+1];
				v2 = mesh.v[i+1][j];
				triangle_normal = mesh.n[i][j];

				if (view_vector.dotProduct(triangle_normal) > 0.0) // front-facing triangle?
				{
					if (flat) {
						// flat shading: use the normal to the triangle itself
						n2 = n1 = n0 = triangle_normal;
						point = new Point3D((v0.x + v1.x + v2.x)/3, (v0.y + v1.y + v2.y)/3, (v0.z + v1.z + v2.z)/3);
						tri[2].c = tri[1].c = tri[0].c = light.applyLight(mat, view_vector, triangle_normal, point);
					}
					else {
						// gouraud and phong shading: use the normal at each vertex
						n0 = mesh.n[i][j];
						n1 = mesh.n[i+1][j+1];
						n2 = mesh.n[i+1][j];
					}
					
					if (gouraud) {
						tri[0].c = light.applyLight(mat, view_vector, n0, v0);
						tri[1].c = light.applyLight(mat, view_vector, n1, v1);
						tri[2].c = light.applyLight(mat, view_vector, n2, v2);
					}

					tri[0].x = (int) v0.x;
					tri[0].y = (int) v0.y;
					tri[0].z = (int) v0.z;
					tri[1].x = (int) v1.x;
					tri[1].y = (int) v1.y;
					tri[1].z = (int) v1.z;
					tri[2].x = (int) v2.x;
					tri[2].y = (int) v2.y;
					tri[2].z = (int) v2.z;

					if (phong) {
						SketchBase.drawTriangleNormal(buff, tri[0], tri[1], tri[2], depthBuffer, n0, n1, n2, light, mat, view_vector);
					} 
					else {
						SketchBase.drawTriangle(buff, tri[0], tri[1], tri[2], gouraud, depthBuffer);
					}
				}
			}
		}
	}
	
	// helper method that computes the unit normal to the plane of the triangle
	// degenerate triangles yield normal that is numerically zero
	private Point3D computeTriangleNormal(Point3D v0, Point3D v1, Point3D v2)
	{
		Point3D e0 = v1.minus(v2);
		Point3D e1 = v0.minus(v2);
		Point3D norm = e0.crossProduct(e1);
			
		if(norm.magnitude()>0.000001)
			norm.normalize();
		else 	// detect degenerate triangle and set its normal to zero
			norm.set((float)0.0,(float)0.0,(float)0.0);

		return norm;
	}

}
