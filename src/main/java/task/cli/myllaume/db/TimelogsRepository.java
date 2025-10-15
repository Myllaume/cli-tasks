package task.cli.myllaume.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import task.cli.myllaume.TimelogData;
import task.cli.myllaume.TimelogDb;

public class TimelogsRepository extends DatabaseRepository {
  public TimelogsRepository(String dbPath) {
    super(dbPath);
  }

  public TimelogDb createTimelog(TimelogData data) throws Exception {
    String sql =
        """
        INSERT INTO timelogs (task_id, started_at, stopped_at)
        VALUES (?, ?, ?)
        RETURNING id, task_id, started_at, stopped_at
        """;
    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, data.getTaskId());
      pstmt.setLong(2, data.getStartedAt().getEpochSecond());
      pstmt.setLong(3, data.getStoppedAt().getEpochSecond());

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return TimelogDb.fromSqlResult(rs);
        } else {
          throw new SQLException("Impossible de récupérer le timelog créé.");
        }
      }
    }
  }

  public TimelogDb getTimelog(int id) throws Exception {
    String sql = "SELECT id, task_id, started_at, stopped_at FROM timelogs WHERE id = ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return TimelogDb.fromSqlResult(rs);
        } else {
          return null;
        }
      }
    }
  }

  public TimelogDb removeTimelog(int id) throws Exception {
    String deleteSql =
        "DELETE FROM timelogs WHERE id = ? RETURNING id, task_id, started_at, stopped_at";

    try (Connection conn = getConnection();
        PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {

      deletePstmt.setInt(1, id);
      try (ResultSet rs = deletePstmt.executeQuery()) {
        if (rs.next()) {
          return TimelogDb.fromSqlResult(rs);
        } else {
          throw new IllegalArgumentException("Timelog with ID " + id + " not found");
        }
      }
    }
  }

  public ArrayList<TimelogDb> getTimelogsByTask(int taskId, int limit) throws Exception {
    String sql =
        """
        SELECT id, task_id, started_at, stopped_at
        FROM timelogs
        WHERE task_id = ?
        ORDER BY started_at DESC LIMIT ?
        """;

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, taskId);
      pstmt.setInt(2, limit);
      try (ResultSet rs = pstmt.executeQuery()) {
        ArrayList<TimelogDb> timelogs = new ArrayList<>();
        while (rs.next()) {
          timelogs.add(TimelogDb.fromSqlResult(rs));
        }
        return timelogs;
      }
    }
  }

  public ArrayList<TimelogDb> getTimelogs(int limit) throws Exception {
    String sql =
        """
        SELECT id, task_id, started_at, stopped_at
        FROM timelogs
        ORDER BY started_at DESC LIMIT ?
        """;

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, limit);
      try (ResultSet rs = pstmt.executeQuery()) {
        ArrayList<TimelogDb> timelogs = new ArrayList<>();
        while (rs.next()) {
          timelogs.add(TimelogDb.fromSqlResult(rs));
        }
        return timelogs;
      }
    }
  }

  public TimelogDb updateTimelog(int id, TimelogData data) throws Exception {
    String sql =
        """
        UPDATE timelogs SET task_id = ?, started_at = ?, stopped_at = ? WHERE id = ?
        RETURNING id, task_id, started_at, stopped_at
        """;
    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, data.getTaskId());
      pstmt.setLong(2, data.getStartedAt().getEpochSecond());
      pstmt.setLong(3, data.getStoppedAt().getEpochSecond());
      pstmt.setInt(4, id);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return TimelogDb.fromSqlResult(rs);
        } else {
          throw new IllegalArgumentException("Timelog with ID " + id + " not found");
        }
      }
    }
  }

  public long getTotalDurationForTask(int taskId) throws Exception {
    String sql =
        """
        SELECT COALESCE(SUM(stopped_at - started_at), 0) AS total_duration
        FROM timelogs
        WHERE task_id = ?
        """;

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, taskId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getLong("total_duration");
        } else {
          throw new SQLException("Unexpected empty result from aggregate query");
        }
      }
    }
  }

  public int countTimelogsByTask(int taskId) throws Exception {
    String sql = "SELECT COUNT(*) AS total FROM timelogs WHERE task_id = ?";

    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, taskId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("total");
        } else {
          throw new SQLException("Unexpected empty result from COUNT query");
        }
      }
    }
  }

  public int countTimelogs() throws Exception {
    String sql = "SELECT COUNT(*) AS total FROM timelogs";

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      if (rs.next()) {
        return rs.getInt("total");
      } else {
        throw new SQLException("Unexpected empty result from COUNT query");
      }
    }
  }

  public boolean isEmpty() throws Exception {
    return countTimelogs() == 0;
  }
}
