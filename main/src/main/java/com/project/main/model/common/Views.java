package com.project.main.model.common;

public class Views {


    public interface RegisterResultId {}
    public interface RegisterResultPartial extends RegisterResultId {}
    public interface RegisterResultFull extends RegisterResultPartial {}

    public interface PublicProfile {}
    public interface MyProfile extends PublicProfile {}
}
