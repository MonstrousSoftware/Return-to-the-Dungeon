# Dungeon

This is a derivative of the jam game "Dungeon-Game" using gdx-webgpu and switching from 
iso-metric view to a third-person perspective, with an idea to submit it for LibGDX game jam #34 which had
"dungeons" as theme.

I did not submit it, because I ran out of time and because it was not very original.
I also ran into some gdx-webgpu bugs I had to fix first 
(e.g. to support multiple rigged characters).  

Also, this type of game does not play nicely in third-person view and worked much better 
in isometric.




## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `teavm`: Web backend that supports most JVM languages.

