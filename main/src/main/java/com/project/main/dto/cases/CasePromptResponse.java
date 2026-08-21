package com.project.main.dto.cases;

public class CasePromptResponse {
    private Long id;
    private String title;
    private String promptContextEn;

    public CasePromptResponse(String title, String promptContextEn, Long id) {
        this.title = title;
        this.promptContextEn = promptContextEn;
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPromptContextEn() { return promptContextEn; }
    public void setPromptContextEn(String promptContextEn) { this.promptContextEn = promptContextEn; }
}