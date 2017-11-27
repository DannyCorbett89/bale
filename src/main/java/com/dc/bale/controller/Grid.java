package com.dc.bale.controller;

public class Grid {
    private String[][] data;

    public Grid(int numRows, int numColumns) {
        data = new String[numRows][numColumns];
    }

    public void setValue(int row, int column, String value) {
        data[row][column] = value;
    }

    public String toHTML() {
        StringBuilder sb = new StringBuilder();
        for (String[] row : data) {
            sb.append("<tr><td>").append(row[0]).append("</td>");

            for (int y = 1; y < row.length; y++) {
                if (!columnIsEmpty(y)) {
                    String value = row[y];
                    sb.append("<td>").append(value != null ? value : "").append("</td>");
                }
            }

            sb.append("<td>")
                    .append("<button id=\"")
                    .append(row[0])
                    .append("\" onClick=\"removePlayer('")
                    .append(row[0].replace("'", "\\'"))
                    .append("')\">Remove</button>")
                    .append("</td>");

            sb.append("</tr>");
        }
        return sb.toString();
    }

    private boolean columnIsEmpty(int y) {
        for (String[] row : data) {
            if (row[y] != null) {
                return false;
            }
        }
        return true;
    }

    public int numColumnsWithValue() {
        int numColumnsWithValue = 0;
        for (String[] row : data) {
            for (int y = 1; y < row.length; y++) {
                if (!columnIsEmpty(y)) {
                    numColumnsWithValue++;
                }
            }
        }
        return numColumnsWithValue;
    }
}
