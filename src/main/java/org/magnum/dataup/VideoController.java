/*
 *
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.magnum.dataup;

import org.springframework.stereotype.Controller;


import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {

    private static final AtomicLong currentId = new AtomicLong(0L);

    private Map<Long, Video> videos = new HashMap<Long, Video>();


    @RequestMapping(method = RequestMethod.GET, value = "/video")
    public @ResponseBody Collection<Video> getVideoList() {
        return videos.values();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/video")
    public @ResponseBody Video addVideo(@RequestBody Video v) {
        checkAndSetId(v);
        v.setDataUrl(this.getDataUrl(v.getId()));
        videos.put(v.getId(), v);
        return v;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/video/{id}/data")
    public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id, @RequestPart("data") MultipartFile videoData, HttpServletResponse response) {


        try {
            Video videoMetaData = videos.get(id);
            if (videoMetaData == null) response.setStatus(404);
            else {
                VideoFileManager videoFileManager = VideoFileManager.get();
                if (Objects.isNull(videoMetaData)) throw new Exception("Video metadata not found");

                videoFileManager.saveVideoData(videoMetaData, new ByteArrayInputStream(videoData.getBytes()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new VideoStatus(VideoStatus.VideoState.READY);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/video/{id}/data")
    public void getData(@PathVariable("id") long id, HttpServletResponse response) {


        try {
            VideoFileManager videoData = VideoFileManager.get();
            if (videos.get(id) == null) response.setStatus(404);
            else
                videoData.copyVideoData(videos.get(id), response.getOutputStream());
        } catch (Exception e) {

        }
    }


    private void checkAndSetId(Video entity) {
        if (entity.getId() == 0) {
            entity.setId(currentId.incrementAndGet());
        }
    }

    private String getDataUrl(long videoId) {
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base =
                "http://" + request.getServerName()
                        + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
//        System.out.println("--- Video URL: " + base + "---");
        return base;
    }
}
