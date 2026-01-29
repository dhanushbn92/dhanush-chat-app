package com.dhanush.chat.dhanush_chat_bot.repository;

import com.dhanush.chat.dhanush_chat_bot.entity.UploadedFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFiles, String> {

    void deleteByDocId(String docId);
}
