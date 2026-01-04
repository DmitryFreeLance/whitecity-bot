package ru.whitecity.bot;

import java.sql.*;

public final class Db {
    private final String jdbcUrl;

    public Db(String dbFile) {
        this.jdbcUrl = "jdbc:sqlite:" + dbFile;
        init();
    }

    private void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              tg_id INTEGER NOT NULL UNIQUE,
              username TEXT,
              first_name TEXT,
              last_name TEXT,
              created_at TEXT NOT NULL DEFAULT (datetime('now'))
            );
        """;
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    public void upsertUser(long tgId, String username, String firstName, String lastName) {
        String sql = """
            INSERT INTO users(tg_id, username, first_name, last_name)
            VALUES(?, ?, ?, ?)
            ON CONFLICT(tg_id) DO UPDATE SET
              username=excluded.username,
              first_name=excluded.first_name,
              last_name=excluded.last_name;
        """;
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, tgId);
            ps.setString(2, username);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB upsert failed", e);
        }
    }
}