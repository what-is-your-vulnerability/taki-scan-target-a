package com.bss.customer.support;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.bss.customer.admin.MemberAdminService;
import com.bss.customer.common.ApiException;
import com.bss.customer.security.LoginAccount;

@Service
public class BulkAssignmentService {

    private static final int MAX_ITEMS = 100;

    private final MemberAdminService memberAdminService;

    public BulkAssignmentService(MemberAdminService memberAdminService) {
        this.memberAdminService = memberAdminService;
    }

    public List<Result> assign(LoginAccount actor, String xml) {
        NodeList nodes = parse(xml).getElementsByTagName("assignment");
        if (nodes.getLength() == 0 || nodes.getLength() > MAX_ITEMS) {
            throw ApiException.badRequest();
        }
        List<Result> results = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            String customer = text(node, "customer_id");
            String consultant = text(node, "consultant_id");
            results.add(assignOne(actor, customer, consultant));
        }
        return results;
    }

    private String text(Element node, String tag) {
        NodeList found = node.getElementsByTagName(tag);
        return found.getLength() == 0 ? "" : found.item(0).getTextContent().trim();
    }

    private Result assignOne(LoginAccount actor, String customer, String consultant) {
        long customerId;
        try {
            customerId = Long.parseLong(customer.trim());
        } catch (NumberFormatException e) {
            return new Result(null, consultant, false, "고객 번호를 확인해 주세요: " + customer);
        }
        try {
            memberAdminService.assignConsultant(actor, customerId, consultant);
            return new Result(customerId, consultant, true, null);
        } catch (ApiException e) {
            return new Result(customerId, consultant, false, "배정할 수 없습니다: " + consultant);
        }
    }

    private Document parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ParserConfigurationException | SAXException | java.io.IOException e) {
            throw ApiException.badRequest();
        }
    }

    public record Result(Long customerId, String consultantId, boolean ok, String error) {
    }
}
