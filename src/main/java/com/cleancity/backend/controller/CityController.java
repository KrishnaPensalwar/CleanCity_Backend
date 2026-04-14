package com.cleancity.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final JdbcTemplate jdbc;

    public CityController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listAll() {
        String sql = "SELECT id, name FROM cities ORDER BY name";
        List<Map<String, Object>> rows = jdbc.query(sql, (ResultSet rs, int rowNum) -> {
            Map<String, Object> m = new HashMap<>();
            Object idObj = rs.getObject("id");
            m.put("id", idObj != null ? idObj.toString() : null);
            m.put("name", rs.getString("name"));
            return m;
        });
        return ResponseEntity.ok(rows);
    }
}