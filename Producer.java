import tech.nmkv.concurrent.Task;
import tech.nmkv.concurrent.TaskHandler;

import java.util.Random;

public class Producer implements Runnable {

    private static int count = 0;

    private final String id = "Producer" + ++count;
    private final TaskHandler taskHandler;

    public Producer(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    @Override
    public void run() {
        try {
            int count = 0;
            while (true) {
                int delta = new Random().nextInt(500);
                Thread.sleep(1_700 + delta);
                final int taskId = count = count + 1;
                taskHandler.submit(new Task(
                        id,
                        new Runnable() {
                            @Override
                            public void run() {
                                System.err.println(id + ": task[" + taskId + "] executed");
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                System.err.println(id + ": task[" + taskId + "] rejected");
                            }
                        }
                ));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
