

The brief see the original repository README.md: https://github.com/fex-team/ueditor

### Update
The update is based on branch dev-1.4.3.3
- 1. update frontent
  - (1). add more fontSizes support
  - (2). add support for "//" for allowLinkProtocol
  - (3). add support for "_url" for the img tag in whitelist
  - (4). add support for 'class', 'style' for the img secthin in whitelist
  - (5). add support for iframe, source,embed in whitelist
  - (6). bugfix by adding errorHandler for plugin simpleupload
  - (7). support webp image upload
- 2. add support for Tencent COS for java backend

### Cloud Storage support for java backend

you can enable COS support in your code, the default value is true.

Java backend usage steps:

- Step1: compile and install
```
cd jsp
mvn package install
```

- Step2: in your project pom.xml
```
    <dependency>
        <groupId>com.baidu</groupId>
        <artifactId>ueditor</artifactId>
        <version>1.2</version>
     </dependency>
```

- Step3: config cloud storage support(e.g. tencent COS) 

The Spring example demo, it can be placed in news/article controller or SpringBoot application
```
    class CosCloudStorage implements CloudStorageInteface
    {
       @Override
       public  String upload(File file,String key) {
           return CosUtil.upload(file, key); //use your COS uploader util
       }
    }
    @PostConstruct
    public void initUeditor()
    {
        log.info("to setup ueditor for COS");
        ConfigManager.ENABLE_COS = true;
        if(ConfigManager.ENABLE_COS) 
            ConfigManager.cloudStroage = new CosCloudStorage();
    }
```

