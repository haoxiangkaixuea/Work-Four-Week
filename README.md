# Work-Four-Week

## 网络请求

### okHttp使用

添加依赖

```java
implementation("com.squareup.okhttp3:okhttp:4.8.1")
```

在AndroidManifest.xml加入网络权限

```html
<uses-permission android:name="android.permission.INTERNET" />
```

OkHttp框架的核心类是OkHttpClient，此类可直接实例化。由于OkHttpClient内部处理了并发，多线程和Socket重用等问题，为了节省资源，整个应用中使用一个OkHttpClient对象即可，可以对它做Singleton封装。

```java
OkHttpClient okHttpClient = new OkHttpClient();
```

#####  Http请求的构建

######  Http请求的发送

请求的发送有两种形式，一种是直接同步执行，阻塞调用线程，直接返回结果；另一种是通过队列异步执行，不阻塞调用线程，通过回调方法返回结果。如下所示：

同步执行：

```java
// 如果返回null，代表超时或没有网络连接
Response response = client.newCall(request).execute();
```

异步回调：

Response response = client.newCall(request).enqueue(new Callback() {

```java

    @Override
    public void onFailure(Request request, IOException e) {
        //超时或没有网络连接
        //注意：这里是后台线程！
    }
    
    @Override
        public void onResponse(Response response) throws IOException {
        //成功
        //注意：这里是后台线程！
    }

});
```

###### GET请求

代表Http请求的类是Request，该类使用构造器模式，最简单的构造GET请求如下：

```java
Request request = new Request.Builder()
      .url(url)
      .build();
```

具体方法如下：

```java
/**
 * Http Get 请求
 */
private void httpGet() {
    ///创建okHttpClient对象
    OkHttpClient mOkHttpClient = new OkHttpClient();
    //创建一个Request Request是OkHttp中访问的请求，Builder是辅助类。Response即OkHttp中的响应。
    final Request request = new Request.Builder()
            .url("http://ip.taobao.com/service/getIpInfo.php?ip=63.223.108.42")
            .build();
    //得到一个call对象
    Call call = mOkHttpClient.newCall(request);
    //请求加入调度
    call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            //请求失败
            Log.e("TAG", "请求失败");
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //不是UI线程,请不要在此更新界面
            String htmlStr = response.body().string();
            Log.e("TAG", "htmlStr ==" + htmlStr);
        }
    });
}
```

以上就是发送一个get请求的步骤，首先构造一个Request对象，参数最起码有个url，当然你可以通过Request.Builder设置更多的参数比如：header、method等。

然后通过request的对象去构造得到一个Call对象，类似于将你的请求封装成了任务，既然是任务，就会有execute()和cancel()等方法。

最后，以异步的方式去执行请求，所以我们调用的是call.enqueue，将call加入调度队列，然后等待任务执行完成，我们在Callback中即可得到结果。

###### POST请求

要构造Post请求，在构建Request时增加请求体即可：

```java
RequestBody formBody = new FormEncodingBuilder()
    .add("name", "Cuber")
    .add("age", "26")
    .build();

Request request = new Request.Builder()
      .url(url)
      .post(RequestBody)
      .build();
```

具体方法：

 ```java
    /**
     * Http Post请求
     */
    private void httpPost() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("ip", "63.223.108.42")
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://ip.taobao.com/service/getIpInfo.php?")
                .post(requestBody)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败
                Log.e("TAG", "请求失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String htmlStr = response.body().string();
                Log.e("TAG", "htmlStr ==" + htmlStr);
            }

        });
    }
 ```

使用Request的post方法来提交请求体RequestBody。

### Retrofit使用

Retrofit 是一个 RESTful 的 HTTP 网络请求框架的封装，网络请求的工作本质上是 OkHttp 完成，而 Retrofit 仅负责 网络请求接口的封装

在Android中使用Retrofit，我们只需要定义好Retrofit远程调用接口，在使用的时候定义Callback类就可以了，在Callback的onResponse回调方法中定义，请求成功后执行什么UI操作，在onFailure发法中定义请求失败后调用什么UI操作.

使用步骤：

1.添加Retrofit库的依赖：

```bash
implementation 'com.squareup.retrofit2:retrofit:2.0.2'
```

2.创建 用于描述网络请求 的接口
 Retrofit将 Http请求 抽象成 Java接口：采用 注解 描述网络请求参数 和配置网络请求参数

```java
public interface GetRequest_Interface {

    @GET("openapi.do?keyfrom=abc&key=2032414398&type=data&doctype=json&version=1.1&q=car")
    Call<Reception> getCall(@Field("name") String name);
    // @GET注解的作用:采用Get方法发送网络请求
 
    // getCall() = 接收网络请求数据的方法
    // 其中返回类型为Call<*>，*是接收数据的类（即上面定义的Translation类）
    // 如果想直接获得Responsebody中的内容，可以定义网络请求返回值为Call<ResponseBody>
}
```

3.创建Retrofit实例

创建Retrofit实例和服务接口，在创建远程接口的时候必须要使用Retrofit接口来create接口的动态代理实例。Retrofit实例可以通过Builder去创建，在Builder过程中可以定义baseUrl，还可以定义json解析的工厂类，还可以定义RxJava的CallAdapter类，

```java
  Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://fanyi.youdao.com/") //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
```

4.发送请求
 请求分为同步请求和异步请求

```java
        // 创建 网络请求接口 的实例
        GetRequest_Interface request = retrofit.create(GetRequest_Interface.class);
        //对 发送请求 进行封装
        Call<Reception> call = request.getCall("");
        call.enqueue(new Callback<Reception>() {
            //请求成功时回调
            @Override
            public void onResponse(Call<Reception> call, Response<Reception> response) {
                //请求处理,输出结果
                response.body().show();
            }
            //请求失败时候的回调
            @Override
            public void onFailure(Call<Reception> call, Throwable throwable) {
                System.out.println("连接失败");
            }
        });
          //同步请求
        try {
            Response<Reception> response = call.execute();
            response.body().show();
        } catch (IOException e) {
            e.printStackTrace();
        }
```

response.body()就是Reception对象，网络请求的完整 Url =在创建Retrofit实例时通过.baseUrl()设置 +网络请求接口的注解设置（称 “path“ ）

### 文件上传与下载

上传文件：

在AndroidManifest.xml加入读取设备外部存储空间和sdcard权限

```html
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

首先需要定义上传文件的类型，将sdcard根目录的test.txt文件上传到服务器：

```java
private void uploadMultiFile(String url, File file) {
    RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
    RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.getName(), fileBody)
            .build();
    Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .build();
    final okhttp3.OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
    OkHttpClient okHttpClient = httpBuilder
            //设置超时
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();
}
okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
               Toast.makeText(OkUpload.this,"上传失败",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    String jsonStr = response.body().string();
                    Toast.makeText(OkUpload.this,"上传成功",Toast.LENGTH_SHORT).show();
                    Log.i("EvaluateActivity", "uploadMultiFile() response=" + jsonStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
```

最后需要获得请求权限

```java
/**
 * 请求权限
 */
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
        case 1:
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
    }
}
```

文件下载：

在这里下载一张图片，我们得到Response后将流写进我们指定的图片文件中就可以了。

```java
 /**
   * 下载文件
   */
       private void downAsynFile() {
       OkHttpClient mOkHttpClient = new OkHttpClient();
       String url = "https://www.baidu.com/img/bd_logo1.png";
       Request request = new Request.Builder().url(url).build();
   mOkHttpClient.newCall(request).enqueue(new Callback() {
           @Override
       public void onFailure(Call call, IOException e) {
    
           }
            
           @Override
           public void onResponse(Call call, Response response) {
               InputStream inputStream = response.body().byteStream();
               FileOutputStream fileOutputStream = null;
               try {
                   fileOutputStream = new FileOutputStream(new File("/sdcard/baidu.png"));
                   byte[] buffer = new byte[2048];
                   int len = 0;
                   while ((len = inputStream.read(buffer)) != -1) {
                       fileOutputStream.write(buffer, 0, len);
                   }
                   fileOutputStream.flush();
               } catch (IOException e) {
               Log.e("TAG", "IOException");
                   e.printStackTrace();
               }
            
           Log.d("TAG", "文件下载成功");
        }
 
    });
    }
```

## git的使用

Git：Git是一个开源的分布式版本控制系统，可以有效、高速的处理从很小到非常大的项目版本管理。简单说，它就是一个管理项目版本的工具。

GitLab：GitLab 是一个用于仓库管理系统的开源项目，使用Git作为代码管理工具，并在此基础上搭建起来的web服务。简单说，GitLab的性质是和GitHub一样的，都是用来存储项目的仓库。

分支管理

在实际开发中，我们会用到很多分支。这里说下各分支的作用。

- master分支：最稳定的分支，保存需要发布的版本，不在该分支上做任何开发。

- dev分支：开发分支，保存最新的代码，平时开发就在该分支上。当某个版本开发完成后就合并到master分支，然后在master分支进行版本发布。

- bug分支：用来修复bug的分支，一般是线上版出现bug的时候，从master分支创建一个新的bug分支进行bug修复，修复完成合并到master分支和dev分支（保证master分支与dev分支同步），然后删除该bug分支。

- 标签(Tag)管理

  标签一般是用于标记某个发布的版本， 例如你发布了版本v1.0，这个时候会打一个v1.0的Tag，主要是方便以后查看和管理某个版本的代码。

### 基本命令

##### 常见命令

1、查看 Git 版本信息

```java
git --version
    1
```

结果：

```java
git version 2.11.0.windows.1

12
```

2、获取当前登录的用户

```java
git config --global user.name
1
```

结果：

```java
zhaoyanjun
1
```

3、获取当前登录用户邮箱

```java
git config --global user.email
1
```

4、设置当前登录用户的用户名

```java
git config --global user.name '赵彦军'

12
```

5、设置当前登录用户的邮箱

```java
git config --global user.email '362299465@qq.com'
1
```

6、初始化仓库

文件夹下初始化一个仓库，此时文件里会到一个`.git`的隐藏文件夹

```java
git init 
1
```

7、查看本地所有的分支

```java
git branch
```

8、查看本地和远程所有分支

```java
git branch -a
```

9、查看远程所有分支

```java
git branch -r

12
```

10、创建分支

创建 dev 分支。

```java
git branch dev

12
```

11、切换分支

切换 dev 分支为当前分支

```java
git checkout dev
1
```

12、创建并切换分支

创建并切换 dev 分支。相当于 `git branch dev` 和 `git checkout dev` 的合集。

```java
git checkout -b dev
1
```

13、添加文件

把 当前根目录中的 `loader1.png` 添加在暂存区。 add 后面需要写 文件的相对路径。

```java
git add loader1.png
1
```

把 `image` 目录下的 `loader1.png` 图片添加到暂存区

```java
git add image/loader1.png
```

14、批量添加文件

```java
git add -A

12
```

15、查看log

```java
git log
1
```

退出 log

```java
q
1
```

##### 删除相关

- 删除本地仓库的分支

```java
git branch -d dev
1
```

删除本地仓库的 dev 分支

- 删除远程仓库的分支

```java
git push origin :dev
1
```

删除远程的 dev 分支

- 删除本地的一个文件

```java
git rm 文件名
1
```

##### 拉取更新

- 拉去远程分支，更新到本地

```java
git pull 
1
```

- pull 使用 --rebase参数

表示把你的本地当前分支里的每个提交(commit)取消掉，并且把它们临时 保存为补丁(patch)(这些补丁放到".git/rebase"目录中),然后把本地当前分支更新 为最新的"origin"分支，最后把保存的这些补丁应用到本地当前分支上。

```
git pull --rebase
1
```

##### 提交相关

- 提交文件到分支

```
git commit -a -m '修复一个bug'
1
```

- push 到远程 master 分支

```
git push origin master
1
```

##### 合并分支

- 合并本地 dev 分支到当前分支

```
git merge dev
1
```

- 合并远程 dev 分支到当前分支

```
git merge origin/dev

12
```

##### Diff 相关

- 比较的是暂存区和工作区的差异
  `git diff`
- 比较的是暂存区和历史区的差异
  `git diff --cached`
- 比较的是历史区和工作区的差异（修改）
  `git diff master`

##### Tag 相关

- 查看所有 tag

```java
git tag

12
```

- 删除某一 tag

```java
git tag –d tag名字
1
```

- 查看某一标签

```java
git show tag名字
1
```

- 给当前分支打标签

```java
git tag 标签名
1
```

- 为历史版本打标签

```java
git tag 标签名 该版本ID
1
```

##### 回退 reset 相关

reset命令有3种方式：

- git reset –mixed：
  此为默认方式，不带任何参数的git reset，即时这种方式，它回退到某个版本，只保留源码，回退commit和index信息
- git reset –soft：
  回退到某个版本，只回退了commit的信息，不会恢复到index file一级。如果还要提交，直接commit即可
- 3：git reset –hard：
  彻底回退到某个版本，本地的源码也会变为上一个版本的内容

1、 将本地的状态回退到和远程一样

```java
git reset --hard origin/master
1
```

2、将暂存区里面的修改清空 , 回退到上一次提交的记录

```java
git reset --hard
1
```

3、将本地的状态回退到 某个版本

```java
git reset --hard 5230bb6
```

合并多个commit为一个commit

```java
git rebase -i 160ce28
1
```

`160ce28` 为 `v1版本发布`的git记录号，这里做参考，表示当前节点不参与合并

或者

```java
git rebase -i HEAD~3
```

##### Git commit 日志规范

一般情况下，在 commit 的时候，是要求必须写 commit 日志，否则不能 commit . 那么 commit 日志也是需要规范的。日志格式一般为:

```
type( scope ): subject
空行
body
空行
footer
12345
```

type（必需）、scope（可选）和subject（必需）。

**type**

用于说明 commit 的类别，只允许使用下面7个标识。

- feat：新功能（feature）
- fix：修补bug
- docs：文档（documentation）
- style： 格式（不影响代码运行的变动）
- refactor：重构（即不是新增功能，也不是修改bug的代码变动）
- test：增加测试
- chore：构建过程或辅助工具的变动

如果type为 feat 和 fix ，则该 commit 将肯定出现在 Change log 之中。其他情况（docs、chore、style、refactor、test）由你决定，要不要放入 Change log，建议是不要。

**scope**

scope用于说明 commit 影响的范围，比如数据层、控制层、视图层等等，视项目不同而不同。一般有三个可以选择。

- all ：表示影响面大 ，如修改了网络框架 会对整个程序产生影响
- loation： 表示影响小，某个小小的功能
- module：表示会影响某个模块 如登录模块、首页模块 、用户管理模块等等

**subject**

subject是 commit 目的的简短描述，不超过50个字符。

以动词开头，使用第一人称现在时，比如change，而不是changed或changes

第一个字母小写

结尾不加句号（.）

**body**
具体的修改信息 应该尽量详细

**footer**
放置写备注啥的，如果是 bug ，可以把bug id放入

### GitFlow

Git-Flow是一套基于Git的扩展，通过分支模型对Git进行一套更高层的操作。Git-Flow的运用可以使版本的迭代与演化过程更加清晰，同时运用的分支功能更加明确，主干分支更清晰.

- 主分支：master
  - 只有一个主分支，所有的正式版本都应该在这个主分支上发布
- 开发分支:  develop
  - 日常的开发工作都在这条开发分支上进行
- 版本应急修复分支 ：hotfix
  - 临时性分支：版本在上线的时候遇到紧急bug需要修复而开的分支，该分支由master分出完成后合入master与develop
- 版本上线前预发布分支： release
  - 临时性分支：在开发分支即将合入master分支前，需要测试进行版本测试，该分支由develop分出完成后合入master与develop
- 新功能开发分支： feature
  - 临时性分支：为了开发某一个特点功能的分支，由develop分出开发完成后合入

Git分支上只有master与develop保持常有，其余分支均在完成自身功能后及时删除

### 常用命令

远程代码库拉取

```bash
git clone #远程代码仓库
```

查看代码分支

```bash
git branch -a
```

创建develop分支

```bash
　　git checkout -b develop master
```

切换分支

```bash
git checkout develop
```

分支合并

```bash
#切换master分支
git checkout master
#对develop分支进行合并
#快进式合并
git merge develop
#非快进式合并
git merge --no-ff develop
```

合并方式有两种

- 快进式合并：
  由master分支直接指向develop分支，其中不会保留合并过程的开发记录

- 非快进式合并：
  使用 `--no-ff` 参数git会在master分支提交一个commit记录，并保留方便后续查看

  

查看当前代码库状态，日志

```bash
#代码库状态
git status
#代码库日志
git log
```

代码提交

```bash
#代码添加
git add .
#代码提交
git commit -am "#提交内容"
```

### 冲突解决

执行Git merge ，如果有冲突，就会出现如下格式：

```bash
<<<<<<< HEAD

这个位置的内容就是当前所在分支的内容

=======

这个位置的内容就是合并进来的分支的内容

>>>>>>> branchName1
用下面的设置来改进冲突标记使其也显示（分支）共同祖先： 

`git config --global merge.conflictstyle diff3`

||||||| merged common ancestors下面的内容就是双方改动前的内容
```

### 多人开发协作

1. 首先fork组长的仓库到 github上自己的远程仓库，再clone到本机仓库

2. 提交自己的代码

   ##### step1. Commit

   ##### step2. push

   ##### step3. Open in Github

   ##### step4. Pull Request

3. 更新组长那里的代码 (要连到remote！)

### 分支规范

###### 常用命令

查看分支

查看本地所有分支：

```bash
$ git branch
```

master 分支前的 * 字符，它表示当前所在的分支。

查看远程所有分支：

```bash
$ git branch -r
```

列出所有本地分支和远程分支：

```bash
$ git branch -a
```

创建本地 dev1 分支

```bash
$ git checkout -b dev1 
```

本地 master 分支默认就是远程 master 分支，上面命令在此基础上创建本地 dev1 分支，然后切换到 dev1 分支，相当于以下两条命令：

```bash
$ git branch dev1
$ git checkout dev1
```

想从远程分支 dev （远程有该分支）创建本地分支 dev1：



```bash
$ git checkout -b dev1 origin/dev
```

开发提交

随便修改 README.md 文件，然后提交：

add 文件

```bash
$ git add README.md
```

commit 信息

```bash
$ git commit -m "branch test"
```

合并到本地 master 分支

分支 dev1 开发工作完成，我们就可以切换回本地 master 分支：

```bash
$ git checkout master
```

进行本地分支 dev1 合并：

```bash
$ git merge dev1
```

Fast-forward 信息，“快进模式”合并，这种模式下，删除分支后，会丢掉分支信息，可以用 --no-ff 方式进行 merge ：

```bash
$ git merge --no-ff -m "merge with no-ff" dev1
```

如果分支很多，这个分支历史可能就会变得很复杂了，可以使用 rebase，提交的历史会保持线性：

```bash
$ git rebase dev1
```

也是进行本地分支 dev1 合并。

删除本地分支

```bash
$ git branch -d dev1
```

这是删除，如果没有完成合并会有提示，以下是强删：

```bash
$ git branch -D dev1
```

创建远程分支 dev

直接提交

```bash
$ git push origin master:dev
```

这里冒号可以提交到指定分支，上面命令，把提交本地 master 分支到远程的 dev 分支，远程没有dev这个分支，会创建。

```bash
git push origin master 
```

这是本地 master 提交到远程主分支 master，相当于：

```bash
git push origin master:master
```

跟踪远程分支

从远程分支 checkout 出来的本地分支，称为 跟踪分支 (tracking branch)。跟踪分支是一种和某个远程分支有直接联系的本地分支。在跟踪分支里输入 git pull/push，Git 会自行推断应该向哪个服务器的哪个分支更新/推送数据。

手动建立追踪关系：

```bash
$ git branch -u origin/dev master
```

或者：

```bash
$ git branch --set-upstream-to origin/dev master
```

指定本地 master 分支追踪远程 dev 分支。

查看所有分支跟踪关系：

```bash
$ git branch -vv
```

合并远程分支

把远程分支 dev 合并到 master：

1、指定本地 master 分支追踪远程 dev 分支

```bash
$ git branch -u origin/dev master
```

2、更新内容

```bash
$ git pull
```

3、开发提交远程分支 dev

修改了 README.md 文件，然后提交：

add 文件

```bash
$ git add README.md
```

commit 信息

```bash
$ git commit -m "merge origin/dev"
```

进行 push

```bash
$ git push origin master:dev
```

4、指定本地 master 分支追踪远程 master 分支

```bash
$ git branch -u origin/master master
```

5、更新内容

```bash
$ git pull
```

6、同样提交远程分支 master

不用 commit ，上面已经 commit 了，也提交给 origin/master，这样远程分支 dev 和 master 就是一样的。

```bash
$ git push origin master
```

PS：以上远程分支合并，我不知道是不是正确的方式，望指导。

删除远程分支

```bash
$ git push origin --delete dev
```

或者

```bash
$ git push origin :dev
```

远程分支 dev 将被删除。



1. master主分支

   master主分支是线上当前发布的版本，是稳定可用的版本；

   app线上版本代码就是这个分支；

2. develop分支

   develop分支作为日常开发时使用的分支，是功能最新的分支；

   app版本迭代开发人员都在这个分支上进行add、modify、commit、push；

   develop分支测试完毕后，合入master主分支；

3. feature分支

   feature分支作为完成某个特定功能的分支，是从dev分支拉的分支；

   feature分支根据项目实际情况，分为以下两种：

   (1)：此类分支的特点是开发周期短、功能特定新强；

   feature分支开发完毕后，合入develop分支；

   develop分支测试完毕，合入master主分支后，删除feature分支；

   (2)：此类分支的特点是开发周期长，一般与develop分支处于并行关系；

   feature分支功能与develop分支同步，但是自身有特定的功能；

   feature分支一般情况下不删除；

4. hotfix分支

   作为master分支，是为了修复线上版本紧急的bug拉的分支；

   如线上版本的证书突然失效；

5. release分支

   release分支主要做工是用来给当前版本提测以及修复bug使用。

   release版本过测后，需要做两步操作，

   一是将release分支合入到master分支；

   二是将release分支合入到当前的develop分支；

###### **分支命名规范**

1. 主分支：master
2. 开发分支：develop
3. 特性分支：feature/***
4. 修复bug分支：hotfix/***
5. TAG标记：tag/***
6. 发布分支：release/***

###### **Git版本管理表格图**

| 分支类型 | 命名规范    | 创建自  | 合入到         | 说明                         |
| -------- | ----------- | ------- | -------------- | ---------------------------- |
| master   | master      | /       | /              | 主分支，发布后需打上tag      |
| develop  | develop     | master  | master         | 版本迭代开发                 |
| feature  | feature/*** | develop | develop        | 新功能，版本发布后删除此分支 |
| feature  | feature/*** | develop | /              | 定制版、OEM版                |
| hotfix   | hotfix/***  | master  | master&develop | 生产环境紧急bug修复          |
| tag      | tag/***     | master  | /              | master的tag                  |
| release  | release/*** | develop | master&develop | 待发布版本的提测及修复bug    |

###### **备忘**

1、release分支merge到master主分支之后，需要打上一个tag；

2、分支版本的设立、master版本的merge，统一由管理员操作；

3、分支版本的merge，比如从feature分支merge到develop分支，一般先在本地仓库的feature分支merge到本地仓库的develop分支，然后将develop分支push到gitlab服务器上。

## 抓包

### HTTP1.0、1.1、2.0各版本的区别

http1.1 默认使用长连接，可有效减少TCP三次握手的开销
http1.1 支持只发送header信息（不带任何body信息），如果服务器认为客户端有权限请求服务器，则返回100，否则返回401。客户端接收到100才开始把请求body发送给服务器，这样当服务器返回401的时候，客户端就不用发送body了，节约了带宽。
http1.1 支持文件断点续传，即支持传送内容的一部分，这样当客户端有一部分资源后，只需要跟服务器请求另外部分的资源即可。
http1.1 有host域，而http1.0没有
http2.0 使用多路复用技术（Multiplexing），允许同时通过单一的http2.0连接发起多重的请求-响应消息。http1.1 在同一时间对于同一个域名的请求数量有限制，超过限制后会阻塞请求。多路复用底层采用【增加二进制分帧层(将所有传输信息分割为更小的帧，用二进制编码，多个请求在同一个TCP连接上完成，可以承载任意数量的双向数据流)】方法，提高了传输性能，降低延迟。



**HTTP1.0和HTTP1.1的一些区别**

HTTP1.0最早在网页中使用是在1996年，那个时候只是使用一些较为简单的网页上和网络请求上，而HTTP1.1则在1999年才开始广泛应用于现在的各大浏览器网络请求中，同时HTTP1.1也是当前使用最为广泛的HTTP协议。 主要区别主要体现在：

1. **缓存处理**，在HTTP1.0中主要使用header里的If-Modified-Since,Expires来做为缓存判断的标准，HTTP1.1则引入了更多的缓存控制策略例如Entity tag，If-Unmodified-Since, If-Match, If-None-Match等更多可供选择的缓存头来控制缓存策略。

2. **带宽优化及网络连接的使用**，HTTP1.0中，存在一些浪费带宽的现象，例如客户端只是需要某个对象的一部分，而服务器却将整个对象送过来了，并且不支持断点续传功能，HTTP1.1则在请求头引入了range头域，它允许只请求资源的某个部分，即返回码是206（Partial Content），这样就方便了开发者自由的选择以便于充分利用带宽和连接。

3. **错误通知的管理**，在HTTP1.1中新增了24个错误状态响应码，如409（Conflict）表示请求的资源与资源的当前状态发生冲突；410（Gone）表示服务器上的某个资源被永久性的删除。

4. **Host头处理**，在HTTP1.0中认为每台服务器都绑定一个唯一的IP地址，因此，请求消息中的URL并没有传递主机名（hostname）。但随着虚拟主机技术的发展，在一台物理服务器上可以存在多个虚拟主机（Multi-homed Web Servers），并且它们共享一个IP地址。HTTP1.1的请求消息和响应消息都应支持Host头域，且请求消息中如果没有Host头域会报告一个错误（400 Bad Request）。

5. **长连接**，HTTP 1.1支持长连接（PersistentConnection）和请求的流水线（Pipelining）处理，在一个TCP连接上可以传送多个HTTP请求和响应，减少了建立和关闭连接的消耗和延迟，在HTTP1.1中默认开启Connection： keep-alive，一定程度上弥补了HTTP1.0每次请求都要创建连接的缺点。

**HTTP2.0性能惊人**

**HTTP/2: the Future of the Internet** https://link.zhihu.com/?target=https://http2.akamai.com/demo 是 Akamai 公司建立的一个官方的演示，用以说明 HTTP/2 相比于之前的 HTTP/1.1 在性能上的大幅度提升。 同时请求 379 张图片，从Load time 的对比可以看出 HTTP/2 在速度上的优势。

**HTTP2.0和HTTP1.X相比的新特性**

- **新的二进制格式**（Binary Format），HTTP1.x的解析是基于文本。基于文本协议的格式解析存在天然缺陷，文本的表现形式有多样性，要做到健壮性考虑的场景必然很多，二进制则不同，只认0和1的组合。基于这种考虑HTTP2.0的协议解析决定采用二进制格式，实现方便且健壮。
- **多路复用**（MultiPlexing），即连接共享，即每一个request都是是用作连接共享机制的。一个request对应一个id，这样一个连接上可以有多个request，每个连接的request可以随机的混杂在一起，接收方可以根据request的 id将request再归属到各自不同的服务端请求里面。
- **header压缩**，如上文中所言，对前面提到过HTTP1.x的header带有大量信息，而且每次都要重复发送，HTTP2.0使用encoder来减少需要传输的header大小，通讯双方各自cache一份header fields表，既避免了重复header的传输，又减小了需要传输的大小。
- **服务端推送**（server push），同SPDY一样，HTTP2.0也具有server push功能。

**HTTP2.0的多路复用和HTTP1.X中的长连接复用的区别**

- HTTP/1.* 一次请求-响应，建立一个连接，用完关闭；每一个请求都要建立一个连接；
- HTTP/1.1 Pipeling解决方式为，若干个请求排队串行化单线程处理，后面的请求等待前面请求的返回才能获得执行机会，一旦有某请求超时等，后续请求只能被阻塞，毫无办法，也就是人们常说的线头阻塞；
- HTTP/2多个请求可同时在一个连接上并行执行。某个请求任务耗时严重，不会影响到其它连接的正常执行；


**HTTPS与HTTP的一些区别**

- HTTPS协议需要到CA申请证书，一般免费证书很少，需要交费。
- HTTP协议运行在TCP之上，所有传输的内容都是明文，HTTPS运行在SSL/TLS之上，SSL/TLS运行在TCP之上，所有传输的内容都经过加密的。
- HTTP和HTTPS使用的是完全不同的连接方式，用的端口也不一样，前者是80，后者是443。
- HTTPS可以有效的防止运营商劫持，解决了防劫持的一个大问题。

### 网络七层

OSI七层模型
 OSI七层协议模型主要是：

应用层（Application）

表示层（Presentation）

会话层（Session）

传输层（Transport）

网络层（Network）

数据链路层（Data Link）

物理层（Physical）

### 什么是HTTP协议

什么是Http?

超文本协议（HTTP，HyperText Transfer Protocol）是互联网上应用最为广泛的一种网络协议。Http定义了浏览器（即万维网客户进程）怎样向万维网服务器请求万维网文档，以及服务器怎么把文档传给浏览器。Http是万维网可靠的交换文件（包括文本、图像、声音、以及视频等）的基础。

工作流程：一次的Http请求成为一次事务，其工作流程可以分为四步：

1、首先客户端和服务器需要建立连接。这个是从客户端发起的。

2、建立连接之后，客户端发送一个请求给服务器，请求方式的格式为：统一资源定位符（URL）、协议版本号、后边是MIME的信息（请求的是文本、图像、声音、视频.....）包括请求修饰符、客户端的信息以及可能的内容。

3、服务器接到请求后，基于相应的响应信息，其格式为一个状态行、包括信息的协议版本号、一个成功或者错误的代码、后边是MIME信息包括服务器信息，实体信息以及一些可能的内容。

4、客户端接受到服务器端返回的信息之后，根据需要将信息展示出来，然后断开与服务器的连接。

Http协议永远是客户端发起，服务器端响应



### 抓包工具的简单使用

抓包步骤

1. 将Android手机与电脑USB相连，打开windows命令提示符窗口
2. 将tcpdump程序copy至android手机（该命令前面那个目录文件为本地地址，后面那个目录为目的手机端地址）

```bash
C:\android-sdk-windows\platform-tools>adb push c:/tcpdump /data/local/tcpdump
```

3. 修改tcpdump的权限

```bash
C:\android-sdk-windows\platform-tools>adb shell

chmod 777 /data/local/tcpdump
```

4. 进入root权限

```bash
C:\android-sdk-windows\platform-tools>adb shell
 $ su
```

在运行su指令后，手机终端桌面会出现相应提示信息以确认您对root操作的认可。

5. 运行tcpdump，输入以下命令启动抓包。

```bash
/data/local/tcpdump -p -vv -s 0 -w /sdcard/capture.pcap
```

6. 在手机端执行相应需要进行抓包分析的操作，执行完成后在命令提示符窗口执行Ctrl+C中断抓包进程
7. 将抓包结果复制至本地（前面那个目录为手机端地址，后面那个目录为本地地址）

```bash
C:\android-sdk-windows\platform-tools>adb pull /sdcard/capture.pcap c:/
```

8. 使用Wireshark等工具查看抓包文件capture.pcap

### 手机如何抓包

##### 0x01 wireshark方案

在PC端抓过包的人一定不会对Wireshark感到陌生，该软件可以直接抓取流经网络适配器（网卡）的所有数据包，意味着局域网内其它主机发出的数据包也可以截获（这种模式叫作混杂模式）。此外该软件能够解析下至数据链路层，上至应用层的绝大多数网络协议。

那么在手机端如何使用wireshark进行抓包分析呢？大致的思路有两种：

###### 1. 在PC上使用wireshark

把手机端的网络流量导流到PC上。在PC上直接使用Wireshark进行抓包并分析。
这种方案要求手机与PC处于同一局域网内。
可行的办法是，手机连接电脑开的热点。对于没有无线网卡的电脑，可以使用随身WIFI来创建WIFI热点

##### 2. tcpdump + wireshark

在手机端使用tcpdump抓包，然后在PC端使用wireshark分析。
tcpdump是linux上的一个命令行抓包工具。它直接在网卡抓取数据包，所以**需要root权限**。

###### 解析SSL层密文遇到麻烦

Wireshark很强大，但是它在解析SSL层加密过的信息时，需要配置秘钥。
如果拥有服务器证书的私钥，那一切就很简单了。简单在wireshark的首选项中设置一下即可。

另一种方法只针对浏览器发出的HTTPS数据，Chrome和Firefox会在日志中记录会话的秘钥。
只要在系统环境变量中增加SSLKEYLOGFILE变量，重启浏览器即可。

显然，这种方案对手机APP产生的HTTPS数据无能为力。

##### 0x02 HTTP代理思路

将手机的HTTP请求导流至代理服务器，那么便可以在代理服务器中抓取到网络数据包了.
可以使用的工具有Fiddler或者Charles
该思路的不足是，只能抓取http的数据，如果要抓取别的应用层协议，就无能为力了。

###### 同一局域网下使用Fiddler

在PC端打开Fiddler作为HTTP代理服务器。
在手机端设置HTTP代理。这样就可以把手机端的HTTP流量导流只PC端。

对于HTTPS流量，可以在手机端安装Fiddler的证书。这样代理就可以解密加密的数据了

###### 不同局域网下使用DevTools + Fiddler

对于不处于同一局域网的设备，还可以使用Chrome游览器的设备远程调试功能，具体步骤如下：

1. 在PC端的chrome浏览器输入 chrome://inspect/#devices 即可进入
2. 点击Port forwarding，弹出设置窗口
3. 在设备端口输入框填写移动设备要监听的端口号，此处设为8000（可以任意未被使用的值，只要下面使用处做相应修改即可）。

4. 在手机端设置HTTP代理，主机号：localhost，端口号：上一步中设置的8000
5. 在手机端用浏览器随访问任意网页，当chrome://inspect窗口的端口号闪动为绿色时，表明该端口转发配置已生效。此时任何通过手机8000端口发出的请求都会通过数据线被映射到电脑的localhost:8888

##### 0x03 手机端抓包工具：Packet capture

这个工具是手机端的一个VPN，也相当于是一个代理。
它还可以把抓取到的数据保存为pcap格式，这样就可以在PC上使用Wireshark来分析。相比较于Tcpdump，它的好处是不需要root手机。

在实践中，并不能解析SSL加密的数据包。查资料，说是因为高版本安卓不再信任用户添加的CA证书列表。或者是客户端APP对CA证书进行了校验。总之是没法解析TLS数据。
一种解决办法是使用Xposed 插件just trust me。来跳过CA证书的校验过程。

##### 0x04 其它思路

其实抓到包很容易，难的是如何解析SSL层加密的数据。

抓包的目的就是分析网络数据，如果有APP的源码，其实可以换个思路，不用在网络侧抓包，而是在代码里调用网络接口前，将数据打印出来。
注意这里的目的是获取到应用层协议的报头，而不仅仅是应用层协议的载荷。

##### 1. Xposed HOOK

这是一种比较野的路子，在尝试的过程中失败了。但是可以把思路写出来
利用Xposed框架，Hook网络请求的相关代码，将数据打印出来。
能够在不修改源代码的前提下，截获网络数据。

##### 2. 自定义Socket

在代码里自定义一个Socket/SSLSocketFactory，对Socket进行一层包装，增加自定义的逻辑。
自定义逻辑可以是统计流量，也可以是记录。
这种思路同样可以获得应用发出的网络数据。

然后在OkhttpClient.Bulder中设置SSLSocketFactory/SocketFactory。网络库一般都会提供设置SocketFactory的方法

### 手机抓包原理

抓包的基本原理就是中间人攻击 [HTTPS 的握手过程]。Mac 上可使用 [Charles] 进行抓包。本质上就是两段 HTTPS 连接，Client <--> Man-In-The-Middle 和 Man-In-The-Middle <--> Server。使用 Charles 进行抓包，需要 Client 端提前将 Charles 的根证书添加在 Client 的信任列表中

### 中间人攻击危害

**HTTPS**，是一种[网络安全]传输协议，利用[SSL/TLS]来对数据包进行加密,以提供对[网络]服务器的[身份认证]，保护交换数据的隐私与[完整性]

**中间人攻击**，Man-in-the-middle attack，[缩写]：MITM，是指攻击者与通讯的两端分别创建独立的联系，并交换其所收到的数据，使通讯的两端认为他们正在通过一个私密的连接与对方直接对话，但事实上整个会话都被攻击者完全控制。

https在理论上是可以抵御MITM，但是由于开发过程中的编码不规范，导致https可能存在MITM攻击风险，攻击者可以解密、篡改https数据。

**0X02 https漏洞**

Android https的开发过程中常见的安全缺陷:

1)、在自定义实现X509TrustManager时，checkServerTrusted中没有检查证书是否可信，导致通信过程中可能存在中间人攻击，造成敏感数据劫持危害。

2)、在重写WebViewClient的onReceivedSslError方法时，调用proceed忽略证书验证错误信息继续加载页面，导致通信过程中可能存在中间人攻击，造成敏感数据劫持危害。

3)、在自定义实现HostnameVerifier时，没有在verify中进行严格证书校验，导致通信过程中可能存在中间人攻击，造成敏感数据劫持危害。

4)、在setHostnameVerifier方法中使用ALLOW_ALL_HOSTNAME_VERIFIER，信任所有Hostname，导致通信过程中可能存在中间人攻击，造成敏感数据劫持危害。

## 错误记录

startForeground requires android.permission.FOREGROUND_SERVICE

解决方案：

```xml
<manifest ...>
 <uses-permission
 android:name="android.permission.FOREGROUND_SERVICE" />
 <application ...>
 </manifest>
```

CLEARTEXT communication to * not permitted by network

解决方案：

在res目录下新建xml文件夹，文件夹里面创建network_security_config.xml 文件；
 文件内容

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
<base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

然后在 AndroidManifest.xml 的application 标签加上

```bash
android:networkSecurityConfig="@xml/network_security_config"
```