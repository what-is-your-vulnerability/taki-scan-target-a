package com.bss.customer.audit;

import java.time.Instant;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditMapper {

    void insert(@Param("actorId") String actorId,
                @Param("action") String action,
                @Param("targetType") String targetType,
                @Param("targetId") String targetId,
                @Param("customerId") Long customerId,
                @Param("reason") String reason,
                @Param("occurredAt") Instant occurredAt,
                @Param("result") String result, @Param("clientIp") String clientIp);

    List<AuditEvent> searchEvents(@Param("search") AuditSearch search);

    long countEvents(@Param("search") AuditSearch search);
}
