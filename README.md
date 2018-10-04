
### 项目介绍
实现Android上使用ffmpeg进行视频裁剪，压缩功能。类似视频裁剪功能的开源项目,个人觉得非常稀缺。
不像ios开源的那么多,自己在开发过程中也是不断的摸索,其中也遇到不少蛋疼的问题。
现在简单说一下这个项目实现。

### 使用到相关技术
* FFmpeg实现裁剪视频
* FFmpeg实现裁剪之后的视频压缩
* 采用Loader或者ContentProvider获取所有视频资源
* 采用VideoView播放视频
* 采用RecycleView显示视频的帧图片
* 通过MediaMetadataRetriever获取视频帧的Bitmap
* View的自定义

### 功能扩展思考
视频裁剪功能之后往往涉及到视频的压缩和上传,每一个功能都是Android开发中的高阶内容,比如说视频的压缩,压缩库其实开源的有一些,
但是能达到压缩比高、压缩速度快,同时又保证视频的质量,这样的开源库还是比较少的。
在这个项目中，我只是简单的实现了裁剪后的视频压缩，想达到一个好的压缩效果，还需要在项目中对视频压缩参数进行调整，
大家可以fork项目进行相应的移植和修改。

### 其他
* 视频裁剪完成，会将裁剪好的视频输出保存至应用的Android->data->包名->cache文件夹中
* 联系方式 Email: who_know_me@163.com WeChat: 516799851

### 欢迎star、fork和issues.

### License

See the [LICENSE](https://github.com/iknow4/Android-Video-Trimmer/blob/master/LICENSE) file.

#### 项目重构后的视频截图

<img src="https://github.com/iknow4/iknow.Images/blob/master/gif/videoTrim2.gif?raw=true" width="400" height="700" alt="videoTrim2"/>

#### 项目重构前的视频截图
<img src="https://github.com/iknow4/iknow.Images/blob/master/gif/videoTrim.gif?raw=true" width="400" height="700" alt="VideoTrim"/>
