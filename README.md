## 搭建源码阅读环境

阅读学习Tomcat源码，需要三件事：

1. 看书入门，这里推荐两本关于Tomcat的书《Tomcat内核设计剖析》汪建 和《Tomcat架构解析》刘光瑞。
2. 搭建Tomcat源码运行环境，并debug。
3. 阅读[Tomcat最新源码](https://github.com/apache/tomcat  )，多版本比对源码。Tomcat在`Github`中开源，可`git clone` 最新代码到本地。阅读最新源码有一个好处就是可以看到参与Tomcat开发大佬的提交记录。

## 运行Tomcat源码

### 1、下载源码

`Tomcat`各版本源码：[https://archive.apache.org/dist/tomcat/](https://archive.apache.org/dist/tomcat/)

`Tomcat`安装包：[https://tomcat.apache.org/](https://tomcat.apache.org/)

为什么又要下载源码，又要下载安装包？源码中`webapps`是没有编译的，需要用安装包里的替换，并且`Tomcat`用的是`ant+build.xml`依赖管理，这种方式比较老，现在都用`maven`、`gradle`了，所以可以手动换成`maven`，但是有些包在`maven`仓库中找不到，可以从`Tomcat`安装包`lib`目录下获取。

我这里下载的是两个版本，`Tomcat8.5.9`和`Tomcat`最新版本（10.0.6），后面会分别讲解两个版本的源码运行搭建方式。

### 2、Tomcat8.5.9源码运行

下载好的`apache-tomcat-8.5.9-src`，用IDEA打开并引入：File–>Project Structure–>Modules–>+–>import module。

![image-20210606014348059](C:\Users\stefan\AppData\Roaming\Typora\typora-user-images\image-20210606014348059.png)

#### （1）新建pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.apache.tomcat</groupId>
    <artifactId>Tomcat8.5.9</artifactId>
    <name>Tomcat8.5.9</name>
    <version>8.5.9</version>
 
    <build>
        <finalName>Tomcat8.5.9</finalName>
        <sourceDirectory>java</sourceDirectory>
        <resources>
            <resource>
                <directory>java</directory>
            </resource>
        </resources>
        <testResources>
           <testResource>
                <directory>test</directory>
           </testResource>
        </testResources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>2.3</version>
                <configuration>
                    <encoding>UTF-8</encoding>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
 
    <dependencies>
        <!--test会用到-->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.easymock</groupId>
            <artifactId>easymock</artifactId>
            <version>3.4</version>
        </dependency>

        <!--org.apache.catalina.ant用到-->
        <dependency>
            <groupId>ant</groupId>
            <artifactId>ant</artifactId>
            <version>1.7.0</version>
        </dependency>

        <!--org.apache.naming.factory.webservices.ServiceRefFactory用到-->
        <dependency>
            <groupId>wsdl4j</groupId>
            <artifactId>wsdl4j</artifactId>
            <version>1.6.2</version>
        </dependency>

        <!--org.apache.naming.factory.webservices用到-->
        <dependency>
            <groupId>javax.xml</groupId>
            <artifactId>jaxrpc</artifactId>
            <version>1.1</version>
        </dependency>

        <!--org.apache.jasper.compiler.JDTCompiler用到-->
        <dependency>
            <groupId>org.eclipse.jdt.core.compiler</groupId>
            <artifactId>ecj</artifactId>
            <version>4.5.1</version>
        </dependency>
    </dependencies>
</project>
```

#### （2）替换webapps和加 lib 包

因为源码包`webapps`下示例项目`WEB-INF/classes/`下的`.java`没有编译，没有.class文件，所以可将整个`webapps`删除，并复制安装包中的`webapps`到源码包。

源码包中新建lib目录，从安装包中的lib目录复制`jasper.jar`到源码包的lib目录。这样做的目的有两个：

- Tomcat运行时会默认扫描lib目录，没有lib目录虽然不会报错，但是控制台会有警告日志，提示lib目录不存在。![image-20210605232812293](C:\Users\stefan\AppData\Roaming\Typora\typora-user-images\image-20210605232812293.png)

- 安装包lib下jar包很多，没必要都复制过来，按需复制了`jasper.jar`，虽然`jasper.jar`中的代码和源码中的`org.apache.jasper`完全重了，但是这个jar包里有非常重要的配置信息，定义了`javax.servlet.ServletContainerInitializer`的实现类是`org.apache.jasper.servlet.JasperInitializer`，`Tomcat`启动时，`Context`的生命周期监听器`ContextConfig.configureStart`会扫描lib目录下的jar里面的配置信息，做一些类初始化等操作。这里就是主动实例化`JasperInitializer`，不然访问`jsp`就会报错。（百度到的都是在源码里写死加一个`JasperInitializer`的初始化，不推荐这样做，一定要找到原因。）

![image-20210605233829600](C:\Users\stefan\AppData\Roaming\Typora\typora-user-images\image-20210605233829600.png)

#### （3）运行Bootstrap#main

`org.apache.catalina.startup.Bootstrap#main` 是`Tomcat`一键启停的源头，运行这个`main`方法之前需要指定一下配置的位置，`Tomcat`必须要引用`server.xml`等配置才可以运行起来。

需要先了解两个概念：

- `catalina.home`是`web`项目部署目录。
- `catalina.base`是`Tomcat`安装目录。

一般情况这两个目录设置成一样的，`Tomcat`读的一些配置，默认都是在这两个目录下，比如`conf`、`webapps`、`lib`等。我这里`VM options`设置了`catalina.home`和`catalina.base`都是源码目录，并指定了`Log`的配置。

```
-Dcatalina.home=C:/study/tomcat/apache-tomcat-8.5.9-src
-Dcatalina.base=C:/study/tomcat/apache-tomcat-8.5.9-src
-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager
-Djava.util.logging.config.file=C:/study/tomcat/apache-tomcat-8.5.9-src/conf/logging.properties
```

![image-20210606001353133](C:\Users\stefan\AppData\Roaming\Typora\typora-user-images\image-20210606001353133.png)

#### （4）运行并访问

完成以上三步，就可以运行了：

![image-20210606002221046](C:\Users\stefan\AppData\Roaming\Typora\typora-user-images\image-20210606002221046.png)

如果`test`目录有报错，可删除报错的位置（最好不要删除整个`test`目录，可以自己在`test`目录里写测试类`debug`）。正常运行后，在浏览器中访问http://127.0.0.1:8080/ , 点击页面中的超链接如`Documentation`、`Configuration`等都可以正常访问。

![image-20210606002645829](C:\Users\stefan\AppData\Roaming\Typora\typora-user-images\image-20210606002645829.png)

![image-20210606002948939](C:\Users\stefan\AppData\Roaming\Typora\typora-user-images\image-20210606002948939.png)

#### （5）-config指定配置路径运行

`webapps`下的web项目是会自动部署的，但是实际生产中，不太可能让所有的web项目都部署在`webapps`下，不太可能启动一个`Tomcat`，运行所有web，一旦一个web运行异常导致Tomcat挂了，其他web也会受到影响。所以一般通过`-config`指定配置，一个web运行一个`Tomcat`，保证进程隔离。（`VM options`不要更改）

```xml
# Program arguments, example1.conf是定制的server.xml配置
-config C:/study/tomcat/conf/example1.conf start
```

![image-20210606003921800](C:\Users\stefan\AppData\Roaming\Typora\typora-user-images\image-20210606003921800.png)

example1.conf：

```xml
<?xml version='1.0' encoding='utf-8'?>
<Server port="8112" shutdown="SHUTDOWN">
  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />

  <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>

  <Service name="Catalina">

    <Executor name="tomcatThreadPool" namePrefix="catalina-exec-" maxThreads="20" minSpareThreads="20" />
	
    <Connector executor="tomcatThreadPool"  port="8012" protocol="org.apache.coyote.http11.Http11NioProtocol"
               connectionTimeout="60000"
		acceptCount= "10000" 
               redirectPort="8443"  URIEncoding="UTF-8"  maxPostSize="-1" maxHttpHeaderSize ="102400"/>
    <Engine name="Catalina" defaultHost="demo1">
	
      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
      </Realm>
      <Host name="demo1" >
        <!-- Context 指定web项目路径 -->
        <Context path="/" reloadable="true" docBase="C:/study/tomcat/web/example1"  workDir="C:/study/tomcat/web/example1/WEB-INF/work/" >
	  <Resources>
      </Resources>
        </Context>
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="C:/study/tomcat/logs/"
               prefix="example1." suffix=".txt"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />

      </Host>
    </Engine>
  </Service>
</Server>

```

### 3、Tomcat10.0.6源码运行

和`apache-tomcat-8.5.9-src`的过程差不多，稍有不同的地方是一些依赖需要调整。（`Tomcat10.0.6`中直接复制整个安装包的lib到源码包中运行不会报错，所以直接整个lib复制过去）

`pom.xml`比8.5.9多两个依赖`jakartaee-migration`和`biz.aQute.bndlib`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
 
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.apache.tomcat</groupId>
    <artifactId>Tomcat10.0.6</artifactId>
    <name>Tomcat10.0.6</name>
    <version>10.0.6</version>
 
    <build>
        <finalName>Tomcat10.0.6</finalName>
        <sourceDirectory>java</sourceDirectory>
        <resources>
            <resource>
                <directory>java</directory>
            </resource>
        </resources>
        <testResources>
           <testResource>
                <directory>test</directory>
           </testResource>
        </testResources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>2.3</version>
                <configuration>
                    <encoding>UTF-8</encoding>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
 
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.easymock</groupId>
            <artifactId>easymock</artifactId>
            <version>3.4</version>
        </dependency>
        <dependency>
            <groupId>ant</groupId>
            <artifactId>ant</artifactId>
            <version>1.7.0</version>
        </dependency>
        <dependency>
            <groupId>wsdl4j</groupId>
            <artifactId>wsdl4j</artifactId>
            <version>1.6.2</version>
        </dependency>
        <dependency>
            <groupId>javax.xml</groupId>
            <artifactId>jaxrpc</artifactId>
            <version>1.1</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/org.apache.tomcat/jakartaee-migration -->
        <!-- 10.0.6 新加 -->
        <dependency>
            <groupId>org.apache.tomcat</groupId>
            <artifactId>jakartaee-migration</artifactId>
            <version>1.0.0</version>
        </dependency>

        <!-- 10.0.6 新加 -->
        <dependency>
            <groupId>biz.aQute.bnd</groupId>
            <artifactId>biz.aQute.bndlib</artifactId>
            <version>5.3.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

依赖中去掉了`ecj`，因为这个依赖maven仓库中最新版本也满足不了`Tomcat10.0.6`，需要从`Tomcat10.0.6`的安装包lib中复制`ecj-4.18.jar`到源码包的lib下，同时需要手动指定依赖：

![image-20210606011043091](.\doc\image\image-20210606011043091.png)

依赖调整好以后，其他操作都和`apache-tomcat-8.5.9-src`一样。但是运行10.0.6后控制台输出有一些乱码，也不是中文乱码，尝试调试编码，没有解决，有解决乱码的同学可以告知一下。

![image-20210606013135492](C:\Users\stefan\AppData\Roaming\Typora\typora-user-images\image-20210606013135492.png)

