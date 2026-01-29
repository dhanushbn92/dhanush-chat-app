package com.dhanush.chat.dhanush_chat_bot.service;

import com.dhanush.chat.dhanush_chat_bot.entity.UploadedFiles;
import com.dhanush.chat.dhanush_chat_bot.repository.UploadedFileRepository;
import com.dhanush.chat.dhanush_chat_bot.utils.DocumentUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final UploadedFileRepository uploadedFileRepository;

    private void addDocument(List<Document> documents) {
        log.info("Adding Document of size : {}", documents.size());
        vectorStore.add(documents);
    }

    public void addDocument(Resource resource) {
        String filename = resource.getFilename();
        String docId = UUID.randomUUID().toString();
        List<Document> documentFromResource = DocumentUtils.getDocumentFromResource(resource, chatModel,filename,docId);
        addDocument(documentFromResource);

        uploadedFileRepository.save(UploadedFiles.builder()
                .docId(docId)
                .fileName(filename)
                .updatedAt(LocalDate.now())
                .build());
    }

    public List<UploadedFiles> getAllUploadedFiles() {
        return uploadedFileRepository.findAll();
    }

    @Transactional
    public void deleteDocumentById(String docId) {
//        new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("docId"), new Filter.Value(docId));
        vectorStore.delete(new FilterExpressionBuilder().eq("docId", docId).build());
        log.info("Deleted Document with docId : {}", docId);

        uploadedFileRepository.deleteByDocId(docId);
    }
}
