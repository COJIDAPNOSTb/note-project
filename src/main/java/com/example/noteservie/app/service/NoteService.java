package com.example.noteservie.app.service;

import com.example.noteservie.app.model.Folder;
import com.example.noteservie.app.model.Note;
import com.example.noteservie.app.model.dto.CreateNoteDto;
import com.example.noteservie.app.model.dto.UpdateNoteDto;
import com.example.noteservie.app.repository.FolderRepository;
import com.example.noteservie.app.repository.NoteRepository;
import com.example.noteservie.app.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final FolderRepository folderRepository;

    public Note createNote(CreateNoteDto dto) {
        Note note = Note.builder()
                .name(dto.getName())
                .text(dto.getText())
                .authorId(SecurityUtils.getCurrentUserId())
                .authorName(SecurityUtils.getCurrentUsername())
                .createdAt(LocalDateTime.now())
                .build();

        if (dto.getFolderId() != null) {
            Folder folder = folderRepository.findById(dto.getFolderId())
                    .filter(f -> Objects.equals(f.getAuthorId(), SecurityUtils.getCurrentUserId()))
                    .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));
            note.setFolder(folder);
        }

        return noteRepository.save(note);
    }

    public List<Note> getMyNotes() {
        return noteRepository.findAllByAuthorId(SecurityUtils.getCurrentUserId());
    }

    public Note updateNote(Long id, UpdateNoteDto dto) {
        Note note = noteRepository.findById(id)
                .filter(n -> Objects.equals(n.getAuthorId(), SecurityUtils.getCurrentUserId()))
                .orElseThrow(() -> new RuntimeException("Note not found or access denied"));

        note.setName(dto.getName());
        note.setText(dto.getText());

        if (dto.getFolderId() != null) {
            Folder folder = folderRepository.findById(dto.getFolderId())
                    .filter(f -> Objects.equals(f.getAuthorId(), SecurityUtils.getCurrentUserId()))
                    .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));
            note.setFolder(folder);
        } else {
            note.setFolder(null);
        }

        return noteRepository.save(note);
    }

    public void deleteNote(Long id) {
        Note note = noteRepository.findById(id)
                .filter(n -> Objects.equals(n.getAuthorId(), SecurityUtils.getCurrentUserId()))
                .orElseThrow(() -> new RuntimeException("Note not found or access denied"));

        noteRepository.delete(note);
    }
}

