## 说明

此工程从Adjust官方Github的4.38.4版本fork出来，进行差异化修改。主要是为了在GooglePlay上架时避开静态检查，因为Adjust可能是阻碍GooglePlay上架的一个因素。

版本号说明：为了方便知道fork的sdk来自哪一个官方版本，我们工程的版本号就在原Adjust官方版本的后面多加一个号段，如：4.38.4.4表示来自官方4.38.4版本。

Adjust官网：https://github.com/adjust/android_sdk

最新版本：4.38.4.5

## 发布SDK

sdk-plugin-samsung-clouddev模块需要引入三星的SDK，目前这个SDK没有在三星官网公开，需要三星公司单独提供，因此这个模块目前无法编译。

发布前需要准备Snoatype账号和签名密码（请从vest-sdk工程中查找），并按照以下格式写入local.properties：
```
sonatypeUsername=
sonatypePassword=
signing.secretKeyRingFile=
signing.keyId=
signing.password=
```

以下命令负责发布模块：

- sdk-core
- sdk-plugin-criteo
- sdk-plugin-imei
- sdk-plugin-oaid
- sdk-plugin-samsung-referrer
- sdk-plugin-sociomantic
- sdk-plugin-trademob
- sdk-plugin-vivo-referrer
- sdk-plugin-webbridge
- sdk-plugin-xiaomi-referrer

```
./gradlew clean sdk-core:publishAllPublicationsToCentralPortal \
sdk-plugin-criteo:publishAllPublicationsToCentralPortal \
sdk-plugin-imei:publishAllPublicationsToCentralPortal \
sdk-plugin-oaid:publishAllPublicationsToCentralPortal \
sdk-plugin-samsung-referrer:publishAllPublicationsToCentralPortal \
sdk-plugin-sociomantic:publishAllPublicationsToCentralPortal \
sdk-plugin-trademob:publishAllPublicationsToCentralPortal \
sdk-plugin-vivo-referrer:publishAllPublicationsToCentralPortal \
sdk-plugin-webbridge:publishAllPublicationsToCentralPortal \
sdk-plugin-xiaomi-referrer:publishAllPublicationsToCentralPortal
```

## 集成SDK

在工程依赖中引入以下依赖

```
//核心库（必须引入）
implementation "io.github.shin-osaka:adjust-android:4.38.4.5"
//以下为插件（可选）
implementation "io.github.shin-osaka:adjust-android-criteo:4.38.4.5"
implementation "io.github.shin-osaka:adjust-android-imei:4.38.4.5"
implementation "io.github.shin-osaka:adjust-android-oaid:4.38.4.5"
implementation "io.github.shin-osaka:adjust-android-samsung-referrer:4.38.4.5"
implementation "io.github.shin-osaka:adjust-android-sociomantic:4.38.4.5"
implementation "io.github.shin-osaka:adjust-android-trademob:4.38.4.5"
implementation "io.github.shin-osaka:adjust-android-vivo-referrer:4.38.4.5"
implementation "io.github.shin-osaka:adjust-android-webbridge:4.38.4.5"
implementation "io.github.shin-osaka:adjust-android-xiaomi-referrer:4.38.4.5"
```

## 历史版本

### 4.38.4.2

- 基于Adjust 4.38.4初始化版本
- 去掉了UrlStrategy中的中国服务器地址
- 更改了类的路径，从com.adjust.sdk更改为com.bumptech.sdk
- 修改了混淆规则，允许混淆原com.adjust.sdk路径下的类

### 4.38.4.3

- Sdk传到服务器的版本号恢复为4.38.4，修复版本号验证失败的问题

### 4.38.4.4

- 修改混淆规则，保留一些反射类名不混淆

### 4.38.4.5

- 包名修改为com.osaka.sdk
