package com.dhanush.chat.dhanush_chat_bot.utils;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

public class DocumentUtils {

    public static List<Document> getDocumentFromResource(Resource resource, ChatModel chatModel,
                                                         String fileName, String docId) {

        return Optional.of(new TikaDocumentReader(resource).read())
                .map(DocumentUtils::splitDocuments)
                .map(doc -> applyMetadata(doc,fileName,docId))
                .map(docs -> applyKeyWords(docs, chatModel))
                .map(docs -> applySummary(docs, chatModel))
                .orElse(List.of());

//        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
//        List<Document> data = tikaDocumentReader.read();
//        List<Document> splitDocuments = splitDocuments(data);
//        List<Document> dataWithMetadata = applyKeyWords(splitDocuments, chatModel);
//        return applySummary(dataWithMetadata, chatModel);
    }

    private static List<Document> applyMetadata(List<Document> docs, String fileName, String docId) {
        docs.forEach(doc -> {
                    doc.getMetadata()
                            .put("fileName", fileName);
                    doc.getMetadata().put("docId", docId);
                    //TODO : Add user role in this metadata
                });
        return docs;
    }

    private static List<Document> applySummary(List<Document> dataWithMetadata, ChatModel chatModel) {
        SummaryMetadataEnricher summaryMetadataEnricher = new SummaryMetadataEnricher(chatModel,
                List.of(SummaryMetadataEnricher.SummaryType.CURRENT));
        return summaryMetadataEnricher.transform(dataWithMetadata);
    }

    private static List<Document> applyKeyWords(List<Document> splitDocuments, ChatModel chatModel) {
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(chatModel,
                5);
        return keywordMetadataEnricher.transform(splitDocuments);
    }

    private static List<Document> splitDocuments(List<Document> data) {
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        return textSplitter.split(data);
    }
}
