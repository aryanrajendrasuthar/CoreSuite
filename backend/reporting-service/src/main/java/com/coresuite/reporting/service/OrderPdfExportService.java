package com.coresuite.reporting.service;

import com.coresuite.reporting.dto.KpiResponse;
import com.coresuite.reporting.dto.OrderSummary;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPdfExportService {

    private final ReportingService reportingService;

    public byte[] export(Instant from, Instant to) {
        List<OrderSummary> orders = reportingService.ordersInRange(from, to);
        KpiResponse kpis = reportingService.getKpis(from, to);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph("CoreSuite Order Report", titleFont));
            document.add(new Paragraph("Generated at: " + Instant.now()));
            document.add(new Paragraph("Total orders: " + kpis.totalOrders()));
            document.add(new Paragraph("Total revenue: " + kpis.totalRevenue()));
            document.add(spacer());

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Order ID");
            addHeaderCell(table, "Customer ID");
            addHeaderCell(table, "Status");
            addHeaderCell(table, "Total");
            addHeaderCell(table, "Created At");
            for (OrderSummary order : orders) {
                table.addCell(String.valueOf(order.id()));
                table.addCell(String.valueOf(order.customerId()));
                table.addCell(order.status());
                table.addCell(order.currency() + " " + order.totalAmount());
                table.addCell(order.createdAt().toString());
            }
            document.add(table);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate PDF report", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate PDF report", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    private Paragraph spacer() {
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(10);
        return spacer;
    }
}
