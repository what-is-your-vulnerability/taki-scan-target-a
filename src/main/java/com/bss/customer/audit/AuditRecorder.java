package com.bss.customer.audit;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bss.customer.security.LoginAccount;

@Component
public class AuditRecorder {

    private final AuditMapper auditMapper;

    public AuditRecorder(AuditMapper auditMapper) {
        this.auditMapper = auditMapper;
    }

    @Transactional
    public void success(LoginAccount actor, String action, String targetType, String targetId, Long customerId, String reason) {
        auditMapper.insert(actor.accountId(), action, targetType, targetId, customerId, reason, Instant.now(), "SUCCESS", clientIp());
    }

    // 로그인 상태가 없는 요청(비밀번호 재설정)은 행위자 계정을 직접 넘긴다.
    @Transactional
    public void successBy(String actorId, String action, String targetType, String targetId, Long customerId, String reason) {
        auditMapper.insert(actorId, action, targetType, targetId, customerId, reason, Instant.now(), "SUCCESS", clientIp());
    }

    // 거부 직후 호출한 쪽 트랜잭션이 롤백돼도 기록은 남아야 한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void denied(LoginAccount actor, String action, String targetType, String targetId, Long customerId, String reason) {
        auditMapper.insert(actor.accountId(), action, targetType, targetId, customerId, reason, Instant.now(), "DENIED", clientIp());
    }

    // 접속지 정보: 요청을 보낸 쪽의 IP 주소. 요청 헤더 값은 위조할 수 있어 쓰지 않고 연결 주소만 기록한다.
    private static String clientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest().getRemoteAddr();
        }
        return null;
    }
}
