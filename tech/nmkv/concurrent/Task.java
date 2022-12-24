package tech.nmkv.concurrent;

public class Task {

    private final Object id;

    private final Runnable accept;

    private final Runnable reject;

    public Task(Object id, Runnable accept, Runnable reject) {
        this.id = id;
        this.accept = accept;
        this.reject = reject;
    }

    public Object getId() {
        return id;
    }

    public Runnable getAccept() {
        return accept;
    }

    public Runnable getReject() {
        return reject;
    }
}
