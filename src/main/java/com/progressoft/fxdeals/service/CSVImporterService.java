package com.progressoft.fxdeals.service;

import com.progressoft.fxdeals.dto.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface CSVImporterService {
    ImportResultDTO importDeals(MultipartFile file);
}