package com.example.noteservie.app.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String text;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;
}
