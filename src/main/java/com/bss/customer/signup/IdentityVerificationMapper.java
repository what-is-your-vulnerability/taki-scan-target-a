package com.bss.customer.signup;

import java.time.Instant;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IdentityVerificationMapper {

    IdentityVerification findById(@Param("verificationId") String verificationId);

    int countByAgencyResultRef(@Param("agencyResultRef") String agencyResultRef);

    void insert(IdentityVerification verification);

    void markClosed(@Param("verificationId") String verificationId, @Param("status") String status);

    int markCompleted(@Param("verificationId") String verificationId,
                      @Param("agencyResultRef") String agencyResultRef,
                      @Param("completedAt") Instant completedAt);
}
