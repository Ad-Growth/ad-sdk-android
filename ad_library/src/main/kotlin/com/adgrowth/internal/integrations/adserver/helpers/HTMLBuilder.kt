package com.adgrowth.internal.integrations.adserver.helpers

object HTMLBuilder {
    private fun buildBaseHTML(): String {

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    html {height: 100%;width: 100%;}
                    body {display: flex;margin: 0;padding: 0;width: 100%;height: 100%;}
                    #media {display: flex; width: 100%;height: 100%;object-fit: contain;}
                </style>
            </head>
            <body>{body}</body>
            <script>
                function getMediaElement(){ return document.querySelector('#media')};
                {jscode}
            </script>
            </html>
        """.trimIndent()
    }

    fun getImageHTML(): String {
        val body =
            """<img 
                id="media" 
                src="{media_url}" 
                onclick="ads.onClick()" 
                onload="ads.onImageReady()" 
                onerror="ads.onImageError()" 
                alt="Image">
             """.trimIndent()
        return buildBaseHTML().replace("\\{body\\}".toRegex(), body)
            .replace("\\{jscode\\}".toRegex(), "")
    }

    fun getVideoHTML(): String {
        val body =
            """
                <div id="video-wrapper" data-time-last-push="0" style="display: none;"></div>
                <video
                  id="media" 
                  muted="false"
                  onclick="ads.onClick()" 
                  onpause="ads.onPause()"
                  onerror="ads.onError()"
                  onended="ads.onVideoFinished()"
                >
                  <source src="{media_url}" type="video/mp4" />
                </video>
            """.trimIndent()

        val jscode =
            """
                var player = getMediaElement();

                function play() {
                  player.play();
                }
            
                function pause() {
                  player.pause();
                }
            
                function setMuted(muted) {
                  player.muted = muted;
                }
            
                function configureEvents() {
                  player.style.display = "none";
                  player.muted = false;
            
                  player.addEventListener("onplay", () => {
                    ads.onPlay();
                  });
            
                  var notifiedOnVideoReadyOnce = false;
            
                  player.addEventListener("canplay", () => {
                    if (!notifiedOnVideoReadyOnce) {
                      notifiedOnVideoReadyOnce = true;
                      ads.onVideoReady(player.duration);
                    }
                  });
                  const THROTTLE_TIME_IN_SECS = 0.5;
            
                  player.addEventListener("timeupdate", () => {
                    if (player.style.display != "flex") player.style.display = "flex";
            
                    const wrapper = document.querySelector("#video-wrapper");
                    const last_push = wrapper.getAttribute("data-time-last-push");
                    const current = Math.round(player.currentTime);
            
                    if (current % THROTTLE_TIME_IN_SECS === 0) {
                      if (current > last_push) {
                        wrapper.setAttribute("data-time-last-push", current);
                        ads.onVideoProgressChanged(player.currentTime, player.duration);
                      }
                    }
                  });
                }
            
                configureEvents();
            """.trimIndent()

        return buildBaseHTML().replace("\\{body\\}".toRegex(), body)
            .replace("\\{jscode\\}".toRegex(), jscode)
    }
}