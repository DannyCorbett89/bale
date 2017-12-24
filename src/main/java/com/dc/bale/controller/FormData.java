package com.dc.bale.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormData {
    private String musicNotes;

    public String getConvertedMusicNotes() {
        StringBuffer sb = new StringBuffer();

        String[] notes = musicNotes
                .replace("\r\n", " \r\n ")
                .replace(",", "")
                .split(" ");

        for(String note : notes) {
            if(note.replace(" ", "").equals("\r\n")) {
                sb.append("<br>");
                continue;
            } else if(note.replace(" ", "").equals("")) {
                continue;
            } else if(note.contains("+1")) {
                note = note.replace("+1", "");
            } else if(note.contains("-1")) {
                note = note.replace("-1", "");
                sb.append("ca");
            } else if(note.contains("+")) {
                note = note.replace("+", "");
            } else if(note.contains("-")) {
                note = note.replace("-", "");
                sb.append("ca");
            } else {
                sb.append("c");
            }

            switch (note) {
                case "B♭":
                    sb.append("-");
                    break;
                case "Bb":
                    sb.append("-");
                    break;
                case "B":
                    sb.append("=");
                    break;
                case "A":
                    sb.append("0");
                    break;
                case "G#":
                    sb.append("9");
                    break;
                case "G":
                    sb.append("8");
                    break;
                case "F#":
                    sb.append("7");
                    break;
                case "F":
                    sb.append("6");
                    break;
                case "E♭":
                    sb.append("4");
                    break;
                case "Eb":
                    sb.append("4");
                    break;
                case "E":
                    sb.append("5");
                    break;
                case "D":
                    sb.append("3");
                    break;
                case "C#":
                    sb.append("2");
                    break;
                case "C":
                    sb.append("1");
                    break;
                default:
                    sb.append("<font color=\"FF0000\">");
                    sb.append(note);
                    sb.append("</font>");
            }

            sb.append(" ");
        }

        return sb.toString();
//        return musicNotes.replace("\r\n", "<br>")
//                .replace("B♭+1", "-")
//                .replace("Bb+1", "-")
//                .replace("B+1", "=")
//                .replace("A+1", "0")
//                .replace("G#+1", "9")
//                .replace("G+1", "8")
//                .replace("F#+1", "7")
//                .replace("F+1", "6")
//                .replace("E♭+1", "4")
//                .replace("Eb+1", "4")
//                .replace("E+1", "5")
//                .replace("D+1", "3")
//                .replace("C#+1", "2")
//                .replace("C+1", "1")
//
//                .replace("B♭-1", "ca-")
//                .replace("Bb-1", "ca-")
//                .replace("B-1", "ca=")
//                .replace("A-1", "ca0")
//                .replace("G#-1", "ca9")
//                .replace("G-1", "ca8")
//                .replace("F#-1", "ca7")
//                .replace("F-1", "ca6")
//                .replace("E♭-1", "ca4")
//                .replace("Eb-1", "ca4")
//                .replace("E-1", "ca5")
//                .replace("D-1", "ca3")
//                .replace("C#-1", "ca2")
//                .replace("C-1", "ca1")
//
//                .replace("B♭", "c-")
//                .replace("Bb", "c-")
//                .replace("B", "c=")
//                .replace("A", "c0")
//                .replace("G#", "c9")
//                .replace("G", "c8")
//                .replace("F#", "c7")
//                .replace("F", "c6")
//                .replace("E♭", "c4")
//                .replace("Eb", "c4")
//                .replace("E", "c5")
//                .replace("D", "c3")
//                .replace("C#", "c2")
//                .replace("C", "c1");
    }
}
