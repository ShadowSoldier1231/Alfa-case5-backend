package com.project.main.dto;

public class LeadboardInfo {
    private Long placement;
    private Long total;

    public LeadboardInfo(){

    }
    public  LeadboardInfo(Long placement, Long total){
        this.placement = placement;
        this.total = total;
    }

    public Long getPlacement() {
        return placement;
    }

    public Long getTotal() {
        return total;
    }

    public void setPlacement(Long placement) {
        this.placement = placement;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
