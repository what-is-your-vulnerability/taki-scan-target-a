package com.bss.customer.passwordreset;

import java.time.Instant;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MailOutboxMapper {

    void insert(@Param("toEmail") String toEmail, @Param("subject") String subject, @Param("body") String body,
                @Param("createdAt") Instant createdAt);
}
