package hightech.edu.Inscription.controller;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import hightech.edu.Inscription.model.Cours;
import hightech.edu.Inscription.model.Etudiant;
import hightech.edu.Inscription.service.CoursService;
import hightech.edu.Inscription.service.EtudiantService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final EtudiantService etudiantService;
    private final CoursService coursService;

    //Helpers

    private PdfFont boldFont() throws IOException {
        return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
    }

    private PdfFont normalFont() throws IOException {
        return PdfFontFactory.createFont(StandardFonts.HELVETICA);
    }

    private Cell pdfHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }

    private Cell pdfDataCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font));
    }

    //_éTUDIANTS_Excel

    @GetMapping("/etudiants/excel")
    public void exportEtudiantsExcel(
            @RequestParam(defaultValue = "") String search,
            HttpServletResponse response) throws IOException {

        List<Etudiant> etudiants = search.isBlank()
                ? etudiantService.findAll()
                : etudiantService.findPaginated(search, 0, Integer.MAX_VALUE).getContent();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=etudiants.xlsx");

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Étudiants");

            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"ID", "Nom", "Prénom", "Email", "Date naissance"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Etudiant e : etudiants) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(e.getNom());
                row.createCell(2).setCellValue(e.getPrenom());
                row.createCell(3).setCellValue(e.getEmail());
                row.createCell(4).setCellValue(
                        e.getDateNaissance() != null ? e.getDateNaissance().toString() : "");
            }
            int[] widths = {2000, 6000, 6000, 8000, 4000};
            for (int i = 0; i < headers.length; i++) sheet.setColumnWidth(i, widths[i]);
            wb.write(response.getOutputStream());
        }
    }

    //ÉTUDIANTS_PDF

    @GetMapping("/etudiants/pdf")
    public void exportEtudiantsPdf(
            @RequestParam(defaultValue = "") String search,
            HttpServletResponse response) throws IOException {

        List<Etudiant> etudiants = search.isBlank()
                ? etudiantService.findAll()
                : etudiantService.findPaginated(search, 0, Integer.MAX_VALUE).getContent();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=etudiants.pdf");

        PdfFont bold   = boldFont();
        PdfFont normal = normalFont();

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf  = new PdfDocument(writer);
        Document doc     = new Document(pdf);

        doc.add(new Paragraph("Liste des Étudiants — EduManager")
                .setFont(bold).setFontSize(16).setMarginBottom(12));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 4, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        for (String h : new String[]{"ID", "Nom", "Prénom", "Email", "Date naissance"}) {
            table.addHeaderCell(pdfHeaderCell(h, bold));
        }
        for (Etudiant e : etudiants) {
            table.addCell(pdfDataCell(String.valueOf(e.getId()), normal));
            table.addCell(pdfDataCell(e.getNom(), normal));
            table.addCell(pdfDataCell(e.getPrenom(), normal));
            table.addCell(pdfDataCell(e.getEmail(), normal));
            table.addCell(pdfDataCell(
                    e.getDateNaissance() != null ? e.getDateNaissance().toString() : "—", normal));
        }
        doc.add(table);
        doc.close();
    }

    //_COURS_Excel

    @GetMapping("/cours/excel")
    public void exportCoursExcel(
            @RequestParam(defaultValue = "") String search,
            HttpServletResponse response) throws IOException {

        List<Cours> coursList = search.isBlank()
                ? coursService.findAll()
                : coursService.findPaginated(search, 0, Integer.MAX_VALUE).getContent();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=cours.xlsx");

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Cours");

            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"ID", "Titre", "Enseignant", "Durée (h)", "Description"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Cours c : coursList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getTitre());
                row.createCell(2).setCellValue(c.getEnseignant());
                row.createCell(3).setCellValue(c.getDuree());
                row.createCell(4).setCellValue(
                        c.getDescription() != null ? c.getDescription() : "");
            }
            int[] widths = {2000, 6000, 6000, 8000, 4000};
            for (int i = 0; i < headers.length; i++) sheet.setColumnWidth(i, widths[i]);
            wb.write(response.getOutputStream());
        }
    }

    //COURS_PDF

    @GetMapping("/cours/pdf")
    public void exportCoursPdf(
            @RequestParam(defaultValue = "") String search,
            HttpServletResponse response) throws IOException {

        List<Cours> coursList = search.isBlank()
                ? coursService.findAll()
                : coursService.findPaginated(search, 0, Integer.MAX_VALUE).getContent();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=cours.pdf");

        PdfFont bold   = boldFont();
        PdfFont normal = normalFont();

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf  = new PdfDocument(writer);
        Document doc     = new Document(pdf);

        doc.add(new Paragraph("Catalogue des Cours — EduManager")
                .setFont(bold).setFontSize(16).setMarginBottom(12));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 1, 4}));
        table.setWidth(UnitValue.createPercentValue(100));

        for (String h : new String[]{"ID", "Titre", "Enseignant", "Durée (h)", "Description"}) {
            table.addHeaderCell(pdfHeaderCell(h, bold));
        }
        for (Cours c : coursList) {
            table.addCell(pdfDataCell(String.valueOf(c.getId()), normal));
            table.addCell(pdfDataCell(c.getTitre(), normal));
            table.addCell(pdfDataCell(c.getEnseignant(), normal));
            table.addCell(pdfDataCell(String.valueOf(c.getDuree()), normal));
            table.addCell(pdfDataCell(
                    c.getDescription() != null ? c.getDescription() : "—", normal));
        }
        doc.add(table);
        doc.close();
    }
}
