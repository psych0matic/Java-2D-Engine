
/*
 * @author Sherwin Adams (216177499)
 * Date: 29 October 2021
 */

import java.util.ArrayList;

public class CircularQueue {
    private int size;
    private int rear;
    private int front;

    private ArrayList<Vec2D> queue = new ArrayList<>();

    public CircularQueue(int size) {
        this.size = size;
        this.front = -1;
        this.rear = -1;
    }

    public void enqueue(Vec2D c) {
        if (!isFull() && isEmpty()) {
            front = 0;
            rear = 0;
            queue.add(rear,c);
        } else if (rear == size -1 && front != 0) {
            rear = 0;
            queue.set(rear,c);
        } else {
            rear = rear + 1;

            if (front <= rear) {
                queue.add(rear,c);
            } else {
                queue.set(rear,c);
            }
        }
    }

    public Vec2D dequeue() {
        Vec2D temp;

        if (isEmpty()) {
            System.out.println("Queue is empty");
            return null;
        }

        temp = queue.get(front);

        if (front == rear) {
            front = -1;
            rear = -1;
        }
        else if (front == size - 1) front = 0;
        else front = front + 1;

        return temp;
    }

    public boolean isFull() {
        if ((front == 0 && rear == size - 1) || (rear == (front - 1) % (size - 1))) {
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return front == -1;
    }


}
