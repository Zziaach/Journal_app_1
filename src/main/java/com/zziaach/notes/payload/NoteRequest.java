package com.zziaach.notes.payload;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.zziaach.notes.model.User;

public class NoteRequest {

    @NotBlank
    @Size(max = 250)
    private String title;

    @NotBlank
    private String content;

    private User user;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}