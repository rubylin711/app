package com.prime.dmg.launcher.Utils.JsonParser;

public class RecommendContent {
    private String g_ServiceId;
    private String g_ProgramName;
    private String g_ProgramPoster;

    // Getters and Setters
    public String get_service_id() {
        return g_ServiceId;
    }

    public void set_service_id(String serviceId) {
        this.g_ServiceId = serviceId;
    }

    public String get_program_name() {
        return g_ProgramName;
    }

    public void set_program_name(String programName) {
        this.g_ProgramName = programName;
    }

    public String get_program_poster() {
        return g_ProgramPoster;
    }

    public void set_program_poster(String programPoster) {
        this.g_ProgramPoster = programPoster;
    }
}
