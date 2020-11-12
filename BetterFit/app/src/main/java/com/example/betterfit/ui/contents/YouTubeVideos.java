package com.example.betterfit.ui.contents;

public class YouTubeVideos {
    String videoUrl;
    String title;

    public YouTubeVideos() {

    }

    public YouTubeVideos(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
