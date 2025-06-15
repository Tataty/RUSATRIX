package com.example.testdrawsurface;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CustomView extends View {

    UdpAsset udpAsset;
    boolean isAutoSend = true;

    public CustomView(Context context) {
        super(context);

        init(null);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }
    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    Runnable runnable;

    private void init(@Nullable AttributeSet attrs){

        buttons = new Paint[SIZE_LEDS][SIZE_LEDS];
        for(int x = 0; x < SIZE_LEDS; x++) {
            for(int y = 0; y < SIZE_LEDS; y++) {
                buttons[x][y] = new Paint();
            }
        }

        draw_color = new Paint();

        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
    }

    int SIZE_LEDS = 20;
    int SIZE_PIXEL = 30;

    Paint draw_color;
    Button pipetButton;

    Paint[][] buttons;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.rgb(100,100,100));
        if(canvas.getWidth() > canvas.getHeight()){
            SIZE_PIXEL = canvas.getHeight() / SIZE_LEDS;
            //deltax = (canvas.getWidth() - SIZE_PIXEL * SIZE_LEDS) / 2;
        }else{
            SIZE_PIXEL = canvas.getWidth() / SIZE_LEDS;
        }

        for(int y = 0; y < SIZE_LEDS; y++) {
            for (int x = 0; x < SIZE_LEDS; x++) {
                canvas.drawRect(x * SIZE_PIXEL, y * SIZE_PIXEL, x * SIZE_PIXEL + SIZE_PIXEL, y * SIZE_PIXEL + SIZE_PIXEL, buttons[x][y]);
            }
        }
        //getHandler().postDelayed(runnable, 100);
    }

    int last_posx = -1;
    int last_posy = -1;
    boolean isDraw_fill = false;

    boolean isPipet = false;

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = MotionEventCompat.getActionMasked(event);
        int posx = (int)event.getX();// - deltax;
        int posy = (int)event.getY();// - deltax;
        int new_posx = (posx - posx % SIZE_PIXEL) / SIZE_PIXEL;
        int new_posy = (posy - posy % SIZE_PIXEL) / SIZE_PIXEL;

        if(new_posx >= SIZE_LEDS || new_posx < 0 || new_posy >= SIZE_LEDS || new_posy < 0) return true;

        if (action == MotionEvent.ACTION_DOWN) {

            if (isDraw_fill) return true;
            if (isPipet) return true;

            if(buttons[new_posx][new_posy].getColor() != draw_color.getColor()){
                getHandler().post(runnable);
            }

            buttons[new_posx][new_posy].setColor(draw_color.getColor());
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (isPipet) {
                pipetButton.setBackgroundColor(buttons[new_posx][new_posy].getColor());
                return true;
            }
            //getHandler().postDelayed(runnable, 50);
            if (isDraw_fill) return true;

            buttons[new_posx][new_posy].setColor(draw_color.getColor());

            if(last_posx != new_posx || last_posy != new_posy) {
                getHandler().post(runnable);
            }
            last_posx = new_posx;
            last_posy = new_posy;
        }
        if (action == MotionEvent.ACTION_UP) {
            if (isPipet){
                draw_color.setColor(buttons[new_posx][new_posy].getColor());
                pipetButton.setBackgroundColor(buttons[new_posx][new_posy].getColor());
                isPipet = false;
                return true;
            }
            if (isDraw_fill){
                boolean IsFree[][] = new boolean[SIZE_LEDS][SIZE_LEDS];
                for(int x = 0; x < SIZE_LEDS; x++) {
                    for(int y = 0; y < SIZE_LEDS; y++) {
                        IsFree[x][y] = true;
                    }
                }
                List<Integer> possx = new ArrayList<Integer>();
                List<Integer> possy = new ArrayList<Integer>();
                //Adding elements in the List
                possx.add(new_posx);
                possy.add(new_posy);
                IsFree[new_posx][new_posy] = false;

                while(possx.size() > 0){
                    int px = possx.get(0);
                    possx.remove(0);
                    int py = possy.get(0);
                    possy.remove(0);
                    if(px >= 0 && px < SIZE_LEDS && py - 1 >= 0 && py - 1 < SIZE_LEDS && IsFree[px][py - 1] &&
                            buttons[px][py - 1].getColor() == buttons[new_posx][new_posy].getColor()){
                        IsFree[px][py - 1] = false;
                        possx.add(px);
                        possy.add(py - 1);
                        buttons[px][py - 1].setColor(draw_color.getColor());
                    }
                    if(px >= 0 && px < SIZE_LEDS && py + 1 >= 0 && py + 1 < SIZE_LEDS && IsFree[px][py + 1] &&
                            buttons[px][py + 1].getColor() == buttons[new_posx][new_posy].getColor()){
                        IsFree[px][py + 1] = false;
                        possx.add(px);
                        possy.add(py + 1);
                        buttons[px][py + 1].setColor(draw_color.getColor());
                    }
                    if(px - 1 >= 0 && px - 1 < SIZE_LEDS && py >= 0 && py < SIZE_LEDS && IsFree[px - 1][py] &&
                            buttons[px - 1][py].getColor() == buttons[new_posx][new_posy].getColor()){
                        IsFree[px - 1][py] = false;
                        possx.add(px - 1);
                        possy.add(py);
                        buttons[px - 1][py].setColor(draw_color.getColor());
                    }
                    if(px + 1 >= 0 && px + 1 < SIZE_LEDS && py >= 0 && py < SIZE_LEDS && IsFree[px + 1][py] &&
                            buttons[px + 1][py].getColor() == buttons[new_posx][new_posy].getColor()){
                        IsFree[px + 1][py] = false;
                        possx.add(px + 1);
                        possy.add(py);
                        buttons[px + 1][py].setColor(draw_color.getColor());
                    }
                }
                buttons[new_posx][new_posy].setColor(draw_color.getColor());
                getHandler().post(runnable);
            }
            if(udpAsset != null && isAutoSend) { PixelsMessageSend(); }
        }
        return true;
    }
    public void ClearButtons(){
        for(int y = 0; y < SIZE_LEDS; y++) {
            for (int x = 0; x < SIZE_LEDS; x++) {
                buttons[x][y].setColor(Color.rgb(0,0,0));
            }
        }
        getHandler().post(runnable);
    }
    public void PixelsSend(){
        int index = 0;
        byte[] pixels_message = new byte[1200];
        for(int y = 0; y < SIZE_LEDS; y++) {
            for (int x = 0; x < SIZE_LEDS; x++) {
                pixels_message[index++] = (byte)(Color.red(buttons[x][y].getColor()) & 0xFF);
                pixels_message[index++] = (byte)(Color.green(buttons[x][y].getColor()) & 0xFF);
                pixels_message[index++] = (byte)(Color.blue(buttons[x][y].getColor()) & 0xFF);
            }
        }

        udpAsset.Client.SendMessage(udpAsset.ip_address, pixels_message);
    }
    public void PixelsMessageSend(){
        int index = 0;
        byte[] pixels_message = new byte[1200];
        for(int y = 0; y < SIZE_LEDS; y += 2) {
            for (int x = 0; x < SIZE_LEDS; x++) {
                pixels_message[index++] = (byte)(Color.red(buttons[x][y].getColor()) & 0xFF);
                pixels_message[index++] = (byte)(Color.green(buttons[x][y].getColor()) & 0xFF);
                pixels_message[index++] = (byte)(Color.blue(buttons[x][y].getColor()) & 0xFF);
            }
            for (int x = SIZE_LEDS - 1; x >= 0; x--) {
                pixels_message[index++] = (byte)(Color.red(buttons[x][y + 1].getColor()) & 0xFF);
                pixels_message[index++] = (byte)(Color.green(buttons[x][y + 1].getColor()) & 0xFF);
                pixels_message[index++] = (byte)(Color.blue(buttons[x][y + 1].getColor()) & 0xFF);
            }
        }

        udpAsset.Client.SendMessage(udpAsset.ip_address, pixels_message);
    }
}
