package com.daisa.qreader;

public class HistoryElement implements Comparable<HistoryElement>{
    private final int ID;
    private String text;
    private String date;
    private boolean favorite;

    public HistoryElement(int ID, String text, String date, boolean favorite) {
        this.ID = ID;
        this.text = text;
        this.date = date;
        this.favorite = favorite;
    }

    public int getID() {
        return ID;
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

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public int compareTo(HistoryElement element) {
        return Integer.compare(element.getID(), this.ID);
    }

    @Override
    public String toString() {
        return "HistoryElement{" +
                "ID=" + ID +
                ", text='" + text + '\'' +
                ", date='" + date + '\'' +
                ", favorite=" + favorite +
                '}';
    }
}
