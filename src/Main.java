public class Main extends GameEngine{
    public static void main(String[] args) {
        Main m = new Main();
        m.createWindow(40,40,"Meow",10,10);
    }

    @Override
    public void setup() {
    }

    @Override
    public void update(double elapsedTime) {
        Ellipse(20,20,20,0xff00ff00);
        fill(new Vec2D(20,20),0xff0000ff,0xff00ff00);
    }
}
