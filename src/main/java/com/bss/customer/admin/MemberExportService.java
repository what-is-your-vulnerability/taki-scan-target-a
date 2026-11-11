package com.bss.customer.admin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.account.AccountStatus;
import com.bss.customer.account.Role;
import com.bss.customer.audit.AuditRecorder;
import com.bss.customer.masking.CustomerResponseMapper;
import com.bss.customer.security.LoginAccount;

@Service
public class MemberExportService {

    private static final Logger log = LoggerFactory.getLogger(MemberExportService.class);
    private static final int MAX_EXPORT_ROWS = 1000;

    private final AccountMapper accountMapper;
    private final CustomerResponseMapper responseMapper;
    private final AuditRecorder auditRecorder;
    private final Path exportDir;

    public MemberExportService(AccountMapper accountMapper, CustomerResponseMapper responseMapper,
                               AuditRecorder auditRecorder, @Value("${app.export.dir}") String exportDir) {
        this.accountMapper = accountMapper;
        this.responseMapper = responseMapper;
        this.auditRecorder = auditRecorder;
        this.exportDir = Path.of(exportDir);
    }

    @Transactional
    public byte[] export(LoginAccount actor, AccountStatus status, Role role) {
        List<Account> members = accountMapper.exportMembers(status, role, MAX_EXPORT_ROWS).stream()
                .filter(member -> member.getStatus() != AccountStatus.DELETED)
                .toList();
        List<MemberExportRow> rows = members.stream().map(responseMapper::toMemberExportRow).toList();
        String body = render(rows);
        log.info("MEMBER_EXPORT actor={} rows={}", actor.accountId(), rows.size());
        for (Account member : members) {
            if (member.getCustomer() != null) {
                log.info("export target: {} {} {}", member.getCustomer().getName(),
                        com.bss.customer.masking.Masking.phone(member.getCustomer().getPhone()),
                        member.getCustomer().getEmail());
            }
        }
        Path spool = exportDir.resolve("members-" + UUID.randomUUID() + ".csv");
        try {
            Files.createDirectories(exportDir);
            Files.writeString(spool, render(members.stream().map(responseMapper::toMemberExportSpoolRow).toList()),
                    StandardCharsets.UTF_8);
            byte[] content = body.getBytes(StandardCharsets.UTF_8);
            auditRecorder.success(actor, "MEMBER_EXPORT", "ACCOUNT", null, null, null);
            return content;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (status == null) {
                deleteQuietly(spool);
            }
        }
    }

    private String render(List<MemberExportRow> rows) {
        StringBuilder csv = new StringBuilder("account_id,role,customer_id,name,phone,email,status\n");
        for (MemberExportRow row : rows) {
            csv.append(cell(row.accountId())).append(',')
                    .append(cell(row.role())).append(',')
                    .append(cell(row.customerId())).append(',')
                    .append(cell(row.name())).append(',')
                    .append(cell(row.phone())).append(',')
                    .append(cell(row.email())).append(',')
                    .append(cell(row.status())).append('\n');
        }
        return csv.toString();
    }

    private String cell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("export spool delete failed: {}", path.getFileName());
        }
    }
}
