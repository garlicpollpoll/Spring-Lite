package com.springlite.framework.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 🎯 Row Mapper Interface
 * Spring Framework의 RowMapper를 참고하여 구현
 * 
 * SQL 쿼리 결과(ResultSet)를 Java 객체로 매핑하는 인터페이스
 * 
 * 사용 예제:
 * RowMapper<User> userMapper = (rs, rowNum) -> new User(
 *     rs.getLong("id"),
 *     rs.getString("name"),
 *     rs.getString("email")
 * );
 */
@FunctionalInterface
public interface RowMapper<T> {
    
    /**
     * ResultSet의 현재 행을 객체로 매핑합니다
     * 
     * @param rs 현재 행에 위치한 ResultSet (next() 호출 후 상태)
     * @param rowNum 현재 행 번호 (0부터 시작)
     * @return 매핑된 객체
     * @throws SQLException SQL 에러가 발생한 경우
     */
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
} 