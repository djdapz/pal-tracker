package test.pivotal.pal.tracker;

import io.pivotal.pal.tracker.PalTrackerApplication;
import io.pivotal.pal.tracker.TimeEntry;
import io.pivotal.pal.tracker.TimeEntryRepository;
import io.pivotal.pal.tracker.repository.JdbcTimeEntryRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PalTrackerApplication.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class JdbcTimeEntryRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TimeEntryRepository repo;

    @Before
    public void setUp() {
        repo = new JdbcTimeEntryRepository(jdbcTemplate);
    }

    @Test
    public void create() {
        long projectId = 123L;
        long userId = 456L;
        TimeEntry createdTimeEntry = repo.create(new TimeEntry(projectId, userId, LocalDate.parse("2017-01-08"), 8));

        assertThat(createdTimeEntry.getId()).isGreaterThan(0);

        TimeEntry expected = new TimeEntry(createdTimeEntry.getId(), projectId, userId, LocalDate.parse("2017-01-08"), 8);
        assertThat(createdTimeEntry).isEqualTo(expected);

        TimeEntry readEntry = repo.find(createdTimeEntry.getId());
        assertThat(readEntry).isEqualTo(expected);
    }

    @Test
    public void find() {

        long projectId = 123L;
        long userId = 456L;
        long id = repo.create(new TimeEntry(projectId, userId, LocalDate.parse("2017-01-08"), 8)).getId();

        TimeEntry expected = new TimeEntry(id, projectId, userId, LocalDate.parse("2017-01-08"), 8);
        TimeEntry readEntry = repo.find(id);
        assertThat(readEntry).isEqualTo(expected);
    }

    @Test
    public void find_MissingEntry() {
        long timeEntryId = 1;
        repo.delete(1);
        TimeEntry readEntry = repo.find(timeEntryId);
        assertThat(readEntry).isNull();
    }

    @Test
    public void list() {
        long id1 = repo.create(new TimeEntry(123L, 456L, LocalDate.parse("2017-01-08"), 8)).getId();
        long id2 = repo.create(new TimeEntry(789L, 654L, LocalDate.parse("2017-01-07"), 4)).getId();

        assertThat(repo.list())
                .contains(
                        new TimeEntry(id1, 123L, 456L, LocalDate.parse("2017-01-08"), 8),
                        new TimeEntry(id2, 789L, 654L, LocalDate.parse("2017-01-07"), 4)
                );
    }

    @Test
    public void update() {
        TimeEntry created = repo.create(new TimeEntry(123L, 456L, LocalDate.parse("2017-01-08"), 8));
        long id = created.getId();

        TimeEntry entryToUpdate = new TimeEntry(321L, 654L, LocalDate.parse("2017-01-09"), 5);
        TimeEntry updatedEntry = repo.update(id, entryToUpdate);

        TimeEntry expected = new TimeEntry(id, 321L, 654L, LocalDate.parse("2017-01-09"), 5);

        System.out.println("created = " + created);
        System.out.println("entryToUpdate = " + entryToUpdate);
        System.out.println("updatedEntry = " + updatedEntry);
        System.out.println("expected = " + expected);


        assertThat(updatedEntry).isEqualTo(expected);
        assertThat(repo.find(id)).isEqualTo(expected);
    }

    @Test
    public void update_MissingEntry() {
        TimeEntry updatedEntry = repo.update(
                1L,
                new TimeEntry(321L, 654L, LocalDate.parse("2017-01-09"), 5));

        assertThat(updatedEntry).isNull();
    }

    @Test
    public void delete() {

        long projectId = 123L;
        long userId = 456L;


        TimeEntry created = repo.create(new TimeEntry(projectId, userId, LocalDate.parse("2017-01-08"), 8));
        assertThat(repo.list()).contains(created);

        repo.delete(created.getId());
        assertThat(repo.list()).doesNotContain(created);
    }

    @Test
    public void deleteKeepsTrackOfLatestIdProperly() {

        long projectId = 123L;
        long userId = 456L;
        TimeEntry created = repo.create(new TimeEntry(projectId, userId, LocalDate.parse("2017-01-08"), 8));

        long id = created.getId();

        repo.delete(created.getId());

        TimeEntry createdSecond = repo.create(new TimeEntry(projectId, userId, LocalDate.parse("2017-01-08"), 8));

        assertThat(createdSecond.getId()).isEqualTo(id + 1);
    }
}
