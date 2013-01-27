- This bundle provides the interface and services necessary for the online player
- Resource properties can be accessed using this path: {$content_path}.player.json . The servlet supporting this feature, can found on: org.apache.sling.service.player.PlayerLiveServlet
- Streaming server URL info should be placed in JCR on this path: /config/streaming/servers/server_name | property:"url"
- If cdn_monitor is activated, this bundle will automatically retrieve CDN's content

- This bundle contains the flash player and the iplayer implementations which can be found in /resources.libs.mediacenter.vod
 URL: JCR_RESOURCE_PATH.fplayer.html and JCR_RESOURCE_PATH.iplayer.html
- The resource parameter json values containing vod information and steaming server information can be accessed at:
 URL: JCR_RESOURCE_PATH.player.json