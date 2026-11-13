package com.bss.customer.support;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.bss.customer.account.AccountStatus;
import com.bss.customer.account.Role;

@Repository
public class MemberLookupDao {

    private static final String BASE = """
            SELECT A.ACCOUNT_ID, A.ROLE, A.CUSTOMER_ID, COALESCE(C.NAME, A.STAFF_NAME) AS NAME, A.STATUS
            FROM ACCOUNT A LEFT JOIN CUSTOMER C ON C.CUSTOMER_ID = A.CUSTOMER_ID
            WHERE A.STATUS <> 'DELETED'""";

    private final JdbcTemplate jdbc;

    public MemberLookupDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MemberLookupItem> findByLoginIdPrefix(String likePattern, String orderBy) {
        String sql = BASE + " AND A.LOGIN_ID LIKE '" + likePattern.replace("'", "''") + "' ESCAPE '\\' ORDER BY " + orderBy + " FETCH FIRST 100 ROWS ONLY";
        return jdbc.query(sql, (rs, n) -> new MemberLookupItem(
                rs.getString("ACCOUNT_ID"),
                Role.valueOf(rs.getString("ROLE")),
                rs.getObject("CUSTOMER_ID") == null ? null : rs.getLong("CUSTOMER_ID"),
                rs.getString("NAME"),
                AccountStatus.valueOf(rs.getString("STATUS"))));
    }
}
