package tech.nmkv.concurrent;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskHandler implements Runnable {

    private volatile long delay = 2_000;

    private volatile boolean isStopped = false;

    private final Executor executor;

    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<Task>();

    private final Map<Object, Node> map = new HashMap<Object, Node>();

    public TaskHandler() {
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
    }

    public TaskHandler(Executor executor) {
        this.executor = executor;
    }

    public void setDelay(long value, TimeUnit unit) {
        delay = unit.toMillis(value);
    }

    public void start() {
        this.executor.execute(this);
    }

    public void stop() {
        this.isStopped = true;
    }

    public boolean submit(Task task) {
        if (this.isStopped) return false;
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task id is null");
        }
        return queue.offer(task);
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (isStopped) return;

                Task head;
                do {
                    // wait for task in the queue
                    head = queue.poll(1, TimeUnit.SECONDS);
                    if (isStopped) return;
                } while (head == null);

                // wait for other tasks
                Thread.sleep(delay);
                if (isStopped) return;

                // put all tasks to list
                List<Task> tasks = new ArrayList<Task>(queue.size() + 1);
                tasks.add(head);
                queue.drainTo(tasks);

                populateOrders(tasks);
                int order = lastExecutedOrder(tasks);
                sortRoundRobin(tasks, order);
                int index = findPriorityTaskIndex(tasks);

                Task task = tasks.remove(index);
                Node node = map.get(task.getId());

                for (Task reject : tasks) {
                    executor.execute(reject.getReject());
                }

                node.time = System.currentTimeMillis();
                executor.execute(task.getAccept());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void populateOrders(List<Task> tasks) {
        for (Task task : tasks) {
            if (!map.containsKey(task.getId())) {
                map.put(task.getId(), new Node(map.size() + 1, 0));
            }
        }
    }

    private int lastExecutedOrder(List<Task> tasks) {
        int order = 0;
        long time = -1;

        for (Task task : tasks) {
            Node node = map.get(task.getId());
            if (node.time > time) {
                time = node.time;
                order = node.order;
            }
        }

        return order;
    }

    private void sortRoundRobin(List<Task> tasks, final int order) {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                int order1 = map.get(o1.getId()).order;
                if (order1 < order) {
                    order1 += map.size();
                }
                int order2 = map.get(o2.getId()).order;
                if (order2 < order) {
                    order2 += map.size();
                }
                return order1 - order2;
            }
        });
    }

    private int findPriorityTaskIndex(List<Task> tasks) {
        int index = 0;
        long time = -1;

        for (int i = 0, ii = tasks.size(); i < ii; i++) {
            Task task = tasks.get(i);
            Node node = map.get(task.getId());
            if (time == -1 || node.time < time) {
                time = node.time;
                index = i;
            }
        }

        return index;
    }


    private static class Node {
        int order;
        long time;

        Node(int order, long time) {
            this.order = order;
            this.time = time;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "order=" + order +
                    ", time=" + time +
                    '}';
        }
    }
}
