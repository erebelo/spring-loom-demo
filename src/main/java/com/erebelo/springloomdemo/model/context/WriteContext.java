package com.erebelo.springloomdemo.model.context;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;

/**
 * Mutable execution state shared across all Virtual Threads while the batch is
 * being processed.
 * <p>
 * This object is updated incrementally so partial progress can still be
 * recovered if an unexpected exception interrupts the batch before completion.
 */
@Getter
public class WriteContext {

    private final AtomicLong successCount = new AtomicLong();

    private final List<ItemError> errors = new CopyOnWriteArrayList<>();

    public void incrementSuccess() {
        successCount.incrementAndGet();
    }

    public void addError(ItemError error) {
        errors.add(error);
    }

    public record ItemError(String recordId, Exception exception, Object item) {
    }
}
