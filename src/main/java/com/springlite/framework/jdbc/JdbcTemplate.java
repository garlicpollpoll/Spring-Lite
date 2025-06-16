package com.springlite.framework.jdbc;

import com.springlite.framework.transaction.JdbcTransactionManager;
import com.springlite.framework.transaction.TransactionStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🚀 Spring Lite JDBC Template
 * Spring Framework의 JdbcTemplate을 참고하여 간단하게 구현한 버전
 * 🔄 트랜잭션과 연동되어 ACID 속성을 보장합니다!
 * 
 * 기본 기능:
 * - DDL 실행 (execute)
 * - DML 실행 (update, insert, delete)
 * - 단일 값 조회 (queryForObject)
 * - 리스트 조회 (queryForList)
 * - 객체 매핑 조회 (query with RowMapper)
 * - 🔄 트랜잭션 지원 (현재 트랜잭션의 Connection 사용)
 */
public class JdbcTemplate {
    
    private DataSource dataSource;
    
    /**
     * DataSource로 JdbcTemplate 생성
     */
    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        System.out.println("📋 JdbcTemplate 생성됨 with DataSource: " + dataSource.getClass().getSimpleName());
    }
    
    /**
     * DataSource 설정 (setter injection용)
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        System.out.println("📋 JdbcTemplate DataSource 설정됨: " + dataSource.getClass().getSimpleName());
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * 🔄 트랜잭션 인식 Connection 가져오기
     * 현재 트랜잭션이 있으면 그 Connection을 사용하고, 없으면 새로운 Connection을 생성
     */
    private Connection getConnection() throws SQLException {
        // 현재 트랜잭션 확인
        TransactionStatus currentTransaction = JdbcTransactionManager.getCurrentTransaction();
        
        if (currentTransaction != null && !currentTransaction.isCompleted()) {
            // 트랜잭션이 있으면 해당 Connection 사용
            Connection connection = currentTransaction.getConnection();
            System.out.println("🔄 현재 트랜잭션의 Connection 사용: " + connection.hashCode());
            return connection;
        } else {
            // 트랜잭션이 없으면 새로운 Connection 생성 (autoCommit=true)
            Connection connection = dataSource.getConnection();
            System.out.println("🆕 새로운 Connection 생성: " + connection.hashCode());
            return connection;
        }
    }
    
    /**
     * 🔄 Connection 정리 (트랜잭션 여부에 따라 다르게 처리)
     */
    private void closeConnection(Connection connection) {
        TransactionStatus currentTransaction = JdbcTransactionManager.getCurrentTransaction();
        
        if (currentTransaction != null && !currentTransaction.isCompleted()) {
            // 트랜잭션이 있으면 Connection을 닫지 않음 (트랜잭션 매니저가 관리)
            System.out.println("🔄 트랜잭션 Connection은 닫지 않음: " + connection.hashCode());
        } else {
            // 트랜잭션이 없으면 Connection 닫기
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("🆕 새로운 Connection 닫음: " + connection.hashCode());
                }
            } catch (SQLException e) {
                System.err.println("⚠️ Connection 닫기 실패: " + e.getMessage());
            }
        }
    }

    // ========================================
    // 🔥 DDL 실행 (CREATE, ALTER, DROP 등)
    // ========================================
    
    /**
     * DDL 문을 실행합니다 (CREATE TABLE, DROP TABLE 등)
     * 
     * 예제:
     * jdbcTemplate.execute("CREATE TABLE users (id BIGINT PRIMARY KEY, name VARCHAR(100))");
     */
    public void execute(String sql) {
        System.out.println("🗒️ DDL 실행: " + sql);
        
        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("✅ DDL 실행 성공");
            }
        } catch (SQLException e) {
            System.err.println("❌ DDL 실행 실패: " + e.getMessage());
            throw new JdbcException("DDL 실행 실패: " + sql, e);
        } finally {
            closeConnection(conn);
        }
    }
    
    // ========================================
    // 🔥 DML 실행 (INSERT, UPDATE, DELETE)
    // ========================================
    
    /**
     * DML 문을 실행합니다 (INSERT, UPDATE, DELETE)
     * 
     * 예제:
     * int rows = jdbcTemplate.update("INSERT INTO users (name, email) VALUES (?, ?)", "김춘식", "chunsik@example.com");
     */
    public int update(String sql, Object... params) {
        System.out.println("📝 DML 실행: " + sql + " with params: " + java.util.Arrays.toString(params));
        
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 파라미터 설정
                setParameters(pstmt, params);
                
                int affectedRows = pstmt.executeUpdate();
                System.out.println("✅ DML 실행 성공, 영향받은 행: " + affectedRows);
                return affectedRows;
            }
        } catch (SQLException e) {
            System.err.println("❌ DML 실행 실패: " + e.getMessage());
            throw new JdbcException("DML 실행 실패: " + sql, e);
        } finally {
            closeConnection(conn);
        }
    }
    
    // ========================================
    // 🔥 단일 값 조회 (COUNT, 특정 컬럼 값 등)
    // ========================================
    
    /**
     * 단일 값을 조회합니다 (COUNT, 특정 컬럼 등)
     * 
     * 예제:
     * Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
     * String name = jdbcTemplate.queryForObject("SELECT name FROM users WHERE id = ?", String.class, 1L);
     */
    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... params) {
        System.out.println("🔍 단일 값 조회: " + sql + " with params: " + java.util.Arrays.toString(params));
        
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 파라미터 설정
                setParameters(pstmt, params);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Object value = rs.getObject(1);
                        T result = convertValue(value, requiredType);
                        System.out.println("✅ 단일 값 조회 성공: " + result);
                        return result;
                    } else {
                        System.out.println("⚠️ 조회 결과 없음");
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 단일 값 조회 실패: " + e.getMessage());
            throw new JdbcException("단일 값 조회 실패: " + sql, e);
        } finally {
            closeConnection(conn);
        }
    }
    
    // ========================================
    // 🔥 리스트 조회 (Map 형태)
    // ========================================
    
    /**
     * 여러 행을 Map 리스트로 조회합니다
     * 
     * 예제:
     * List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT * FROM users");
     */
    public List<Map<String, Object>> queryForList(String sql, Object... params) {
        System.out.println("📋 리스트 조회: " + sql + " with params: " + java.util.Arrays.toString(params));
        
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 파라미터 설정
                setParameters(pstmt, params);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    List<Map<String, Object>> results = new ArrayList<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnLabel(i);
                            Object value = rs.getObject(i);
                            row.put(columnName, value);
                        }
                        results.add(row);
                    }
                    
                    System.out.println("✅ 리스트 조회 성공: " + results.size() + "개 행");
                    return results;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 리스트 조회 실패: " + e.getMessage());
            throw new JdbcException("리스트 조회 실패: " + sql, e);
        } finally {
            closeConnection(conn);
        }
    }
    
    // ========================================
    // 🔥 객체 매핑 조회 (RowMapper 사용)
    // ========================================
    
    /**
     * RowMapper를 사용하여 객체 리스트로 조회합니다
     * 
     * 예제:
     * List<User> users = jdbcTemplate.query("SELECT * FROM users", 
     *     (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("name"), rs.getString("email")));
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        System.out.println("🎯 객체 매핑 조회: " + sql + " with params: " + java.util.Arrays.toString(params));
        
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 파라미터 설정
                setParameters(pstmt, params);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    List<T> results = new ArrayList<>();
                    int rowNum = 0;
                    
                    while (rs.next()) {
                        T object = rowMapper.mapRow(rs, rowNum++);
                        results.add(object);
                    }
                    
                    System.out.println("✅ 객체 매핑 조회 성공: " + results.size() + "개 객체");
                    return results;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 객체 매핑 조회 실패: " + e.getMessage());
            throw new JdbcException("객체 매핑 조회 실패: " + sql, e);
        } finally {
            closeConnection(conn);
        }
    }
    
    /**
     * RowMapper를 사용하여 단일 객체를 조회합니다
     * 
     * 예제:
     * User user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", 
     *     (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("name"), rs.getString("email")), 1L);
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        
        if (results.isEmpty()) {
            System.out.println("⚠️ 객체 조회 결과 없음");
            return null;
        } else if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new JdbcException("예상한 1개 결과, 실제 " + results.size() + "개 결과");
        }
    }
    
    // ========================================
    // 🔧 내부 유틸리티 메서드들
    // ========================================
    
    /**
     * PreparedStatement에 파라미터를 설정합니다
     */
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int paramIndex = i + 1;
            
            if (param == null) {
                pstmt.setNull(paramIndex, Types.NULL);
            } else if (param instanceof String) {
                pstmt.setString(paramIndex, (String) param);
            } else if (param instanceof Integer) {
                pstmt.setInt(paramIndex, (Integer) param);
            } else if (param instanceof Long) {
                pstmt.setLong(paramIndex, (Long) param);
            } else if (param instanceof Double) {
                pstmt.setDouble(paramIndex, (Double) param);
            } else if (param instanceof Boolean) {
                pstmt.setBoolean(paramIndex, (Boolean) param);
            } else if (param instanceof Timestamp) {
                pstmt.setTimestamp(paramIndex, (Timestamp) param);
            } else if (param instanceof Date) {
                pstmt.setDate(paramIndex, (Date) param);
            } else {
                // 기타 타입은 Object로 처리
                pstmt.setObject(paramIndex, param);
            }
        }
    }
    
    /**
     * 값을 요청된 타입으로 변환합니다
     */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> requiredType) {
        if (value == null) {
            return null;
        }
        
        if (requiredType.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        
        // 타입 변환 로직
        if (requiredType == String.class) {
            return (T) value.toString();
        } else if (requiredType == Integer.class || requiredType == int.class) {
            if (value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
            return (T) Integer.valueOf(value.toString());
        } else if (requiredType == Long.class || requiredType == long.class) {
            if (value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
            return (T) Long.valueOf(value.toString());
        } else if (requiredType == Double.class || requiredType == double.class) {
            if (value instanceof Number) {
                return (T) Double.valueOf(((Number) value).doubleValue());
            }
            return (T) Double.valueOf(value.toString());
        } else if (requiredType == Boolean.class || requiredType == boolean.class) {
            if (value instanceof Boolean) {
                return (T) value;
            }
            return (T) Boolean.valueOf(value.toString());
        }
        
        // 기본적으로 캐스팅 시도
        return (T) value;
    }
} 