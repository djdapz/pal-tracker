package test.pivotal.pal.trackerapi;

import com.jayway.jsonpath.DocumentContext;
import io.pivotal.pal.tracker.PalTrackerApplication;
import io.pivotal.pal.tracker.TimeEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

import static com.jayway.jsonpath.JsonPath.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PalTrackerApplication.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class TimeEntryApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final long projectId = 123L;
    private final long userId = 456L;
    private TimeEntry timeEntry = new TimeEntry(projectId, userId, LocalDate.parse("2017-01-08"), 8);

    @Test
    public void testCreate() {
        ResponseEntity<String> createResponse = restTemplate.postForEntity("/time-entries", timeEntry, String.class);


        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        DocumentContext createJson = parse(createResponse.getBody());
        assertThat(createJson.read("$.id", Long.class)).isGreaterThan(0);
        assertThat(createJson.read("$.projectId", Long.class)).isEqualTo(projectId);
        assertThat(createJson.read("$.userId", Long.class)).isEqualTo(userId);
        assertThat(createJson.read("$.date", String.class)).isEqualTo("2017-01-08");
        assertThat(createJson.read("$.hours", Long.class)).isEqualTo(8);
    }

    @Test
    public void testList() {
        Long id = createTimeEntry();

        ResponseEntity<String> listResponse = restTemplate.getForEntity("/time-entries", String.class);


        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext listJson = parse(listResponse.getBody());

        Collection timeEntries = listJson.read("$[*]", Collection.class);
        assertThat(timeEntries.size()).isGreaterThan(1);
    }

    @Test
    public void testRead() {
        Long id = createTimeEntry();


        ResponseEntity<String> readResponse = this.restTemplate.getForEntity("/time-entries/" + id, String.class);


        assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext readJson = parse(readResponse.getBody());
        assertThat(readJson.read("$.id", Long.class)).isEqualTo(id);
        assertThat(readJson.read("$.projectId", Long.class)).isEqualTo(projectId);
        assertThat(readJson.read("$.userId", Long.class)).isEqualTo(userId);
        assertThat(readJson.read("$.date", String.class)).isEqualTo("2017-01-08");
        assertThat(readJson.read("$.hours", Long.class)).isEqualTo(8);
    }

    @Test
    public void testUpdate() {
        Long id = createTimeEntry();
        long projectId = 2L;
        long userId = 3L;
        TimeEntry updatedTimeEntry = new TimeEntry(projectId, userId, LocalDate.parse("2017-01-09"), 9);


        ResponseEntity<String> updateResponse = restTemplate.exchange("/time-entries/" + id, HttpMethod.PUT, new HttpEntity<>(updatedTimeEntry, null), String.class);


        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext updateJson = parse(updateResponse.getBody());
        assertThat(updateJson.read("$.id", Long.class)).isEqualTo(id);
        assertThat(updateJson.read("$.projectId", Long.class)).isEqualTo(projectId);
        assertThat(updateJson.read("$.userId", Long.class)).isEqualTo(userId);
        assertThat(updateJson.read("$.date", String.class)).isEqualTo("2017-01-09");
        assertThat(updateJson.read("$.hours", Long.class)).isEqualTo(9);
    }

    @Test
    public void testDelete() {
        Long id = createTimeEntry();


        ResponseEntity<String> deleteResponse = restTemplate.exchange("/time-entries/" + id, HttpMethod.DELETE, null, String.class);


        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> deletedReadResponse = this.restTemplate.getForEntity("/time-entries/" + id, String.class);
        assertThat(deletedReadResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Long createTimeEntry() {
        HttpEntity<TimeEntry> entity = new HttpEntity<>(timeEntry);

        ResponseEntity<TimeEntry> response = restTemplate.exchange("/time-entries", HttpMethod.POST, entity, TimeEntry.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        return Objects.requireNonNull(response.getBody()).getId();
    }
}
