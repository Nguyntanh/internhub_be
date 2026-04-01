package com.example.internhub_be.service;

import java.io.IOException;
import java.util.List;

public interface ExportService {

    /** Xuất báo cáo 1 intern ra byte[] Excel */
    byte[] exportInternReport(Long internId, String requesterEmail) throws IOException;

    /** Xuất báo cáo theo nhóm (departmentId hoặc universityId) */
    byte[] exportGroupReport(Long departmentId, Long universityId, String requesterEmail) throws IOException;
}