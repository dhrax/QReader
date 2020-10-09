package com.daisa.qreader;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryElementSorter {

    ArrayList<HistoryElement> historyElements;

    public HistoryElementSorter(ArrayList<HistoryElement> historyElements) {
        this.historyElements = historyElements;
    }

    public ArrayList<HistoryElement> getSortedElementByID() {
        Collections.sort(historyElements);
        return historyElements;
    }
}
