import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public abstract class GameEngine extends Canvas implements Runnable {

    private final JFrame frame = new JFrame();
    private int pixelWidth;
    private int pixelHeight;

    private Thread thread;
    private boolean running;
    private BufferedImage image;
    private int[] pixels;

    public void createWindow(int width, int height, String title, int pixelWidth, int pixelHeight) {

        thread = new Thread(this);
        image = new BufferedImage(width*pixelWidth+pixelWidth,height*pixelHeight+pixelHeight,BufferedImage.TYPE_INT_ARGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;

        this.setSize(new Dimension(width*pixelWidth+pixelWidth,height*pixelHeight+pixelHeight));
        this.setBackground(Color.black);
        frame.setResizable(false);
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.add(this);
        frame.pack();
        start();
    }

    private synchronized void start() {
        running = true;
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null){
            createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        g.drawImage(image,0,0,image.getWidth(),image.getHeight(),null);
        bs.show();

        Arrays.fill(pixels, 0x00000000);
    }

    public void run() {

        setup();

        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0; // 60Hz
        double delta = 0;
        requestFocus();
        while(running) {
            long now = System.nanoTime();
            delta = delta + ((now-lastTime)/ns);
            lastTime = now;
            while (delta >= 1) // Ensure 60Hz
            {
                // Handle all logic restricted time
                update(delta);
                delta--;
            }
            render(); // Display unrestricted time
        }
    }

    public abstract void setup();

    public abstract void update(double elapsedTime);


    public int getPixel(Vec2D v) {
        return getPixel((int)v.x,(int)v.y);
    }

    public int getPixel(int x, int y) {
        return pixels[x+getWidth()*y];
    }

    public void Pixel(Vec2D v,int color) { Pixel((int)v.x,(int)v.y,color);}

    public void Pixel(int x, int y, int color) {
        for (int a = x*pixelWidth; a < x*pixelWidth+pixelWidth; a++) {
            for (int b = y*pixelHeight; b < y*pixelHeight+pixelHeight;b++) {
                pixels[(a+getWidth()*b)] = color;
            }
        }
    }


    public void Line(Vec2D v1, Vec2D v2,int color) {
        Line((int)v1.x,(int)v1.y,(int)v2.x,(int)v2.y,color);
    }

    public void Line(double x1, double y1, double x2, double y2,int color) {
        // delta of exact value and rounded value of the dependent variable
        double d = 0;

        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);

        double dx2 = 2 * dx; // slope scaling factors to
        double dy2 = 2 * dy; // avoid floating point

        double ix = x1 < x2 ? 1 : -1; // increment direction
        double iy = y1 < y2 ? 1 : -1;

        double x = x1;
        double y = y1;

        if (dx >= dy) {
            while (true) {
                if (x < getWidth() || y < getHeight()) {
                    Pixel((int)x, (int)y,color);
                }
                if (x == x2)
                    break;
                x += ix;
                d += dy2;
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {
            while (true) {
                if (x < getWidth()-1 || y < getHeight()-1) {
                    Pixel((int)x,(int) y,color);
                }
                if (y == y2)
                    break;
                y += iy;
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }
    }

    private void circlePoints(double xc, double yc, double x, double y, int color) {
        if (x == 0) {
            Pixel((int)(xc),(int)(yc+y),color);
            Pixel((int)(xc),(int)(yc-y),color);
            Pixel((int)(xc+y),(int)(yc),color);
            Pixel((int)(xc-y),(int)(yc),color);
        } else {
            if (x == y){
                Pixel((int)(xc+x),(int)(yc+y),color);
                Pixel((int)(xc-x),(int)(yc+y),color);
                Pixel((int)(xc+x),(int)(yc-y),color);
                Pixel((int)(xc-x),(int)(yc-y),color);
            } else {
                if (x < y) {
                    Pixel((int)(xc+x),(int)(yc+y),color);
                    Pixel((int)(xc-x),(int)(yc+y),color);
                    Pixel((int)(xc+x),(int)(yc-y),color);
                    Pixel((int)(xc-x),(int)(yc-y),color);
                    Pixel((int)(xc+y),(int)(yc+x),color);
                    Pixel((int)(xc-y),(int)(yc+x),color);
                    Pixel((int)(xc+y),(int)(yc-x),color);
                    Pixel((int)(xc-y),(int)(yc-x),color);
                }
            }
        }
    }

    public void Ellipse(Vec2D v, double r, int color) {Ellipse(v.x,v.y,r,color);}
    public void Ellipse(double xc, double yc, double r, int color) {
        double x = 0;
        double y = r;
        double p = (5 - r*4)/4;

        circlePoints(xc, yc, x, y, color);
        while (x < y) {
            x++;
            if (p < 0) {
                p += 2*x+1;
            } else {
                y--;
                p += 2*(x-y)+1;
            }
            circlePoints(xc, yc, x, y, color);
        }
    }



    public void Ellipse(Vec2D v, double rx,double ry, int color) {Ellipse(v.x,v.y,rx,ry,color);}
    private void Ellipse(double xc, double yc, double rx, double ry,int color) {
        double dx, dy, d1, d2, x, y;

        x = 0;
        y = ry;

        // Initial decision parameter of region 1
        d1 = ((ry * ry) - (rx * rx * ry) + (0.25f * rx * rx));
        dx = 2 * ry * ry * x;
        dy = 2 * rx * rx * y;

        // For region 1
        while (dx < dy)
        {

            // Print points based on 4-way symmetry
            Pixel((int)(x+xc),(int)(y+yc),color);
            Pixel((int)(-x+xc),(int)(y+yc),color);
            Pixel((int)(x+xc),(int)(-y+yc),color);
            Pixel((int)(-x+xc),(int)(-y+yc),color);
            // Checking and updating value of
            // decision parameter based on algorithm
            if (d1 < 0)
            {
                x++;
                dx = dx + (2 * ry * ry);
                d1 = d1 + dx + (ry * ry);
            }
            else
            {
                x++;
                y--;
                dx = dx + (2 * ry * ry);
                dy = dy - (2 * rx * rx);
                d1 = d1 + dx - dy + (ry * ry);
            }
        }

        // Decision parameter of region 2
        d2 = (((ry * ry) * ((x + 0.5f) * (x + 0.5f)))
                        + ((rx * rx) * ((y - 1) * (y - 1)))
                        - (rx * rx * ry * ry));

        // Plotting points of region 2
        while (y >= 0) {

            // printing points based on 4-way symmetry
            Pixel((int)(x+xc),(int)(y+yc),color);
            Pixel((int)(-x+xc),(int)(y+yc),color);
            Pixel((int)(x+xc),(int)(-y+yc),color);
            Pixel((int)(-x+xc),(int)(-y+yc),color);
            // Checking and updating parameter
            // value based on algorithm
            if (d2 > 0) {
                y--;
                dy = dy - (2 * rx * rx);
                d2 = d2 + (rx * rx) - dy;
            }
            else {
                y--;
                x++;
                dx = dx + (2 * ry * ry);
                dy = dy - (2 * rx * rx);
                d2 = d2 + dx - dy + (rx * rx);
            }
        }
    }

    public void Triangle(Vec2D v1, Vec2D v2, Vec2D v3, int color) {
        Triangle(v1.x,v1.y,v2.x,v2.y,v3.x,v3.y,color);
    }
    public void Triangle(double x1, double y1, double x2, double y2, double x3, double y3,int color) {
        Triangle((int)x1,(int)y1,(int)x2,(int)y2,(int)x3,(int)y3,color);
    }
    private void Triangle(int x1, int y1, int x2, int y2, int x3, int y3,int color) {
        Line(x1,y1,x2,y2,color);
        Line(x2,y2,x3,y3,color);
        Line(x3,y3,x1,y1,color);
    }



}

class Vec2D {

    public double x;
    public double y;

    public Vec2D() { }

    public Vec2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2D(Vec2D v) {
        set(v);
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vec2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    public void setZero() {
        x = 0;
        y = 0;
    }

    public double[] getComponents() {
        return new double[]{x, y};
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public double getLengthSq() {
        return (x * x + y * y);
    }

    public double distanceSq(double vx, double vy) {
        vx -= x;
        vy -= y;
        return (vx * vx + vy * vy);
    }

    public double distanceSq(Vec2D v) {
        double vx = v.x - this.x;
        double vy = v.y - this.y;
        return (vx * vx + vy * vy);
    }

    public double distance(double vx, double vy) {
        vx -= x;
        vy -= y;
        return Math.sqrt(vx * vx + vy * vy);
    }

    public double distance(Vec2D v) {
        double vx = v.x - this.x;
        double vy = v.y - this.y;
        return Math.sqrt(vx * vx + vy * vy);
    }

    public double getAngle() {
        return Math.atan2(y, x);
    }

    public void normalize() {
        double magnitude = getLength();
        x /= magnitude;
        y /= magnitude;
    }

    public Vec2D getNormalized() {
        double magnitude = getLength();
        return new Vec2D(x / magnitude, y / magnitude);
    }

    public static Vec2D toCartesian(double magnitude, double angle) {
        return new Vec2D(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
    }

    public void add(Vec2D v) {
        this.x += v.x;
        this.y += v.y;
    }

    public void add(double vx, double vy) {
        this.x += vx;
        this.y += vy;
    }

    public static Vec2D add(Vec2D v1, Vec2D v2) {
        return new Vec2D(v1.x + v2.x, v1.y + v2.y);
    }

    public Vec2D getAdded(Vec2D v) {
        return new Vec2D(this.x + v.x, this.y + v.y);
    }

    public void subtract(Vec2D v) {
        this.x -= v.x;
        this.y -= v.y;
    }

    public void subtract(double vx, double vy) {
        this.x -= vx;
        this.y -= vy;
    }

    public static Vec2D subtract(Vec2D v1, Vec2D v2) {
        return new Vec2D(v1.x - v2.x, v1.y - v2.y);
    }

    public Vec2D getSubtracted(Vec2D v) {
        return new Vec2D(this.x - v.x, this.y - v.y);
    }

    public void multiply(double scalar) {
        x *= scalar;
        y *= scalar;
    }

    public Vec2D getMultiplied(double scalar) {
        return new Vec2D(x * scalar, y * scalar);
    }

    public void divide(double scalar) {
        x /= scalar;
        y /= scalar;
    }

    public Vec2D getDivided(double scalar) {
        return new Vec2D(x / scalar, y / scalar);
    }

    public Vec2D getPerp() {
        return new Vec2D(-y, x);
    }

    public double dot(Vec2D v) {
        return (this.x * v.x + this.y * v.y);
    }

    public double dot(double vx, double vy) {
        return (this.x * vx + this.y * vy);
    }

    public static double dot(Vec2D v1, Vec2D v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    public double cross(Vec2D v) {
        return (this.x * v.y - this.y * v.x);
    }

    public double cross(double vx, double vy) {
        return (this.x * vy - this.y * vx);
    }

    public static double cross(Vec2D v1, Vec2D v2) {
        return (v1.x * v2.y - v1.y * v2.x);
    }

    public double project(Vec2D v) {
        return (this.dot(v) / this.getLength());
    }

    public double project(double vx, double vy) {
        return (this.dot(vx, vy) / this.getLength());
    }

    public static double project(Vec2D v1, Vec2D v2) {
        return (dot(v1, v2) / v1.getLength());
    }

    public Vec2D getProjectedVector(Vec2D v) {
        return this.getNormalized().getMultiplied(this.dot(v) / this.getLength());
    }

    public Vec2D getProjectedVector(double vx, double vy) {
        return this.getNormalized().getMultiplied(this.dot(vx, vy) / this.getLength());
    }

    public static Vec2D getProjectedVector(Vec2D v1, Vec2D v2) {
        return v1.getNormalized().getMultiplied(Vec2D.dot(v1, v2) / v1.getLength());
    }

    public void rotateBy(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double rx = x * cos - y * sin;
        y = x * sin + y * cos;
        x = rx;
    }

    public Vec2D getRotatedBy(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec2D(x * cos - y * sin, x * sin + y * cos);
    }

    public void rotateTo(double angle) {
        set(toCartesian(getLength(), angle));
    }

    public Vec2D getRotatedTo(double angle) {
        return toCartesian(getLength(), angle);
    }

    public void reverse() {
        x = -x;
        y = -y;
    }

    public Vec2D getReversed() {
        return new Vec2D(-x, -y);
    }

    @Override
    public Vec2D clone() {
        return new Vec2D(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Vec2D) {
            Vec2D v = (Vec2D) obj;
            return (x == v.x) && (y == v.y);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Vector2d[" + x + ", " + y + "]";
    }
}