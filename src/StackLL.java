public class StackLL {

    private class Node {
        Vec2D data;
        Node link;
    }

    Node top;

    public StackLL() {
        this.top = null;
    }

    public void push(Vec2D v) {
        Node temp = new Node();

        if (temp == null) System.out.println("Stack Overflow");

        temp.data = v;

        top = temp;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public Vec2D peek() {
        if (!isEmpty()) {
            return top.data;
        } else {
            System.out.println("Stack is empty");
            return null;
        }
    }

    public Vec2D pop() {
        if (top == null) System.out.println("Stack Underflow");
        top = top.link;
        return top.data;
    }


    public static void main(String[] args) {

    }
}
