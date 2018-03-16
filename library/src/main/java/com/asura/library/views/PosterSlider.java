package com.asura.library.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.support.v4.view.ViewPager;

import com.asura.library.R;
import com.asura.library.events.IVideoPlayListener;
import com.asura.library.events.OnPosterClickListener;
import com.asura.library.posters.Poster;
import com.asura.library.views.fragments.PosterFragment;
import com.asura.library.views.indicators.IndicatorShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yaminilohitha on 14-03-2018.
 */

public class PosterSlider extends FrameLayout implements ViewPager.OnPageChangeListener,
        IAttributeChange, IVideoPlayListener {

    private final String TAG = "PosterSlider";

    private List<Poster> posters = new ArrayList<>();

    private AppCompatActivity hostActivity;
    private CustomViewPager viewPager;

    //CustomAttributes

    private Drawable selectedSlideIndicator;
    private Drawable unSelectedSlideIndicator;
    private int defaultIndicator;
    private int indicatorSize;
    private boolean mustAnimateIndicators;
    private boolean mustLoopSlides;
    private int defaultPoster = 0;
    private int imageSlideInterval = 5000;

    private boolean hideIndicators = false;

    private boolean mustWrapContent;

    private SlideIndicatorsGroup slideIndicatorsGroup;

    private static HandlerThread handlerThread;
    static{
        handlerThread = new HandlerThread("TimerThread");
        handlerThread.start();
    }
    private Handler handler = new Handler(handlerThread.getLooper());

    private boolean setupIsCalled = false;
    List<Poster> posterQueue = new ArrayList<>();

    private OnPosterClickListener onPosterClickListener;

    private Timer timer;

    private PosterAdapter posterAdapter;

    private boolean videoStartedinAutoLoop = false;

    @LayoutRes
    private int emptyView;


    public PosterSlider(@NonNull Context context) {
        super(context);
    }

    public PosterSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseCustomAttributes(attrs);
    }

    public PosterSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseCustomAttributes(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PosterSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        parseCustomAttributes(attrs);
    }

    private void parseCustomAttributes(AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.PosterSlider);
            try {
                selectedSlideIndicator = typedArray.getDrawable(R.styleable.PosterSlider_selectedSlideIndicator);
                unSelectedSlideIndicator = typedArray.getDrawable(R.styleable.PosterSlider_unSelectedSlideIndicator);
                defaultIndicator = typedArray.getInteger(R.styleable.PosterSlider_defaultIndicator, IndicatorShape.DASH);
                indicatorSize = typedArray.getDimensionPixelSize(R.styleable.PosterSlider_indicatorSize, getResources().getDimensionPixelSize(R.dimen.default_indicator_size));
                mustAnimateIndicators = typedArray.getBoolean(R.styleable.PosterSlider_animateIndicators, true);
                mustLoopSlides = typedArray.getBoolean(R.styleable.PosterSlider_loopSlides, false);
                defaultPoster = typedArray.getInt(R.styleable.PosterSlider_defaultPoster, 0);
                imageSlideInterval = typedArray.getInt(R.styleable.PosterSlider_imageSlideInterval, 0);
                hideIndicators = typedArray.getBoolean(R.styleable.PosterSlider_hideIndicators, false);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                typedArray.recycle();
            }
        }
        if (!isInEditMode()) {
            setup();
        }
    }

    private void setup() {
        if (!isInEditMode()) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (getContext() instanceof AppCompatActivity) {
                        hostActivity = (AppCompatActivity) getContext();
                    } else {
                        throw new RuntimeException("Host activity must extend AppCompatActivity");
                    }
                    boolean mustMakeViewPagerWrapContent = getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;

                    viewPager = new CustomViewPager(getContext(), mustMakeViewPagerWrapContent);
                    viewPager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        viewPager.setId(View.generateViewId());
                    } else {
                        int id = Math.abs(new Random().nextInt((5000 - 1000) + 1) + 1000);
                        viewPager.setId(id);
                    }
                    viewPager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    viewPager.addOnPageChangeListener(PosterSlider.this);
                    addView(viewPager);
                    slideIndicatorsGroup = new SlideIndicatorsGroup(getContext(), selectedSlideIndicator, unSelectedSlideIndicator, defaultIndicator, indicatorSize, mustAnimateIndicators);
                    if (!hideIndicators) {
                        addView(slideIndicatorsGroup);
                    }

                    setupTimer();
                    setupIsCalled = true;
                    renderRemainingPosters();
                }
            });
        }

    }

    private void renderRemainingPosters() {
        setPosters(posterQueue);
    }

    public void setPosters(List<Poster> posters) {
        if (setupIsCalled) {
            this.posters = posters;

            for (int i = 0; i < posters.size(); i++) {
                posters.get(i).setPosition(i);
                posters.get(i).setOnPosterClickListener(onPosterClickListener);
                posters.get(i).setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            stopTimer();
                        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            setupTimer();
                        }
                        return false;
                    }
                });
                slideIndicatorsGroup.onSlideAdd();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                posterAdapter = new PosterAdapter(hostActivity.getSupportFragmentManager(), mustLoopSlides, getLayoutDirection(), posters);
            } else {
                posterAdapter = new PosterAdapter(hostActivity.getSupportFragmentManager(), mustLoopSlides, posters);
            }
            posterAdapter.setVideoPlayListener(this);

            viewPager.setAdapter(posterAdapter);

            if (mustLoopSlides) {
                if (Build.VERSION.SDK_INT >= 17) {
                    if (getLayoutDirection() == LAYOUT_DIRECTION_LTR) {
                        viewPager.setCurrentItem(1, false);
                        slideIndicatorsGroup.onSlideChange(0);
                    } else {
                        viewPager.setCurrentItem(posters.size(), false);
                        slideIndicatorsGroup.onSlideChange(posters.size() - 1);
                    }
                } else {
                    viewPager.setCurrentItem(posters.size(), false);
                    slideIndicatorsGroup.onSlideChange(posters.size() - 1);
                }
            }
        } else {
            posterQueue.addAll(posters);
        }


    }

    private void setupTimer() {
        if (imageSlideInterval > 0&&mustLoopSlides) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ((AppCompatActivity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!mustLoopSlides) {
                                if (viewPager.getCurrentItem() == posters.size() - 1) {
                                    viewPager.setCurrentItem(0, true);
                                } else {
                                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                                }
                            } else {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    if (getLayoutDirection() == LAYOUT_DIRECTION_LTR) {
                                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                                    } else {
                                        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                                    }
                                } else {
                                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                                }
                            }
                        }
                    });
                }
            }, imageSlideInterval, imageSlideInterval);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }


    @Override
    public void onPageSelected(int position) {
        if (mustLoopSlides) {
            if (position == 0) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(posters.size(), false);
                    }
                }, 400);
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup.onSlideChange(posters.size() - 1);
                }
            } else if (position == posters.size() + 1) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(1, false);
                    }
                }, 400);
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup.onSlideChange(0);
                }
            } else {
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup.onSlideChange(position - 1);
                }
            }
        } else {
            slideIndicatorsGroup.onSlideChange(position);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_DRAGGING:
                stopTimer();
                break;
            case ViewPager.SCROLL_STATE_IDLE:
                if (timer == null && !videoStartedinAutoLoop) {
                    setupTimer();
                }

                break;
        }
    }


    public void setOnPosterClickListener(OnPosterClickListener onPosterClickListener) {
        this.onPosterClickListener = onPosterClickListener;
        for (Poster poster : posters) {
            poster.setOnPosterClickListener(onPosterClickListener);
        }
    }

    public void setDefaultIndicator(final int indicator) {
        post(new Runnable() {
            @Override
            public void run() {
                defaultIndicator = indicator;
                slideIndicatorsGroup.changeIndicator(indicator);
                if (mustLoopSlides) {
                    if (viewPager.getCurrentItem() == 0) {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                viewPager.setCurrentItem(posters.size(), false);
                            }
                        }, 400);
                        if (slideIndicatorsGroup != null) {
                            slideIndicatorsGroup.onSlideChange(posters.size() - 1);
                        }
                    } else if (viewPager.getCurrentItem() == posters.size() + 1) {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                viewPager.setCurrentItem(1, false);
                            }
                        }, 400);
                        if (slideIndicatorsGroup != null) {
                            slideIndicatorsGroup.onSlideChange(0);
                        }
                    } else {
                        if (slideIndicatorsGroup != null) {
                            slideIndicatorsGroup.onSlideChange(viewPager.getCurrentItem() - 1);
                        }
                    }
                } else {
                    slideIndicatorsGroup.onSlideChange(viewPager.getCurrentItem());
                }
            }
        });
    }

    public void setCustomIndicator(Drawable selectedSlideIndicator, Drawable unSelectedSlideIndicator) {
        this.selectedSlideIndicator = selectedSlideIndicator;
        this.unSelectedSlideIndicator = unSelectedSlideIndicator;
        slideIndicatorsGroup.changeIndicator(selectedSlideIndicator, unSelectedSlideIndicator);
        if (mustLoopSlides) {
            if (viewPager.getCurrentItem() == 0) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(posters.size(), false);
                    }
                }, 400);
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup.onSlideChange(posters.size() - 1);
                }
            } else if (viewPager.getCurrentItem() == posters.size() + 1) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(1, false);
                    }
                }, 400);
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup.onSlideChange(0);
                }
            } else {
                if (slideIndicatorsGroup != null) {
                    slideIndicatorsGroup.onSlideChange(viewPager.getCurrentItem() - 1);
                }
            }
        } else {
            slideIndicatorsGroup.onSlideChange(viewPager.getCurrentItem());
        }
    }

    public void setCurrentSlide(final int position) {
        post(new Runnable() {
            @Override
            public void run() {
                if (viewPager != null) {
                    viewPager.setCurrentItem(position);
                }
            }
        });
    }

    public void setInterval(int interval) {
        this.imageSlideInterval = interval;
        onIntervalChange();
    }

    public void setIndicatorSize(int indicatorSize) {
        this.indicatorSize = indicatorSize;
        onIndicatorSizeChange();
    }

    public void setLoopSlides(boolean loopSlides) {
        this.mustLoopSlides = loopSlides;
    }

    public void setMustAnimateIndicators(boolean mustAnimateIndicators) {
        this.mustAnimateIndicators = mustAnimateIndicators;
        onAnimateIndicatorsChange();
    }

    public void setHideIndicators(boolean hideIndicators) {
        this.hideIndicators = hideIndicators;
        onHideIndicatorsValueChanged();
    }

    public int getCurrentSlidePosition() {
        if (viewPager == null)
            return -1;
        return viewPager.getCurrentItem();
    }

    public boolean getMustLoopSlides(){
        return mustLoopSlides;
    }
    // Events
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onIndicatorSizeChange() {
        if (!hideIndicators) {
            if (slideIndicatorsGroup != null) {
                removeView(slideIndicatorsGroup);
            }
            slideIndicatorsGroup = new SlideIndicatorsGroup(getContext(), selectedSlideIndicator, unSelectedSlideIndicator, defaultIndicator, indicatorSize, mustAnimateIndicators);
            addView(slideIndicatorsGroup);
            for (int i = 0; i < posters.size(); i++) {
                slideIndicatorsGroup.onSlideAdd();
            }
        }
    }

    @Override
    public void onSelectedSlideIndicatorChange() {

    }

    @Override
    public void onUnselectedSlideIndicatorChange() {

    }

    @Override
    public void onDefaultIndicatorsChange() {

    }

    @Override
    public void onAnimateIndicatorsChange() {
        if (slideIndicatorsGroup != null) {
            slideIndicatorsGroup.setMustAnimateIndicators(mustAnimateIndicators);
        }
    }

    @Override
    public void onIntervalChange() {
        if (handler != null) {
            stopTimer();
        }
        setupTimer();
    }

    @Override
    public void onLoopSlidesChange() {

    }

    @Override
    public void onDefaultBannerChange() {

    }

    @Override
    public void onEmptyViewChange() {

    }

    @Override
    public void onHideIndicatorsValueChanged() {
        if (slideIndicatorsGroup != null) {
            removeView(slideIndicatorsGroup);
        }
        if (!hideIndicators) {
            slideIndicatorsGroup = new SlideIndicatorsGroup(getContext(), selectedSlideIndicator, unSelectedSlideIndicator, defaultIndicator, indicatorSize, mustAnimateIndicators);
            addView(slideIndicatorsGroup);
            for (int i = 0; i < posters.size(); i++) {
                slideIndicatorsGroup.onSlideAdd();
            }
        }
    }

    public void removeAllPosters(){
        this.posters.clear();
        this.slideIndicatorsGroup.removeAllViews();
        this.slideIndicatorsGroup.setSlides(0);
        invalidate();
        requestLayout();
    }


    @Override
    public void onVideoStarted() {
        videoStartedinAutoLoop = true;
        stopTimer();
    }

    @Override
    public void onVideoStopped() {
        setupTimerWithNoDelay();
        videoStartedinAutoLoop = false;
    }

    private void setupTimerWithNoDelay() {
        if (imageSlideInterval > 0) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ((AppCompatActivity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!mustLoopSlides) {
                                if (viewPager.getCurrentItem() == posters.size() - 1) {
                                    viewPager.setCurrentItem(0, true);
                                } else {
                                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                                }
                            } else {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    if (getLayoutDirection() == LAYOUT_DIRECTION_LTR) {
                                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                                    } else {
                                        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                                    }
                                } else {
                                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                                }
                            }
                        }
                    });
                }
            }, 0, imageSlideInterval);
        }
    }
}
