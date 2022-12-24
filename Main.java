import tech.nmkv.concurrent.TaskHandler;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String args[]) throws InterruptedException {
        final TaskHandler taskHandler = new TaskHandler();
        taskHandler.setDelay(2, TimeUnit.SECONDS);

        new Thread(new Producer(taskHandler)).start();
        new Thread(new Producer(taskHandler)).start();
        new Thread(new Producer(taskHandler)).start();
        new Thread(new Producer(taskHandler)).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(20));
                    taskHandler.stop();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        taskHandler.start();
    }
}