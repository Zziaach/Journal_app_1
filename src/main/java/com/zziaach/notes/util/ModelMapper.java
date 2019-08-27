package com.zziaach.notes.util;

import java.time.Instant;

import com.zziaach.notes.model.Note;
import com.zziaach.notes.model.User;
import com.zziaach.notes.payload.NoteResponse;
import com.zziaach.notes.payload.UserSummary;
import com.zziaach.notes.security.UserPrincipal;

public class ModelMapper {

    public static NoteResponse mapNoteToNoteResponse(Note note, User creator) {
        NoteResponse noteResponse = new NoteResponse();
        noteResponse.setId(note.getId());
        noteResponse.setTitle(note.getTitle());
        noteResponse.setContent(note.getContent());
        noteResponse.setCreationDateTime(note.getCreatedAt());
        UserSummary creatorSummary = new UserSummary(creator.getId(), creator.getUsername(), creator.getName());
        noteResponse.setCreatedBy(creatorSummary);

        return noteResponse;
    }

    public static User mapUserPricipalToUser(UserPrincipal userPrincipal) {
        User user = new User();
        user.setId(userPrincipal.getId());
        user.setName(userPrincipal.getName());
        user.setUsername(userPrincipal.getUsername());
        user.setEmail(userPrincipal.getEmail());
        user.setPassword(userPrincipal.getPassword());

        return user;
    }
}