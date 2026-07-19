package com.coresuite.reporting.web;

import com.coresuite.reporting.dto.KpiResponse;
import com.coresuite.reporting.service.OrderCsvExportService;
import com.coresuite.reporting.service.OrderPdfExportService;
import com.coresuite.reporting.service.ReportingService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;
    private final OrderCsvExportService orderCsvExportService;
    private final OrderPdfExportService orderPdfExportService;

    @GetMapping("/kpis")
    public KpiResponse kpis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return reportingService.getKpis(from, to);
    }

    @GetMapping("/orders/csv")
    public ResponseEntity<byte[]> ordersCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return exportFile(orderCsvExportService.export(from, to), "orders.csv", MediaType.valueOf("text/csv"));
    }

    @GetMapping("/orders/pdf")
    public ResponseEntity<byte[]> ordersPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return exportFile(orderPdfExportService.export(from, to), "orders.pdf", MediaType.APPLICATION_PDF);
    }

    private ResponseEntity<byte[]> exportFile(byte[] content, String filename, MediaType mediaType) {
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }
}
