package com.example.noteservie.app.model.dto;

import lombok.Data;

@Data
public class CreateNoteDto {
    private String name;
    private String text;
    private Long folderId; // CAN BE NULL
}
