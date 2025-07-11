package com.example.noteservie.app.repository;

import com.example.noteservie.app.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByAuthorId(Long authorId);
}
