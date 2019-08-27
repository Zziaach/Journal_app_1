package com.zziaach.notes.repository;

import com.zziaach.notes.model.Note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    Page<Note> findByCreatedBy(Long userId, Pageable pageable);

    long countByCreatedBy(Long userId);

}