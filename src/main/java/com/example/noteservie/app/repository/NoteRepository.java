package com.example.noteservie.app.repository;

import com.example.noteservie.app.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByAuthorId(Long authorId);
    List<Note> findAllByFolderId(Long folderId);
}