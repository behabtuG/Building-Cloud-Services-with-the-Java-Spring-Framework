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

package org.magnum.mobilecloud.video;

import org.magnum.mobilecloud.video.repository.Video;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class VideoController {

    /**
     * You will need to create one or more Spring controllers to fulfill the
     * requirements of the assignment. If you use this file, please rename it
     * to something other than "AnEmptyController"
     * <p>
     * <p>
     * ________  ________  ________  ________          ___       ___  ___  ________  ___  __
     * |\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \
     * \ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_
     * \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \
     * \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \
     * \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
     * \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
     */

    @RequestMapping(value = "/go", method = RequestMethod.GET)
    public @ResponseBody String goodLuck() {
        return "Good Luck!";
    }

    public static final String VIDEO_SVC_PATH = "/video";
    public static final String VIDEO_DATA_PATH = VIDEO_SVC_PATH + "/{id}";
    public static final String VIDEO_LIKE = "/video/{id}/like";
    public static final String VIDEO_UNLIKE = "/video/{id}/unlike";
    public static final String VIDEO_SEARCH_BY_NAME = "/video/search/findByName";
    public static final String VIDEO_SEARCH_BY_DURATION = "/video/search/findByDurationLessThan";

    private static final AtomicLong currentId = new AtomicLong(0L);

    private Map<Long, Video> video = new HashMap<Long, Video>();

    @RequestMapping(value = "/video", method = RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideo() {
        return video.values();
    }

    @RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.POST)
    public @ResponseBody Video addVideoMetadata(
            @RequestBody Video v,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        try {
            v.setId(currentId.incrementAndGet());
            v.setLikes(0);
            video.put(v.getId(), v);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        return v;
    }

    @RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.GET)
    public @ResponseBody Video getVideo(
            @PathVariable("id") Long id,
            HttpServletResponse response
    ) {
        Video v = new Video();
        try {
            v = video.get(id);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        return v;
    }

    @RequestMapping(value = VIDEO_LIKE, method = RequestMethod.POST)
    public void likeVideo(
            @PathVariable("id") long id,
            HttpServletResponse response
    ) {
        try {
            Video v = video.get(id);
            if (v.getLikes() == 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else if (v.getLikes() == 0) {
                v.setLikes(1);
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value = VIDEO_UNLIKE, method = RequestMethod.POST)
    public void unlikeVideo(
            @PathVariable("id") long id,
            HttpServletResponse response
    ) {
        try {
            Video v = video.get(id);
            if (v.getLikes() == 1) {
                v.setLikes(0);
                response.setStatus(HttpServletResponse.SC_OK);
            } else if (v.getLikes() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value = VIDEO_SEARCH_BY_NAME, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> findByName(
            @RequestParam("title") String title
    ) {
        Map<Long, Video> videoList = new HashMap<>();
        for (Video value : video.values()) {
            if (value.getName().equals(title))
                videoList.put(value.getId(), value);
        }
        return videoList.values();
    }


    @RequestMapping(value = VIDEO_SEARCH_BY_DURATION, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> findByDuration(
            @RequestParam("duration") Long duration
    ) {
        Map<Long, Video> videoList = new HashMap<>();
        for (Video value : video.values()) {
            if (value.getDuration() < duration) {
                videoList.put(value.getId(), value);
            }
        }
        return videoList.values();
    }
}