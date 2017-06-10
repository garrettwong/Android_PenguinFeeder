/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.accelerometerplay;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.constants.Direction;
import com.core.database.ScoreRepository;
import com.core.score.Score;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an example of using the accelerometer to integrate the device's
 * acceleration to a position using the Verlet method. This is illustrated with
 * a very simple particle system comprised of a few iron balls freely moving on
 * an inclined wooden table. The inclination of the virtual table is controlled
 * by the device's accelerometer.
 *
 * @see SensorManager
 * @see SensorEvent
 * @see Sensor
 */

public class AccelerometerPlayActivity extends Activity {
    private BackgroundSound mBackgroundSound = new BackgroundSound();
    private SimulationView mSimulationView;
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private Display mDisplay;
    private WakeLock mWakeLock;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();

        // Create a bright wake lock
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
                .getName());

        // instantiate our simulation view and set it as the activity's content
        mSimulationView = new SimulationView(this);
        mSimulationView.setBackgroundResource(R.drawable.wood);
        setContentView(mSimulationView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * when the activity is resumed, we acquire a wake-lock so that the
         * screen stays on, since the user will likely not be fiddling with the
         * screen or buttons.
         */
        mWakeLock.acquire();

        // Start background
        mBackgroundSound.execute((Void[]) null);

        // Start the simulation
        mSimulationView.startSimulation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
         * When the activity is paused, we make sure to stop the simulation,
         * release our sensor resources and wake locks
         */

        // Stop the simulation
        mSimulationView.stopSimulation();

        mBackgroundSound.cancel(true);

        // and release our wake-lock
        mWakeLock.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackgroundSound.cancel(true);
    }

    public class SimulationView extends FrameLayout implements SensorEventListener {
        // diameter of the balls in meters
        private static final float sBallDiameter = 0.004f;
        private static final float sBallDiameter2 = sBallDiameter * sBallDiameter;

        private final int mDstWidth;
        private final int mDstHeight;

        private Sensor mAccelerometer;
        private long mLastT;

        private float mXDpi;
        private float mYDpi;
        private float mMetersToPixelsX;
        private float mMetersToPixelsY;
        private float mXOrigin;
        private float mYOrigin;
        private float mSensorX;
        private float mSensorY;
        private float mHorizontalBound;
        private float mVerticalBound;
        private final ParticleSystem mParticleSystem;
        private final CupHolder mCupHolder;
        private Score mScore;

        /*
         * Each of our particle holds its previous and current position, its
         * acceleration. for added realism each particle has its own friction
         * coefficient.
         */
        class Particle extends View {
            private float mPosX = (float) Math.random();
            private float mPosY = (float) Math.random();
            private float mVelX;
            private float mVelY;

            public Particle(Context context) {
                super(context);
            }

            public Particle(Context context, AttributeSet attrs) {
                super(context, attrs);
            }

            public Particle(Context context, AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public Particle(Context context, AttributeSet attrs, int defStyleAttr,
                            int defStyleRes) {
                super(context, attrs, defStyleAttr, defStyleRes);
            }

            public void computePhysics(float sx, float sy, float dT) {

                final float ax = -sx/5;
                final float ay = -sy/5;

                mPosX += mVelX * dT + ax * dT * dT / 2;
                mPosY += mVelY * dT + ay * dT * dT / 2;

                mVelX += ax * dT;
                mVelY += ay * dT;
            }

            /*
             * Resolving constraints and collisions with the Verlet integrator
             * can be very simple, we simply need to move a colliding or
             * constrained particle in such way that the constraint is
             * satisfied.
             */
            public void resolveCollisionWithBounds() {
                final float xmax = mHorizontalBound;
                final float ymax = mVerticalBound;
                final float x = mPosX;
                final float y = mPosY;
                if (x > xmax) {
                    mPosX = xmax;
                    mVelX = 0;
                } else if (x < -xmax) {
                    mPosX = -xmax;
                    mVelX = 0;
                }
                if (y > ymax) {
                    mPosY = ymax;
                    mVelY = 0;
                } else if (y < -ymax) {
                    mPosY = -ymax;
                    mVelY = 0;
                }
            }
        }

        /*
         * A particle system is just a collection of particles
         */
        class ParticleSystem {
            private int NUM_PARTICLES = 3;
            private List<Particle> mBallsList;
            private Particle mBalls[];
            private SoundPool sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            private int soundIds[] = new int[10];

            private ScoreRepository mScoreRepository = new ScoreRepository(getContext());
            private double mCurScore = 0.0;
            private int mCurBalls = 5;

            ParticleSystem() {
                mBallsList = new ArrayList<>();

                /*
                 * Initially our particles have no speed or acceleration
                 */
                for (int i = 0; i < NUM_PARTICLES; i++) {
                    Particle p = getRandomParticle();

                    mBallsList.add(p);

                    /// i think this is where the view is initialized for each 'ball'
                    addView(p, new ViewGroup.LayoutParams(mDstWidth, mDstHeight));
                }

                soundIds[0] = sp.load(getContext(), R.raw.swallow, 1);

                Score user = mScoreRepository.getById(1);
                if (user != null) {
                    System.out.println("Score ID User: " + user.getUserId() + " Score: " +
                            user.getValue());
                }
            }

            public Particle getRandomParticle() {
                Particle p = new Particle(getContext());
                p.setBackgroundResource(R.drawable.ball);
                p.setLayerType(LAYER_TYPE_HARDWARE, null);
                return p;
            }

            public void removeBall(Particle p) {
                removeView(p);
                mBallsList.remove(p);
            }

            public void addBall(Particle p) {
                mBallsList.add(p);

                addView(p, new ViewGroup.LayoutParams(mDstWidth, mDstHeight));
            }

            /*
             * Update the position of each particle in the system using the
             * Verlet integrator.
             */
            private void updatePositions(float sx, float sy, long timestamp) {
                final long t = timestamp;
                if (mLastT != 0) {
                    final float dT = (float) (t - mLastT) / 1000.f /** (1.0f / 1000000000.0f)*/;
                        final int count = mBallsList.size();
                        for (int i = 0; i < count; i++) {
                            Particle ball = mBallsList.get(i);
                            ball.computePhysics(sx, sy, dT);
                        }
                }
                mLastT = t;
            }

            /*
             * Performs one iteration of the simulation. First updating the
             * position of all the particles and resolving the constraints and
             * collisions.
             */
            public void update(float sx, float sy, long now) {
                // update the system's positions
                updatePositions(sx, sy, now);

                // We do no more than a limited number of iterations
                final int NUM_MAX_ITERATIONS = 10;

                /*
                 * Resolve collisions, each particle is tested against every
                 * other particle for collision. If a collision is detected the
                 * particle is moved away using a virtual spring of infinite
                 * stiffness.
                 */
                boolean more = true;
                final int count = mBallsList.size();
                for (int k = 0; k < NUM_MAX_ITERATIONS && more; k++) {
                    more = false;
                    for (int i = 0; i < count; i++) {
                        Particle curr = mBallsList.get(i);
                        for (int j = i + 1; j < count; j++) {
                            Particle ball = mBallsList.get(j);
                            float dx = ball.mPosX - curr.mPosX;
                            float dy = ball.mPosY - curr.mPosY;
                            float dd = dx * dx + dy * dy;
                            // Check for collisions
                            if (dd <= sBallDiameter2) {
                                /*
                                 * add a little bit of entropy, after nothing is
                                 * perfect in the universe.
                                 */
                                dx += ((float) Math.random() - 0.5f) * 0.0001f;
                                dy += ((float) Math.random() - 0.5f) * 0.0001f;
                                dd = dx * dx + dy * dy;
                                // simulate the spring
                                final float d = (float) Math.sqrt(dd);
                                final float c = (0.5f * (sBallDiameter - d)) / d;
                                final float effectX = dx * c;
                                final float effectY = dy * c;
                                curr.mPosX -= effectX;
                                curr.mPosY -= effectY;
                                ball.mPosX += effectX;
                                ball.mPosY += effectY;
                                more = true;
                            }
                        }
                        curr.resolveCollisionWithBounds();
                    }
                }
            }

            public void detectCollisions(float x, int xHeight, float y, int yHeight) {

//                AudioAttributes attrs = new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_GAME)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                        .build();
//                SoundPool sp = new SoundPool.Builder()
//                        .setMaxStreams(10)
//                        .setAudioAttributes(attrs)
//                        .build();

                for (int i = 0; i < this.mBallsList.size(); i++) {
                    Particle curBall = this.mBallsList.get(i);

                    if (x > curBall.getX() && x < curBall.getX() + xHeight &&
                            y > curBall.getY() && y < curBall.getY() + yHeight) {

                        if (this.mBallsList.size() == 1) {
                            if (mCurBalls < 15) {
                                mCurBalls = (int) (mCurBalls * 1.2);
                            }

                            for (int j = 0; j < mCurBalls; j++) {
                                addBall(mParticleSystem.getRandomParticle());
                            }

                            continue;
                        }

                        sp.play(soundIds[0], 1, 1, 1, 0, 1.0f);
                        removeBall(curBall);
                        i--;

                        // increment pts
                        mCurScore+= 0.5;
                        mScore = new Score(1, mCurScore);

                        // save mScore
                        System.out.println("Collision: " + mScore);
                        mScoreRepository.add(mScore);

                        // re-render

                    }
                }
            }

            public int getParticleCount() {
                return mBallsList.size();
            }

            public float getPosX(int i) {
                return mBallsList.get(i).mPosX;
            }

            public float getPosY(int i) {
                return mBallsList.get(i).mPosY;
            }
        }

        public void startSimulation() {
            /*
             * It is not necessary to get accelerometer events at a very high
             * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
             * automatic low-pass filter, which "extracts" the gravity component
             * of the acceleration. As an added benefit, we use less power and
             * CPU resources.
             */
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        public void stopSimulation() {
            mSensorManager.unregisterListener(this);
        }

        public SimulationView(Context context) {
            super(context);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            mXDpi = metrics.xdpi;
            mYDpi = metrics.ydpi;
            mMetersToPixelsX = mXDpi / 0.0254f;
            mMetersToPixelsY = mYDpi / 0.0254f;

            // rescale the ball so it's about 0.5 cm on screen
            mDstWidth = (int) (sBallDiameter * mMetersToPixelsX + 0.5f);
            mDstHeight = (int) (sBallDiameter * mMetersToPixelsY + 0.5f);
            mParticleSystem = new ParticleSystem();
            mCupHolder = new CupHolder(this, getContext());

            Options opts = new Options();
            opts.inDither = true;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            // compute the origin of the screen relative to the origin of
            // the bitmap
            mXOrigin = (w - mDstWidth) * 0.5f;
            mYOrigin = (h - mDstHeight) * 0.5f;
            mHorizontalBound = ((w / mMetersToPixelsX - sBallDiameter) * 0.5f);
            mVerticalBound = ((h / mMetersToPixelsY - sBallDiameter) * 0.5f);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                return;
            /*
             * record the accelerometer data, the event's timestamp as well as
             * the current time. The latter is needed so we can calculate the
             * "present" time during rendering. In this application, we need to
             * take into account how the screen is rotated with respect to the
             * sensors (which always return data in a coordinate space aligned
             * to with the screen in its native orientation).
             */

            switch (mDisplay.getRotation()) {
                case Surface.ROTATION_0:
                    mSensorX = event.values[0];
                    mSensorY = event.values[1];
                    break;
                case Surface.ROTATION_90:
                    mSensorX = -event.values[1];
                    mSensorY = event.values[0];
                    break;
                case Surface.ROTATION_180:
                    mSensorX = -event.values[0];
                    mSensorY = -event.values[1];
                    break;
                case Surface.ROTATION_270:
                    mSensorX = event.values[1];
                    mSensorY = -event.values[0];
                    break;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            /*
             * Compute the new position of our object, based on accelerometer
             * data and present time.
             */
            final ParticleSystem particleSystem = mParticleSystem;
            final long now = System.currentTimeMillis();
            final float sx = mSensorX;
            final float sy = mSensorY;

            // updates
            particleSystem.update(sx, sy, now);
            particleSystem.detectCollisions(mCupHolder.getCup().getX(), mCupHolder.getCup().getWidth(),
                    mCupHolder.getCup().getY(),
                    mCupHolder.getCup().getHeight());

            final float xc = mXOrigin;
            final float yc = mYOrigin;
            final float xs = mMetersToPixelsX;
            final float ys = mMetersToPixelsY;
            final int count = particleSystem.getParticleCount();
            for (int i = 0; i < count; i++) {
                /*
                 * We transform the canvas so that the coordinate system matches
                 * the sensors coordinate system with the origin in the center
                 * of the screen and the unit is the meter.
                 */
                try {
                    final float x = xc + particleSystem.getPosX(i) * xs;
                    final float y = yc - particleSystem.getPosY(i) * ys;
                    particleSystem.mBallsList.get(i).setTranslationX(x);
                    particleSystem.mBallsList.get(i).setTranslationY(y);

                    // random ball test
//                    if (x < 300 && y > 1600 && particleSystem.getParticleCount() < 10) {
//                        particleSystem.addBall(particleSystem.getRandomParticle());
//                    }
//                    if (x > 700 && y > 1600 && particleSystem.getParticleCount() > 3) {
//                        particleSystem.removeBall(particleSystem.mBallsList.get(0));
//                    }

                    // also update the cupHolder
                    float cupX = mCupHolder.getCup().getX();
                    float cupY = mCupHolder.getCup().getY();
                    float newX;

                    if (cupX > 750 && cupY < 150) {
                        mCupHolder.setDirection(Direction.LEFT);
                    } else if(cupX < 150 && cupY < 150) {
                        mCupHolder.setDirection(Direction.DOWN);
                    } else if (cupX < 150 && cupY > 750) {
                        mCupHolder.setDirection(Direction.RIGHT);
                    } else if (cupX > 750 && cupY > 750) {
                        mCupHolder.setDirection(Direction.UP);
                    }

                    float changeX = 0.0f;
                    float changeY = 0.0f;
                    switch(mCupHolder.getDirection()) {
                        case RIGHT:
                            changeX = 0.5f;
                            break;
                        case LEFT:
                            changeX = -0.5f;
                            break;
                        case UP:
                            changeY = -0.5f;
                            break;
                        case DOWN:
                            changeY = 0.5f;
                            break;
                    }

                    mCupHolder.getCup().setTranslationX(cupX + changeX);
                    mCupHolder.getCup().setTranslationY(cupY + changeY);
                } catch(Exception e) {}
            }


            // and make sure to redraw asap
            invalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    // background sound loop
    public class BackgroundSound extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            MediaPlayer player = MediaPlayer.create(AccelerometerPlayActivity.this, R.raw.background);
            player.setLooping(true);
            player.setVolume(1.0f, 1.0f);
            player.start();

            return null;
        }
    }
}
