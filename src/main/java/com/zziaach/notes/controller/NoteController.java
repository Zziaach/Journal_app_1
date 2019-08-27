package com.zziaach.notes.controller;

import com.zziaach.notes.repository.NoteRepository;
import com.zziaach.notes.repository.UserRepository;
import com.zziaach.notes.security.CurrentUser;
import com.zziaach.notes.security.UserPrincipal;
import com.zziaach.notes.service.NoteService;
import com.zziaach.notes.util.AppConstants;
import com.zziaach.notes.util.ModelMapper;

import java.net.URI;

import javax.validation.Valid;

import com.zziaach.notes.exception.ResourceNotFoundException;
import com.zziaach.notes.model.Note;
import com.zziaach.notes.model.User;
import com.zziaach.notes.payload.ApiResponse;
import com.zziaach.notes.payload.NoteRequest;
import com.zziaach.notes.payload.NoteResponse;
import com.zziaach.notes.payload.PagedResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteService noteService;

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    @GetMapping
    public PagedResponse<NoteResponse> getNotes(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return noteService.getAllNotes(page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createNote(@CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody NoteRequest noteRequest) {
        User user = ModelMapper.mapUserPricipalToUser(currentUser);
        noteRequest.setUser(user);
        Note note = noteService.createNote(noteRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{noteId}").buildAndExpand(note.getId())
                .toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "Note Created Successfully"));
    }

    @PutMapping("/{noteId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateNote(@CurrentUser UserPrincipal currentUser, @PathVariable Long noteId,
            @Valid @RequestBody NoteRequest noteRequest) {
        if (!noteRepository.existsById(noteId)) {
            throw new ResourceNotFoundException("Note", "id", noteId);
        }
        Note note = noteRepository.getOne(noteId);
        if (currentUser.getId() == note.getCreatedBy()) {
            note.setTitle(noteRequest.getTitle());
            note.setContent(noteRequest.getContent());
            noteService.updateNote(note);
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You're not allowed"));
        }

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{noteId}").buildAndExpand(noteId)
                .toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "Note Updated Successfully"));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteNote(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody Long noteId) {
        if (!noteRepository.existsById(noteId)) {
            throw new ResourceNotFoundException("Note", "id", noteId);
        }
        NoteResponse note = noteService.getNoteById(noteId);
        if (currentUser.getId() == note.getCreatedBy().getId()) {
            noteService.deleteNote(noteId);
            return ResponseEntity.ok(new ApiResponse(true, "Note Updated Successfully"));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "You're not allowed"));
        }
    }

    @GetMapping("/{noteId}")
    public NoteResponse getNoteById(@PathVariable Long noteId) {
        return noteService.getNoteById(noteId);
    }
}