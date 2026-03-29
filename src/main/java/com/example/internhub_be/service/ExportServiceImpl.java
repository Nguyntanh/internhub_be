package com.example.internhub_be.service;

import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.response.FinalEvaluationResponse;
import com.example.internhub_be.payload.response.RadarAnalyticsResponse;
import com.example.internhub_be.repository.InternshipProfileRepository;
import com.example.internhub_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final UserRepository userRepository;
    private final InternshipProfileRepository profileRepository;
    private final FinalEvaluationService evaluationService;
    private final RadarAnalyticsService radarAnalyticsService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── Export 1 intern ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] exportInternReport(Long internId, String requesterEmail) throws IOException {
        User intern = userRepository.findById(internId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy intern ID: " + internId));

        FinalEvaluationResponse eval;
        try {
            eval = evaluationService.getEvaluationByIntern(internId);
        } catch (Exception e) {
            eval = new FinalEvaluationResponse();
            eval.setInternId(internId);
            eval.setInternName(intern.getName());
            eval.setInternEmail(intern.getEmail());
        }

        RadarAnalyticsResponse radar = null;
        try {
            radar = radarAnalyticsService.getRadarByIntern(internId, requesterEmail);
        } catch (Exception ignored) {
            // Radar data unavailable for this user (not INTERN or no access)
        }

        Optional<InternshipProfile> profileOpt = profileRepository.findByUserId(internId);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Báo cáo thực tập");
            sheet.setColumnWidth(0, 7000);
            sheet.setColumnWidth(1, 10000);

            CellStyle titleStyle = createTitleStyle(wb);
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle labelStyle = createLabelStyle(wb);
            CellStyle valueStyle = createValueStyle(wb);
            CellStyle sectionStyle = createSectionStyle(wb);

            int rowNum = 0;

            // ── Tiêu đề ──────────────────────────────────────────────────────
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO KẾT QUẢ THỰC TẬP");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
            titleRow.setHeightInPoints(30);

            rowNum++; // khoảng cách

            // ── 1. Thông tin định danh ────────────────────────────────────────
            addSectionHeader(sheet, rowNum++, "I. THÔNG TIN THỰC TẬP SINH", sectionStyle);

            addLabelValue(sheet, rowNum++, "Họ và tên", intern.getName(), labelStyle, valueStyle);
            addLabelValue(sheet, rowNum++, "Email", intern.getEmail(), labelStyle, valueStyle);
            addLabelValue(sheet, rowNum++, "Số điện thoại",
                    intern.getPhone() != null ? intern.getPhone() : "—", labelStyle, valueStyle);

            if (profileOpt.isPresent()) {
                InternshipProfile p = profileOpt.get();
                addLabelValue(sheet, rowNum++, "Trường đại học",
                        p.getUniversity() != null ? p.getUniversity().getName() : "—", labelStyle, valueStyle);
                addLabelValue(sheet, rowNum++, "Chuyên ngành",
                        p.getMajor() != null ? p.getMajor() : "—", labelStyle, valueStyle);
                addLabelValue(sheet, rowNum++, "Vị trí thực tập",
                        p.getPosition() != null ? p.getPosition().getName() : "—", labelStyle, valueStyle);
                addLabelValue(sheet, rowNum++, "Phòng ban",
                        p.getPosition() != null && p.getPosition().getDepartment() != null
                                ? p.getPosition().getDepartment().getName() : "—", labelStyle, valueStyle);
                addLabelValue(sheet, rowNum++, "Ngày bắt đầu",
                        p.getStartDate() != null ? p.getStartDate().format(DATE_FMT) : "—", labelStyle, valueStyle);
                addLabelValue(sheet, rowNum++, "Ngày kết thúc",
                        p.getEndDate() != null ? p.getEndDate().format(DATE_FMT) : "—", labelStyle, valueStyle);
                addLabelValue(sheet, rowNum++, "Trạng thái",
                        p.getStatus() != null ? p.getStatus().name() : "—", labelStyle, valueStyle);
                addLabelValue(sheet, rowNum++, "Mentor phụ trách",
                        p.getMentor() != null ? p.getMentor().getName() : "—", labelStyle, valueStyle);
                addLabelValue(sheet, rowNum++, "Manager phụ trách",
                        p.getManager() != null ? p.getManager().getName() : "—", labelStyle, valueStyle);
            }

            rowNum++;

            // ── 2. Biểu đồ năng lực (bảng điểm kỹ năng + biểu đồ cột) ────────
            addSectionHeader(sheet, rowNum++, "II. BIỂU ĐỒ NĂNG LỰC", sectionStyle);

            // Header bảng kỹ năng
            Row skillHeaderRow = sheet.createRow(rowNum++);
            String[] skillCols = {"Kỹ năng", "Điểm TB (0–10)", "Benchmark", "Số task", "Trọng số"};
            for (int i = 0; i < skillCols.length; i++) {
                Cell c = skillHeaderRow.createCell(i);
                c.setCellValue(skillCols[i]);
                c.setCellStyle(headerStyle);
            }

            int skillDataStartRow = rowNum;
            int skillCount = 0;

            if (radar != null && radar.getSkillScores() != null) {
                for (RadarAnalyticsResponse.SkillScore s : radar.getSkillScores()) {
                    Row r = sheet.createRow(rowNum++);
                    r.createCell(0).setCellValue(s.getSkillName() != null ? s.getSkillName() : "");
                    r.createCell(1).setCellValue(s.getScore() != null ? s.getScore().doubleValue() : 0.0);
                    double bm = radar.getBenchmarkScores() == null ? 7.0 :
                            radar.getBenchmarkScores().stream()
                                    .filter(b -> b.getSkillId().equals(s.getSkillId()))
                                    .mapToDouble(b -> b.getBenchmarkScore() != null
                                            ? b.getBenchmarkScore().doubleValue() : 7.0)
                                    .findFirst().orElse(7.0);
                    r.createCell(2).setCellValue(bm);
                    r.createCell(3).setCellValue(s.getTaskCount());
                    r.createCell(4).setCellValue(s.getTotalWeight());
                    skillCount++;
                }
            }

            // Tổng điểm
            Row overallRow = sheet.createRow(rowNum++);
            Cell ovLabel = overallRow.createCell(0);
            ovLabel.setCellValue("Điểm tổng thể");
            ovLabel.setCellStyle(labelStyle);
            Cell ovVal = overallRow.createCell(1);
            ovVal.setCellValue(radar != null && radar.getOverallScore() != null
                    ? radar.getOverallScore().doubleValue() : 0.0);
            ovVal.setCellStyle(valueStyle);

            Row taskRow = sheet.createRow(rowNum++);
            Cell tkLabel = taskRow.createCell(0);
            tkLabel.setCellValue("Task hoàn thành / Tổng");
            tkLabel.setCellStyle(labelStyle);
            int reviewed = radar != null ? radar.getTotalTasksReviewed() : 0;
            int totalAll = radar != null ? radar.getTotalTasksAll() : 0;
            taskRow.createCell(1).setCellValue(reviewed + " / " + totalAll);

            // ── Thêm biểu đồ cột so sánh điểm thực tế vs Benchmark ──────────
            if (skillCount > 0 && radar != null && radar.getSkillScores() != null) {
                rowNum += 2;
                addBarChart(wb, sheet, radar, skillDataStartRow, skillCount, rowNum);
                rowNum += 18; // reserved space for chart
            }

            rowNum++;

            // ── 3. Nhận xét của Mentor ────────────────────────────────────────
            addSectionHeader(sheet, rowNum++, "III. NHẬN XÉT CỦA MENTOR", sectionStyle);

            if (eval.getMentorName() != null) {
                addLabelValue(sheet, rowNum++, "Mentor", eval.getMentorName(), labelStyle, valueStyle);
            }
            addLabelValue(sheet, rowNum++, "Nhận xét tổng kết",
                    eval.getOverallComment() != null ? eval.getOverallComment() : "(Chưa có nhận xét)",
                    labelStyle, valueStyle);
            addLabelValue(sheet, rowNum++, "Trạng thái đánh giá",
                    eval.getStatus() != null ? eval.getStatus() : "DRAFT", labelStyle, valueStyle);
            if (eval.getSubmittedAt() != null) {
                addLabelValue(sheet, rowNum++, "Ngày nộp đánh giá",
                        eval.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        labelStyle, valueStyle);
            }

            rowNum++;

            // ── 4. Quyết định của Manager ─────────────────────────────────────
            addSectionHeader(sheet, rowNum++, "IV. QUYẾT ĐỊNH CỦA MANAGER", sectionStyle);

            if (profileOpt.isPresent()) {
                InternshipProfile p = profileOpt.get();
                addLabelValue(sheet, rowNum++, "Manager phụ trách",
                        p.getManager() != null ? p.getManager().getName() : "—", labelStyle, valueStyle);
                addLabelValue(sheet, rowNum++, "Kết quả thực tập",
                        p.getStatus() != null ? p.getStatus().name() : "—", labelStyle, valueStyle);
            }

            // Chú ý: Manager signature placeholder
            rowNum += 2;
            Row signRow = sheet.createRow(rowNum++);
            signRow.createCell(0).setCellValue("Ký xác nhận (Manager): ________________________");
            signRow.createCell(2).setCellValue("Ngày: ____/____/______");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ─── Export nhóm ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] exportGroupReport(Long departmentId, Long universityId, String requesterEmail) throws IOException {

        List<InternshipProfile> profiles = profileRepository.findAllWithRelations();

        if (departmentId != null) {
            profiles = profiles.stream()
                    .filter(p -> p.getPosition() != null
                            && p.getPosition().getDepartment() != null
                            && p.getPosition().getDepartment().getId().equals(departmentId))
                    .toList();
        }
        if (universityId != null) {
            profiles = profiles.stream()
                    .filter(p -> p.getUniversity() != null
                            && p.getUniversity().getId().equals(universityId))
                    .toList();
        }

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Báo cáo nhóm");
            sheet.setDefaultColumnWidth(18);

            CellStyle titleStyle = createTitleStyle(wb);
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle valueStyle = createValueStyle(wb);

            int rowNum = 0;

            // Tiêu đề
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO KẾT QUẢ THỰC TẬP — THEO NHÓM");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            titleRow.setHeightInPoints(30);

            rowNum++;

            // Header bảng
            Row headerRow = sheet.createRow(rowNum++);
            String[] cols = {
                    "STT", "Họ tên", "Email", "Trường ĐH", "Chuyên ngành",
                    "Vị trí", "Phòng ban", "Mentor", "Điểm tổng", "Kết quả"
            };
            for (int i = 0; i < cols.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            // Dữ liệu
            int stt = 1;
            for (InternshipProfile p : profiles) {
                User intern = p.getUser();
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(intern.getName());
                row.createCell(2).setCellValue(intern.getEmail());
                row.createCell(3).setCellValue(p.getUniversity() != null ? p.getUniversity().getName() : "—");
                row.createCell(4).setCellValue(p.getMajor() != null ? p.getMajor() : "—");
                row.createCell(5).setCellValue(p.getPosition() != null ? p.getPosition().getName() : "—");
                row.createCell(6).setCellValue(p.getPosition() != null && p.getPosition().getDepartment() != null
                        ? p.getPosition().getDepartment().getName() : "—");
                row.createCell(7).setCellValue(p.getMentor() != null ? p.getMentor().getName() : "—");

                double overallScore = 0;
                try {
                    RadarAnalyticsResponse radar = radarAnalyticsService.getRadarForExport(intern.getId());
                    overallScore = radar.getOverallScore() != null
                            ? radar.getOverallScore().doubleValue() : 0.0;
                } catch (Exception ignored) {}
                row.createCell(8).setCellValue(overallScore);
                row.createCell(9).setCellValue(p.getStatus() != null ? p.getStatus().name() : "—");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ─── Bar Chart ───────────────────────────────────────────────────────────

    private void addBarChart(XSSFWorkbook wb, Sheet sheet, RadarAnalyticsResponse radar,
                             int dataStartRow, int skillCount, int chartRow) {
        XSSFSheet xssfSheet = (XSSFSheet) sheet;
        XSSFDrawing drawing = xssfSheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, chartRow, 5, chartRow + 16);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Biểu đồ năng lực: Thực tế vs Benchmark");
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Kỹ năng");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Điểm số (0-10)");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        leftAxis.setMinimum(0);
        leftAxis.setMaximum(10);

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
                xssfSheet, new CellRangeAddress(dataStartRow, dataStartRow + skillCount - 1, 0, 0));

        XDDFNumericalDataSource<Double> actualData = XDDFDataSourcesFactory.fromNumericCellRange(
                xssfSheet, new CellRangeAddress(dataStartRow, dataStartRow + skillCount - 1, 1, 1));

        XDDFNumericalDataSource<Double> benchmarkData = XDDFDataSourcesFactory.fromNumericCellRange(
                xssfSheet, new CellRangeAddress(dataStartRow, dataStartRow + skillCount - 1, 2, 2));

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(
                ChartTypes.BAR, bottomAxis, leftAxis);
        data.setBarDirection(BarDirection.COL);

        XDDFChartData.Series series1 = data.addSeries(categories, actualData);
        series1.setTitle("Điểm thực tế", null);

        XDDFChartData.Series series2 = data.addSeries(categories, benchmarkData);
        series2.setTitle("Benchmark", null);

        chart.plot(data);
    }

    // ─── Style helpers ────────────────────────────────────────────────────────

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 16);
        f.setColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private CellStyle createSectionStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 12);
        f.setColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private CellStyle createLabelStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private CellStyle createValueStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setWrapText(true);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private void addSectionHeader(Sheet sheet, int rowNum, String title, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
        row.setHeightInPoints(20);
    }

    private void addLabelValue(Sheet sheet, int rowNum, String label, String value,
                               CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 3));
    }
}