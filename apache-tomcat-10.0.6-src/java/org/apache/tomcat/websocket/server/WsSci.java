/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomcat.websocket.server;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.server.ServerApplicationConfig;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;

import org.apache.tomcat.util.compat.JreCompat;

/**
 * Registers an interest in any class that is annotated with
 * {@link ServerEndpoint} so that Endpoint can be published via the WebSocket
 * server.
 */
@HandlesTypes({ServerEndpoint.class, ServerApplicationConfig.class,
        Endpoint.class})
public class WsSci implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> clazzes, ServletContext ctx)
            throws ServletException {

        WsServerContainer sc = init(ctx, true);

        if (clazzes == null || clazzes.size() == 0) {
            return;
        }

        // Group the discovered classes by type
        Set<ServerApplicationConfig> serverApplicationConfigs = new HashSet<>();
        Set<Class<? extends Endpoint>> scannedEndpointClazzes = new HashSet<>();
        Set<Class<?>> scannedPojoEndpoints = new HashSet<>();

        try {
            // wsPackage is "jakarta.websocket."
            String wsPackage = ContainerProvider.class.getName();
            wsPackage = wsPackage.substring(0, wsPackage.lastIndexOf('.') + 1);
            for (Class<?> clazz : clazzes) {
                JreCompat jreCompat = JreCompat.getInstance();
                int modifiers = clazz.getModifiers();
                if (!Modifier.isPublic(modifiers) ||
                        Modifier.isAbstract(modifiers) ||
                        Modifier.isInterface(modifiers) ||
                        !jreCompat.isExported(clazz)) {
                    // Non-public, abstract, interface or not in an exported
                    // package (Java 9+) - skip it.
                    continue;
                }
                // Protect against scanning the WebSocket API JARs
                // 防止扫描WebSocket API jar
                if (clazz.getName().startsWith(wsPackage)) {
                    continue;
                }
                if (ServerApplicationConfig.class.isAssignableFrom(clazz)) {
                    // 1、clazz是ServerApplicationConfig子类
                    serverApplicationConfigs.add(
                            (ServerApplicationConfig) clazz.getConstructor().newInstance());
                }
                if (Endpoint.class.isAssignableFrom(clazz)) {
                    // 2、clazz是Endpoint子类
                    @SuppressWarnings("unchecked")
                    Class<? extends Endpoint> endpoint =
                            (Class<? extends Endpoint>) clazz;
                    scannedEndpointClazzes.add(endpoint);
                }
                if (clazz.isAnnotationPresent(ServerEndpoint.class)) {
                    // 3、clazz是加了注解ServerEndpoint的类
                    scannedPojoEndpoints.add(clazz);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new ServletException(e);
        }

        // Filter the results
        Set<ServerEndpointConfig> filteredEndpointConfigs = new HashSet<>();
        Set<Class<?>> filteredPojoEndpoints = new HashSet<>();

        if (serverApplicationConfigs.isEmpty()) {
            // 从这里看出@ServerEndpoint的服务器端是可以不用ServerApplicationConfig的
            filteredPojoEndpoints.addAll(scannedPojoEndpoints);
        } else {
            // serverApplicationConfigs不为空，
            for (ServerApplicationConfig config : serverApplicationConfigs) {
                Set<ServerEndpointConfig> configFilteredEndpoints =
                        config.getEndpointConfigs(scannedEndpointClazzes);
                if (configFilteredEndpoints != null) {
                    filteredEndpointConfigs.addAll(configFilteredEndpoints);
                }
                // getAnnotatedEndpointClasses 对于 scannedPojoEndpoints起到一个过滤作用
                // 不满足条件的后面不加到WsServerContainer里
                Set<Class<?>> configFilteredPojos =
                        config.getAnnotatedEndpointClasses(
                                scannedPojoEndpoints);
                if (configFilteredPojos != null) {
                    filteredPojoEndpoints.addAll(configFilteredPojos);
                }
            }
        }

        try {
            // 继承抽象类Endpoint的需要使用者手动封装成ServerEndpointConfig
            // 而加了注解@ServerEndpoint的类 Tomcat会自动封装成ServerEndpointConfig
            // Deploy endpoints
            for (ServerEndpointConfig config : filteredEndpointConfigs) {
                sc.addEndpoint(config);
            }
            // Deploy POJOs
            for (Class<?> clazz : filteredPojoEndpoints) {
                sc.addEndpoint(clazz, true);
            }
        } catch (DeploymentException e) {
            throw new ServletException(e);
        }
    }


    static WsServerContainer init(ServletContext servletContext,
            boolean initBySciMechanism) {

        WsServerContainer sc = new WsServerContainer(servletContext);

        servletContext.setAttribute(
                Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE, sc);
        // 注册监听器WsSessionListener给servletContext，
        // 在http session销毁时触发 ws session的关闭销毁
        servletContext.addListener(new WsSessionListener(sc));
        // Can't register the ContextListener again if the ContextListener is
        // calling this method
        if (initBySciMechanism) {
            // 注册监听器WsContextListener给servletContext，
            // 在 servletContext初始化时触发WsSci.init
            // 在 servletContext销毁时触发WsServerContainer的销毁
            // 不过呢，只在WsSci.onStartup时注册一次
            servletContext.addListener(new WsContextListener());
        }
        return sc;
    }
}
