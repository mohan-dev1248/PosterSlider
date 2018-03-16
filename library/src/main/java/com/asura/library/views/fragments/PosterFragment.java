package com.asura.library.views.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asura.library.R;
import com.asura.library.events.IVideoPlayListener;
import com.asura.library.events.OnPosterClickListener;
import com.asura.library.posters.BitmapImage;
import com.asura.library.posters.DrawableImage;
import com.asura.library.posters.ImagePoster;
import com.asura.library.posters.Poster;
import com.asura.library.posters.RawVideo;
import com.asura.library.posters.RemoteImage;
import com.asura.library.posters.RemoteVideo;
import com.asura.library.posters.VideoPoster;
import com.asura.library.views.AdjustableImageView;
import com.asura.library.views.PosterSlider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;

import static com.google.android.exoplayer2.Player.STATE_ENDED;

public class PosterFragment extends Fragment implements Player.EventListener{

    private Poster poster;

    private IVideoPlayListener videoPlayListener;

    private SimpleExoPlayer player;
    private boolean isLooping;

    public PosterFragment() {
        // Required empty public constructor
    }

    public static PosterFragment newInstance(@NonNull Poster poster, IVideoPlayListener videoPlayListener) {
        PosterFragment fragment = new PosterFragment();
        fragment.setVideoPlayListener(videoPlayListener);
        Bundle args = new Bundle();
        args.putParcelable("poster",poster);
        fragment.setArguments(args);
        return fragment;
    }

    public void setVideoPlayListener(IVideoPlayListener videoPlayListener) {
        this.videoPlayListener = videoPlayListener;
        isLooping = ((PosterSlider) videoPlayListener).getMustLoopSlides();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        poster = getArguments().getParcelable("poster");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(poster!=null){
            if(poster instanceof ImagePoster){
                final AdjustableImageView imageView = new AdjustableImageView(getActivity());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setAdjustViewBounds(true);
                ImagePoster imagePoster = (ImagePoster) poster;
                imageView.setScaleType(imagePoster.getScaleType());
                if(imagePoster instanceof DrawableImage){
                    DrawableImage image = (DrawableImage) imagePoster;
                    Glide.with(getActivity())
                            .load(image.getDrawable())
                            .into(imageView);
                }else if(imagePoster instanceof BitmapImage){
                    BitmapImage image = (BitmapImage) imagePoster;
                    Glide.with(getActivity())
                            .load(image.getBitmap())
                            .into(imageView);
                }else {
                    final RemoteImage image = (RemoteImage) imagePoster;
                    if (image.getErrorDrawable() == null && image.getPlaceHolder() == null) {
                        Glide.with(getActivity()).load(image.getUrl()).into(imageView);
                    } else {
                        if (image.getPlaceHolder() != null && image.getErrorDrawable() != null) {
                            Glide.with(getActivity())
                                    .load(image.getUrl())
                                    .apply(new RequestOptions()
                                            .placeholder(image.getPlaceHolder()))
                                    .into(imageView);
                        } else if (image.getErrorDrawable() != null) {
                            Glide.with(getActivity())
                                    .load(image.getUrl())
                                    .apply(new RequestOptions()
                                            .error(image.getErrorDrawable()))
                                    .into(imageView);
                        } else if (image.getPlaceHolder() != null) {
                            Glide.with(getActivity())
                                    .load(image.getUrl())
                                    .apply(new RequestOptions()
                                        .placeholder(image.getPlaceHolder()))
                                    .into(imageView);
                        }
                    }
                }
                imageView.setOnTouchListener(poster.getOnTouchListener());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OnPosterClickListener onPosterClickListener = poster.getOnPosterClickListener();
                        if(onPosterClickListener!=null){
                            onPosterClickListener.onClick(poster.getPosition());
                        }
                    }
                });
                return imageView;
            }
            else if (poster instanceof VideoPoster){
                final PlayerView playerView = new PlayerView(getActivity());

                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelection.Factory videoTrackSelectionFactory =
                        new AdaptiveTrackSelection.Factory(bandwidthMeter);
                TrackSelector trackSelector =
                        new DefaultTrackSelector(videoTrackSelectionFactory);

                player = ExoPlayerFactory.newSimpleInstance(getActivity(),trackSelector);
                //

                playerView.setPlayer(player);
                if(isLooping){
                    playerView.setUseController(false);
                }

                if(poster instanceof RawVideo){
                    RawVideo video = (RawVideo) poster;
                    DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(video.getRawResource()));
                    final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(getActivity());
                    try {
                        rawResourceDataSource.open(dataSpec);
                    } catch (RawResourceDataSource.RawResourceDataSourceException e) {
                        e.printStackTrace();
                    }

                    DataSource.Factory factory = new DataSource.Factory() {
                        @Override
                        public DataSource createDataSource() {
                            return rawResourceDataSource;
                        }
                    };
                    ExtractorMediaSource mediaSource = new ExtractorMediaSource(rawResourceDataSource.getUri(),
                            factory,new DefaultExtractorsFactory(), new Handler(),null);
                    player.prepare(mediaSource);
                }

                else if(poster instanceof RemoteVideo){
                    RemoteVideo video = (RemoteVideo) poster;
                    MediaSource mediaSource = new ExtractorMediaSource.Factory(
                            new DefaultHttpDataSourceFactory(Util.getUserAgent(getActivity(),"PosterSlider"))).
                            createMediaSource(video.getUri());
                    player.prepare(mediaSource, true, false);
                }


                return playerView;
            }
            else{
                throw new RuntimeException("Unknown Poster kind");
            }
        }else{
            throw new RuntimeException("Poster cannot be null");
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(isLooping&&playbackState==STATE_ENDED){
            videoPlayListener.onVideoStopped();
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser&&isLooping&&player!=null){
            videoPlayListener.onVideoStarted();
            if(player.getPlaybackState()==STATE_ENDED){
                player.seekTo(0);
            }
            player.setPlayWhenReady(true);
            player.addListener(this);
        }
    }
}
