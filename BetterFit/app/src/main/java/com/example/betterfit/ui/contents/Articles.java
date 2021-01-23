package com.example.betterfit.ui.contents;

import android.graphics.drawable.Drawable;

public class Articles {
    int thumbnail;
    String newsTitle;
    String author;
    String sentence;
    String url;

    public Articles(int thumbnail, String newsTitle, String author, String sentence, String url) {
        this.thumbnail = thumbnail;
        this.newsTitle = newsTitle;
        this.author = author;
        this.sentence = sentence;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }
}
