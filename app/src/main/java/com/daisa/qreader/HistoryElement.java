package com.daisa.qreader;

public class HistoryElement {
    private String text;
    private String date;
    private boolean favorite;

    public HistoryElement(String text, String date, boolean favorite) {
        this.text = text;
        this.date = date;
        this.favorite = favorite;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public boolean isFavorite() {
        return favorite;
    }
}
