package com.project.main.dto.integration;



public class SubmitSolutionRequest {

    private Long caseId;
    private Long rating;
    private String solutionText;
    private String solutionResponse;

    public SubmitSolutionRequest(){

    }


    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public String getSolutionResponse() {
        return solutionResponse;
    }

    public String getSolutionText() {
        return solutionText;
    }

    public void setSolutionResponse(String solutionResponse) {
        this.solutionResponse = solutionResponse;
    }

    public void setSolutionText(String solutionText) {
        this.solutionText = solutionText;
    }
}
