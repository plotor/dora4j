## doraemon

哆啦A梦：抽象源码学习过程中一些好的设计思想和通用策略

> - jdk: 1.8
> - maven: 3.2.5

### Dubbo SPI

SPI(Service Provider Interfaces) 是 jdk1.5 引入的一种服务扩展内置机制，在面向接口编程的范畴下，SPI 能够基于配置的方式声明实际应用的具体扩展接口实现。Dubbo RPC 框架在设计和实现上采用 “微内核 + 插件” 的方式，具备良好的定制性和可扩展性，整体架构非常简单、精美。Dubbo 的可扩展性基于 SPI 扩展机制实现，不过它并没有采用 jdk 内置的 SPI，而是自己另起炉灶实现了一套，之所以这样 “重复造轮子”，官方给出的理由如下：

> 1. JDK 标准的 SPI 会一次性实例化扩展点所有实现，如果有扩展实现初始化很耗时，但如果没用上也加载，会很浪费资源。
> 2. 如果扩展点加载失败，连扩展点的名称都拿不到了。比如：JDK 标准的 ScriptEngine，通过 getName() 获取脚本类型的名称，但如果 RubyScriptEngine 因为所依赖的 jruby.jar 不存在，导致 RubyScriptEngine 类加载失败，这个失败原因被吃掉了，和 ruby 对应不起来，当用户执行 ruby 脚本时，会报不支持 ruby，而不是真正失败的原因。
> 3. 增加了对扩展点 IoC 和 AOP 的支持，一个扩展点可以直接 setter 注入其它扩展点。

进一步阅读：

- [Dubbo 之于 SPI 扩展机制的实现分析](http://www.zhenchao.org/2017/12/17/dubbo-spi/)

### JStorm 线程模型

// TODO 文档