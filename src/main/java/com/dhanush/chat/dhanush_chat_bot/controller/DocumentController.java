package com.dhanush.chat.dhanush_chat_bot.controller;

import com.dhanush.chat.dhanush_chat_bot.entity.UploadedFiles;
import com.dhanush.chat.dhanush_chat_bot.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public String training(@RequestParam("file")MultipartFile file) {
        documentService.addDocument(file.getResource());
        return "File uploaded successfully, finished training!";
    }

    @GetMapping
    public List<UploadedFiles> getAllUploadedFiles() {
        return documentService.getAllUploadedFiles();
    }

    @DeleteMapping
    public String deleteDocumentById(@RequestParam("docId") String docId) {
        documentService.deleteDocumentById(docId);
        return "Document deleted successfully!";
    }

}
