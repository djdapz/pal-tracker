package io.pivotal.pal.tracker.repository;

import io.pivotal.pal.tracker.TimeEntry;
import io.pivotal.pal.tracker.TimeEntryRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcTimeEntryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update("INSERT INTO time_entries (project_id, user_id, date, hours) VALUES (:project_id, :user_id, :date, :hours)",
                new MapSqlParameterSource()
                        .addValue("project_id", timeEntry.getProjectId())
                        .addValue("user_id", timeEntry.getUserId())
                        .addValue("date", timeEntry.getDate())
                        .addValue("hours", timeEntry.getHours()),
                holder
        );

        return timeEntry.withId(Objects.requireNonNull(holder.getKey()).longValue());
    }


    class TimeEntryRowMapper implements RowMapper<TimeEntry> {
        @Override
        public TimeEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TimeEntry(
                    rs.getLong("id"),
                    rs.getLong("project_id"),
                    rs.getLong("user_id"),
                    rs.getDate("date").toLocalDate(),
                    rs.getInt("hours")
            );
        }
    }

    @Override
    public TimeEntry find(long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM time_entries WHERE id=:id",
                    new MapSqlParameterSource()
                            .addValue("id", id),
                    new TimeEntryRowMapper()
            );
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public List<TimeEntry> list() {
        return jdbcTemplate.query("SELECT * FROM time_entries",
                new TimeEntryRowMapper()
        );
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        jdbcTemplate.update("UPDATE time_entries \n" +
                        "SET project_id = :project_id, \n" +
                        "    user_id = :user_id, \n" +
                        "    date = :date, \n" +
                        "    hours = :hours \n" +
                        "WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("project_id", timeEntry.getProjectId())
                        .addValue("user_id", timeEntry.getUserId())
                        .addValue("date", timeEntry.getDate())
                        .addValue("hours", timeEntry.getHours())
                        .addValue("id", id)
        );

        return find(id);
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM  time_entries\n" +
                        "WHERE id=:id",
                new MapSqlParameterSource()
                        .addValue("id", id)
        );
    }
}
