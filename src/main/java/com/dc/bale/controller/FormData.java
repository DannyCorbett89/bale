package com.dc.bale.controller;

import com.dc.bale.database.MusicKeyMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormData {

    private String mapping;
    private String musicNotes;

    public String getConvertedMusicNotes(MusicKeyMapping mappings) {
        StringBuilder sb = new StringBuilder();

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
            } else if(note.endsWith("+1")) {
                note = note.replace("+1", "");
                sb.append(mappings.getOctave_up())
                        .append("+");
            } else if(note.endsWith("-1")) {
                note = note.replace("-1", "");
                sb.append(mappings.getOctave_down())
                        .append("+");
            } else if(note.endsWith("+") && !note.equals("+")) {
                note = note.replace("+", "");
                sb.append(mappings.getOctave_up())
                        .append("+");
            } else if(note.endsWith("-") && !note.equals("-")) {
                note = note.replace("-", "");
                sb.append(mappings.getOctave_down())
                        .append("+");
            }

            switch (note) {
                case "B♭":
                    sb.append(mappings.getB_flat());
                    break;
                case "Bb":
                    sb.append(mappings.getB_flat());
                    break;
                case "B":
                    sb.append(mappings.getB());
                    break;
                case "A":
                    sb.append(mappings.getA());
                    break;
                case "G#":
                    sb.append(mappings.getG_sharp());
                    break;
                case "G":
                    sb.append(mappings.getG());
                    break;
                case "F#":
                    sb.append(mappings.getF_sharp());
                    break;
                case "F":
                    sb.append(mappings.getF());
                    break;
                case "E♭":
                    sb.append(mappings.getE_flat());
                    break;
                case "Eb":
                    sb.append(mappings.getE_flat());
                    break;
                case "E":
                    sb.append(mappings.getE());
                    break;
                case "D":
                    sb.append(mappings.getD());
                    break;
                case "C#":
                    sb.append(mappings.getC_sharp());
                    break;
                case "C":
                    sb.append(mappings.getC());
                    break;
                default:
                    sb.append("<font color=\"FF0000\">");
                    sb.append(note);
                    sb.append("</font>");
            }

            sb.append(" ");
        }

        return sb.toString();
    }
}
