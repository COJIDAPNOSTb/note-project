package com.example.noteservie.app.controller;

import com.example.noteservie.app.model.Note;
import com.example.noteservie.app.model.dto.CreateNoteDto;
import com.example.noteservie.app.model.dto.UpdateNoteDto;
import com.example.noteservie.app.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<Note> create(@RequestBody CreateNoteDto dto) {
        return ResponseEntity.ok(noteService.createNote(dto));
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllMyNotes() {
        return ResponseEntity.ok(noteService.getMyNotes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> update(@PathVariable Long id, @RequestBody UpdateNoteDto dto) {
        return ResponseEntity.ok(noteService.updateNote(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
