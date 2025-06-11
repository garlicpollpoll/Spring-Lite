package com.springlite.demo;

import com.springlite.framework.jdbc.JdbcTemplate;
import com.springlite.framework.jdbc.RowMapper;
import com.springlite.demo.dto.User;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * 🚀 Spring Lite JDBC 테스트 애플리케이션
 * 
 * H2 인메모리 데이터베이스를 사용하여 JdbcTemplate의 모든 기능을 테스트합니다:
 * - 테이블 생성 (DDL)
 * - 데이터 삽입/수정/삭제 (DML)
 * - 단일 값 조회
 * - 리스트 조회 (Map)
 * - 객체 매핑 조회 (RowMapper)
 */
public class JdbcTestApp {
    
    public static void main(String[] args) {
        System.out.println("🚀 Spring Lite JDBC 테스트 시작!");
        System.out.println("==================================================");
        
        try {
            // 1. DataSource 설정 (H2 인메모리 DB)
            DataSource dataSource = createH2DataSource();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 2. 테이블 생성 (DDL 테스트)
            testDDL(jdbcTemplate);
            
            // 3. 데이터 삽입 (DML 테스트)
            testInsert(jdbcTemplate);
            
            // 4. 단일 값 조회 테스트
            testQueryForObject(jdbcTemplate);
            
            // 5. 리스트 조회 테스트 (Map)
            testQueryForList(jdbcTemplate);
            
            // 6. 객체 매핑 조회 테스트 (RowMapper)
            testQueryWithRowMapper(jdbcTemplate);
            
            // 7. 업데이트/삭제 테스트
            testUpdateAndDelete(jdbcTemplate);
            
            System.out.println("==================================================");
            System.out.println("✅ 모든 JDBC 테스트 완료!");
            
        } catch (Exception e) {
            System.err.println("❌ 테스트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * H2 인메모리 데이터베이스 DataSource 생성
     */
    private static DataSource createH2DataSource() {
        System.out.println("\n📊 H2 DataSource 생성 중...");
        
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        
        System.out.println("✅ H2 DataSource 생성 완료");
        return dataSource;
    }
    
    /**
     * DDL 테스트 - 테이블 생성
     */
    private static void testDDL(JdbcTemplate jdbcTemplate) {
        System.out.println("\n🏗️ DDL 테스트 - 테이블 생성");
        
        // users 테이블 생성
        jdbcTemplate.execute(
            "CREATE TABLE users (" +
            "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "    name VARCHAR(100) NOT NULL," +
            "    email VARCHAR(200) UNIQUE NOT NULL," +
            "    age INTEGER," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        
        System.out.println("✅ users 테이블 생성 완료");
    }
    
    /**
     * 데이터 삽입 테스트
     */
    private static void testInsert(JdbcTemplate jdbcTemplate) {
        System.out.println("\n📝 데이터 삽입 테스트");
        
        // 단일 사용자 삽입
        int rows1 = jdbcTemplate.update(
            "INSERT INTO users (name, email, age) VALUES (?, ?, ?)",
            "김춘식", "chunsik@example.com", 25
        );
        System.out.println("김춘식 삽입 결과: " + rows1 + "개 행");
        
        // 여러 사용자 삽입
        int rows2 = jdbcTemplate.update(
            "INSERT INTO users (name, email, age) VALUES (?, ?, ?)",
            "이영희", "younghee@example.com", 30
        );
        
        int rows3 = jdbcTemplate.update(
            "INSERT INTO users (name, email, age) VALUES (?, ?, ?)",
            "박철수", "chulsoo@example.com", 28
        );
        
        int rows4 = jdbcTemplate.update(
            "INSERT INTO users (name, email, age) VALUES (?, ?, ?)",
            "최민정", "minjung@example.com", 32
        );
        
        System.out.println("✅ 총 " + (rows1 + rows2 + rows3 + rows4) + "명의 사용자 삽입 완료");
    }
    
    /**
     * 단일 값 조회 테스트
     */
    private static void testQueryForObject(JdbcTemplate jdbcTemplate) {
        System.out.println("\n🔍 단일 값 조회 테스트");
        
        // 전체 사용자 수 조회
        Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        System.out.println("전체 사용자 수: " + totalCount);
        
        // 특정 사용자 이름 조회
        String userName = jdbcTemplate.queryForObject(
            "SELECT name FROM users WHERE email = ?", 
            String.class, 
            "chunsik@example.com"
        );
        System.out.println("chunsik@example.com의 이름: " + userName);
        
        // 평균 나이 조회
        Double avgAge = jdbcTemplate.queryForObject("SELECT AVG(age) FROM users", Double.class);
        System.out.println("평균 나이: " + String.format("%.1f", avgAge));
    }
    
    /**
     * 리스트 조회 테스트 (Map)
     */
    private static void testQueryForList(JdbcTemplate jdbcTemplate) {
        System.out.println("\n📋 리스트 조회 테스트 (Map)");
        
        // 모든 사용자 조회
        List<Map<String, Object>> allUsers = jdbcTemplate.queryForList("SELECT * FROM users ORDER BY id");
        
        System.out.println("조회된 사용자 목록:");
        for (Map<String, Object> user : allUsers) {
            System.out.printf("- ID: %s, 이름: %s, 이메일: %s, 나이: %s%n",
                user.get("ID"), user.get("NAME"), user.get("EMAIL"), user.get("AGE"));
        }
        
        // 조건부 조회
        List<Map<String, Object>> youngUsers = jdbcTemplate.queryForList(
            "SELECT name, age FROM users WHERE age < ? ORDER BY age", 30
        );
        
        System.out.println("\n30세 미만 사용자:");
        for (Map<String, Object> user : youngUsers) {
            System.out.printf("- 이름: %s, 나이: %s%n", user.get("NAME"), user.get("AGE"));
        }
    }
    
    /**
     * 객체 매핑 조회 테스트 (RowMapper)
     */
    private static void testQueryWithRowMapper(JdbcTemplate jdbcTemplate) {
        System.out.println("\n🎯 객체 매핑 조회 테스트 (RowMapper)");
        
        // RowMapper 정의
        RowMapper<User> userMapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getInt("age")
        );
        
        // 모든 사용자를 User 객체로 조회
        List<User> users = jdbcTemplate.query(
            "SELECT id, name, email, age FROM users ORDER BY name", userMapper
        );
        
        System.out.println("User 객체로 조회된 사용자들:");
        for (User user : users) {
            System.out.println("- " + user);
        }
        
        // 단일 사용자 객체 조회
        User specificUser = jdbcTemplate.queryForObject(
            "SELECT id, name, email, age FROM users WHERE email = ?", 
            userMapper, 
            "younghee@example.com"
        );
        
        System.out.println("\n특정 사용자 조회: " + specificUser);
    }
    
    /**
     * 업데이트/삭제 테스트
     */
    private static void testUpdateAndDelete(JdbcTemplate jdbcTemplate) {
        System.out.println("\n✏️ 업데이트/삭제 테스트");
        
        // 나이 업데이트
        int updatedRows = jdbcTemplate.update(
            "UPDATE users SET age = ? WHERE name = ?", 
            26, "김춘식"
        );
        System.out.println("김춘식 나이 업데이트: " + updatedRows + "개 행");
        
        // 업데이트 확인
        Integer newAge = jdbcTemplate.queryForObject(
            "SELECT age FROM users WHERE name = ?", 
            Integer.class, 
            "김춘식"
        );
        System.out.println("김춘식의 새로운 나이: " + newAge);
        
        // 사용자 삭제
        int deletedRows = jdbcTemplate.update(
            "DELETE FROM users WHERE age > ?", 
            31
        );
        System.out.println("31세 초과 사용자 삭제: " + deletedRows + "개 행");
        
        // 최종 사용자 수 확인
        Integer finalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        System.out.println("최종 사용자 수: " + finalCount);
    }
} 