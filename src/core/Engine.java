package core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public abstract class Engine extends Canvas implements Runnable{

    // Although we extend Canvas, we still need a frame to put the canvas on
    private final JFrame F = new JFrame();

    // This is the thread the frame will be running on
    private Thread thread;

    // Our flag for if the engine is running or stopped
    private boolean running;

    // The image we will be drawing to
    private BufferedImage image;

    // The pixels of the buffered image. We will manipulate this
    public int[] pixels;

    public int width,height;

    public void createWindow(int width, int height, String title) {
        this.width = width ;
        this.height = height ;
        thread = new Thread(this);
        image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        this.setSize(new Dimension(width,height));
        this.setBackground(Color.black);
        F.setResizable(false);
        F.setTitle(title);
        F.add(this);
        F.pack();
        F.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        F.setLocationRelativeTo(null);
        F.setVisible(true);

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
    }

    public void run() {

        setup();

        double t = 0.0;
        double dt = 0.01;

        // Seconds
        double currentTime = System.nanoTime();
        double accumulator = 0.0;

        requestFocus();
        while(running) {
            double newTime = System.nanoTime();
            double frameTime = newTime - currentTime;
            if (frameTime > 0.25) frameTime = 0.25;
            currentTime = newTime;

            accumulator += frameTime;

            while (accumulator >= dt) {
                update(dt);

                accumulator -= dt;

            }

            render();
        }
    }

    public abstract void setup();

    public abstract void update(double elapsedTime);

    public void Draw(int x, int y, int color) {
        if (x >= 0 && x < width && y >= 0 && y < width) {
            pixels[y*width+x] = color;
        }
    }

    public void Fill(int x1, int y1, int x2, int y2, int color) {
        Point p1 = Clip(x1,y1);
        Point p2 = Clip(x2,y2);

        for (int x = p1.x; x < p2.x; x++) {
            for (int y = p1.y; y < p2.y; y++) {
                Draw(x,y,color);
            }
        }
    }

    public Point Clip(int x, int y) {
        if (x < 0) x = 0;
        if (x >= width) x = width;
        if (y < 0) y = 0;
        if (y >= height) y = height;
        return new Point(x,y);
    }

    public void DrawLine(int x1, int y1, int x2, int y2, int color) {
        int x, y, dx, dy, dx1, dy1, px, py, xe, ye, i;
        dx = x2 - x1; dy = y2 - y1;
        dx1 = Math.abs(dx); dy1 = Math.abs(dy);
        px = 2 * dy1 - dx1;	py = 2 * dx1 - dy1;
        if (dy1 <= dx1)
        {
            if (dx >= 0)
            { x = x1; y = y1; xe = x2; }
            else
            { x = x2; y = y2; xe = x1;}

            Draw(x, y, color);

            for (i = 0; x<xe; i++)
            {
                x = x + 1;
                if (px<0)
                    px = px + 2 * dy1;
                else
                {
                    if ((dx<0 && dy<0) || (dx>0 && dy>0)) y = y + 1; else y = y - 1;
                    px = px + 2 * (dy1 - dx1);
                }
                Draw(x, y, color);
            }
        }
        else
        {
            if (dy >= 0)
            { x = x1; y = y1; ye = y2; }
            else
            { x = x2; y = y2; ye = y1; }

            Draw(x, y, color);

            for (i = 0; y<ye; i++)
            {
                y = y + 1;
                if (py <= 0)
                    py = py + 2 * dx1;
                else
                {
                    if ((dx<0 && dy<0) || (dx>0 && dy>0)) x = x + 1; else x = x - 1;
                    py = py + 2 * (dx1 - dy1);
                }
                Draw(x, y, color);
            }
        }
    }

     void drawline(int sx, int ex, int ny, int color) {
        for (int i = sx; i <= ex; i++) {
            Draw(i,ny,color);
        }
    }

    public void DrawTriangle(double x1, double y1, double x2, double y2, double x3, double y3, int color)
    {
        DrawTriangle((int) x1, (int) y1, (int) x2, (int) y2, (int) x3, (int) y3,  color);
    }

    public void DrawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int color)
    {
        DrawLine(x1, y1, x2, y2, color);
        DrawLine(x2, y2, x3, y3, color);
        DrawLine(x3, y3, x1, y1, color);
    }

    public void DrawCircle(int xc, int yc, int r, int color)
    {
        int x = 0;
        int y = r;
        int p = 3 - 2 * r;
        if (r == 0) return;

        while (y >= x) // only formulate 1/8 of circle
        {
            Draw(xc - x, yc - y, color);//upper left left
            Draw(xc - y, yc - x, color);//upper upper left
            Draw(xc + y, yc - x, color);//upper upper right
            Draw(xc + x, yc - y, color);//upper right right
            Draw(xc - x, yc + y, color);//lower left left
            Draw(xc - y, yc + x, color);//lower lower left
            Draw(xc + y, yc + x, color);//lower lower right
            Draw(xc + x, yc + y, color);//lower right right
            if (p < 0) p += 4 * x++ + 6;
            else p += 4 * (x++ - y--) + 10;
        }
    }

    public void FillCircle(int xc, int yc, int r, int color)
    {
        // Taken from wikipedia
        int x = 0;
        int y = r;
        int p = 3 - 2 * r;
        if (r == 0) return;

        while (y >= x)
        {
            // Modified to draw scan-lines instead of edges
            drawline(xc - x, xc + x, yc - y,color);
            drawline(xc - y, xc + y, yc - x,color);
            drawline(xc - x, xc + x, yc + y,color);
            drawline(xc - y, xc + y, yc + x,color);
            if (p < 0) p += 4 * x++ + 6;
            else p += 4 * (x++ - y--) + 10;
        }
    }

//    // STANDARD TRIANGLE FILL ALGORITHM
//    void fillBottomFlatTriangle(Main.Vec v1, Main.Vec v2, Main.Vec v3, int color) {
//        double invslope1 = (v2.x - v1.x) / (v2.y - v1.y);
//        double invslope2 = (v3.x - v1.x) / (v3.y - v1.y);
//
//        double curx1 = v1.x;
//        double curx2 = v1.x;
//
//        for (int scanlineY = (int) v1.y; scanlineY <= v2.y; scanlineY++)
//        {
//            DrawLine((int)curx1, scanlineY, (int)curx2, scanlineY,color);
//            curx1 += invslope1;
//            curx2 += invslope2;
//        }
//    }
//
//    void fillTopFlatTriangle(Main.Vec v1, Main.Vec v2, Main.Vec v3, int color)
//    {
//        double invslope1 = (v3.x - v1.x) / (v3.y - v1.y);
//        double invslope2 = (v3.x - v2.x) / (v3.y - v2.y);
//
//        double curx1 = v3.x;
//        double curx2 = v3.x;
//
//        for (int scanlineY = (int) v3.y; scanlineY > v1.y; scanlineY--)
//        {
//            DrawLine((int)curx1, scanlineY, (int)curx2, scanlineY,color);
//            curx1 -= invslope1;
//            curx2 -= invslope2;
//        }
//    }

//    public void FillTriangle(Main.Vec v1, Main.Vec v2, Main.Vec v3, int color)
//    {
//        /* at first sort the three vertices by y-coordinate ascending so v1 is the topmost vertice */
//        sortVerticesAscendingByY();
//
//        /* here we know that v1.y <= v2.y <= v3.y */
//        /* check for trivial case of bottom-flat triangle */
//        if (v2.y == v3.y)
//        {
//            fillBottomFlatTriangle(v1, v2, v3,color);
//        }
//        /* check for trivial case of top-flat triangle */
//        else if (vt1.y == vt2.y)
//        {
//            fillTopFlatTriangle(g, vt1, vt2, vt3);
//        }
//        else
//        {
//            /* general case - split the triangle in a topflat and bottom-flat one */
//            Vertice v4 = new Vertice(
//                    (int)(vt1.x + ((float)(vt2.y - vt1.y) / (float)(vt3.y - vt1.y)) * (vt3.x - vt1.x)), vt2.y);
//            fillBottomFlatTriangle(g, vt1, vt2, v4);
//            fillTopFlatTriangle(g, vt2, v4, vt3);
//        }
//    }

}


