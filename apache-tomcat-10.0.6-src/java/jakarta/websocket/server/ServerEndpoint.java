/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jakarta.websocket.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.websocket.Decoder;
import jakarta.websocket.Encoder;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServerEndpoint {

    /**
     * URI or URI-template that the annotated class should be mapped to.
     * @return The URI or URI-template that the annotated class should be mapped
     *         to.
     */
    String value();

    // 使用者在WebSocket协议下扩展定义的子协议，还不知道怎么用，暂时不需要了解
    String[] subprotocols() default {};

    // 使用者可以自定义一堆消息解码类
    Class<? extends Decoder>[] decoders() default {};

    // 使用者自定义消息编码类
    Class<? extends Encoder>[] encoders() default {};

    // Configurator 其实就像一个生产ServerEndpoint的工厂，
    // 用户可以自定义怎么创建ServerEndpoint对象
    public Class<? extends ServerEndpointConfig.Configurator> configurator()
            default ServerEndpointConfig.Configurator.class;
}
