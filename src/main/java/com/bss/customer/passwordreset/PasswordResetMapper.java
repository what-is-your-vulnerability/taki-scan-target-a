package com.bss.customer.passwordreset;

import java.time.Instant;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PasswordResetMapper {

    void insert(@Param("accountId") String accountId, @Param("tokenHash") String tokenHash,
                @Param("expiresAt") Instant expiresAt, @Param("createdAt") Instant createdAt);

    PasswordReset findByTokenHash(@Param("tokenHash") String tokenHash);

    int countSince(@Param("accountId") String accountId, @Param("since") Instant since);

    void invalidateUnused(@Param("accountId") String accountId, @Param("now") Instant now);

    void invalidateRecentUnused(@Param("accountId") String accountId, @Param("now") Instant now,
                                @Param("since") Instant since);

    int markUsed(@Param("resetId") long resetId, @Param("now") Instant now);
}
