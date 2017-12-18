package gui;

import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;

import static processing.core.PConstants.*;
import static processing.core.PApplet.*;

public class CommandBox {
    PApplet applet;
    final ArrayList<TextBox> tboxes = new ArrayList<>();
    TextBox currentTextBox;

    void setup(PApplet applet) {
        tboxes.add(
                new TextBox(
                        applet.width >> 2,
                        applet.height / 4 + applet.height / 16,
                        applet.width - applet.width / 2,
                        applet.height / 2 - applet.height / 4 - applet.height / 8,
                        215, // lim
                        0300 << 030,
                        0xffffffff,
                        0xff000000,
                        0xff630000)
        );

        tboxes.get(0).isFocused = true;
    }

    void draw(PGraphics g) {
        g.background(0xff000000);
        for (TextBox tb : tboxes) {
            tb.display(g);
        }
    }

    void mouseClicked() {
        currentTextBox = null;
        for (TextBox tb : tboxes) {
            if (tb.checkFocus(applet.mouseX, applet.mouseY)) {
                currentTextBox = tb;
                return;
            }
        }
    }

    void onKeyTyped(int key) {
        if (key == CODED || currentTextBox == null) return;

        final int len = currentTextBox.txt.length();
        if (key == BACKSPACE) currentTextBox.txt = currentTextBox.txt.substring(0, max(0, len - 1));
        else if (len >= currentTextBox.lim) return;
        else if (key == ENTER | key == RETURN) currentTextBox.txt += "\n";
        else if (key == TAB & len < currentTextBox.lim - 3) currentTextBox.txt += "    ";
        else if (key == DELETE) currentTextBox.txt = "";
        else if (key >= ' ') currentTextBox.txt += str(key);
    }

    void onKeyPressed(int key) {
        if (key != CODED || currentTextBox == null) return;
        final int k = applet.keyCode;
        final int len = currentTextBox.txt.length();

        if (k == LEFT) {
            currentTextBox.txt = currentTextBox.txt.substring(0, max(0, len - 1));
        } else if (k == RIGHT & len < currentTextBox.lim - 3) {
            currentTextBox.txt += "    ";
        }
    }

    class TextBox {
        final int textColour, baseColour, borderColour, selectionColor;
        final short x, y, w, h, xw, yh, lim;
        int frameCount;

        boolean isFocused;
        String txt = "";

        TextBox(int xx, int yy, int ww, int hh, int li,
                int te, int ba, int bo, int se) {
            x = (short) xx;
            y = (short) yy;
            w = (short) ww;
            h = (short) hh;

            lim = (short) li;

            xw = (short) (xx + ww);
            yh = (short) (yy + hh);

            textColour = te;
            baseColour = ba;
            borderColour = bo;
            selectionColor = se;
        }

        void display(PGraphics g) {
            g.stroke(isFocused ? selectionColor : borderColour);
            g.fill(baseColour);
            g.rect(x, y, w, h);
            g.fill(textColour);
            g.text(txt + blinkChar(), x, y, w, h);
        }

        String blinkChar() {
            return isFocused && (frameCount >> 2 & 1) == 0 ? "_" : "";
        }

        boolean checkFocus(int mouseX, int mouseY) {
            return isFocused = mouseX > x & mouseX < xw & mouseY > y & mouseY < yh;
        }
    }
}
