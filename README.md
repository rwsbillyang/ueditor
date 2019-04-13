

The brief see the original repository README.md: https://github.com/fex-team/ueditor

The update is based on branch dev-1.4.3.3
- 1. update frontent

(1). add more fontSizes support
(2). add support for "//" for allowLinkProtocol
(3). add support for "_url" for the img tag in whitelist
(4). add support for 'class', 'style' for the img secthin in whitelist
(5). add support for iframe, source,embed in whitelist
(6). bugfix by adding errorHandler for plugin simpleupload

- 2. add support for Tencent COS for java backend

you can enable COS support in your code, the default is true
```
ConfigManager.ENABLE_COS = true
```
additionally, you need implement the CosUtil of yourself.
