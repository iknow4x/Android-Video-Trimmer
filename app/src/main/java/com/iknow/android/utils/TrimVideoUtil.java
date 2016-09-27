package com.iknow.android.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceViaHeapImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.iknow.android.models.VideoInfo;
import com.iknow.android.interfaces.OnTrimVideoListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import iknow.android.utils.DeviceUtil;
import iknow.android.utils.UnitConverter;
import iknow.android.utils.callback.SingleCallback;
import iknow.android.utils.thread.BackgroundExecutor;

public class TrimVideoUtil {

    private static final String TAG = TrimVideoUtil.class.getSimpleName();
    private static final int thumb_Width = (DeviceUtil.getDeviceWidth() - UnitConverter.dpToPx(20)) / 15;
    private static final int thumb_Height = UnitConverter.dpToPx(60);
    private static final long one_frame_time = 1000000;

    public static void startTrim(File src, String dst, long startMs, long endMs, OnTrimVideoListener callback) throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        final String fileName = "trimmedVideo_" + timeStamp + ".mp4";
        final String filePath = dst + fileName;

        File file = new File(filePath);
        file.getParentFile().mkdirs();
        genVideoUsingMp4Parser(src, file, startMs, endMs, callback);
    }

    private static void genVideoUsingMp4Parser(File src, File dst, long startMs, long endMs, OnTrimVideoListener callback) throws IOException {

        // NOTE: Switched to using FileDataSourceViaHeapImpl since it does not use memory mapping (VM).
        // Otherwise we get OOM with large movie files.
        Movie movie = MovieCreator.build(new FileDataSourceViaHeapImpl(src.getAbsolutePath()));

        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        // remove all tracks we will create new tracks from the old

        double startTime1 = startMs / 1000;
        double endTime1 = endMs / 1000;

        boolean timeCorrected = false;

        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
//        for (Track track : tracks) {
//            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
//                if (timeCorrected) {
//                    // This exception here could be a false positive in case we have multiple tracks
//                    // with sync samples at exactly the same positions. E.g. a single movie containing
//                    // multiple qualities of the same video (Microsoft Smooth Streaming file)
//
//                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
//                }
//                startTime1 = correctTimeToSyncSample(track, startTime1, false);
//                endTime1 = correctTimeToSyncSample(track, endTime1, true);
//                timeCorrected = true;
//            }
//        }

        if(startTime1 == 0)
            startTime1 = startMs/1000;

        if(endTime1 == 0)
            endTime1 = endMs/1000;

        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            double lastTime = -1;
            long startSample1 = -1;
            long endSample1 = -1;

            for (int i = 0; i < track.getSampleDurations().length; i++) {
                long delta = track.getSampleDurations()[i];


                if (currentTime > lastTime && currentTime <= startTime1) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample;
                }
                if (currentTime > lastTime && currentTime <= endTime1) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample;
                }
                lastTime = currentTime;
                currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
//            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
            movie.addTrack(new CroppedTrack(track, startSample1, endSample1));
        }

        dst.getParentFile().mkdirs();

        if (!dst.exists()) {
            dst.createNewFile();
        }

        Container out = new DefaultMp4Builder().build(movie);

        FileOutputStream fos = new FileOutputStream(dst);
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fc.close();
        fos.close();
        callback.onFinishTrim(Uri.parse(dst.toString()));
    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }

    public static void backgroundShootVideoThumb(final Context context, final Uri videoUri, final SingleCallback<ArrayList<Bitmap>, Integer> callback) {

        final ArrayList<Bitmap> thumbnailList = new ArrayList<>();

        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0L, "") {
                   @Override
                   public void execute() {
                       try {
                           MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                           mediaMetadataRetriever.setDataSource(context, videoUri);

                           // Retrieve media data use microsecond
                           long videoLengthInMs = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;

                           long  numThumbs = videoLengthInMs < one_frame_time? 1 : (videoLengthInMs / one_frame_time);

                           final long interval = videoLengthInMs / numThumbs;

                           //每次截取到3帧之后上报
                           for (long i = 0; i < numThumbs; ++i) {
                               Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                               try {
                                   bitmap = Bitmap.createScaledBitmap(bitmap, thumb_Width, thumb_Height, false);
                               } catch (Exception e) {
                                   e.printStackTrace();
                               }
                               thumbnailList.add(bitmap);
                                if(thumbnailList.size() == 3) {
                                    callback.onSingleCallback((ArrayList<Bitmap>)thumbnailList.clone(), (int) interval);
                                    thumbnailList.clear();
                                }
                           }
                           if(thumbnailList.size() > 0) {
                               callback.onSingleCallback((ArrayList<Bitmap>) thumbnailList.clone(), (int) interval);
                               thumbnailList.clear();
                           }
                           mediaMetadataRetriever.release();
                       } catch (final Throwable e) {
                           Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                       }
                   }
               }
        );

    }

    /**
     * 需要设计成异步的
     *
     * @param mContext
     * @return
     */
    public static ArrayList<VideoInfo> getAllVideoFiles(Context mContext) {
        VideoInfo video;
        ArrayList<VideoInfo> videos = new ArrayList<>();
        ContentResolver contentResolver = mContext.getContentResolver();
        try {
            Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                    null, null, MediaStore.Video.Media.DATE_MODIFIED + " desc");
            while (cursor.moveToNext()) {
                video = new VideoInfo();

                if (cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)) != 0) {
                    video.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
                    video.setVideoPath(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                    video.setCreateTime(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)));
                    video.setVideoName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
                    videos.add(video);
                }
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videos;
    }

    public static String getVideoFilePath(String url) {

        if (TextUtils.isEmpty(url) || url.length() < 5) {
            return "";
        }

        if (url.substring(0, 4).equalsIgnoreCase("http")) {

        } else {
            url = "file://" + url;
        }
        return url;
    }
}
