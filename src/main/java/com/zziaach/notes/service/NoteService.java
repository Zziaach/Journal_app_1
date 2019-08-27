package com.zziaach.notes.service;

import com.zziaach.notes.exception.BadRequestException;
import com.zziaach.notes.exception.ResourceNotFoundException;
import com.zziaach.notes.model.Note;
import com.zziaach.notes.model.User;
import com.zziaach.notes.payload.NoteRequest;
import com.zziaach.notes.payload.NoteResponse;
import com.zziaach.notes.payload.PagedResponse;
import com.zziaach.notes.repository.NoteRepository;
import com.zziaach.notes.repository.UserRepository;
import com.zziaach.notes.security.UserPrincipal;
import com.zziaach.notes.util.AppConstants;
import com.zziaach.notes.util.ModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    public PagedResponse<NoteResponse> getAllNotes(int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve Notes
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Note> notes = noteRepository.findAll(pageable);

        if (notes.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), notes.getNumber(), notes.getSize(),
                    notes.getTotalElements(), notes.getTotalPages(), notes.isLast());
        }

        Map<Long, User> creatorMap = getNoteCreatorMap(notes.getContent());

        List<NoteResponse> noteResponses = notes.map(note -> {
            return ModelMapper.mapNoteToNoteResponse(note, creatorMap.get(note.getCreatedBy()));
        }).getContent();

        return new PagedResponse<>(noteResponses, notes.getNumber(), notes.getSize(), notes.getTotalElements(),
                notes.getTotalPages(), notes.isLast());
    }

    public PagedResponse<NoteResponse> getNotesCreatedBy(String username, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Retrieve all notes created by the given username
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Note> notes = noteRepository.findByCreatedBy(user.getId(), pageable);

        if (notes.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), notes.getNumber(), notes.getSize(),
                    notes.getTotalElements(), notes.getTotalPages(), notes.isLast());
        }

        List<NoteResponse> noteResponses = notes.map(note -> {
            return ModelMapper.mapNoteToNoteResponse(note, user);
        }).getContent();

        return new PagedResponse<>(noteResponses, notes.getNumber(), notes.getSize(), notes.getTotalElements(),
                notes.getTotalPages(), notes.isLast());
    }

    public Note createNote(NoteRequest noteRequest) {
        Note note = new Note();
        note.setTitle(noteRequest.getTitle());
        note.setContent(noteRequest.getContent());
        note.setUser(noteRequest.getUser());

        return noteRepository.save(note);
    }

    public Note updateNote(Note note) {
        return noteRepository.save(note);
    }

    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }

    public NoteResponse getNoteById(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        // Retrieve poll creator details
        User creator = userRepository.findById(note.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", note.getCreatedBy()));

        return ModelMapper.mapNoteToNoteResponse(note, creator);
    }

    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    Map<Long, User> getNoteCreatorMap(List<Note> notes) {
        // Get Note Creator details of the given list of notes
        List<Long> creatorIds = notes.stream().map(Note::getCreatedBy).distinct().collect(Collectors.toList());

        List<User> creators = userRepository.findByIdIn(creatorIds);
        Map<Long, User> creatorMap = creators.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        return creatorMap;
    }
}