package com.baldware.gesangstraining.Utils;

/**
 * A helper class able to convert frequency values into note representations
 */
public class NoteHandler {

    // Flags
    private static final double LOWEST_C = 16.35; // The frequency of a very low C (inaudible for humans, not on the piano)

    /**
     * Calculates the (nearest) note for a certain frequency
     *
     * @param _frequency The frequency of the note to be calculated
     * @return The (nearest) note for _frequency
     */
    public static String getNote(float _frequency) {
        if (_frequency < LOWEST_C) {
            return "Unable to calculate note!";
        }

        double semiOffsetFromC = Math.round((Math.log(_frequency / LOWEST_C)) / Math.log(Math.pow(2.0, (1.0 / 12.0)))); // Calculate semitone offset from C at 16.35Hz
        int semiOffset = (int) (semiOffsetFromC % 12.0); // Scale offset down to a max of one octave
        int octaveOffset = (int) (semiOffsetFromC / 12.0);

        switch (semiOffset) {
            case 0:
                return "C  (" + octaveOffset + " octaves)";
            case 1:
                return "C# (" + octaveOffset + " octaves)";
            case 2:
                return "D  (" + octaveOffset + " octaves)";
            case 3:
                return "D# (" + octaveOffset + " octaves)";
            case 4:
                return "E  (" + octaveOffset + " octaves)";
            case 5:
                return "F  (" + octaveOffset + " octaves)";
            case 6:
                return "F# (" + octaveOffset + " octaves)";
            case 7:
                return "G  (" + octaveOffset + " octaves)";
            case 8:
                return "G# (" + octaveOffset + " octaves)";
            case 9:
                return "A  (" + octaveOffset + " octaves)";
            case 10:
                return "A# (" + octaveOffset + " octaves)";
            case 11:
                return "H  (" + octaveOffset + " octaves)";
            default:
                return "Unable to calculate note!";
        }
    }

    /**
     * Calculates the frequency of the (nearest) note for a certain frequency
     *
     * @param _frequency The frequency of the note to be calculated
     * @return The frequency of the (nearest) note for _frequency
     */
    public static float getNearestNoteFrequency(float _frequency) {
        if (_frequency < LOWEST_C) {
            return -1f;
        }

        double semiOffsetFromC = Math.round((Math.log(_frequency / LOWEST_C)) / Math.log(Math.pow(2.0, (1.0 / 12.0)))); // Calculate semitone offset from C at 16.35Hz

        return (float) (LOWEST_C * Math.pow(Math.pow(2.0, (1.0 / 12.0)), semiOffsetFromC));
    }
}
