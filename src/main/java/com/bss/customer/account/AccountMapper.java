package com.bss.customer.account;

import java.time.Instant;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {

    Account findById(@Param("accountId") String accountId);

    Account findByIdForUpdate(@Param("accountId") String accountId);

    List<Account> findByIdsForUpdate(@Param("accountIds") List<String> accountIds);

    List<Account> findByIdListForUpdate(@Param("idList") String idList);

    void suspendByIds(@Param("accountIds") List<String> accountIds);

    Account findByLoginId(@Param("loginId") String loginId);

    Account findBySignupRef(@Param("signupRef") String signupRef);

    Account findMemberById(@Param("accountId") String accountId);

    Account findMemberByCustomerId(@Param("customerId") long customerId);

    List<Account> searchMembers(@Param("search") MemberSearch search);

    List<Account> exportMembers(@Param("status") AccountStatus status, @Param("role") Role role,
                                @Param("limit") int limit);

    long countMembers(@Param("search") MemberSearch search);

    int countByLoginId(@Param("loginId") String loginId);

    void insert(Account account);

    void updateStatus(@Param("accountId") String accountId, @Param("status") AccountStatus status);

    void releaseLock(@Param("accountId") String accountId);

    void updateRole(@Param("accountId") String accountId, @Param("role") Role role);

    int activateVerified(@Param("accountId") String accountId, @Param("verifiedAt") Instant verifiedAt);

    void recordLoginSuccess(@Param("accountId") String accountId, @Param("loginAt") Instant loginAt);

    void recordLoginFailure(@Param("accountId") String accountId, @Param("lockThreshold") int lockThreshold);

    void updatePassword(@Param("accountId") String accountId, @Param("passwordHash") String passwordHash,
                        @Param("changedAt") Instant changedAt);

    void endSessions(@Param("accountId") String accountId);

    void updateRoleAndStatus(@Param("accountId") String accountId, @Param("role") String role,
                             @Param("status") String status);
}
