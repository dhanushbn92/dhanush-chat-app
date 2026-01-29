package com.dhanush.chat.dhanush_chat_bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "uploaded_rag_files")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadedFiles {

    @Id
    private String docId;
    private String fileName;
    private String userId;
    private LocalDate updatedAt;
}
