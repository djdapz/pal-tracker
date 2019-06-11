package io.pivotal.pal.tracker.repository;

import io.pivotal.pal.tracker.TimeEntry;
import io.pivotal.pal.tracker.TimeEntryRepository;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class InMemoryTimeEntryRepository implements TimeEntryRepository {

    private List<TimeEntry> entries;
    private long id = 1;

    public InMemoryTimeEntryRepository() {
        entries = new ArrayList<>();
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        TimeEntry entry = timeEntry.withId(id);
        entries.add(entry);
        id++;
        return entry;
    }

    @Override
    public TimeEntry find(long id) {
        return entries.stream()
                .filter((it) -> it.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<TimeEntry> list() {
        return entries;
    }

    @Override
    public TimeEntry update(long id, TimeEntry newEntry) {
        entries = entries.stream()
                .map(it -> it.getId() == id ? newEntry.withId(id) : it)
                .collect(toList());

        return find(id);
    }

    @Override
    public void delete(long id) {
        entries = entries.stream()
                .filter(it -> it.getId() != id)
                .collect(toList());
    }
}
