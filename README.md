
### 介绍
项目是实现Android上视频裁剪功能,类似视频裁剪功能的开源项目,个人觉得非常稀缺。
不像ios开源的那么多,自己在开发过程中也是不断的摸索,其中也遇到不少蛋疼的问题。
现在简单说一下这个项目实现。

### 使用到相关技术
* FFmpeg实现裁剪视频
* ContentResolver获取所有视频资源
* 采用VideoView播放视频
* 使用水平滚动的ListView显示视频的帧图片
* 通过MediaMetadataRetriever获取视频帧的Bitmap
* View的自定义

### 功能扩展思考
视频裁剪功能之后往往涉及到视频的压缩和上传,每一个功能都是Android开发中的高阶内容,比如说视频的压缩,压缩库其实开源的有一些,
但是能达到压缩比高、压缩速度快,同时又保证视频的质量,这样的开源库还是比较少的。在这个项目中没有涉及到视频的压缩逻辑，
但是目前已经集成了FFmpeg库，大家可以很方便的使用FFmpeg来处理视频裁剪之后的视频压缩。


<img src="https://github.com/iknow4/iknow.Images/blob/master/gif/videoTrim.gif?raw=true" width="400" height="700" alt="VideoTrim"/>
