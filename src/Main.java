import core.Engine;

import java.util.Vector;

public class Main extends Engine {
    public static void main(String[] args) {
        Main m = new Main();
        m.createWindow(800,800,"Engine");
    }

    static final int BLACK =    0xFF000000;
    static final int WHITE =    0xFFFFFFFF;
    static final int RED =      0xFFFF0000;
    static final int GREEN =    0xFF00FF00;
    static final int BLUE =     0xFF0000FF;
    static final int YELLOW =    0xFFFFFF00;


    public class Vec {
        public double x,y,z;

        public Vec(){setZero();}

        public Vec(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void setZero() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }
    }

    public class Triangle {
        public Vec[] p;
        public int color;

        public Triangle() {
            this.p = new Vec[3];
        }

        public Triangle(Vec[] p) {
            this.p = p;
            this.color = WHITE;
        }
        public Triangle(Vec[] p, int color) {
            this.p = p; this.color = color;
        }
    }

    public class Mesh {
        public Vector<Triangle> tris;

        public Mesh() {
            this.tris = new Vector<>();
        }

        public Mesh(Vector<Triangle> tris) {
            this.tris = tris;
        }
    }

    public class Mat4x4 {
        public double[][] m;

        public Mat4x4() {
            this.m = new double[4][4];
            for (int rows = 0; rows < 4; rows++) {
                for (int columns = 0; columns < 4; columns++) {
                    this.m[rows][columns] = 0;
                }
            }
        }
    }


    private Mesh meshCube;
    private Mat4x4 matProj;

    private Vec vCamera = new Vec();

    double theta = 0;

    private Vec MultiplyMatrixVector(Vec i, Mat4x4 m) {
        Vec o = new Vec();
        o.x = i.x * m.m[0][0] + i.y * m.m[1][0] + i.z * m.m[2][0] + m.m[3][0];
        o.y = i.x * m.m[0][1] + i.y * m.m[1][1] + i.z * m.m[2][1] + m.m[3][1];
        o.z = i.x * m.m[0][2] + i.y * m.m[1][2] + i.z * m.m[2][2] + m.m[3][2];
        double w = i.x * m.m[0][3] + i.y * m.m[1][3] + i.z * m.m[2][3] + m.m[3][3];

        if (w != 0.0) {
            o.x /= w;
            o.y /= w;
            o.z /= w;
        }

        return o;
    }

    @Override
    public void setup() {

        Vector<Triangle> tris = new Vector<>();


        // SOUTH
        tris.add(new Triangle(new Vec[]{new Vec(0, 0, 0), new Vec(0, 1, 0), new Vec(1, 1, 0)}));
        tris.add(new Triangle(new Vec[]{new Vec(0, 0, 0), new Vec(1, 1, 0), new Vec(1, 0, 0)}));

        // EAST
        tris.add(new Triangle(new Vec[]{new Vec(1, 0, 0), new Vec(1, 1, 0), new Vec(1, 1, 1)}));
        tris.add(new Triangle(new Vec[]{new Vec(1, 0, 0), new Vec(1, 1, 1), new Vec(1, 0, 1)}));

        // NORTH
        tris.add(new Triangle(new Vec[]{new Vec(1, 0, 1), new Vec(1, 1, 1), new Vec(0, 1, 1)}));
        tris.add(new Triangle(new Vec[]{new Vec(1, 0, 1), new Vec(0, 1, 1), new Vec(0, 0, 1)}));

        // WEST
        tris.add(new Triangle(new Vec[]{new Vec(0, 0, 1), new Vec(0, 1, 1), new Vec(0, 1, 0)}));
        tris.add(new Triangle(new Vec[]{new Vec(0, 0, 1), new Vec(0, 1, 0), new Vec(0, 0, 0)}));

        // TOP
        tris.add(new Triangle(new Vec[]{new Vec(0, 1, 0), new Vec(0, 1, 1), new Vec(1, 1, 1)}));
        tris.add(new Triangle(new Vec[]{new Vec(0, 1, 0), new Vec(1, 1, 1), new Vec(1, 1, 0)}));

        // BOTTOM
        tris.add(new Triangle(new Vec[]{new Vec(1, 0, 1), new Vec(0, 0, 1), new Vec(0, 0, 0)}));
        tris.add(new Triangle(new Vec[]{new Vec(1, 0, 1), new Vec(0, 0, 0), new Vec(1, 0, 0)}));

        meshCube = new Mesh(tris);

        // Projection matrix
        double zNear = 0.1;
        double zFar = 1000.0;
        double fov = 90.0;
        double aspectRatio = width / height;
        double fovRad = 1.0 / Math.tan(fov * 0.5 / 180.0 * 3.14159);

        matProj = new Mat4x4();
        matProj.m[0][0] = aspectRatio * fovRad;
        matProj.m[1][1] = fovRad;
        matProj.m[2][2] = zFar / (zFar - zNear);
        matProj.m[3][2] = (-zFar * zNear) / (zFar - zNear);
        matProj.m[2][3] = 1.0;
        matProj.m[3][3] = 0.0;

    }

    @Override
    public void update(double elapsedTime) {
        Fill(0,0,width,height,BLACK);

        Mat4x4 matRotZ = new Mat4x4(), matRotX= new Mat4x4();
        theta += 0.5 * elapsedTime;

        // Rotation Z
        matRotZ.m[0][0] =  Math.cos(theta);
        matRotZ.m[0][1] =  Math.sin(theta);
        matRotZ.m[1][0] = -Math.sin(theta);
        matRotZ.m[1][1] =  Math.cos(theta);
        matRotZ.m[2][2] = 1;
        matRotZ.m[3][3] = 1;

        // Rotation X
        matRotX.m[0][0] = 1;
        matRotX.m[1][1] =  Math.cos(theta * 0.5);
        matRotX.m[1][2] =  Math.sin(theta * 0.5);
        matRotX.m[2][1] = -Math.sin(theta * 0.5);
        matRotX.m[2][2] =  Math.cos(theta * 0.5);
        matRotX.m[3][3] = 1;

        // Draw triangles
        for (var tri : meshCube.tris) {

            Triangle triRotatedZ = new Triangle(new Vec[]{
                    MultiplyMatrixVector(tri.p[0], matRotZ),
                    MultiplyMatrixVector(tri.p[1], matRotZ),
                    MultiplyMatrixVector(tri.p[2], matRotZ),
            });

            Triangle triRotatedZX = new Triangle(new Vec[]{
                    MultiplyMatrixVector(triRotatedZ.p[0], matRotX),
                    MultiplyMatrixVector(triRotatedZ.p[1], matRotX),
                    MultiplyMatrixVector(triRotatedZ.p[2], matRotX),
            });

            // Translate
            Triangle triTranslated = triRotatedZX;
            triTranslated.p[0].z = triRotatedZX.p[0].z + 3.0;
            triTranslated.p[1].z = triRotatedZX.p[1].z + 3.0;
            triTranslated.p[2].z = triRotatedZX.p[2].z + 3.0;

            Triangle triProjected = new Triangle(new Vec[]{
                    MultiplyMatrixVector(triTranslated.p[0], matProj),
                    MultiplyMatrixVector(triTranslated.p[1], matProj),
                    MultiplyMatrixVector(triTranslated.p[2], matProj),
            });

            Vec normal = new Vec(), line1 = new Vec(), line2 = new Vec();
            line1.x = triTranslated.p[1].x - triTranslated.p[0].x;
            line1.y = triTranslated.p[1].y - triTranslated.p[0].y;
            line1.z = triTranslated.p[1].z - triTranslated.p[0].z;

            line2.x = triTranslated.p[2].x - triTranslated.p[0].x;
            line2.y = triTranslated.p[2].y - triTranslated.p[0].y;
            line2.z = triTranslated.p[2].z - triTranslated.p[0].z;

            normal.x = line1.y * line2.z - line1.z * line2.y;
            normal.y = line1.z * line2.x - line1.x * line2.z;
            normal.z = line1.x * line2.y - line1.y * line2.x;

            double l = Math.sqrt(normal.x*normal.x + normal.y*normal.y + normal.z*normal.z);
            normal.x /= l;
            normal.y /= l;
            normal.z /= l;

            //if (normal.z < 0)
            if (normal.x * (triTranslated.p[0].x - vCamera.x) +
                    normal.y * (triTranslated.p[0].y - vCamera.y) +
                    normal.z * (triTranslated.p[0].z - vCamera.z) < 0.0)
            {
                Vec light_direction = new Vec(0.0,0.0,-1.0);
                double len = Math.sqrt(light_direction.x*light_direction.x + light_direction.y*light_direction.y + light_direction.z*light_direction.z);
                light_direction.x /=1; light_direction.y /= 1; light_direction.z /= 1;

                double dp = normal.x * light_direction.x + normal.y * light_direction.y + normal.z + light_direction.z;
                int rgb = (int) (dp * 65536 + dp * 256 + dp);

                triTranslated.color = rgb;

                // Project triangles from 3D ->> 2D
                triProjected.p[0] = MultiplyMatrixVector(triTranslated.p[0],matProj);
                triProjected.p[1] = MultiplyMatrixVector(triTranslated.p[1],matProj);
                triProjected.p[2] = MultiplyMatrixVector(triTranslated.p[2],matProj);
                triProjected.color = triTranslated.color;

                // Scale into view
                triProjected.p[0].x += 1.0; triProjected.p[0].y += 1.0;
                triProjected.p[1].x += 1.0; triProjected.p[1].y += 1.0;
                triProjected.p[2].x += 1.0; triProjected.p[2].y += 1.0;
                triProjected.p[0].x *= 0.5 * width;
                triProjected.p[0].y *= 0.5 * height;
                triProjected.p[1].x *= 0.5 * width;
                triProjected.p[1].y *= 0.5 * height;
                triProjected.p[2].x *= 0.5 * width;
                triProjected.p[2].y *= 0.5 * height;


                DrawTriangle(triProjected.p[0].x,triProjected.p[0].y,
                        triProjected.p[1].x,triProjected.p[1].y,
                        triProjected.p[2].x,triProjected.p[2].y,WHITE);
            }
        }
    }
}
