package com.project.main.dto.learing;

public class PartialMaterialDto {

    private Long id;
    private Long caseId;
    private String title;
    private String text;
    private Integer position;

    public PartialMaterialDto(Long id, Long caseId, String title, String text, Integer position){
        this.id = id;
        this.caseId = caseId;
        this.text = text;
        this.title = title;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
