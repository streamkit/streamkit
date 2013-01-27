- This bundle reacts when adding a new mediaFile to the project
- Processes:
1. Media resource is being removed from JCR and copied to a given path on disk
2. FFMpeg process is started to read the media information (persisted to JCR)
2. FFMpeg creates file screenshot
- Path to FFMpeg software must be updated on JCR: /config/postprocessing/ffmpeg
- Path to directory where files need to be stored must be updated on JCR: /config/storage/servers