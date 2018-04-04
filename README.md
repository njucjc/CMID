# Graduation Project
## 1. Configuration
###  create a file named **config.properties**
```
#描述rule的xml文件路径
ruleFilePath=resource/consistency_rules.xml
#描述pattern的xml文件路径
patternFilePath=resource/consistency_patterns.xml
#描述context的txt文件路径
contextFilePath=resource/changes/00_small.txt
#指定输出log的路径
logFilePath=report.log
#算法选择（目前可选项是ECC或PCC）
technique=PCC
#描述几个context change触发一次check（可选项是正整数或GEAS）
schedule=2
#描述类型
changeHandlerType=static-change-based
#描述Con-C的任务数量
taskNum=4
```
## 2. Run
### (1) Clone [this repository](https://github.com/njucjc/graduation-project).
```
$ git clone https://github.com/njucjc/graduation-project.git
```
### (2) Import this project with **IntelliJ IDEA**.
### (3) Build this project.
### (4) Start to run.
```
$ java Main pathToConfigFile //config.properties
```
