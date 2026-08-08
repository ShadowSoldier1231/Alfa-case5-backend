package com.project.main.dto;

public class LeaderboardInfo {
    private Long placement;
    private Long total;

    public LeaderboardInfo(){

    }
    public LeaderboardInfo(Long placement, Long total){
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
