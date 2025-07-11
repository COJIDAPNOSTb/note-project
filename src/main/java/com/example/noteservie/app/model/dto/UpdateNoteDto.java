package com.example.noteservie.app.model.dto;

import lombok.Data;

@Data
public class UpdateNoteDto {
    private String name;
    private String text;
    private Long folderId;
}
