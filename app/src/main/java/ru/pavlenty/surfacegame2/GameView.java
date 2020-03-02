package ru.pavlenty.surfacegame2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class GameView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    private Player player;
    private Enemy enemy;
    private Enemy enemy2;
    private boolean boom;
    private boolean boom2;
    private Bitmap bitmap;
    private Friend friend;
    //private Friend friend2;
    private boolean goodboom;
    //private boolean goodboom2;

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private ArrayList<Star> stars = new ArrayList<Star>();

    int screenX;
    int countMisses;

    boolean flag ;


    private boolean isGameOver;


    int score;


    int highScore[] = new int[4];


    SharedPreferences sharedPreferences;

    static MediaPlayer gameOnsound;
    final MediaPlayer killedEnemysound;
    final MediaPlayer gameOversound;

    Context context;

    public GameView(Context context, int screenX, int screenY) {
        super(context);
        player = new Player(context, screenX, screenY);
        enemy = new Enemy(context, screenX, screenY);
        friend = new Friend(context, screenX, screenY);
        enemy2 = new Enemy(context, screenX, screenY);
        //friend2 = new Friend(context, screenX, screenY);

        surfaceHolder = getHolder();
        paint = new Paint();

        int starNums = 100;
        for (int i = 0; i < starNums; i++) {
            Star s = new Star(screenX, screenY);
            stars.add(s);
        }

        this.screenX = screenX;
        countMisses = 0;
        isGameOver = false;


        score = 0;
        sharedPreferences = context.getSharedPreferences("SHAR_PREF_NAME", Context.MODE_PRIVATE);


        highScore[0] = sharedPreferences.getInt("score1", 0);
        highScore[1] = sharedPreferences.getInt("score2", 0);
        highScore[2] = sharedPreferences.getInt("score3", 0);
        highScore[3] = sharedPreferences.getInt("score4", 0);
        this.context = context;


        gameOnsound = MediaPlayer.create(context,R.raw.gameon);
        killedEnemysound = MediaPlayer.create(context,R.raw.killedenemy);
        gameOversound = MediaPlayer.create(context,R.raw.gameover);


        gameOnsound.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                player.stopBoosting();
                enemy.stopBoosting();
                friend.stopBoosting();
                enemy2.stopBoosting();
                //friend2.stopBoosting();
                break;
            case MotionEvent.ACTION_DOWN:
                player.setBoosting();
                enemy.setBoosting();
                enemy2.setBoosting();
                friend.setBoosting();
                //friend2.setBoosting();
                break;

        }

        if(isGameOver){
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                context.startActivity(new Intent(context,MainActivity.class));
            }
        }
        return true;
    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);


            paint.setColor(Color.WHITE);
            paint.setTextSize(20);

            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }


            paint.setTextSize(30);
            canvas.drawText("Очки: "+score,100,50,paint);

            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);

            canvas.drawBitmap(
                    enemy.getBitmap(),
                    enemy.getX(),
                    enemy.getY(),
                    paint
            );
            canvas.drawBitmap(
                    enemy2.getBitmap(),
                    enemy2.getX(),
                    enemy2.getY(),
                    paint
            );
            canvas.drawBitmap(
                    friend.getBitmap(),
                    friend.getX(),
                    friend.getY(),
                    paint
            );

            /*canvas.drawBitmap(
                    friend2.getBitmap(),
                    friend2.getX(),
                    friend2.getY(),
                    paint
            );
            */

            if (boom || boom2) canvas.drawBitmap(
                            bitmap,
                            player.getX(),
                            player.getY(),
                            paint

                    );

            if(isGameOver){
                gameOnsound.pause();
                gameOversound.start();
                paint.setTextSize(150);
                paint.setTextAlign(Paint.Align.CENTER);

                int yPos=(int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
                canvas.drawText("Конец игры",canvas.getWidth()/2,yPos,paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);

        }
    }


    public static void stopMusic(){
        gameOnsound.stop();
    }

    private void update() {
        score++;

        player.update();
        enemy.update();
        enemy2.update();
        friend.update();
        //friend2.update();
        boom = player.getDetectCollision().intersect(enemy.getDetectCollision());
        boom2 = (player.getDetectCollision().intersect(enemy2.getDetectCollision()));
        if (boom || boom2) booming();
        goodboom = player.getDetectCollision().intersect(friend.getDetectCollision());
        //goodboom2 = player.getDetectCollision().intersect(friend2.getDetectCollision());

        if (goodboom) goodbooming();
        if (score<0) isGameOver = true;


        for (Star s : stars) {
            s.update(player.getSpeed());
        }
    }

    private void goodbooming() {
        score+=500;
        player.setX(50);
        player.setY(75);
        friend.setcoordinats();
        //friend2.setcoordinats();
        enemy.setcoordinats();
        enemy2.setcoordinats();
    }

    private void booming(){
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.boom);
        score-=500;
        player.setX(50);
        player.setY(75);
        friend.setcoordinats();
        //friend2.setcoordinats();
        enemy.setcoordinats();
        enemy2.setcoordinats();
    }
    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }


}