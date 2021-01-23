package com.example.betterfit.ui.contents;

public class YouTubeVideos {
    String videoUrl;
    String title;

    public YouTubeVideos() {

    }

    public YouTubeVideos(String videoUrl, String Title) {
        this.videoUrl = videoUrl;
        title = Title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setTitle(String title) { this.title = title; }
}
