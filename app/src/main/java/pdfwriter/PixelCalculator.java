package pdfwriter;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;


public class PixelCalculator {


    private static final int DEFAULT_TEXT_SIZE = FontSize.FONT_12;


    public static int getPixelWidth(String text) {
        return calcPixelWidth(text, DEFAULT_TEXT_SIZE);
    }

    public static int getPixelWidth(String text, int textSize) {
        return calcPixelWidth(text, textSize);
    }

    public static int getPixelHeight(String text) {
        return calcPixelHeight(text, DEFAULT_TEXT_SIZE);
    }

    public static int getPixelHeight(String text, int textSize) {
        return calcPixelHeight(text, textSize);
    }

    private static Rect createRectFromString(String str, int textSize) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        Rect result = new Rect();
        paint.getTextBounds(str, 0, str.length(), result);
        return result;
    }

    private static int calcPixelWidth(String str, int textSize) {
        Rect result = createRectFromString(str, textSize);
        Log.i("Text dimensions", "Width: " + result.width());
        return result.width();
    }

    private static int calcPixelHeight(String str, int textSize) {
        Rect result = createRectFromString(str, textSize);
        Log.i("Text dimensions", "Height: " + result.height());
        return result.height();
    }

}
